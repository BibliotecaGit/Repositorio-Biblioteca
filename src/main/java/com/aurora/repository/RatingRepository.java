package com.aurora.repository;

import com.aurora.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    List<Rating> findByBookId(Long bookId);
    List<Rating> findByUserId(Long userId);
    Optional<Rating> findByUserIdAndBookId(Long userId, Long bookId);
}
