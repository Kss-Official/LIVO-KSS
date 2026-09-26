package com.livo.api.engines.conflict;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Sweep-Line Interval Intersection Algorithm (O(N log N)).
 * Efficiently detects overlapping time blocks without database table locks.
 */
@Component
public class ConflictDetector {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeInterval {
        private UUID id;
        private String title;
        private LocalTime startTime;
        private LocalTime endTime;
        private String type; // SCHEDULE_BLOCK, EVENT, ROUTINE
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConflictPair {
        private TimeInterval blockA;
        private TimeInterval blockB;
        private String message;
    }

    private static class Endpoint {
        LocalTime time;
        boolean isStart;
        TimeInterval interval;

        Endpoint(LocalTime time, boolean isStart, TimeInterval interval) {
            this.time = time;
            this.isStart = isStart;
            this.interval = interval;
        }
    }

    public List<ConflictPair> detectConflicts(List<TimeInterval> intervals) {
        if (intervals == null || intervals.size() < 2) {
            return Collections.emptyList();
        }

        List<Endpoint> endpoints = new ArrayList<>();
        for (TimeInterval interval : intervals) {
            if (interval.getStartTime() != null && interval.getEndTime() != null
                    && interval.getEndTime().isAfter(interval.getStartTime())) {
                endpoints.add(new Endpoint(interval.getStartTime(), true, interval));
                endpoints.add(new Endpoint(interval.getEndTime(), false, interval));
            }
        }

        // Sort sweep-line endpoints: earlier time first; if equal, END before START
        endpoints.sort((a, b) -> {
            int cmp = a.time.compareTo(b.time);
            if (cmp != 0) return cmp;
            if (!a.isStart && b.isStart) return -1;
            if (a.isStart && !b.isStart) return 1;
            return 0;
        });

        List<ConflictPair> conflicts = new ArrayList<>();
        List<TimeInterval> activeIntervals = new ArrayList<>();

        for (Endpoint ep : endpoints) {
            if (ep.isStart) {
                for (TimeInterval active : activeIntervals) {
                    conflicts.add(ConflictPair.builder()
                            .blockA(active)
                            .blockB(ep.interval)
                            .message(String.format("'%s' (%s-%s) conflicts with '%s' (%s-%s)",
                                    active.getTitle(), active.getStartTime(), active.getEndTime(),
                                    ep.interval.getTitle(), ep.interval.getStartTime(), ep.interval.getEndTime()))
                            .build());
                }
                activeIntervals.add(ep.interval);
            } else {
                activeIntervals.remove(ep.interval);
            }
        }

        return conflicts;
    }

    public boolean overlaps(LocalTime startA, LocalTime endA, LocalTime startB, LocalTime endB) {
        if (startA == null || endA == null || startB == null || endB == null) return false;
        return startA.isBefore(endB) && endA.isAfter(startB);
    }
}
