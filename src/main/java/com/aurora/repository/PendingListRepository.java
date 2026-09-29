package com.aurora.repository;

import com.aurora.entity.PendingList;
import com.aurora.entity.enums.PendingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PendingListRepository extends JpaRepository<PendingList, Long> {
    List<PendingList> findByBookIdAndStatusOrderByDataEntradaAsc(Long bookId, PendingStatus status);
    Optional<PendingList> findByBookIdAndUserIdAndStatus(Long bookId, Long userId, PendingStatus status);
    Optional<PendingList> findFirstByBookIdAndStatusOrderByDataEntradaAsc(Long bookId, PendingStatus status);
    List<PendingList> findByUserIdAndStatus(Long userId, PendingStatus status);
}
