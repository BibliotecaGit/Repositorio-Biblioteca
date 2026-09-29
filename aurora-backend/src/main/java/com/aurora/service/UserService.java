package com.aurora.service;

import com.aurora.dto.user.UserResponse;
import com.aurora.dto.user.UserUpdateRequest;
import com.aurora.entity.User;
import com.aurora.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AuthService authService;

    @Transactional
    public UserResponse updateUser(User user, UserUpdateRequest request) {
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "E-mail já cadastrado no sistema");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getNome() != null) user.setNome(request.getNome());
        if (request.getFotoUrl() != null) user.setFotoUrl(request.getFotoUrl());
        if (request.getUf() != null) user.setUf(request.getUf());
        if (request.getCidade() != null) user.setCidade(request.getCidade());
        if (request.getEndLogradouro() != null) user.setEndLogradouro(request.getEndLogradouro());
        if (request.getEndNumero() != null) user.setEndNumero(request.getEndNumero());
        if (request.getEndComplemento() != null) user.setEndComplemento(request.getEndComplemento());
        if (request.getEndBairro() != null) user.setEndBairro(request.getEndBairro());
        if (request.getEndCidade() != null) user.setEndCidade(request.getEndCidade());
        if (request.getEndUf() != null) user.setEndUf(request.getEndUf());

        if (request.getEndCep() != null) {
            String digits = request.getEndCep().replaceAll("\\D", "");
            if (digits.length() != 8) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CEP deve conter 8 dígitos");
            }
            user.setEndCep(digits);
        }

        if (request.getCartaoNumero() != null && !request.getCartaoNumero().isBlank()) {
            String cleanNumber = request.getCartaoNumero().replaceAll("\\D", "");
            if (cleanNumber.length() >= 4) {
                String last4 = cleanNumber.substring(cleanNumber.length() - 4);
                user.setCartaoMascarado("**** " + last4);
                user.setCartaoToken("TOK-" + System.currentTimeMillis());
            }
        }

        if (request.getTema() != null) user.setTema(request.getTema());
        if (request.getIdioma() != null) user.setIdioma(request.getIdioma());

        User updated = userRepository.save(user);
        return authService.mapToUserResponse(updated);
    }
}
