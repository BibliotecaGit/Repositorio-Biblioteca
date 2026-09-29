package com.aurora.repository;

import com.aurora.entity.Waitlist;
import com.aurora.entity.enums.WaitlistStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface WaitlistRepository extends JpaRepository<Waitlist, Long> {
    List<Waitlist> findByBookIdAndStatusOrderByPosicaoAsc(Long bookId, WaitlistStatus status);
    long countByBookIdAndStatusIn(Long bookId, List<WaitlistStatus> statuses);
    Optional<Waitlist> findByBookIdAndUserIdAndStatusIn(Long bookId, Long userId, List<WaitlistStatus> statuses);
    Optional<Waitlist> findFirstByBookIdAndStatusOrderByPosicaoAsc(Long bookId, WaitlistStatus status);
    List<Waitlist> findByUserIdAndStatusIn(Long userId, List<WaitlistStatus> statuses);
}
