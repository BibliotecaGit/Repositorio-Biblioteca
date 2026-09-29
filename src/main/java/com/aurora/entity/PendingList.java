package com.aurora.entity;

import com.aurora.entity.enums.PendingStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "LISTA_PENDENTES")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pendente")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_livro", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PendingStatus status = PendingStatus.ativo;

    @Column(name = "data_entrada", nullable = false)
    @Builder.Default
    private LocalDateTime dataEntrada = LocalDateTime.now();

    @Column(name = "data_promocao")
    private LocalDateTime dataPromocao;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
