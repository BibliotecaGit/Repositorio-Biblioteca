package com.aurora.dto.collection;

import com.aurora.dto.book.BookResponse;
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
public class RatingResponse {
    private Long id;
    private Long userId;
    private Long bookId;
    private String bookTitle;
    private BigDecimal nota;
    private LocalDateTime dataAvaliacao;
}
