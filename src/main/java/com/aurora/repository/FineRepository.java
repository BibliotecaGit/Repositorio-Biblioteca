package com.aurora.repository;

import com.aurora.entity.Fine;
import com.aurora.entity.enums.FineStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FineRepository extends JpaRepository<Fine, Long> {
    List<Fine> findByUserId(Long userId);
    List<Fine> findByUserIdAndStatus(Long userId, FineStatus status);
    boolean existsByUserIdAndStatus(Long userId, FineStatus status);
}
