package com.aurora.entity;

import com.aurora.entity.enums.FilterType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "USO_FILTRO", uniqueConstraints = {
    @UniqueConstraint(name = "uq_uso", columnNames = {"id_usuario", "tipo_filtro", "valor"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilterUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_filtro", nullable = false)
    private FilterType tipoFiltro;

    @Column(nullable = false, length = 120)
    private String valor;

    @Column(nullable = false)
    @Builder.Default
    private Integer contagem = 0;
}
