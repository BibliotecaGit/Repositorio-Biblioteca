package com.aurora.dto.book;

import com.aurora.entity.enums.BookSize;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BookRequest {
    @NotBlank(message = "Título é obrigatório")
    private String titulo;

    @NotBlank(message = "Autor é obrigatório")
    private String autor;

    @NotBlank(message = "Gênero é obrigatório")
    private String genero;

    @NotNull(message = "Número de páginas é obrigatório")
    @Min(value = 1, message = "Número de páginas deve ser maior que 0")
    private Integer numPaginas;

    @NotNull(message = "Valor do livro é obrigatório")
    private BigDecimal valorLivro;

    @NotNull(message = "Preço do aluguel é obrigatório")
    private BigDecimal precoAluguel;

    private String capaUrl;
    private LocalDate dataLancamento;
    private Boolean ehInfantil;
    private BookSize faixaTamanhoOverride;
}
