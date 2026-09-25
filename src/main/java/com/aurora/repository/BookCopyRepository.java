package com.aurora.repository;

import com.aurora.entity.BookCopy;
import com.aurora.entity.enums.CopyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BookCopyRepository extends JpaRepository<BookCopy, Long> {
    List<BookCopy> findByBookId(Long bookId);
    List<BookCopy> findByBookIdAndStatus(Long bookId, CopyStatus status);
    long countByBookId(Long bookId);
    Optional<BookCopy> findFirstByBookIdAndStatus(Long bookId, CopyStatus status);
}
