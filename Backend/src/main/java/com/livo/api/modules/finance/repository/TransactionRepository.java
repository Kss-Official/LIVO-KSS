package com.livo.api.modules.finance.repository;

import com.livo.api.modules.finance.entity.TransactionEntity;
import com.livo.api.modules.finance.entity.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, UUID>, JpaSpecificationExecutor<TransactionEntity> {

    List<TransactionEntity> findAllByUserIdAndDeletedAtIsNull(UUID userId);

    List<TransactionEntity> findAllByUserIdAndDeletedAtIsNullOrderByTransactionDateDesc(UUID userId);

    Optional<TransactionEntity> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);

    List<TransactionEntity> findAllByUserIdAndTransactionDateBetweenAndDeletedAtIsNullOrderByTransactionDateDesc(
            UUID userId, LocalDate startDate, LocalDate endDate
    );

    List<TransactionEntity> findAllByUserIdAndCategoryAndDeletedAtIsNull(UUID userId, String category);

    List<TransactionEntity> findAllByUserIdAndTypeAndDeletedAtIsNull(UUID userId, TransactionType type);

    List<TransactionEntity> findAllByUserIdAndGoalIdAndDeletedAtIsNull(UUID userId, UUID goalId);

    List<TransactionEntity> findAllByUserIdAndTripIdAndDeletedAtIsNull(UUID userId, UUID tripId);

    List<TransactionEntity> findAllByUserIdAndTripIdInAndDeletedAtIsNull(UUID userId, Collection<UUID> tripIds);

    List<TransactionEntity> findAllByUserIdAndCategoryAndTransactionDateBetweenAndDeletedAtIsNull(
            UUID userId, String category, LocalDate startDate, LocalDate endDate
    );

    List<TransactionEntity> findAllByUserIdAndTypeAndTransactionDateBetweenAndDeletedAtIsNull(
            UUID userId, TransactionType type, LocalDate startDate, LocalDate endDate
    );

    long countByUserIdAndDeletedAtIsNull(UUID userId);

    List<TransactionEntity> findAllByUserIdAndUpdatedAtAfter(UUID userId, java.time.Instant since);

    Optional<TransactionEntity> findByIdAndUserId(UUID id, UUID userId);

    @org.springframework.data.jpa.repository.Query("SELECT t FROM TransactionEntity t WHERE t.userId = :userId AND t.deletedAt IS NULL " +
            "AND (:startDate IS NULL OR :endDate IS NULL OR (t.transactionDate >= :startDate AND t.transactionDate <= :endDate)) " +
            "AND (:type IS NULL OR t.type = :type) " +
            "AND (:category IS NULL OR LOWER(t.category) = :category) " +
            "AND (:goalId IS NULL OR t.goalId = :goalId) " +
            "AND (:tripId IS NULL OR t.tripId = :tripId) " +
            "ORDER BY t.transactionDate DESC, t.createdAt DESC")
    List<TransactionEntity> findFilteredTransactions(
            @org.springframework.data.repository.query.Param("userId") UUID userId,
            @org.springframework.data.repository.query.Param("startDate") LocalDate startDate,
            @org.springframework.data.repository.query.Param("endDate") LocalDate endDate,
            @org.springframework.data.repository.query.Param("type") TransactionType type,
            @org.springframework.data.repository.query.Param("category") String category,
            @org.springframework.data.repository.query.Param("goalId") UUID goalId,
            @org.springframework.data.repository.query.Param("tripId") UUID tripId
    );
 
    @org.springframework.data.jpa.repository.Query("SELECT t FROM TransactionEntity t WHERE t.userId = :userId AND t.deletedAt IS NULL AND (" +
           "LOWER(t.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "(t.description IS NOT NULL AND LOWER(t.description) LIKE LOWER(CONCAT('%', :query, '%'))) OR " +
           "(t.category IS NOT NULL AND LOWER(t.category) LIKE LOWER(CONCAT('%', :query, '%'))))")
    List<TransactionEntity> searchByKeyword(@org.springframework.data.repository.query.Param("userId") UUID userId, @org.springframework.data.repository.query.Param("query") String query);
}

