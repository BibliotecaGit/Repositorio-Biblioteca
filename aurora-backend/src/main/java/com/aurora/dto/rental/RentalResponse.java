package com.aurora.dto.rental;

import com.aurora.entity.enums.RentalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RentalResponse {
    private Long id;
    private Long userId;
    private String userName;
    private Long bookId;
    private String bookTitle;
    private Long bookCopyId;
    private LocalDateTime dataAluguel;
    private LocalDate dataDevolucaoPrevista;
    private LocalDate dataDevolucaoReal;
    private RentalStatus status;
    private Integer extensaoContador;
    private BigDecimal precoCobrado;
}
