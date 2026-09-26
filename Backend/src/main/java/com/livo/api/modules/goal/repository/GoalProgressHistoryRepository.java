package com.livo.api.modules.goal.repository;

import com.livo.api.modules.goal.entity.GoalProgressHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface GoalProgressHistoryRepository extends JpaRepository<GoalProgressHistoryEntity, UUID> {

    List<GoalProgressHistoryEntity> findAllByGoalIdAndUserIdOrderByRecordedDateDescCreatedAtDesc(UUID goalId, UUID userId);

    List<GoalProgressHistoryEntity> findAllByUserIdAndRecordedDateOrderByCreatedAtDesc(UUID userId, LocalDate recordedDate);
}
