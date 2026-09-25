package com.aurora.dto.fine;

import com.aurora.entity.enums.FineStatus;
import com.aurora.entity.enums.FineType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FineResponse {
    private Long id;
    private Long rentalId;
    private Long userId;
    private FineType tipo;
    private BigDecimal valor;
    private Integer diasAtraso;
    private FineStatus status;
    private LocalDateTime dataPagamento;
    private LocalDateTime createdAt;
}
