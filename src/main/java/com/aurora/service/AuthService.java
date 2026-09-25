package com.aurora.service;

import com.aurora.dto.auth.AuthResponse;
import com.aurora.dto.auth.LoginRequest;
import com.aurora.dto.auth.RegisterRequest;
import com.aurora.dto.user.UserResponse;
import com.aurora.entity.User;
import com.aurora.entity.enums.UserType;
import com.aurora.repository.UserRepository;
import com.aurora.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Este e-mail já está em uso");
        }

        User user = User.builder()
                .nome(request.getNome())
                .email(request.getEmail())
                .senhaHash(passwordEncoder.encode(request.getSenha()))
                .tipo(UserType.aluno)
                .build();

        User savedUser = userRepository.save(user);
        String token = tokenProvider.generateToken(savedUser.getEmail());

        return AuthResponse.builder()
                .token(token)
                .user(mapToUserResponse(savedUser))
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha incorretos"));

        if (!passwordEncoder.matches(request.getSenha(), user.getSenhaHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha incorretos");
        }

        String token = tokenProvider.generateToken(user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .user(mapToUserResponse(user))
                .build();
    }

    public UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .nome(user.getNome())
                .email(user.getEmail())
                .tipo(user.getTipo())
                .fotoUrl(user.getFotoUrl())
                .idioma(user.getIdioma())
                .tema(user.getTema())
                .uf(user.getUf())
                .cidade(user.getCidade())
                .endLogradouro(user.getEndLogradouro())
                .endNumero(user.getEndNumero())
                .endComplemento(user.getEndComplemento())
                .endBairro(user.getEndBairro())
                .endCidade(user.getEndCidade())
                .endUf(user.getEndUf())
                .endCep(user.getEndCep())
                .cartaoMascarado(user.getCartaoMascarado())
                .bloqueadoAte(user.getBloqueadoAte())
                .build();
    }
}
