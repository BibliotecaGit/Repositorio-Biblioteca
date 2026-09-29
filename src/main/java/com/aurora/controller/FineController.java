package com.aurora.controller;

import com.aurora.dto.fine.FineResponse;
import com.aurora.entity.User;
import com.aurora.repository.FineRepository;
import com.aurora.repository.UserRepository;
import com.aurora.service.RentalQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/fines")
@RequiredArgsConstructor
public class FineController {

    private final FineRepository fineRepository;
    private final UserRepository userRepository;
    private final RentalQueueService rentalQueueService;

    private User getUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado");
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
    }

    @GetMapping("/my-fines")
    public ResponseEntity<List<FineResponse>> getMyFines(Authentication authentication) {
        User user = getUser(authentication);
        var fines = fineRepository.findByUserId(user.getId());
        return ResponseEntity.ok(fines.stream().map(rentalQueueService::mapToFineResponse).toList());
    }

    @PostMapping("/{fineId}/pay")
    public ResponseEntity<FineResponse> payFine(Authentication authentication, @PathVariable Long fineId) {
        User user = getUser(authentication);
        return ResponseEntity.ok(rentalQueueService.payFine(user, fineId));
    }
}
