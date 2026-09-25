package com.aurora.repository;

import com.aurora.entity.Rental;
import com.aurora.entity.enums.RentalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RentalRepository extends JpaRepository<Rental, Long> {
    List<Rental> findByUserIdAndStatus(Long userId, RentalStatus status);
    long countByUserIdAndStatus(Long userId, RentalStatus status);
    Optional<Rental> findFirstByBookCopyIdAndStatus(Long bookCopyId, RentalStatus status);
    List<Rental> findByStatusAndDataDevolucaoPrevistaBefore(RentalStatus status, LocalDate date);
}
