package com.aurora.repository;

import com.aurora.entity.ReadBook;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ReadBookRepository extends JpaRepository<ReadBook, Long> {
    List<ReadBook> findByUserId(Long userId);
    Optional<ReadBook> findByUserIdAndBookId(Long userId, Long bookId);
    boolean existsByUserIdAndBookId(Long userId, Long bookId);
}
