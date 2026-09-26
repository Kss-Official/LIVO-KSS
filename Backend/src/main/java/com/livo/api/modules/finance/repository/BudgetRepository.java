package com.livo.api.modules.finance.repository;

import com.livo.api.modules.finance.entity.BudgetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BudgetRepository extends JpaRepository<BudgetEntity, UUID> {

    List<BudgetEntity> findAllByUserIdAndDeletedAtIsNull(UUID userId);

    Optional<BudgetEntity> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);

    List<BudgetEntity> findAllByUserIdAndMonthStartAndDeletedAtIsNull(UUID userId, LocalDate monthStart);

    Optional<BudgetEntity> findByUserIdAndCategoryAndMonthStartAndDeletedAtIsNull(UUID userId, String category, LocalDate monthStart);

    boolean existsByUserIdAndCategoryAndMonthStartAndDeletedAtIsNull(UUID userId, String category, LocalDate monthStart);

    long countByUserIdAndDeletedAtIsNull(UUID userId);
}
