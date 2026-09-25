package com.aurora.dto.book;

import com.aurora.entity.enums.BookSize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookResponse {
    private Long id;
    private String titulo;
    private String autor;
    private String genero;
    private BookSize faixaTamanho;
    private Integer numPaginas;
    private BigDecimal valorLivro;
    private BigDecimal precoAluguel;
    private String capaUrl;
    private LocalDate dataLancamento;
    private BigDecimal avaliacaoMedia;
    private Integer totalAvaliacoes;
    private Integer alugueisUltimoAno;
    private Boolean ehInfantil;
    private Long copiasDisponiveis;
    private Long totalCopias;
    private Boolean canAlugar;
    private Boolean canFila;
    private Boolean canPendente;
}
