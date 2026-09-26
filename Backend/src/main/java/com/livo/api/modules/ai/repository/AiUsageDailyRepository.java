package com.livo.api.modules.ai.repository;

import com.livo.api.modules.ai.entity.AiUsageDailyEntity;
import com.livo.api.modules.ai.entity.AiUsageDailyId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface AiUsageDailyRepository extends JpaRepository<AiUsageDailyEntity, AiUsageDailyId> {

    Optional<AiUsageDailyEntity> findByIdUserIdAndIdUsageDate(UUID userId, LocalDate usageDate);

    List<AiUsageDailyEntity> findAllByIdUserId(UUID userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "INSERT INTO ai_usage_daily (user_id, usage_date, chat_count, voice_count) " +
            "VALUES (:userId, :usageDate, 1, 0) " +
            "ON CONFLICT (user_id, usage_date) " +
            "DO UPDATE SET chat_count = ai_usage_daily.chat_count + 1 " +
            "WHERE ai_usage_daily.chat_count < :dailyQuota", nativeQuery = true)
    int incrementChatCountIfWithinQuota(
            @Param("userId") UUID userId,
            @Param("usageDate") LocalDate usageDate,
            @Param("dailyQuota") int dailyQuota
    );
}
