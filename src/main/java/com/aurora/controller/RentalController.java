package com.aurora.controller;

import com.aurora.dto.fine.FineResponse;
import com.aurora.dto.rental.RentalResponse;
import com.aurora.entity.Rental;
import com.aurora.entity.User;
import com.aurora.entity.enums.RentalStatus;
import com.aurora.repository.RentalRepository;
import com.aurora.repository.UserRepository;
import com.aurora.service.RentalQueueService;
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

    private final RentalQueueService rentalQueueService;
    private final UserRepository userRepository;
    private final RentalRepository rentalRepository;

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
        return ResponseEntity.status(HttpStatus.CREATED).body(rentalQueueService.rentBook(user, bookId));
    }

    @PostMapping("/{rentalId}/return")
    public ResponseEntity<RentalResponse> returnBook(@PathVariable Long rentalId) {
        return ResponseEntity.ok(rentalQueueService.returnBook(rentalId));
    }

    @PostMapping("/{rentalId}/extend")
    public ResponseEntity<RentalResponse> extendRental(Authentication authentication, @PathVariable Long rentalId) {
        User user = getUser(authentication);
        return ResponseEntity.ok(rentalQueueService.extendRental(user, rentalId));
    }

    @PostMapping("/{rentalId}/damage-fine")
    @PreAuthorize("hasAnyRole('BIBLIOTECARIO', 'ADMINISTRADOR')")
    public ResponseEntity<FineResponse> applyDamageFine(@PathVariable Long rentalId) {
        return ResponseEntity.ok(rentalQueueService.applyDamageFine(rentalId));
    }

    @GetMapping("/my-rentals")
    public ResponseEntity<List<RentalResponse>> getMyRentals(Authentication authentication) {
        User user = getUser(authentication);
        List<Rental> rentals = rentalRepository.findByUserIdAndStatus(user.getId(), RentalStatus.ativo);
        return ResponseEntity.ok(rentals.stream().map(rentalQueueService::mapToRentalResponse).toList());
    }
}
