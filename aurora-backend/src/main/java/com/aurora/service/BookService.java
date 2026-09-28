package com.aurora.service;

import com.aurora.dto.book.BookRequest;
import com.aurora.dto.book.BookResponse;
import com.aurora.entity.Book;
import com.aurora.entity.BookCopy;
import com.aurora.entity.enums.BookSize;
import com.aurora.entity.enums.CopyStatus;
import com.aurora.repository.BookCopyRepository;
import com.aurora.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final BookCopyRepository bookCopyRepository;

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
                .ehInfantil(request.getGenero() != null && request.getGenero().equalsIgnoreCase("Infantil"))
                .build();

        book.updateFaixaTamanho();
        Book savedBook = bookRepository.save(book);

        for (int i = 0; i < 3; i++) {
            BookCopy copy = BookCopy.builder()
                    .book(savedBook)
                    .status(CopyStatus.disponivel)
                    .build();
            bookCopyRepository.save(copy);
        }

        return mapToBookResponse(savedBook);
    }

    @Transactional
    public BookResponse updateBook(Long id, BookRequest request) {
        Book book = bookRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new IllegalArgumentException("Livro não encontrado"));

        book.setTitulo(request.getTitulo());
        book.setAutor(request.getAutor());
        book.setGenero(request.getGenero());
        book.setNumPaginas(request.getNumPaginas());
        book.setValorLivro(request.getValorLivro());
        book.setPrecoAluguel(request.getPrecoAluguel());
        book.setCapaUrl(request.getCapaUrl());
        book.setDataLancamento(request.getDataLancamento());
        book.setEhInfantil(request.getGenero() != null && request.getGenero().equalsIgnoreCase("Infantil"));

        book.updateFaixaTamanho();
        Book savedBook = bookRepository.save(book);

        return mapToBookResponse(savedBook);
    }

    @Transactional
    public void deleteBook(Long id) {
        Book book = bookRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new IllegalArgumentException("Livro não encontrado"));

        long rentedCopies = bookCopyRepository.countByBookIdAndStatus(id, CopyStatus.alugado);
        if (rentedCopies > 0) {
            throw new IllegalStateException("Não é possível excluir um livro com exemplares atualmente alugados.");
        }

        book.setDeletedAt(LocalDateTime.now());
        bookRepository.save(book);
    }

    @Transactional(readOnly = true)
    public BookResponse getBookById(Long id) {
        Book book = bookRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new IllegalArgumentException("Livro não encontrado"));
        return mapToBookResponse(book);
    }

    @Transactional(readOnly = true)
    public Page<BookResponse> getAllBooks(Pageable pageable) {
        return bookRepository.findAllByDeletedAtIsNull(pageable)
                .map(this::mapToBookResponse);
    }

    @Transactional(readOnly = true)
    public List<BookResponse> getAllBooks() {
        return bookRepository.findAllByDeletedAtIsNull().stream()
                .map(this::mapToBookResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookResponse> searchBooks(String query) {
        return bookRepository.searchActiveBooks(query)
                .stream()
                .map(this::mapToBookResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookResponse> filterBooks(BookSize tamanho, String genero, String autor, Boolean novidades, Boolean destaques, Boolean infantis) {
        List<Book> books;
        if (Boolean.TRUE.equals(novidades)) {
            LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6);
            books = bookRepository.findByDataLancamentoAfterAndDeletedAtIsNull(sixMonthsAgo);
        } else if (Boolean.TRUE.equals(destaques)) {
            books = bookRepository.findDestaques();
        } else if (Boolean.TRUE.equals(infantis)) {
            books = bookRepository.findByEhInfantilTrueAndDeletedAtIsNull();
        } else if (tamanho != null) {
            books = bookRepository.findByFaixaTamanhoAndDeletedAtIsNull(tamanho);
        } else if (genero != null && !genero.isBlank()) {
            books = bookRepository.findByGeneroIgnoreCaseAndDeletedAtIsNull(genero);
        } else if (autor != null && !autor.isBlank()) {
            books = bookRepository.findByAutorIgnoreCaseAndDeletedAtIsNull(autor);
        } else {
            books = bookRepository.findAllByDeletedAtIsNull();
        }

        return books.stream()
                .map(this::mapToBookResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookResponse> getNovidades() {
        LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6);
        List<Book> novidades = bookRepository.findByDataLancamentoAfterAndDeletedAtIsNull(sixMonthsAgo);
        if (novidades.isEmpty()) {
            novidades = bookRepository.findTop10ByDeletedAtIsNullOrderByCreatedAtDesc();
        }
        return novidades.stream()
                .map(this::mapToBookResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookResponse> getDestaques() {
        return bookRepository.findDestaques().stream()
                .map(this::mapToBookResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookResponse> getInfantis() {
        return bookRepository.findByEhInfantilTrueAndDeletedAtIsNull().stream()
                .map(this::mapToBookResponse)
                .collect(Collectors.toList());
    }

    public BookResponse mapToBookResponse(Book book) {
        long availableCopies = bookCopyRepository.countByBookIdAndStatus(book.getId(), CopyStatus.disponivel);

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
                .ehInfantil(book.getEhInfantil())
                .disponivel(availableCopies > 0)
                .copiasDisponiveis((int) availableCopies)
                .build();
    }
}
