package com.aurora.controller;

import com.aurora.dto.queue.PendingResponse;
import com.aurora.dto.queue.WaitlistResponse;
import com.aurora.dto.rental.RentalResponse;
import com.aurora.entity.User;
import com.aurora.entity.enums.PendingStatus;
import com.aurora.entity.enums.WaitlistStatus;
import com.aurora.repository.PendingListRepository;
import com.aurora.repository.UserRepository;
import com.aurora.repository.WaitlistRepository;
import com.aurora.service.RentalQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/queue")
@RequiredArgsConstructor
public class QueueController {

    private final RentalQueueService rentalQueueService;
    private final UserRepository userRepository;
    private final WaitlistRepository waitlistRepository;
    private final PendingListRepository pendingListRepository;

    private User getUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado");
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
    }

    @PostMapping("/book/{bookId}")
    public ResponseEntity<WaitlistResponse> enterQueue(Authentication authentication, @PathVariable Long bookId) {
        User user = getUser(authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(rentalQueueService.enterQueue(user, bookId));
    }

    @PostMapping("/pending/book/{bookId}")
    public ResponseEntity<PendingResponse> enterPending(Authentication authentication, @PathVariable Long bookId) {
        User user = getUser(authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(rentalQueueService.enterPending(user, bookId));
    }

    @PostMapping("/confirm/{waitlistId}")
    public ResponseEntity<RentalResponse> confirmQueueRental(Authentication authentication, @PathVariable Long waitlistId) {
        User user = getUser(authentication);
        return ResponseEntity.ok(rentalQueueService.confirmQueueRental(user, waitlistId));
    }

    @GetMapping("/my-queue")
    public ResponseEntity<List<WaitlistResponse>> getMyQueue(Authentication authentication) {
        User user = getUser(authentication);
        var list = waitlistRepository.findByUserIdAndStatusIn(user.getId(), List.of(WaitlistStatus.aguardando, WaitlistStatus.notificado));
        return ResponseEntity.ok(list.stream().map(rentalQueueService::mapToWaitlistResponse).toList());
    }

    @GetMapping("/my-pending")
    public ResponseEntity<List<PendingResponse>> getMyPending(Authentication authentication) {
        User user = getUser(authentication);
        var list = pendingListRepository.findByUserIdAndStatus(user.getId(), PendingStatus.ativo);
        return ResponseEntity.ok(list.stream().map(rentalQueueService::mapToPendingResponse).toList());
    }
}
