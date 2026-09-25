package com.aurora.entity;

import com.aurora.entity.enums.BookSize;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "LIVRO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_livro")
    private Long id;

    @Column(nullable = false, length = 255)
    private String titulo;

    @Column(nullable = false, length = 190)
    private String autor;

    @Column(nullable = false, length = 80)
    private String genero;

    @Enumerated(EnumType.STRING)
    @Column(name = "faixa_tamanho", nullable = false)
    private BookSize faixaTamanho;

    @Column(name = "num_paginas", nullable = false)
    private Integer numPaginas;

    @Column(name = "valor_livro", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorLivro;

    @Column(name = "preco_aluguel", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoAluguel;

    @Column(name = "capa_url", length = 500)
    private String capaUrl;

    @Column(name = "data_lancamento")
    private LocalDate dataLancamento;

    @Column(name = "avaliacao_media", nullable = false, precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal avaliacaoMedia = BigDecimal.ZERO;

    @Column(name = "total_avaliacoes", nullable = false)
    @Builder.Default
    private Integer totalAvaliacoes = 0;

    @Column(name = "alugueis_ultimo_ano", nullable = false)
    @Builder.Default
    private Integer alugueisUltimoAno = 0;

    @Column(name = "eh_infantil", nullable = false)
    @Builder.Default
    private Boolean ehInfantil = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public void updateFaixaTamanho() {
        if (numPaginas == null) return;
        if (numPaginas <= 100) {
            this.faixaTamanho = BookSize.pequeno;
        } else if (numPaginas <= 150) {
            this.faixaTamanho = BookSize.medio_pequeno;
        } else if (numPaginas <= 250) {
            this.faixaTamanho = BookSize.medio_padrao;
        } else {
            this.faixaTamanho = BookSize.grande;
        }
    }
}
