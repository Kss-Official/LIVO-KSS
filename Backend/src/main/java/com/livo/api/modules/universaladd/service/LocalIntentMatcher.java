package com.livo.api.modules.universaladd.service;

import com.livo.api.modules.universaladd.dto.ParsedEntityDraft;
import com.livo.api.modules.universaladd.dto.UniversalAddDomain;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class LocalIntentMatcher {

    private static final Pattern HASHTAG_PATTERN = Pattern.compile("#([a-zA-Z0-9_-]+)");
    private static final Pattern AMOUNT_CURRENCY_PATTERN_1 = Pattern.compile("(?i)(?:rs\\.?|inr|usd|\\$|€|£|eur)\\s*(\\d+(?:\\.\\d{1,2})?)");
    private static final Pattern AMOUNT_CURRENCY_PATTERN_2 = Pattern.compile("(?i)(\\d+(?:\\.\\d{1,2})?)\\s*(?:rs\\.?|inr|usd|\\$|€|£|eur)");
    private static final Pattern TIME_PATTERN = Pattern.compile("(?i)\\b(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)\\b");
    private static final Pattern TIME_24H_PATTERN = Pattern.compile("\\b([01]?\\d|2[0-3]):([0-5]\\d)\\b");
    private static final Pattern DATE_ISO_RANGE_PATTERN = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})\\s+(?:to|-|until)\\s+(\\d{4}-\\d{2}-\\d{2})");
    private static final Pattern DATE_ISO_PATTERN = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})");

    public ParsedEntityDraft parse(String rawInput, UniversalAddDomain preferredDomain) {
        if (rawInput == null || rawInput.isBlank()) {
            return ParsedEntityDraft.builder()
                    .domain(preferredDomain != null ? preferredDomain : UniversalAddDomain.TASK)
                    .title("")
                    .rawInput("")
                    .confidenceScore(0.0)
                    .build();
        }

        String text = rawInput.trim();

        // 1. Extract hashtags
        List<String> tags = new ArrayList<>();
        Matcher tagMatcher = HASHTAG_PATTERN.matcher(text);
        while (tagMatcher.find()) {
            tags.add(tagMatcher.group(1).toLowerCase());
        }

        // 2. Extract Amount and Currency
        BigDecimal amount = null;
        String currency = "INR";
        Matcher amountMatcher = AMOUNT_CURRENCY_PATTERN_1.matcher(text);
        if (amountMatcher.find()) {
            amount = new BigDecimal(amountMatcher.group(1));
            String match = amountMatcher.group(0).toLowerCase();
            if (match.contains("$") || match.contains("usd")) currency = "USD";
            else if (match.contains("€") || match.contains("eur")) currency = "EUR";
            else if (match.contains("£")) currency = "GBP";
            else currency = "INR";
        } else {
            amountMatcher = AMOUNT_CURRENCY_PATTERN_2.matcher(text);
            if (amountMatcher.find()) {
                amount = new BigDecimal(amountMatcher.group(1));
                String match = amountMatcher.group(0).toLowerCase();
                if (match.contains("$") || match.contains("usd")) currency = "USD";
                else if (match.contains("€") || match.contains("eur")) currency = "EUR";
                else if (match.contains("£")) currency = "GBP";
                else currency = "INR";
            }
        }

        // 3. Extract Dates
        LocalDate date = null;
        LocalDate endDate = null;
        String lower = text.toLowerCase();

        Matcher rangeMatcher = DATE_ISO_RANGE_PATTERN.matcher(text);
        if (rangeMatcher.find()) {
            try {
                date = LocalDate.parse(rangeMatcher.group(1));
                endDate = LocalDate.parse(rangeMatcher.group(2));
            } catch (Exception ignored) {}
        } else {
            Matcher isoMatcher = DATE_ISO_PATTERN.matcher(text);
            if (isoMatcher.find()) {
                try {
                    date = LocalDate.parse(isoMatcher.group(1));
                } catch (Exception ignored) {}
            }
        }

        if (date == null) {
            if (lower.contains("tomorrow")) {
                date = LocalDate.now().plusDays(1);
            } else if (lower.contains("today") || lower.contains("tonight")) {
                date = LocalDate.now();
            } else if (lower.contains("next monday")) {
                date = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
            } else if (lower.contains("next tuesday")) {
                date = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.TUESDAY));
            } else if (lower.contains("next wednesday")) {
                date = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.WEDNESDAY));
            } else if (lower.contains("next thursday")) {
                date = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.THURSDAY));
            } else if (lower.contains("next friday") || lower.contains("on friday")) {
                date = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.FRIDAY));
            } else if (lower.contains("next saturday")) {
                date = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SATURDAY));
            } else if (lower.contains("next sunday")) {
                date = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
            }
        }

        // 4. Extract Times
        LocalTime time = null;
        Matcher timeMatcher = TIME_PATTERN.matcher(text);
        if (timeMatcher.find()) {
            int hour = Integer.parseInt(timeMatcher.group(1));
            int minute = timeMatcher.group(2) != null ? Integer.parseInt(timeMatcher.group(2)) : 0;
            String ampm = timeMatcher.group(3).toLowerCase();
            if (ampm.equals("pm") && hour < 12) hour += 12;
            else if (ampm.equals("am") && hour == 12) hour = 0;
            time = LocalTime.of(hour, minute);
        } else {
            Matcher time24Matcher = TIME_24H_PATTERN.matcher(text);
            if (time24Matcher.find()) {
                try {
                    time = LocalTime.parse(time24Matcher.group(0));
                } catch (Exception ignored) {}
            }
        }

        if (time == null && lower.contains("tonight")) {
            time = LocalTime.of(20, 0);
        } else if (time == null && lower.contains("morning")) {
            time = LocalTime.of(8, 0);
        } else if (time == null && lower.contains("evening")) {
            time = LocalTime.of(18, 0);
        }

        // 5. Extract Frequency
        String frequency = null;
        if (lower.contains("daily") || lower.contains("every day") || lower.contains("every morning") || lower.contains("every evening") || lower.contains("every night")) {
            frequency = "DAILY";
        } else if (lower.contains("weekly") || lower.contains("every week")) {
            frequency = "WEEKLY";
        } else if (lower.contains("weekday") || lower.contains("weekdays")) {
            frequency = "CUSTOM";
        }

        // 6. Extract Location or Destination
        String locationOrDestination = null;
        Pattern atLocationPattern = Pattern.compile("(?i)\\bat\\s+([a-zA-Z][a-zA-Z0-9_\\s]*?)(?:\\s+(?:on|tomorrow|today|at|#|with)|$)");
        Matcher locMatcher = atLocationPattern.matcher(text);
        while (locMatcher.find()) {
            String candidate = locMatcher.group(1).trim();
            if (!candidate.equalsIgnoreCase("subway") && !candidate.matches("\\d+.*") && !candidate.isBlank()) {
                locationOrDestination = candidate;
                break;
            }
        }

        Pattern tripPattern = Pattern.compile("(?i)\\b(?:trip to|vacation to|flight to|travel to|visit to|to)\\s+([a-zA-Z][a-zA-Z0-9_\\s]*?)(?:\\s+(?:from|on|tomorrow|today|at|#|with|for)|$)");
        Matcher tripMatcher = tripPattern.matcher(text);
        while (tripMatcher.find()) {
            String candidate = tripMatcher.group(1).trim();
            if (!candidate.isBlank()) {
                locationOrDestination = candidate;
                break;
            }
        }

        if (locationOrDestination != null && !locationOrDestination.isBlank()) {
            locationOrDestination = Character.toUpperCase(locationOrDestination.charAt(0)) + locationOrDestination.substring(1);
        }

        // 7. Determine Domain
        UniversalAddDomain domain = preferredDomain;
        double confidence = 0.85;

        if (domain == null) {
            // Intent scoring to prioritize specific domain intents over generic amount detection
            int tripScore = 0;
            if (lower.contains("trip to") || lower.contains("vacation to") || lower.contains("flight to") || lower.contains("holiday to") || lower.contains("travel to")) {
                tripScore += 10;
            }

            int habitScore = 0;
            if (frequency != null) {
                habitScore += 8;
            }
            if (lower.contains("habit") || lower.contains("streak") || lower.contains("meditate") || lower.contains("drink water") || lower.contains("workout daily")) {
                habitScore += 10;
            }

            int eventScore = 0;
            if (lower.contains("meeting") || lower.contains("meet with") || lower.contains("sync with") || lower.contains("webinar") || lower.contains("conference") || lower.contains("interview") || lower.contains("call with")) {
                eventScore += 10;
            }

            int goalScore = 0;
            if (lower.contains("goal:") || lower.contains("my goal is") || lower.contains("achieve") || lower.contains("master ") || lower.contains("target:")) {
                goalScore += 10;
            }

            int learningScore = 0;
            if (lower.contains("learn ") || lower.contains("study ") || lower.contains("course on") || lower.contains("read book") || lower.contains("certification")) {
                learningScore += 10;
            }

            int expenseScore = 0;
            if (lower.contains("spent") || lower.contains("spend") || lower.contains("paid") || lower.contains("bought") || lower.contains("expense")) {
                expenseScore += 10;
            }
            if (amount != null) {
                expenseScore += 5;
            }
            if (lower.contains("cost") || lower.contains("price")) {
                expenseScore += 3;
            }

            int maxScore = Math.max(expenseScore,
                    Math.max(tripScore,
                    Math.max(habitScore,
                    Math.max(eventScore,
                    Math.max(goalScore, learningScore)))));

            if (maxScore == 0) {
                domain = UniversalAddDomain.TASK;
                confidence = 0.80;
            } else if (maxScore == tripScore) {
                domain = UniversalAddDomain.TRIP;
                confidence = 0.92;
            } else if (maxScore == habitScore) {
                domain = UniversalAddDomain.HABIT;
                confidence = 0.90;
            } else if (maxScore == eventScore) {
                domain = UniversalAddDomain.EVENT;
                confidence = 0.90;
            } else if (maxScore == goalScore) {
                domain = UniversalAddDomain.GOAL;
                confidence = 0.85;
            } else if (maxScore == learningScore) {
                domain = UniversalAddDomain.LEARNING;
                confidence = 0.88;
            } else {
                domain = UniversalAddDomain.EXPENSE;
                confidence = 0.95;
            }
        }

        // 8. Infer Category
        String category = inferCategory(lower, domain, tags);

        // 9. Clean Title
        String cleanTitle = cleanTitle(text, domain, locationOrDestination);

        return ParsedEntityDraft.builder()
                .domain(domain)
                .title(cleanTitle)
                .description(rawInput)
                .category(category)
                .tags(tags)
                .date(date)
                .time(time)
                .endDate(endDate)
                .amount(amount)
                .currency(currency)
                .frequency(frequency != null ? frequency : (domain == UniversalAddDomain.HABIT ? "DAILY" : null))
                .locationOrDestination(locationOrDestination)
                .priority("MEDIUM")
                .confidenceScore(confidence)
                .rawInput(rawInput)
                .build();
    }

    private String inferCategory(String lower, UniversalAddDomain domain, List<String> tags) {
        if (!tags.isEmpty()) {
            return tags.get(0).toUpperCase();
        }

        if (domain == UniversalAddDomain.EXPENSE) {
            if (lower.contains("lunch") || lower.contains("dinner") || lower.contains("breakfast") || lower.contains("coffee") || lower.contains("food") || lower.contains("subway") || lower.contains("restaurant") || lower.contains("grocer")) {
                return "FOOD";
            }
            if (lower.contains("uber") || lower.contains("taxi") || lower.contains("cab") || lower.contains("flight") || lower.contains("fuel") || lower.contains("petrol")) {
                return "TRANSPORT";
            }
            if (lower.contains("movie") || lower.contains("netflix") || lower.contains("game")) {
                return "ENTERTAINMENT";
            }
            return "SHOPPING";
        }

        if (domain == UniversalAddDomain.HABIT) {
            if (lower.contains("meditate") || lower.contains("water") || lower.contains("workout") || lower.contains("gym") || lower.contains("run") || lower.contains("sleep")) {
                return "HEALTH";
            }
            return "PERSONAL";
        }

        if (domain == UniversalAddDomain.LEARNING) {
            if (lower.contains("code") || lower.contains("java") || lower.contains("python") || lower.contains("flutter") || lower.contains("tech")) {
                return "TECH";
            }
            return "GENERAL";
        }

        if (domain == UniversalAddDomain.TRIP) {
            return "LEISURE";
        }

        if (domain == UniversalAddDomain.TASK) {
            if (lower.contains("errand") || lower.contains("grocer") || lower.contains("buy") || lower.contains("clean")) {
                return "PERSONAL";
            }
            if (lower.contains("meeting") || lower.contains("review") || lower.contains("deploy") || lower.contains("code") || lower.contains("design") || lower.contains("report")) {
                return "WORK";
            }
            return "WORK";
        }

        return "GENERAL";
    }

    private String cleanTitle(String text, UniversalAddDomain domain, String locationOrDestination) {
        String cleaned = text;

        // Remove hashtags
        cleaned = cleaned.replaceAll("#[a-zA-Z0-9_-]+", "");

        // Remove date keywords
        cleaned = cleaned.replaceAll("(?i)\\b(tomorrow|today|tonight|next\\s+(monday|tuesday|wednesday|thursday|friday|saturday|sunday)|on\\s+friday|from\\s+\\d{4}-\\d{2}-\\d{2}\\s+to\\s+\\d{4}-\\d{2}-\\d{2}|\\d{4}-\\d{2}-\\d{2})\\b", "");

        // Remove time expressions
        cleaned = cleaned.replaceAll("(?i)\\bat\\s+\\d{1,2}(?::\\d{2})?\\s*(am|pm)?\\b", "");
        cleaned = cleaned.replaceAll("(?i)\\b\\d{1,2}(?::\\d{2})?\\s*(am|pm)\\b", "");
        cleaned = cleaned.replaceAll("(?i)\\b[01]?\\d:[0-5]\\d\\b", "");

        // Remove frequency expressions
        cleaned = cleaned.replaceAll("(?i)\\b(daily|every\\s+day|every\\s+morning|every\\s+evening|every\\s+night|weekly|weekdays)\\b", "");

        // For expense, if text has "Spent X on Y at Z", clean to "Y at Z" or "Lunch at Subway"
        if (domain == UniversalAddDomain.EXPENSE) {
            cleaned = cleaned.replaceAll("(?i)\\b(spent|spend|paid|cost|bought)\\s+(?:rs\\.?|inr|usd|\\$|€|£|eur)?\\s*\\d+(?:\\.\\d{1,2})?\\s*(?:rs\\.?|inr|usd|\\$|€|£|eur)?\\s+(?:on|for)?\\s*", "");
            cleaned = cleaned.replaceAll("(?i)\\b(spent|spend|paid|cost|bought)\\b", "");
            cleaned = cleaned.replaceAll("(?i)(?:rs\\.?|inr|usd|\\$|€|£|eur)\\s*\\d+(?:\\.\\d{1,2})?", "");
            cleaned = cleaned.replaceAll("(?i)\\d+(?:\\.\\d{1,2})?\\s*(?:rs\\.?|inr|usd|\\$|€|£|eur)?", "");
        }

        cleaned = cleaned.replaceAll("\\s+", " ").trim();

        // Capitalize first letter
        if (cleaned.length() > 0) {
            cleaned = Character.toUpperCase(cleaned.charAt(0)) + cleaned.substring(1);
        }

        return cleaned.isBlank() ? text : cleaned;
    }
}
