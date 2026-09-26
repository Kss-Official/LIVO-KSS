package com.livo.api.common.event;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Domain event published when a habit check-in is logged.
 */
public record HabitCheckedInEvent(
        UUID habitId,
        UUID userId,
        UUID goalId,
        String habitTitle,
        LocalDate logDate,
        int currentStreak,
        int previousStreak,
        boolean streakIncreased
) {
    public HabitCheckedInEvent(UUID habitId, UUID userId, UUID goalId, String habitTitle, LocalDate logDate, int currentStreak) {
        this(habitId, userId, goalId, habitTitle, logDate, currentStreak, currentStreak - 1, true);
    }
}
