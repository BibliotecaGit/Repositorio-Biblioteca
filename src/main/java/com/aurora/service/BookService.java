package com.aurora.service;

import com.aurora.dto.book.BookRequest;
import com.aurora.dto.book.BookResponse;
import com.aurora.entity.Book;
import com.aurora.entity.BookCopy;
import com.aurora.entity.enums.CopyStatus;
import com.aurora.entity.enums.WaitlistStatus;
import com.aurora.repository.BookCopyRepository;
import com.aurora.repository.BookRepository;
import com.aurora.repository.WaitlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final BookCopyRepository bookCopyRepository;
    private final WaitlistRepository waitlistRepository;

    @Transactional
    public BookResponse createBook(BookRequest request) {
        Book book = Book.builder()
                .titulo(request.getTitulo())
                .autor(request.getAutor())
                .genero(request.getGenero())
                .numPaginas(request.getNumPaginas())
                .valorLivro(request.getValorLivro())
                .precoAluguel(request.getPrecoAluguel())
                .capaUrl(request.getCapaUrl())
                .dataLancamento(request.getDataLancamento())
                .ehInfantil(request.getEhInfantil() != null && request.getEhInfantil())
                .build();

        if (request.getFaixaTamanhoOverride() != null) {
            book.setFaixaTamanho(request.getFaixaTamanhoOverride());
        } else {
            book.updateFaixaTamanho();
        }

        Book savedBook = bookRepository.save(book);

        // Create 3 copies by default (CA-004.5)
        for (int i = 1; i <= 3; i++) {
            BookCopy copy = BookCopy.builder()
                    .book(savedBook)
                    .codigoBarras("BAR-" + savedBook.getId() + "-" + i)
                    .status(CopyStatus.disponivel)
                    .build();
            bookCopyRepository.save(copy);
        }

        return mapToBookResponse(savedBook);
    }

    @Transactional
    public BookResponse updateBook(Long id, BookRequest request) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Livro não encontrado"));

        if (book.getDeletedAt() != null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Livro não encontrado");
        }

        book.setTitulo(request.getTitulo());
        book.setAutor(request.getAutor());
        book.setGenero(request.getGenero());
        book.setNumPaginas(request.getNumPaginas());
        book.setValorLivro(request.getValorLivro());
        book.setPrecoAluguel(request.getPrecoAluguel());
        book.setCapaUrl(request.getCapaUrl());
        book.setDataLancamento(request.getDataLancamento());
        if (request.getEhInfantil() != null) {
            book.setEhInfantil(request.getEhInfantil());
        }

        if (request.getFaixaTamanhoOverride() != null) {
            book.setFaixaTamanho(request.getFaixaTamanhoOverride());
        } else {
            book.updateFaixaTamanho();
        }

        Book updatedBook = bookRepository.save(book);
        return mapToBookResponse(updatedBook);
    }

    @Transactional
    public void deleteBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Livro não encontrado"));

        // CA-007.1: cannot delete if copies currently rented
        List<BookCopy> rentedCopies = bookCopyRepository.findByBookIdAndStatus(id, CopyStatus.alugado);
        if (!rentedCopies.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não é permitido excluir um livro que possua exemplares atualmente alugados");
        }

        // CA-007.2: cannot delete if waitlist exists
        long queueCount = waitlistRepository.countByBookIdAndStatusIn(id, List.of(WaitlistStatus.aguardando, WaitlistStatus.notificado));
        if (queueCount > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não é permitido excluir um livro que possua usuários na fila de espera");
        }

        book.setDeletedAt(LocalDateTime.now());
        bookRepository.save(book);
    }

    public List<BookResponse> getAllBooks() {
        return bookRepository.findByDeletedAtIsNull().stream()
                .map(this::mapToBookResponse)
                .toList();
    }

    public BookResponse getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Livro não encontrado"));
        if (book.getDeletedAt() != null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Livro não encontrado");
        }
        return mapToBookResponse(book);
    }

    public List<BookResponse> searchBooks(String query) {
        return bookRepository.searchBooks(query).stream()
                .map(this::mapToBookResponse)
                .toList();
    }

    public List<BookResponse> getNovidades() {
        LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6);
        List<Book> books = bookRepository.findByDataLancamentoAfterAndDeletedAtIsNull(sixMonthsAgo);
        if (books.isEmpty()) {
            books = bookRepository.findByDeletedAtIsNull();
        }
        return books.stream().map(this::mapToBookResponse).toList();
    }

    public List<BookResponse> getDestaques() {
        return bookRepository.findDestaques().stream()
                .map(this::mapToBookResponse)
                .toList();
    }

    public List<BookResponse> getInfantis() {
        return bookRepository.findByEhInfantilTrueAndDeletedAtIsNull().stream()
                .map(this::mapToBookResponse)
                .toList();
    }

    public BookResponse mapToBookResponse(Book book) {
        long totalCopias = bookCopyRepository.countByBookId(book.getId());
        long copiasDisponiveis = bookCopyRepository.findByBookIdAndStatus(book.getId(), CopyStatus.disponivel).size();
        long queueCount = waitlistRepository.countByBookIdAndStatusIn(book.getId(), List.of(WaitlistStatus.aguardando, WaitlistStatus.notificado));

        boolean canAlugar = copiasDisponiveis > 0 && queueCount == 0;
        boolean canFila = copiasDisponiveis == 0 && queueCount < 10;
        boolean canPendente = queueCount >= 10;

        return BookResponse.builder()
                .id(book.getId())
                .titulo(book.getTitulo())
                .autor(book.getAutor())
                .genero(book.getGenero())
                .faixaTamanho(book.getFaixaTamanho())
                .numPaginas(book.getNumPaginas())
                .valorLivro(book.getValorLivro())
                .precoAluguel(book.getPrecoAluguel())
                .capaUrl(book.getCapaUrl())
                .dataLancamento(book.getDataLancamento())
                .avaliacaoMedia(book.getAvaliacaoMedia())
                .totalAvaliacoes(book.getTotalAvaliacoes())
                .alugueisUltimoAno(book.getAlugueisUltimoAno())
                .ehInfantil(book.getEhInfantil())
                .copiasDisponiveis(copiasDisponiveis)
                .totalCopias(totalCopias)
                .canAlugar(canAlugar)
                .canFila(canFila)
                .canPendente(canPendente)
                .build();
    }
}
