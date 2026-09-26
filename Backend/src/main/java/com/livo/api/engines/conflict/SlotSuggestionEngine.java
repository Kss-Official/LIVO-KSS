package com.livo.api.engines.conflict;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Finds contiguous free gaps between scheduled items and suggests optimal placement slots.
 */
@Component
public class SlotSuggestionEngine {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FreeSlot {
        private LocalTime startTime;
        private LocalTime endTime;
        private long durationMinutes;
        private String label;
    }

    public List<FreeSlot> findAvailableSlots(
            LocalTime wakeTime,
            LocalTime sleepTime,
            List<ConflictDetector.TimeInterval> occupiedIntervals,
            int minimumDurationMinutes
    ) {
        LocalTime effectiveWake = (wakeTime != null) ? wakeTime : LocalTime.of(7, 0);
        LocalTime effectiveSleep = (sleepTime != null) ? sleepTime : LocalTime.of(23, 0);

        List<ConflictDetector.TimeInterval> sorted = new ArrayList<>(
                (occupiedIntervals != null) ? occupiedIntervals : List.of()
        );
        sorted.removeIf(i -> i.getStartTime() == null || i.getEndTime() == null || !i.getEndTime().isAfter(i.getStartTime()));
        sorted.sort(Comparator.comparing(ConflictDetector.TimeInterval::getStartTime));

        // Merge overlapping occupied intervals
        List<ConflictDetector.TimeInterval> merged = new ArrayList<>();
        for (ConflictDetector.TimeInterval item : sorted) {
            if (merged.isEmpty()) {
                merged.add(item);
            } else {
                ConflictDetector.TimeInterval last = merged.get(merged.size() - 1);
                if (!item.getStartTime().isAfter(last.getEndTime())) {
                    if (item.getEndTime().isAfter(last.getEndTime())) {
                        last.setEndTime(item.getEndTime());
                    }
                } else {
                    merged.add(item);
                }
            }
        }

        List<FreeSlot> freeSlots = new ArrayList<>();
        LocalTime pointer = effectiveWake;

        for (ConflictDetector.TimeInterval occupied : merged) {
            if (occupied.getStartTime().isAfter(pointer)) {
                LocalTime gapEnd = occupied.getStartTime().isBefore(effectiveSleep)
                        ? occupied.getStartTime()
                        : effectiveSleep;
                long gapMinutes = Duration.between(pointer, gapEnd).toMinutes();
                if (gapMinutes >= minimumDurationMinutes) {
                    freeSlots.add(FreeSlot.builder()
                            .startTime(pointer)
                            .endTime(gapEnd)
                            .durationMinutes(gapMinutes)
                            .label(gapMinutes + "m free slot")
                            .build());
                }
            }
            if (occupied.getEndTime().isAfter(pointer)) {
                pointer = occupied.getEndTime();
            }
            if (!pointer.isBefore(effectiveSleep)) {
                break;
            }
        }

        if (pointer.isBefore(effectiveSleep)) {
            long gapMinutes = Duration.between(pointer, effectiveSleep).toMinutes();
            if (gapMinutes >= minimumDurationMinutes) {
                freeSlots.add(FreeSlot.builder()
                        .startTime(pointer)
                        .endTime(effectiveSleep)
                        .durationMinutes(gapMinutes)
                        .label(gapMinutes + "m free slot before bedtime")
                        .build());
            }
        }

        return freeSlots;
    }
}
