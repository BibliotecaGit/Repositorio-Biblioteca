package com.aurora.controller;

import com.aurora.dto.book.BookResponse;
import com.aurora.entity.User;
import com.aurora.entity.enums.FilterType;
import com.aurora.repository.UserRepository;
import com.aurora.service.CollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/suggestions")
@RequiredArgsConstructor
public class SuggestionController {

    private final CollectionService collectionService;
    private final UserRepository userRepository;

    private User getUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado");
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
    }

    @GetMapping
    public ResponseEntity<List<BookResponse>> getSuggestions(Authentication authentication) {
        User user = getUser(authentication);
        return ResponseEntity.ok(collectionService.getSuggestions(user));
    }

    @PostMapping("/record-filter")
    public ResponseEntity<Void> recordFilterUsage(Authentication authentication,
                                                  @RequestParam FilterType filterType,
                                                  @RequestParam String valor) {
        User user = getUser(authentication);
        collectionService.recordFilterUsage(user, filterType, valor);
        return ResponseEntity.ok().build();
    }
}
