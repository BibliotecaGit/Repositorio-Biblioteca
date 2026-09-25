package com.aurora.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "AVALIACAO", uniqueConstraints = {
    @UniqueConstraint(name = "uq_avaliacao", columnNames = {"id_usuario", "id_livro"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_avaliacao")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_livro", nullable = false)
    private Book book;

    @Column(nullable = false, precision = 2, scale = 1)
    private BigDecimal nota;

    @Column(name = "data_avaliacao", nullable = false)
    @Builder.Default
    private LocalDateTime dataAvaliacao = LocalDateTime.now();
}
