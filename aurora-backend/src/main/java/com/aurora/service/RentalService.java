package com.aurora.service;

import com.aurora.dto.fine.FineResponse;
import com.aurora.dto.rental.RentalResponse;
import com.aurora.entity.*;
import com.aurora.entity.enums.CopyStatus;
import com.aurora.entity.enums.FineStatus;
import com.aurora.entity.enums.FineType;
import com.aurora.entity.enums.RentalStatus;
import com.aurora.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RentalService {

    private final RentalRepository rentalRepository;
    private final BookCopyRepository bookCopyRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final FineRepository fineRepository;

    @Transactional
    public RentalResponse rentBook(Long userId, Long bookId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        if (user.getBloqueadoAte() != null && user.getBloqueadoAte().isAfter(LocalDate.now())) {
            throw new IllegalStateException("Usuário está bloqueado até " + user.getBloqueadoAte());
        }

        List<Fine> pendingFines = fineRepository.findByUserIdAndStatus(userId, FineStatus.pendente);
        if (!pendingFines.isEmpty()) {
            throw new IllegalStateException("Você possui multas pendentes e não pode realizar novos aluguéis.");
        }

        long activeRentalsCount = rentalRepository.countByUserIdAndStatus(userId, RentalStatus.ativo);
        if (activeRentalsCount >= 3) {
            throw new IllegalStateException("Limite de 3 livros alugados simultaneamente atingido.");
        }

        Book book = bookRepository.findByIdAndDeletedAtIsNull(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Livro não encontrado"));

        BookCopy availableCopy = bookCopyRepository.findByBookIdAndStatus(bookId, CopyStatus.disponivel)
                .stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Não há cópias disponíveis para este livro."));

        availableCopy.setStatus(CopyStatus.alugado);
        bookCopyRepository.save(availableCopy);

        int rentalDays = (book.getNumPaginas() <= 150) ? 15 : 30;
        LocalDate dueDate = LocalDate.now().plusDays(rentalDays);

        Rental rental = Rental.builder()
                .user(user)
                .bookCopy(availableCopy)
                .dataAluguel(LocalDateTime.now())
                .dataDevolucaoPrevista(dueDate)
                .status(RentalStatus.ativo)
                .extensaoContador(0)
                .precoCobrado(book.getPrecoAluguel())
                .build();

        Rental savedRental = rentalRepository.save(rental);

        book.setAlugueisUltimoAno(book.getAlugueisUltimoAno() + 1);
        bookRepository.save(book);

        return mapToRentalResponse(savedRental);
    }

    @Transactional
    public RentalResponse extendRental(Long rentalId, Long userId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new IllegalArgumentException("Aluguel não encontrado"));

        if (!rental.getUser().getId().equals(userId)) {
            throw new IllegalStateException("Apenas o próprio usuário pode solicitar extensão.");
        }

        if (rental.getStatus() != RentalStatus.ativo) {
            throw new IllegalStateException("Apenas aluguéis ativos podem ser estendidos.");
        }

        if (rental.getExtensaoContador() >= 2) {
            throw new IllegalStateException("Máximo de 2 extensões atingido. Devolução obrigatória.");
        }

        int currentCount = rental.getExtensaoContador();
        LocalDate newDueDate;
        if (currentCount == 0) {
            newDueDate = LocalDate.now().plusMonths(2);
            rental.setExtensaoContador(1);
        } else {
            newDueDate = LocalDate.now().plusMonths(1);
            rental.setExtensaoContador(2);
        }

        rental.setDataDevolucaoPrevista(newDueDate);
        Rental updatedRental = rentalRepository.save(rental);

        return mapToRentalResponse(updatedRental);
    }

    @Transactional
    public RentalResponse returnBook(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new IllegalArgumentException("Aluguel não encontrado"));

        if (rental.getStatus() == RentalStatus.devolvido) {
            throw new IllegalStateException("Este livro já foi devolvido.");
        }

        LocalDate now = LocalDate.now();
        rental.setDataDevolucaoReal(now);
        rental.setStatus(RentalStatus.devolvido);

        BookCopy copy = rental.getBookCopy();
        copy.setStatus(CopyStatus.disponivel);
        bookCopyRepository.save(copy);

        if (now.isAfter(rental.getDataDevolucaoPrevista())) {
            long daysLate = ChronoUnit.DAYS.between(rental.getDataDevolucaoPrevista(), now);
            BigDecimal bookValue = copy.getBook().getValorLivro();
            BigDecimal calculatedFine = BigDecimal.valueOf(daysLate * 3.00);
            BigDecimal fineValue = calculatedFine.min(bookValue);

            Fine fine = Fine.builder()
                    .rental(rental)
                    .user(rental.getUser())
                    .tipo(FineType.atraso)
                    .valor(fineValue)
                    .diasAtraso((int) daysLate)
                    .status(FineStatus.pendente)
                    .createdAt(LocalDateTime.now())
                    .build();

            fineRepository.save(fine);

            if (daysLate > 15) {
                User user = rental.getUser();
                LocalDate blockUntil = now.plusDays(7);
                if (user.getBloqueadoAte() == null || blockUntil.isAfter(user.getBloqueadoAte())) {
                    user.setBloqueadoAte(blockUntil);
                    userRepository.save(user);
                }
            }
        }

        Rental savedRental = rentalRepository.save(rental);
        return mapToRentalResponse(savedRental);
    }

    @Transactional
    public FineResponse applyDamageFine(Long rentalId, String damageType) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new IllegalArgumentException("Aluguel não encontrado"));

        BookCopy copy = rental.getBookCopy();
        copy.setStatus(CopyStatus.danificado);
        bookCopyRepository.save(copy);

        Fine fine = Fine.builder()
                .rental(rental)
                .user(rental.getUser())
                .tipo(FineType.dano)
                .valor(new BigDecimal("20.00"))
                .status(FineStatus.pendente)
                .createdAt(LocalDateTime.now())
                .build();

        Fine savedFine = fineRepository.save(fine);

        return FineResponse.builder()
                .id(savedFine.getId())
                .rentalId(rental.getId())
                .userId(rental.getUser().getId())
                .tipo(savedFine.getTipo())
                .valor(savedFine.getValor())
                .diasAtraso(savedFine.getDiasAtraso() != null ? savedFine.getDiasAtraso() : 0)
                .status(savedFine.getStatus())
                .createdAt(savedFine.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<RentalResponse> getUserRentals(Long userId) {
        return rentalRepository.findByUserId(userId)
                .stream()
                .map(this::mapToRentalResponse)
                .collect(Collectors.toList());
    }

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void processDailyOverdueRentals() {
        LocalDate today = LocalDate.now();
        List<Rental> overdueRentals = rentalRepository.findByStatusAndDataDevolucaoPrevistaBefore(RentalStatus.ativo, today);

        for (Rental rental : overdueRentals) {
            long daysLate = ChronoUnit.DAYS.between(rental.getDataDevolucaoPrevista(), today);
            BigDecimal bookValue = rental.getBookCopy().getBook().getValorLivro();
            BigDecimal calculatedFine = BigDecimal.valueOf(daysLate * 3.00);
            BigDecimal fineValue = calculatedFine.min(bookValue);

            Fine fine = fineRepository.findByRentalIdAndTipo(rental.getId(), FineType.atraso)
                    .orElse(Fine.builder()
                            .rental(rental)
                            .user(rental.getUser())
                            .tipo(FineType.atraso)
                            .createdAt(LocalDateTime.now())
                            .build());

            fine.setValor(fineValue);
            fine.setDiasAtraso((int) daysLate);
            fine.setStatus(FineStatus.pendente);
            fineRepository.save(fine);

            User user = rental.getUser();
            if (daysLate > 15) {
                LocalDate blockUntil = today.plusDays(7);
                if (calculatedFine.compareTo(bookValue) >= 0 && daysLate > 15) {
                    blockUntil = blockUntil.plusDays(daysLate - 15);
                }
                if (user.getBloqueadoAte() == null || blockUntil.isAfter(user.getBloqueadoAte())) {
                    user.setBloqueadoAte(blockUntil);
                    userRepository.save(user);
                }
            }
        }
    }

    public RentalResponse mapToRentalResponse(Rental rental) {
        return RentalResponse.builder()
                .id(rental.getId())
                .userId(rental.getUser().getId())
                .userName(rental.getUser().getNome())
                .bookId(rental.getBookCopy().getBook().getId())
                .bookTitle(rental.getBookCopy().getBook().getTitulo())
                .bookCopyId(rental.getBookCopy().getId())
                .dataAluguel(rental.getDataAluguel())
                .dataDevolucaoPrevista(rental.getDataDevolucaoPrevista())
                .dataDevolucaoReal(rental.getDataDevolucaoReal())
                .status(rental.getStatus())
                .extensaoContador(rental.getExtensaoContador())
                .precoCobrado(rental.getPrecoCobrado())
                .build();
    }
}
