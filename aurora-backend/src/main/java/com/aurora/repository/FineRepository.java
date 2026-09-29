package com.aurora.repository;

import com.aurora.entity.Fine;
import com.aurora.entity.enums.FineStatus;
import com.aurora.entity.enums.FineType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FineRepository extends JpaRepository<Fine, Long> {
    List<Fine> findByUserId(Long userId);
    List<Fine> findByUserIdAndStatus(Long userId, FineStatus status);
    boolean existsByUserIdAndStatus(Long userId, FineStatus status);
    Optional<Fine> findByRentalIdAndTipo(Long rentalId, FineType tipo);
}
