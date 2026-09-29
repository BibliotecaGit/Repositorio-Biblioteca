package com.aurora.service;

import com.aurora.dto.fine.FineResponse;
import com.aurora.dto.queue.PendingResponse;
import com.aurora.dto.queue.WaitlistResponse;
import com.aurora.dto.rental.RentalResponse;
import com.aurora.entity.*;
import com.aurora.entity.enums.*;
import com.aurora.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RentalQueueService {

    private final RentalRepository rentalRepository;
    private final BookRepository bookRepository;
    private final BookCopyRepository bookCopyRepository;
    private final WaitlistRepository waitlistRepository;
    private final PendingListRepository pendingListRepository;
    private final FineRepository fineRepository;
    private final UserRepository userRepository;

    // --- ELIGIBILITY CHECK ---
    public void validateUserEligibility(User user) {
        // RN-006: Check block date
        if (user.getBloqueadoAte() != null && user.getBloqueadoAte().isAfter(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário está bloqueado até " + user.getBloqueadoAte());
        }

        // RN-006: Check active pending fines
        if (fineRepository.existsByUserIdAndStatus(user.getId(), FineStatus.pendente)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário possui multas pendentes de pagamento");
        }

        // RN-006: Check maximum active rentals (< 3)
        long activeRentals = rentalRepository.countByUserIdAndStatus(user.getId(), RentalStatus.ativo);
        if (activeRentals >= 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Limite máximo de 3 livros alugados simultaneamente atingido");
        }
    }

    // --- ALUGAR LIVRO DISPONÍVEL (RF-010, UC-003) ---
    @Transactional
    public RentalResponse rentBook(User user, Long bookId) {
        validateUserEligibility(user);

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Livro não encontrado"));

        if (book.getDeletedAt() != null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Livro não encontrado");
        }

        // Check queue: cannot rent if queue exists
        long queueCount = waitlistRepository.countByBookIdAndStatusIn(bookId, List.of(WaitlistStatus.aguardando, WaitlistStatus.notificado));
        if (queueCount > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Livro possui fila de espera. Entre na fila.");
        }

        // Find available copy
        BookCopy copy = bookCopyRepository.findFirstByBookIdAndStatus(bookId, CopyStatus.disponivel)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nenhum exemplar disponível no momento"));

        copy.setStatus(CopyStatus.alugado);
        bookCopyRepository.save(copy);

        // Calculate return date (RN-001)
        int days = book.getNumPaginas() <= 150 ? 15 : 30;
        LocalDate prevista = LocalDate.now().plusDays(days);

        Rental rental = Rental.builder()
                .user(user)
                .bookCopy(copy)
                .dataAluguel(LocalDateTime.now())
                .dataDevolucaoPrevista(prevista)
                .status(RentalStatus.ativo)
                .extensaoContador(0)
                .precoCobrado(book.getPrecoAluguel())
                .build();

        // Increment book's rental counter
        book.setAlugueisUltimoAno(book.getAlugueisUltimoAno() + 1);
        bookRepository.save(book);

        Rental saved = rentalRepository.save(rental);
        return mapToRentalResponse(saved);
    }

    // --- ENTRAR NA FILA DE ESPERA (RF-011, UC-004) ---
    @Transactional
    public WaitlistResponse enterQueue(User user, Long bookId) {
        validateUserEligibility(user);

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Livro não encontrado"));

        // Cannot enter queue if copies are available and queue is empty (RN-003)
        List<BookCopy> availableCopies = bookCopyRepository.findByBookIdAndStatus(bookId, CopyStatus.disponivel);
        long currentQueueCount = waitlistRepository.countByBookIdAndStatusIn(bookId, List.of(WaitlistStatus.aguardando, WaitlistStatus.notificado));

        if (!availableCopies.isEmpty() && currentQueueCount == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Há exemplares disponíveis. Alugue diretamente.");
        }

        // Check max queue size (10 people)
        if (currentQueueCount >= 10) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fila de espera está cheia (máximo 10 pessoas). Fique como 'Pendente'.");
        }

        // Cannot be in queue twice
        Optional<Waitlist> existing = waitlistRepository.findByBookIdAndUserIdAndStatusIn(bookId, user.getId(),
                List.of(WaitlistStatus.aguardando, WaitlistStatus.notificado));
        if (existing.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Você já está na fila para este livro");
        }

        Waitlist waitlist = Waitlist.builder()
                .book(book)
                .user(user)
                .posicao((int) currentQueueCount + 1)
                .status(WaitlistStatus.aguardando)
                .dataEntrada(LocalDateTime.now())
                .build();

        Waitlist saved = waitlistRepository.save(waitlist);

        // If user was in pending list, cancel pending status
        pendingListRepository.findByBookIdAndUserIdAndStatus(bookId, user.getId(), PendingStatus.ativo)
                .ifPresent(p -> {
                    p.setStatus(PendingStatus.promovido);
                    p.setDataPromocao(LocalDateTime.now());
                    pendingListRepository.save(p);
                });

        return mapToWaitlistResponse(saved);
    }

    // --- MARCAR-SE COMO PENDENTE (RF-011A, UC-005) ---
    @Transactional
    public PendingResponse enterPending(User user, Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Livro não encontrado"));

        // CA-011A.1: Pendente only when queue has 10 people
        long queueCount = waitlistRepository.countByBookIdAndStatusIn(bookId, List.of(WaitlistStatus.aguardando, WaitlistStatus.notificado));
        if (queueCount < 10) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A fila ainda não está cheia. Entre na fila de espera.");
        }

        // CA-011A.2: Cannot be pending twice or pending & in queue
        Optional<PendingList> existingPending = pendingListRepository.findByBookIdAndUserIdAndStatus(bookId, user.getId(), PendingStatus.ativo);
        if (existingPending.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Você já está na lista de pendentes para este livro");
        }

        PendingList pending = PendingList.builder()
                .book(book)
                .user(user)
                .status(PendingStatus.ativo)
                .dataEntrada(LocalDateTime.now())
                .build();

        PendingList saved = pendingListRepository.save(pending);
        return mapToPendingResponse(saved);
    }

    // --- PROMOÇÃO AUTOMÁTICA PENDENTE -> FILA ---
    public void promoteNextPendingIfSpaceAvailable(Long bookId) {
        long queueCount = waitlistRepository.countByBookIdAndStatusIn(bookId, List.of(WaitlistStatus.aguardando, WaitlistStatus.notificado));
        if (queueCount < 10) {
            Optional<PendingList> nextPending = pendingListRepository.findFirstByBookIdAndStatusOrderByDataEntradaAsc(bookId, PendingStatus.ativo);
            if (nextPending.isPresent()) {
                PendingList pending = nextPending.get();
                pending.setStatus(PendingStatus.promovido);
                pending.setDataPromocao(LocalDateTime.now());
                pendingListRepository.save(pending);

                Waitlist waitlist = Waitlist.builder()
                        .book(pending.getBook())
                        .user(pending.getUser())
                        .posicao((int) queueCount + 1)
                        .status(WaitlistStatus.aguardando)
                        .dataEntrada(LocalDateTime.now())
                        .build();
                waitlistRepository.save(waitlist);
            }
        }
    }

    // --- DEVOLUÇÃO DE LIVRO (RF-012, UC-013) ---
    @Transactional
    public RentalResponse returnBook(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aluguel não encontrado"));

        if (rental.getStatus() == RentalStatus.devolvido) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Este livro já foi devolvido");
        }

        LocalDate today = LocalDate.now();
        rental.setDataDevolucaoReal(today);
        rental.setStatus(RentalStatus.devolvido);

        BookCopy copy = rental.getBookCopy();
        copy.setStatus(CopyStatus.disponivel);
        bookCopyRepository.save(copy);

        // Calculate late fee if overdue (RF-016, RN-004)
        if (today.isAfter(rental.getDataDevolucaoPrevista())) {
            long daysOverdue = ChronoUnit.DAYS.between(rental.getDataDevolucaoPrevista(), today);
            BigDecimal valorMulta = BigDecimal.valueOf(daysOverdue * 3.0);

            BigDecimal teto = copy.getBook().getValorLivro();
            if (valorMulta.compareTo(teto) > 0) {
                valorMulta = teto;
            }

            Fine fine = Fine.builder()
                    .rental(rental)
                    .user(rental.getUser())
                    .tipo(FineType.atraso)
                    .valor(valorMulta)
                    .diasAtraso((int) daysOverdue)
                    .status(FineStatus.pendente)
                    .build();
            fineRepository.save(fine);

            // Apply block if overdue > 15 days
            if (daysOverdue > 15) {
                User u = rental.getUser();
                LocalDate blockUntil = today.plusDays(7);
                u.setBloqueadoAte(blockUntil);
                userRepository.save(u);
            }
        }

        // Check waitlist for this book
        Long bookId = copy.getBook().getId();
        Optional<Waitlist> nextInQueue = waitlistRepository.findFirstByBookIdAndStatusOrderByPosicaoAsc(bookId, WaitlistStatus.aguardando);

        if (nextInQueue.isPresent()) {
            Waitlist w = nextInQueue.get();
            w.setStatus(WaitlistStatus.notificado);
            w.setDataNotificacao(LocalDateTime.now());
            w.setDataLimiteResposta(today.plusDays(3));
            waitlistRepository.save(w);
        }

        rentalRepository.save(rental);
        return mapToRentalResponse(rental);
    }

    // --- CONFIRMAÇÃO DE ALUGUEL PELA FILA (RF-013) ---
    @Transactional
    public RentalResponse confirmQueueRental(User user, Long waitlistId) {
        Waitlist waitlist = waitlistRepository.findById(waitlistId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro de fila não encontrado"));

        if (!waitlist.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Registro de fila pertence a outro usuário");
        }

        if (waitlist.getStatus() != WaitlistStatus.notificado) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Registro de fila não está notificado para confirmação");
        }

        if (waitlist.getDataLimiteResposta() != null && LocalDate.now().isAfter(waitlist.getDataLimiteResposta())) {
            waitlist.setStatus(WaitlistStatus.expirado);
            waitlistRepository.save(waitlist);
            reorderQueueAndNotifyNext(waitlist.getBook().getId());
            promoteNextPendingIfSpaceAvailable(waitlist.getBook().getId());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Prazo para confirmação expirou");
        }

        validateUserEligibility(user);

        BookCopy copy = bookCopyRepository.findFirstByBookIdAndStatus(waitlist.getBook().getId(), CopyStatus.disponivel)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nenhum exemplar disponível para confirmação"));

        copy.setStatus(CopyStatus.alugado);
        bookCopyRepository.save(copy);

        waitlist.setStatus(WaitlistStatus.confirmado);
        waitlistRepository.save(waitlist);

        Book book = waitlist.getBook();
        int days = book.getNumPaginas() <= 150 ? 15 : 30;

        Rental rental = Rental.builder()
                .user(user)
                .bookCopy(copy)
                .dataAluguel(LocalDateTime.now())
                .dataDevolucaoPrevista(LocalDate.now().plusDays(days))
                .status(RentalStatus.ativo)
                .extensaoContador(0)
                .precoCobrado(book.getPrecoAluguel())
                .build();

        book.setAlugueisUltimoAno(book.getAlugueisUltimoAno() + 1);
        bookRepository.save(book);

        reorderQueueAndNotifyNext(book.getId());
        promoteNextPendingIfSpaceAvailable(book.getId());

        Rental saved = rentalRepository.save(rental);
        return mapToRentalResponse(saved);
    }

    // --- EXTENSÃO DE PRAZO (RF-014, RN-002) ---
    @Transactional
    public RentalResponse extendRental(User user, Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aluguel não encontrado"));

        if (!rental.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado");
        }

        if (rental.getStatus() != RentalStatus.ativo) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Apenas aluguéis ativos podem ser estendidos");
        }

        Long bookId = rental.getBookCopy().getBook().getId();
        long queueCount = waitlistRepository.countByBookIdAndStatusIn(bookId, List.of(WaitlistStatus.aguardando, WaitlistStatus.notificado));
        if (queueCount > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não é possível estender o prazo quando há pessoas na fila de espera");
        }

        if (rental.getExtensaoContador() >= 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Limite máximo de 2 extensões atingido");
        }

        LocalDate baseDate = rental.getDataDevolucaoPrevista().isBefore(LocalDate.now()) ? LocalDate.now() : rental.getDataDevolucaoPrevista();

        if (rental.getExtensaoContador() == 0) {
            rental.setDataDevolucaoPrevista(baseDate.plusMonths(2));
            rental.setExtensaoContador(1);
        } else if (rental.getExtensaoContador() == 1) {
            rental.setDataDevolucaoPrevista(baseDate.plusMonths(1));
            rental.setExtensaoContador(2);
        }

        Rental saved = rentalRepository.save(rental);
        return mapToRentalResponse(saved);
    }

    // --- MULTA POR DANO (RF-017, UC-014) ---
    @Transactional
    public FineResponse applyDamageFine(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aluguel não encontrado"));

        rental.getBookCopy().setStatus(CopyStatus.danificado);
        bookCopyRepository.save(rental.getBookCopy());

        Fine fine = Fine.builder()
                .rental(rental)
                .user(rental.getUser())
                .tipo(FineType.dano)
                .valor(BigDecimal.valueOf(20.00)) // Fixed R$ 20.00 (CA-017.3)
                .status(FineStatus.pendente)
                .build();

        Fine saved = fineRepository.save(fine);
        return mapToFineResponse(saved);
    }

    // --- PAGAR MULTA ---
    @Transactional
    public FineResponse payFine(User user, Long fineId) {
        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Multa não encontrada"));

        if (!fine.getUser().getId().equals(user.getId()) && user.getTipo() == UserType.aluno) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado");
        }

        if (fine.getStatus() == FineStatus.pago) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Esta multa já foi paga");
        }

        fine.setStatus(FineStatus.pago);
        fine.setDataPagamento(LocalDateTime.now());
        Fine saved = fineRepository.save(fine);
        return mapToFineResponse(saved);
    }

    // --- REORDER QUEUE & NOTIFY ---
    private void reorderQueueAndNotifyNext(Long bookId) {
        List<Waitlist> queue = waitlistRepository.findByBookIdAndStatusOrderByPosicaoAsc(bookId, WaitlistStatus.aguardando);
        int pos = 1;
        for (Waitlist w : queue) {
            w.setPosicao(pos++);
            waitlistRepository.save(w);
        }
    }

    // --- MAPPER METHODS ---
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

    public WaitlistResponse mapToWaitlistResponse(Waitlist w) {
        return WaitlistResponse.builder()
                .id(w.getId())
                .bookId(w.getBook().getId())
                .bookTitle(w.getBook().getTitulo())
                .userId(w.getUser().getId())
                .posicao(w.getPosicao())
                .status(w.getStatus())
                .dataEntrada(w.getDataEntrada())
                .dataNotificacao(w.getDataNotificacao())
                .dataLimiteResposta(w.getDataLimiteResposta())
                .build();
    }

    public PendingResponse mapToPendingResponse(PendingList p) {
        return PendingResponse.builder()
                .id(p.getId())
                .bookId(p.getBook().getId())
                .bookTitle(p.getBook().getTitulo())
                .userId(p.getUser().getId())
                .status(p.getStatus())
                .dataEntrada(p.getDataEntrada())
                .dataPromocao(p.getDataPromocao())
                .build();
    }

    public FineResponse mapToFineResponse(Fine f) {
        return FineResponse.builder()
                .id(f.getId())
                .rentalId(f.getRental().getId())
                .userId(f.getUser().getId())
                .tipo(f.getTipo())
                .valor(f.getValor())
                .diasAtraso(f.getDiasAtraso())
                .status(f.getStatus())
                .dataPagamento(f.getDataPagamento())
                .createdAt(f.getCreatedAt())
                .build();
    }
}
