package com.livo.api.modules.search.service;

import com.livo.api.modules.event.repository.EventRepository;
import com.livo.api.modules.finance.repository.TransactionRepository;
import com.livo.api.modules.goal.repository.GoalRepository;
import com.livo.api.modules.habit.repository.HabitRepository;
import com.livo.api.modules.learning.repository.LearningItemRepository;
import com.livo.api.modules.search.dto.SearchDomain;
import com.livo.api.modules.search.dto.SearchResponse;
import com.livo.api.modules.search.dto.SearchResultItem;
import com.livo.api.modules.task.repository.TaskRepository;
import com.livo.api.modules.trip.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final TaskRepository taskRepository;
    private final GoalRepository goalRepository;
    private final EventRepository eventRepository;
    private final HabitRepository habitRepository;
    private final LearningItemRepository learningItemRepository;
    private final TransactionRepository transactionRepository;
    private final TripRepository tripRepository;
    @org.springframework.beans.factory.annotation.Qualifier("boundedTaskExecutor")
    private final java.util.concurrent.Executor boundedTaskExecutor;

    @Override
    @Transactional(readOnly = true)
    public SearchResponse search(UUID userId, String query, SearchDomain domain, Integer limit) {
        if (query == null || query.trim().isEmpty()) {
            return SearchResponse.builder()
                    .query(query != null ? query : "")
                    .domainFilter(domain != null ? domain : SearchDomain.ALL)
                    .totalResults(0)
                    .items(Collections.emptyList())
                    .build();
        }

        String cleanQuery = query.trim();
        SearchDomain domainFilter = domain != null ? domain : SearchDomain.ALL;
        int maxLimit = (limit != null && limit > 0) ? Math.min(limit, 100) : 20;

        List<SearchResultItem> results = new ArrayList<>();

        if (domainFilter == SearchDomain.ALL) {
            java.util.concurrent.CompletableFuture<List<SearchResultItem>> tasksFuture =
                    java.util.concurrent.CompletableFuture.supplyAsync(() -> searchTasks(userId, cleanQuery), boundedTaskExecutor);
            java.util.concurrent.CompletableFuture<List<SearchResultItem>> goalsFuture =
                    java.util.concurrent.CompletableFuture.supplyAsync(() -> searchGoals(userId, cleanQuery), boundedTaskExecutor);
            java.util.concurrent.CompletableFuture<List<SearchResultItem>> eventsFuture =
                    java.util.concurrent.CompletableFuture.supplyAsync(() -> searchEvents(userId, cleanQuery), boundedTaskExecutor);
            java.util.concurrent.CompletableFuture<List<SearchResultItem>> habitsFuture =
                    java.util.concurrent.CompletableFuture.supplyAsync(() -> searchHabits(userId, cleanQuery), boundedTaskExecutor);
            java.util.concurrent.CompletableFuture<List<SearchResultItem>> learningFuture =
                    java.util.concurrent.CompletableFuture.supplyAsync(() -> searchLearning(userId, cleanQuery), boundedTaskExecutor);
            java.util.concurrent.CompletableFuture<List<SearchResultItem>> expensesFuture =
                    java.util.concurrent.CompletableFuture.supplyAsync(() -> searchExpenses(userId, cleanQuery), boundedTaskExecutor);
            java.util.concurrent.CompletableFuture<List<SearchResultItem>> tripsFuture =
                    java.util.concurrent.CompletableFuture.supplyAsync(() -> searchTrips(userId, cleanQuery), boundedTaskExecutor);

            java.util.concurrent.CompletableFuture.allOf(tasksFuture, goalsFuture, eventsFuture, habitsFuture, learningFuture, expensesFuture, tripsFuture).join();

            results.addAll(tasksFuture.join());
            results.addAll(goalsFuture.join());
            results.addAll(eventsFuture.join());
            results.addAll(habitsFuture.join());
            results.addAll(learningFuture.join());
            results.addAll(expensesFuture.join());
            results.addAll(tripsFuture.join());
        } else {
            switch (domainFilter) {
                case TASK -> results.addAll(searchTasks(userId, cleanQuery));
                case GOAL -> results.addAll(searchGoals(userId, cleanQuery));
                case EVENT -> results.addAll(searchEvents(userId, cleanQuery));
                case HABIT -> results.addAll(searchHabits(userId, cleanQuery));
                case LEARNING -> results.addAll(searchLearning(userId, cleanQuery));
                case EXPENSE -> results.addAll(searchExpenses(userId, cleanQuery));
                case TRIP -> results.addAll(searchTrips(userId, cleanQuery));
                default -> {}
            }
        }

        // Sort results by relevanceScore descending, then by title
        results.sort((a, b) -> {
            int cmp = Double.compare(b.getRelevanceScore(), a.getRelevanceScore());
            if (cmp != 0) return cmp;
            return a.getTitle().compareToIgnoreCase(b.getTitle());
        });

        List<SearchResultItem> pagedItems = results.stream().limit(maxLimit).toList();

        return SearchResponse.builder()
                .query(cleanQuery)
                .domainFilter(domainFilter)
                .totalResults(results.size())
                .items(pagedItems)
                .build();
    }

    /**
     * Computes relevance score combining exact, prefix, substring, and trigram similarity heuristics.
     */
    private double computeRelevanceScore(String query, String title, String... secondaryTexts) {
        if (query == null || query.isBlank()) return 0.0;
        String q = query.toLowerCase().trim();
        double score = 0.0;

        if (title != null && !title.isBlank()) {
            String t = title.toLowerCase().trim();
            if (t.equals(q)) {
                score = 1.0;
            } else if (t.startsWith(q)) {
                score = 0.95;
            } else if (t.contains(q)) {
                score = 0.85;
            } else {
                double tri = calculateTrigramSimilarity(q, t);
                score = Math.max(score, tri * 0.8);
            }
        }

        for (String sec : secondaryTexts) {
            if (sec != null && !sec.isBlank()) {
                String s = sec.toLowerCase().trim();
                if (s.equals(q)) {
                    score = Math.max(score, 0.75);
                } else if (s.contains(q)) {
                    score = Math.max(score, 0.65);
                } else {
                    double tri = calculateTrigramSimilarity(q, s);
                    score = Math.max(score, tri * 0.5);
                }
            }
        }

        return Math.round(Math.min(score, 1.0) * 100.0) / 100.0;
    }

    /**
     * Calculates trigram Dice similarity coefficient between two strings.
     */
    private double calculateTrigramSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) return 0.0;
        String a = s1.toLowerCase().trim();
        String b = s2.toLowerCase().trim();
        if (a.isEmpty() || b.isEmpty()) return 0.0;
        if (a.equals(b)) return 1.0;

        Set<String> trigramsA = extractTrigrams(a);
        Set<String> trigramsB = extractTrigrams(b);
        if (trigramsA.isEmpty() || trigramsB.isEmpty()) {
            return a.contains(b) || b.contains(a) ? 0.5 : 0.0;
        }

        int matches = 0;
        for (String t : trigramsA) {
            if (trigramsB.contains(t)) {
                matches++;
            }
        }
        return (2.0 * matches) / (trigramsA.size() + trigramsB.size());
    }

    private Set<String> extractTrigrams(String s) {
        Set<String> trigrams = new HashSet<>();
        String padded = "  " + s + " ";
        for (int i = 0; i <= padded.length() - 3; i++) {
            trigrams.add(padded.substring(i, i + 3));
        }
        return trigrams;
    }

    /**
     * Extracts a context window snippet surrounding the matching query.
     */
    private String extractSnippet(String query, String primaryText, String... fallbackTexts) {
        String q = query.toLowerCase().trim();

        if (primaryText != null && !primaryText.isBlank()) {
            String lower = primaryText.toLowerCase();
            int idx = lower.indexOf(q);
            if (idx >= 0) {
                int start = Math.max(0, idx - 25);
                int end = Math.min(primaryText.length(), idx + q.length() + 35);
                String snippet = primaryText.substring(start, end).trim();
                if (start > 0) snippet = "..." + snippet;
                if (end < primaryText.length()) snippet = snippet + "...";
                return snippet;
            }
        }

        for (String fallback : fallbackTexts) {
            if (fallback != null && !fallback.isBlank()) {
                String lower = fallback.toLowerCase();
                int idx = lower.indexOf(q);
                if (idx >= 0) {
                    int start = Math.max(0, idx - 25);
                    int end = Math.min(fallback.length(), idx + q.length() + 35);
                    String snippet = fallback.substring(start, end).trim();
                    if (start > 0) snippet = "..." + snippet;
                    if (end < fallback.length()) snippet = snippet + "...";
                    return snippet;
                }
            }
        }

        if (primaryText != null && !primaryText.isBlank()) {
            return primaryText.length() > 60 ? primaryText.substring(0, 60) + "..." : primaryText;
        }
        for (String fallback : fallbackTexts) {
            if (fallback != null && !fallback.isBlank()) {
                return fallback.length() > 60 ? fallback.substring(0, 60) + "..." : fallback;
            }
        }
        return "";
    }

    private List<SearchResultItem> searchTasks(UUID userId, String cleanQuery) {
        List<SearchResultItem> list = new ArrayList<>();
        taskRepository.searchByKeyword(userId, cleanQuery).forEach(task -> {
            double score = computeRelevanceScore(cleanQuery, task.getTitle(), task.getDescription());
            String snippet = extractSnippet(cleanQuery, task.getDescription());
            list.add(SearchResultItem.builder()
                    .id(task.getId())
                    .domainType(SearchDomain.TASK)
                    .title(task.getTitle())
                    .snippet(snippet)
                    .categoryOrStatus(task.getStatus() != null ? task.getStatus().name() : task.getCategory())
                    .dateOrTime(task.getDueDate() != null ? task.getDueDate().toString() : null)
                    .relevanceScore(score)
                    .build());
        });
        return list;
    }

    private List<SearchResultItem> searchGoals(UUID userId, String cleanQuery) {
        List<SearchResultItem> list = new ArrayList<>();
        goalRepository.searchByKeyword(userId, cleanQuery).forEach(goal -> {
            double score = computeRelevanceScore(cleanQuery, goal.getTitle(), goal.getDescription(), goal.getTargetDescription());
            String snippet = extractSnippet(cleanQuery, goal.getDescription(), goal.getTargetDescription());
            list.add(SearchResultItem.builder()
                    .id(goal.getId())
                    .domainType(SearchDomain.GOAL)
                    .title(goal.getTitle())
                    .snippet(snippet)
                    .categoryOrStatus(goal.getStatus() != null ? goal.getStatus().name() : goal.getCategory())
                    .dateOrTime(goal.getTargetDate() != null ? goal.getTargetDate().toString() : null)
                    .relevanceScore(score)
                    .build());
        });
        return list;
    }

    private List<SearchResultItem> searchEvents(UUID userId, String cleanQuery) {
        List<SearchResultItem> list = new ArrayList<>();
        eventRepository.searchByKeyword(userId, cleanQuery).forEach(event -> {
            double score = computeRelevanceScore(cleanQuery, event.getTitle(), event.getDescription(), event.getLocation());
            String snippet = extractSnippet(cleanQuery, event.getDescription(), event.getLocation());
            list.add(SearchResultItem.builder()
                    .id(event.getId())
                    .domainType(SearchDomain.EVENT)
                    .title(event.getTitle())
                    .snippet(snippet)
                    .categoryOrStatus(event.getCategory() != null ? event.getCategory() : (event.getPriority() != null ? event.getPriority().name() : "EVENT"))
                    .dateOrTime(event.getStartTime() != null ? event.getStartTime().toString() : null)
                    .relevanceScore(score)
                    .build());
        });
        return list;
    }

    private List<SearchResultItem> searchHabits(UUID userId, String cleanQuery) {
        List<SearchResultItem> list = new ArrayList<>();
        habitRepository.searchByKeyword(userId, cleanQuery).forEach(habit -> {
            double score = computeRelevanceScore(cleanQuery, habit.getTitle(), habit.getDescription(), habit.getMotivationNote());
            String snippet = extractSnippet(cleanQuery, habit.getMotivationNote(), habit.getDescription());
            list.add(SearchResultItem.builder()
                    .id(habit.getId())
                    .domainType(SearchDomain.HABIT)
                    .title(habit.getTitle())
                    .snippet(snippet)
                    .categoryOrStatus(habit.getFrequencyType() != null ? habit.getFrequencyType().name() : "DAILY")
                    .dateOrTime(null)
                    .relevanceScore(score)
                    .build());
        });
        return list;
    }

    private List<SearchResultItem> searchLearning(UUID userId, String cleanQuery) {
        List<SearchResultItem> list = new ArrayList<>();
        learningItemRepository.searchByKeyword(userId, cleanQuery).forEach(learning -> {
            double score = computeRelevanceScore(cleanQuery, learning.getTitle(), learning.getDescription(), learning.getNotes(), learning.getCategory());
            String snippet = extractSnippet(cleanQuery, learning.getNotes(), learning.getDescription());
            list.add(SearchResultItem.builder()
                    .id(learning.getId())
                    .domainType(SearchDomain.LEARNING)
                    .title(learning.getTitle())
                    .snippet(snippet)
                    .categoryOrStatus(learning.getStatus() != null ? learning.getStatus().name() : learning.getCategory())
                    .dateOrTime(null)
                    .relevanceScore(score)
                    .build());
        });
        return list;
    }

    private List<SearchResultItem> searchExpenses(UUID userId, String cleanQuery) {
        List<SearchResultItem> list = new ArrayList<>();
        transactionRepository.searchByKeyword(userId, cleanQuery).forEach(tx -> {
            double score = computeRelevanceScore(cleanQuery, tx.getTitle(), tx.getDescription(), tx.getCategory());
            String fallbackDesc = tx.getType() + ": " + tx.getAmount() + " " + tx.getCurrency();
            String snippet = extractSnippet(cleanQuery, tx.getDescription(), fallbackDesc);
            list.add(SearchResultItem.builder()
                    .id(tx.getId())
                    .domainType(SearchDomain.EXPENSE)
                    .title(tx.getTitle())
                    .snippet(snippet)
                    .categoryOrStatus(tx.getCategory() != null ? tx.getCategory() : tx.getType().name())
                    .dateOrTime(tx.getTransactionDate() != null ? tx.getTransactionDate().toString() : null)
                    .relevanceScore(score)
                    .build());
        });
        return list;
    }

    private List<SearchResultItem> searchTrips(UUID userId, String cleanQuery) {
        List<SearchResultItem> list = new ArrayList<>();
        tripRepository.searchByKeyword(userId, cleanQuery).forEach(trip -> {
            double score = computeRelevanceScore(cleanQuery, trip.getTitle(), trip.getDestination());
            String snippet = extractSnippet(cleanQuery, trip.getDestination());
            list.add(SearchResultItem.builder()
                    .id(trip.getId())
                    .domainType(SearchDomain.TRIP)
                    .title(trip.getTitle())
                    .snippet(snippet)
                    .categoryOrStatus(trip.getTripType() != null ? trip.getTripType().name() : "TRIP")
                    .dateOrTime(trip.getStartDate() != null ? trip.getStartDate().toString() : null)
                    .relevanceScore(score)
                    .build());
        });
        return list;
    }
}
