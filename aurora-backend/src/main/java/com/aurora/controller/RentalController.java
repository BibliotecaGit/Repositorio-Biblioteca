package com.aurora.controller;

import com.aurora.dto.fine.FineResponse;
import com.aurora.dto.rental.RentalResponse;
import com.aurora.entity.User;
import com.aurora.repository.UserRepository;
import com.aurora.service.RentalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/rentals")
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;
    private final UserRepository userRepository;

    private User getUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado");
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
    }

    @PostMapping("/book/{bookId}")
    public ResponseEntity<RentalResponse> rentBook(Authentication authentication, @PathVariable Long bookId) {
        User user = getUser(authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(rentalService.rentBook(user.getId(), bookId));
    }

    @PostMapping("/{rentalId}/return")
    public ResponseEntity<RentalResponse> returnBook(@PathVariable Long rentalId) {
        return ResponseEntity.ok(rentalService.returnBook(rentalId));
    }

    @PostMapping("/{rentalId}/extend")
    public ResponseEntity<RentalResponse> extendRental(Authentication authentication, @PathVariable Long rentalId) {
        User user = getUser(authentication);
        return ResponseEntity.ok(rentalService.extendRental(rentalId, user.getId()));
    }

    @PostMapping("/{rentalId}/damage-fine")
    @PreAuthorize("hasAnyRole('BIBLIOTECARIO', 'ADMINISTRADOR')")
    public ResponseEntity<FineResponse> applyDamageFine(@PathVariable Long rentalId, @RequestParam(defaultValue = "Dano ao exemplar") String damageType) {
        return ResponseEntity.ok(rentalService.applyDamageFine(rentalId, damageType));
    }

    @GetMapping("/my-rentals")
    public ResponseEntity<List<RentalResponse>> getMyRentals(Authentication authentication) {
        User user = getUser(authentication);
        return ResponseEntity.ok(rentalService.getUserRentals(user.getId()));
    }
}
