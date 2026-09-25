package com.aurora.repository;

import com.aurora.entity.FilterUsage;
import com.aurora.entity.enums.FilterType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FilterUsageRepository extends JpaRepository<FilterUsage, Long> {
    List<FilterUsage> findByUserId(Long userId);
    Optional<FilterUsage> findByUserIdAndTipoFiltroAndValor(Long userId, FilterType tipoFiltro, String valor);
    List<FilterUsage> findByUserIdAndTipoFiltroOrderByContagemDesc(Long userId, FilterType tipoFiltro);
}
