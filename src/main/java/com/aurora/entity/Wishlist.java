package com.aurora.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "LISTA_DESEJOS", uniqueConstraints = {
    @UniqueConstraint(name = "uq_desejo", columnNames = {"id_usuario", "id_livro"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wishlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_livro", nullable = false)
    private Book book;

    @Column(name = "data_marcacao", nullable = false)
    @Builder.Default
    private LocalDateTime dataMarcacao = LocalDateTime.now();
}
