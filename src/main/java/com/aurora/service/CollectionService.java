package com.aurora.service;

import com.aurora.dto.book.BookResponse;
import com.aurora.dto.collection.RatingRequest;
import com.aurora.dto.collection.RatingResponse;
import com.aurora.entity.*;
import com.aurora.entity.enums.FilterType;
import com.aurora.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CollectionService {

    private final ReadBookRepository readBookRepository;
    private final WishlistRepository wishlistRepository;
    private final RatingRepository ratingRepository;
    private final BookRepository bookRepository;
    private final FilterUsageRepository filterUsageRepository;
    private final BookService bookService;

    // --- JÁ LIDOS (RF-024) ---
    @Transactional
    public void markAsRead(User user, Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Livro não encontrado"));

        if (!readBookRepository.existsByUserIdAndBookId(user.getId(), bookId)) {
            ReadBook readBook = ReadBook.builder()
                    .user(user)
                    .book(book)
                    .dataMarcacao(LocalDateTime.now())
                    .build();
            readBookRepository.save(readBook);
        }
    }

    @Transactional
    public void unmarkAsRead(User user, Long bookId) {
        readBookRepository.findByUserIdAndBookId(user.getId(), bookId)
                .ifPresent(readBookRepository::delete);
    }

    public List<BookResponse> getReadBooks(User user) {
        return readBookRepository.findByUserId(user.getId()).stream()
                .map(rb -> bookService.mapToBookResponse(rb.getBook()))
                .toList();
    }

    // --- LISTA DE DESEJOS (RF-025) ---
    @Transactional
    public void addToWishlist(User user, Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Livro não encontrado"));

        if (!wishlistRepository.existsByUserIdAndBookId(user.getId(), bookId)) {
            Wishlist wishlist = Wishlist.builder()
                    .user(user)
                    .book(book)
                    .dataMarcacao(LocalDateTime.now())
                    .build();
            wishlistRepository.save(wishlist);
        }
    }

    @Transactional
    public void removeFromWishlist(User user, Long bookId) {
        wishlistRepository.findByUserIdAndBookId(user.getId(), bookId)
                .ifPresent(wishlistRepository::delete);
    }

    public List<BookResponse> getWishlist(User user) {
        return wishlistRepository.findByUserId(user.getId()).stream()
                .map(w -> bookService.mapToBookResponse(w.getBook()))
                .toList();
    }

    // --- AVALIAÇÕES (RF-026, RN-009) ---
    @Transactional
    public RatingResponse rateBook(User user, Long bookId, RatingRequest request) {
        // CA-026.1: Must be marked as Read
        if (!readBookRepository.existsByUserIdAndBookId(user.getId(), bookId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Apenas livros marcados como 'Lido' podem ser avaliados");
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Livro não encontrado"));

        Optional<Rating> existing = ratingRepository.findByUserIdAndBookId(user.getId(), bookId);
        Rating rating;
        if (existing.isPresent()) {
            rating = existing.get();
            rating.setNota(request.getNota());
            rating.setDataAvaliacao(LocalDateTime.now());
        } else {
            rating = Rating.builder()
                    .user(user)
                    .book(book)
                    .nota(request.getNota())
                    .dataAvaliacao(LocalDateTime.now())
                    .build();
        }

        Rating savedRating = ratingRepository.save(rating);

        // Recalculate book average
        recalculateBookRating(book);

        return RatingResponse.builder()
                .id(savedRating.getId())
                .userId(user.getId())
                .bookId(book.getId())
                .bookTitle(book.getTitulo())
                .nota(savedRating.getNota())
                .dataAvaliacao(savedRating.getDataAvaliacao())
                .build();
    }

    private void recalculateBookRating(Book book) {
        List<Rating> ratings = ratingRepository.findByBookId(book.getId());
        if (ratings.isEmpty()) {
            book.setAvaliacaoMedia(BigDecimal.ZERO);
            book.setTotalAvaliacoes(0);
        } else {
            BigDecimal sum = BigDecimal.ZERO;
            for (Rating r : ratings) {
                sum = sum.add(r.getNota());
            }
            BigDecimal avg = sum.divide(BigDecimal.valueOf(ratings.size()), 2, RoundingMode.HALF_UP);
            book.setAvaliacaoMedia(avg);
            book.setTotalAvaliacoes(ratings.size());
        }
        bookRepository.save(book);
    }

    public List<RatingResponse> getUserRatings(User user) {
        return ratingRepository.findByUserId(user.getId()).stream()
                .map(r -> RatingResponse.builder()
                        .id(r.getId())
                        .userId(r.getUser().getId())
                        .bookId(r.getBook().getId())
                        .bookTitle(r.getBook().getTitulo())
                        .nota(r.getNota())
                        .dataAvaliacao(r.getDataAvaliacao())
                        .build())
                .toList();
    }

    // --- REGISTRO E USO DE FILTROS (RF-023, RN-010) ---
    @Transactional
    public void recordFilterUsage(User user, FilterType filterType, String valor) {
        Optional<FilterUsage> existing = filterUsageRepository.findByUserIdAndTipoFiltroAndValor(user.getId(), filterType, valor);
        if (existing.isPresent()) {
            FilterUsage fu = existing.get();
            fu.setContagem(fu.getContagem() + 1);
            filterUsageRepository.save(fu);
        } else {
            FilterUsage fu = FilterUsage.builder()
                    .user(user)
                    .tipoFiltro(filterType)
                    .valor(valor)
                    .contagem(1)
                    .build();
            filterUsageRepository.save(fu);
        }
    }

    // --- SUGESTÕES PERSONALIZADAS (RF-023, RN-010) ---
    public List<BookResponse> getSuggestions(User user) {
        List<FilterUsage> topGeneros = filterUsageRepository.findByUserIdAndTipoFiltroOrderByContagemDesc(user.getId(), FilterType.genero);
        List<FilterUsage> topAutores = filterUsageRepository.findByUserIdAndTipoFiltroOrderByContagemDesc(user.getId(), FilterType.autor);

        List<Book> books;
        if (!topGeneros.isEmpty()) {
            String favoriteGenre = topGeneros.get(0).getValor();
            books = bookRepository.findByGeneroAndDeletedAtIsNull(favoriteGenre);
        } else if (!topAutores.isEmpty()) {
            String favoriteAuthor = topAutores.get(0).getValor();
            books = bookRepository.findByAutorAndDeletedAtIsNull(favoriteAuthor);
        } else {
            books = bookRepository.findByDeletedAtIsNull();
        }

        return books.stream().map(bookService::mapToBookResponse).toList();
    }
}
