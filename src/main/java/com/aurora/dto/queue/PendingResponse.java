package com.aurora.dto.queue;

import com.aurora.entity.enums.PendingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PendingResponse {
    private Long id;
    private Long bookId;
    private String bookTitle;
    private Long userId;
    private PendingStatus status;
    private LocalDateTime dataEntrada;
    private LocalDateTime dataPromocao;
}
