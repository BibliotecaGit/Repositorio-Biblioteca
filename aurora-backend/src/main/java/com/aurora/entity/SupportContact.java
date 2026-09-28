package com.aurora.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "contato_suporte")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportContact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    private User user;

    @Column(nullable = false, length = 190)
    private String email;

    @Column(nullable = false, length = 190)
    private String assunto;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensagem;

    @Builder.Default
    @Column(nullable = false)
    private Boolean respondido = false;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
