package com.aurora.controller;

import com.aurora.dto.user.UserResponse;
import com.aurora.dto.user.UserUpdateRequest;
import com.aurora.entity.User;
import com.aurora.repository.UserRepository;
import com.aurora.service.AuthService;
import com.aurora.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final AuthService authService;

    private User getUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado");
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
    }

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile(Authentication authentication) {
        User user = getUser(authentication);
        return ResponseEntity.ok(authService.mapToUserResponse(user));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateProfile(Authentication authentication, @RequestBody UserUpdateRequest request) {
        User user = getUser(authentication);
        return ResponseEntity.ok(userService.updateUser(user, request));
    }
}
