package com.aurora.dto.queue;

import com.aurora.entity.enums.WaitlistStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WaitlistResponse {
    private Long id;
    private Long bookId;
    private String bookTitle;
    private Long userId;
    private Integer posicao;
    private WaitlistStatus status;
    private LocalDateTime dataEntrada;
    private LocalDateTime dataNotificacao;
    private LocalDate dataLimiteResposta;
}
