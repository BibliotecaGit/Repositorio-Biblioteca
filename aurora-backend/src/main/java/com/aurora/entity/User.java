package com.aurora.entity;

import com.aurora.entity.enums.Language;
import com.aurora.entity.enums.Theme;
import com.aurora.entity.enums.UserType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "USUARIO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, unique = true, length = 190)
    private String email;

    @Column(name = "senha_hash", nullable = false, length = 255)
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserType tipo = UserType.aluno;

    @Column(name = "foto_url", length = 500)
    private String fotoUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Language idioma = Language.pt_BR;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Theme tema = Theme.claro;

    @Column(length = 2)
    private String uf;

    @Column(length = 120)
    private String cidade;

    @Column(name = "end_logradouro", length = 190)
    private String endLogradouro;

    @Column(name = "end_numero", length = 20)
    private String endNumero;

    @Column(name = "end_complemento", length = 120)
    private String endComplemento;

    @Column(name = "end_bairro", length = 120)
    private String endBairro;

    @Column(name = "end_cidade", length = 120)
    private String endCidade;

    @Column(name = "end_uf", length = 2)
    private String endUf;

    @Column(name = "end_cep", length = 8)
    private String endCep;

    @Column(name = "cartao_token", length = 255)
    private String cartaoToken;

    @Column(name = "cartao_mascarado", length = 25)
    private String cartaoMascarado;

    @Column(name = "bloqueado_ate")
    private LocalDate bloqueadoAte;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
