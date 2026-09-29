package com.aurora.controller;

import com.aurora.dto.fine.FineResponse;
import com.aurora.entity.Fine;
import com.aurora.entity.User;
import com.aurora.entity.enums.FineStatus;
import com.aurora.repository.FineRepository;
import com.aurora.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/fines")
@RequiredArgsConstructor
public class FineController {

    private final FineRepository fineRepository;
    private final UserRepository userRepository;

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
        List<Fine> fines = fineRepository.findByUserId(user.getId());
        List<FineResponse> responses = fines.stream()
                .map(this::mapToFineResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('BIBLIOTECARIO', 'ADMINISTRADOR')")
    public ResponseEntity<List<FineResponse>> getAllFines() {
        List<Fine> fines = fineRepository.findAll();
        List<FineResponse> responses = fines.stream()
                .map(this::mapToFineResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/{fineId}/pay")
    public ResponseEntity<FineResponse> payFine(Authentication authentication, @PathVariable Long fineId) {
        User user = getUser(authentication);
        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Multa não encontrada"));

        if (!fine.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão para pagar esta multa");
        }

        fine.setStatus(FineStatus.pago);
        fine.setDataPagamento(LocalDateTime.now());
        Fine savedFine = fineRepository.save(fine);

        return ResponseEntity.ok(mapToFineResponse(savedFine));
    }

    private FineResponse mapToFineResponse(Fine fine) {
        return FineResponse.builder()
                .id(fine.getId())
                .rentalId(fine.getRental().getId())
                .userId(fine.getUser().getId())
                .tipo(fine.getTipo())
                .valor(fine.getValor())
                .diasAtraso(fine.getDiasAtraso() != null ? fine.getDiasAtraso().intValue() : 0)
                .status(fine.getStatus())
                .createdAt(fine.getCreatedAt())
                .build();
    }
}
