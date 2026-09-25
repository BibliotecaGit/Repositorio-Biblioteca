package com.aurora.controller;

import com.aurora.dto.book.BookResponse;
import com.aurora.dto.collection.RatingRequest;
import com.aurora.dto.collection.RatingResponse;
import com.aurora.dto.common.MessageResponse;
import com.aurora.entity.User;
import com.aurora.repository.UserRepository;
import com.aurora.service.CollectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/collections")
@RequiredArgsConstructor
public class CollectionController {

    private final CollectionService collectionService;
    private final UserRepository userRepository;

    private User getUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado");
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
    }

    // Já Lidos
    @PostMapping("/read/{bookId}")
    public ResponseEntity<MessageResponse> markAsRead(Authentication authentication, @PathVariable Long bookId) {
        User user = getUser(authentication);
        collectionService.markAsRead(user, bookId);
        return ResponseEntity.ok(new MessageResponse("Livro marcado como 'Lido'", true));
    }

    @DeleteMapping("/read/{bookId}")
    public ResponseEntity<MessageResponse> unmarkAsRead(Authentication authentication, @PathVariable Long bookId) {
        User user = getUser(authentication);
        collectionService.unmarkAsRead(user, bookId);
        return ResponseEntity.ok(new MessageResponse("Livro desmarcado de 'Lido'", true));
    }

    @GetMapping("/read")
    public ResponseEntity<List<BookResponse>> getReadBooks(Authentication authentication) {
        User user = getUser(authentication);
        return ResponseEntity.ok(collectionService.getReadBooks(user));
    }

    // Lista de Desejos
    @PostMapping("/wishlist/{bookId}")
    public ResponseEntity<MessageResponse> addToWishlist(Authentication authentication, @PathVariable Long bookId) {
        User user = getUser(authentication);
        collectionService.addToWishlist(user, bookId);
        return ResponseEntity.ok(new MessageResponse("Livro adicionado à Lista de Desejos", true));
    }

    @DeleteMapping("/wishlist/{bookId}")
    public ResponseEntity<MessageResponse> removeFromWishlist(Authentication authentication, @PathVariable Long bookId) {
        User user = getUser(authentication);
        collectionService.removeFromWishlist(user, bookId);
        return ResponseEntity.ok(new MessageResponse("Item removido da Lista de Desejos", true));
    }

    @GetMapping("/wishlist")
    public ResponseEntity<List<BookResponse>> getWishlist(Authentication authentication) {
        User user = getUser(authentication);
        return ResponseEntity.ok(collectionService.getWishlist(user));
    }

    // Avaliações
    @PostMapping("/ratings/book/{bookId}")
    public ResponseEntity<RatingResponse> rateBook(Authentication authentication, @PathVariable Long bookId, @Valid @RequestBody RatingRequest request) {
        User user = getUser(authentication);
        return ResponseEntity.ok(collectionService.rateBook(user, bookId, request));
    }

    @GetMapping("/ratings/my-ratings")
    public ResponseEntity<List<RatingResponse>> getMyRatings(Authentication authentication) {
        User user = getUser(authentication);
        return ResponseEntity.ok(collectionService.getUserRatings(user));
    }
}
