package com.livo.api.modules.universaladd.service;

import com.livo.api.modules.event.dto.CreateEventRequest;
import com.livo.api.modules.event.dto.EventResponse;
import com.livo.api.modules.event.entity.enums.EventPriority;
import com.livo.api.modules.event.service.EventService;
import com.livo.api.modules.finance.dto.CreateTransactionRequest;
import com.livo.api.modules.finance.dto.TransactionResponse;
import com.livo.api.modules.finance.entity.enums.PaymentMethod;
import com.livo.api.modules.finance.entity.enums.TransactionType;
import com.livo.api.modules.finance.service.FinanceService;
import com.livo.api.modules.goal.dto.CreateGoalRequest;
import com.livo.api.modules.goal.dto.GoalResponse;
import com.livo.api.modules.goal.entity.enums.GoalPriority;
import com.livo.api.modules.goal.service.GoalService;
import com.livo.api.modules.habit.dto.CreateHabitRequest;
import com.livo.api.modules.habit.dto.HabitResponse;
import com.livo.api.modules.habit.entity.enums.HabitFrequency;
import com.livo.api.modules.habit.service.HabitService;
import com.livo.api.modules.learning.dto.CreateLearningItemRequest;
import com.livo.api.modules.learning.dto.LearningItemResponse;
import com.livo.api.modules.learning.entity.enums.DifficultyLevel;
import com.livo.api.modules.learning.entity.enums.LearningType;
import com.livo.api.modules.learning.service.LearningService;
import com.livo.api.modules.task.dto.CreateTaskRequest;
import com.livo.api.modules.task.dto.TaskResponse;
import com.livo.api.modules.task.entity.enums.TaskPriority;
import com.livo.api.modules.task.service.TaskService;
import com.livo.api.modules.trip.dto.CreateTripRequest;
import com.livo.api.modules.trip.dto.TripResponse;
import com.livo.api.modules.trip.entity.enums.TripType;
import com.livo.api.modules.trip.service.TripService;
import com.livo.api.modules.universaladd.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UniversalAddServiceImpl implements UniversalAddService {

    private final LocalIntentMatcher localIntentMatcher;
    private final TaskService taskService;
    private final FinanceService financeService;
    private final EventService eventService;
    private final HabitService habitService;
    private final GoalService goalService;
    private final LearningService learningService;
    private final TripService tripService;
    private final com.livo.api.modules.ocr.service.OcrService ocrService;

    @Override
    public UniversalAddResponse parseInput(UUID userId, UniversalAddRequest request) {
        ParsedEntityDraft draft = localIntentMatcher.parse(request.getInput(), request.getPreferredDomain());

        String explanation = String.format("Parsed input as %s with confidence %.2f: '%s'",
                draft.getDomain(), draft.getConfidenceScore(), draft.getTitle());

        return UniversalAddResponse.builder()
                .rawInput(request.getInput())
                .detectedDomain(draft.getDomain())
                .confidence(draft.getConfidenceScore())
                .parsedDraft(draft)
                .explanation(explanation)
                .build();
    }

    @Override
    @Transactional
    public UniversalAddConfirmResponse confirmAndCreate(UUID userId, UniversalAddConfirmRequest request) {
        ParsedEntityDraft draft = request.getDraft();
        UniversalAddDomain domain = draft.getDomain() != null ? draft.getDomain() : UniversalAddDomain.TASK;

        log.info("Confirming universal-add draft for user {} in domain {}", userId, domain);

        switch (domain) {
            case EXPENSE: {
                CreateTransactionRequest txReq = CreateTransactionRequest.builder()
                        .title(draft.getTitle() != null && !draft.getTitle().isBlank() ? draft.getTitle() : "Expense")
                        .description(draft.getDescription())
                        .type(TransactionType.EXPENSE)
                        .amount(draft.getAmount() != null ? draft.getAmount() : new BigDecimal("0.00"))
                        .currency(draft.getCurrency() != null ? draft.getCurrency() : "INR")
                        .category(draft.getCategory() != null ? draft.getCategory() : "FOOD")
                        .paymentMethod(PaymentMethod.UPI)
                        .transactionDate(draft.getDate() != null ? draft.getDate() : LocalDate.now())
                        .build();
                TransactionResponse res = financeService.createTransaction(userId, txReq);
                return UniversalAddConfirmResponse.builder()
                        .entityId(res.getId())
                        .domain(UniversalAddDomain.EXPENSE)
                        .title(res.getTitle())
                        .message("Expense successfully recorded")
                        .entityDetails(res)
                        .build();
            }

            case EVENT: {
                Instant startInstant = draft.getDate() != null
                        ? draft.getDate().atTime(draft.getTime() != null ? draft.getTime() : LocalTime.of(9, 0)).atZone(ZoneId.systemDefault()).toInstant()
                        : Instant.now().plusSeconds(3600);
                Instant endInstant = draft.getEndDate() != null && draft.getEndTime() != null
                        ? draft.getEndDate().atTime(draft.getEndTime()).atZone(ZoneId.systemDefault()).toInstant()
                        : startInstant.plusSeconds(3600);

                CreateEventRequest eventReq = CreateEventRequest.builder()
                        .title(draft.getTitle() != null && !draft.getTitle().isBlank() ? draft.getTitle() : "Event")
                        .description(draft.getDescription())
                        .location(draft.getLocationOrDestination())
                        .startTime(startInstant)
                        .endTime(endInstant)
                        .priority(safeEventPriority(draft.getPriority()))
                        .build();
                EventResponse res = eventService.createEvent(userId, eventReq);
                return UniversalAddConfirmResponse.builder()
                        .entityId(res.getId())
                        .domain(UniversalAddDomain.EVENT)
                        .title(res.getTitle())
                        .message("Event successfully scheduled")
                        .entityDetails(res)
                        .build();
            }

            case HABIT: {
                CreateHabitRequest habitReq = CreateHabitRequest.builder()
                        .title(draft.getTitle() != null && !draft.getTitle().isBlank() ? draft.getTitle() : "Habit")
                        .description(draft.getDescription())
                        .motivationNote(draft.getRawInput())
                        .frequencyType(safeHabitFrequency(draft.getFrequency()))
                        .build();
                HabitResponse res = habitService.createHabit(userId, habitReq);
                return UniversalAddConfirmResponse.builder()
                        .entityId(res.getId())
                        .domain(UniversalAddDomain.HABIT)
                        .title(res.getTitle())
                        .message("Habit successfully created")
                        .entityDetails(res)
                        .build();
            }

            case GOAL: {
                CreateGoalRequest goalReq = CreateGoalRequest.builder()
                        .title(draft.getTitle() != null && !draft.getTitle().isBlank() ? draft.getTitle() : "Goal")
                        .description(draft.getDescription())
                        .targetDescription(draft.getRawInput())
                        .targetDate(draft.getDate() != null ? draft.getDate() : LocalDate.now().plusMonths(3))
                        .priority(safeGoalPriority(draft.getPriority()))
                        .build();
                GoalResponse res = goalService.createGoal(userId, goalReq);
                return UniversalAddConfirmResponse.builder()
                        .entityId(res.getId())
                        .domain(UniversalAddDomain.GOAL)
                        .title(res.getTitle())
                        .message("Goal successfully established")
                        .entityDetails(res)
                        .build();
            }

            case LEARNING: {
                CreateLearningItemRequest learningReq = CreateLearningItemRequest.builder()
                        .title(draft.getTitle() != null && !draft.getTitle().isBlank() ? draft.getTitle() : "Learning Item")
                        .description(draft.getDescription())
                        .notes(draft.getRawInput())
                        .category(draft.getCategory() != null ? draft.getCategory() : "GENERAL")
                        .learningType(LearningType.COURSE)
                        .difficultyLevel(DifficultyLevel.INTERMEDIATE)
                        .build();
                LearningItemResponse res = learningService.createLearningItem(userId, learningReq);
                return UniversalAddConfirmResponse.builder()
                        .entityId(res.getId())
                        .domain(UniversalAddDomain.LEARNING)
                        .title(res.getTitle())
                        .message("Learning item successfully added")
                        .entityDetails(res)
                        .build();
            }

            case TRIP: {
                LocalDate startDate = draft.getDate() != null ? draft.getDate() : LocalDate.now().plusWeeks(1);
                LocalDate endDate = draft.getEndDate() != null ? draft.getEndDate() : startDate.plusDays(5);
                String dest = (draft.getLocationOrDestination() != null && !draft.getLocationOrDestination().isBlank() && !"null".equalsIgnoreCase(draft.getLocationOrDestination().trim()))
                        ? draft.getLocationOrDestination().trim()
                        : "Destination";
                String tripTitle = (draft.getTitle() != null && !draft.getTitle().isBlank() && !"Trip to null".equalsIgnoreCase(draft.getTitle().trim()))
                        ? draft.getTitle().trim()
                        : (!"Destination".equalsIgnoreCase(dest) ? "Trip to " + dest : "Trip");

                CreateTripRequest tripReq = CreateTripRequest.builder()
                        .title(tripTitle)
                        .destination(dest)
                        .startDate(startDate)
                        .endDate(endDate)
                        .tripType(TripType.LEISURE)
                        .budgetAmount(draft.getAmount())
                        .build();
                TripResponse res = tripService.createTrip(userId, tripReq);
                return UniversalAddConfirmResponse.builder()
                        .entityId(res.getId())
                        .domain(UniversalAddDomain.TRIP)
                        .title(res.getTitle())
                        .message("Trip successfully created")
                        .entityDetails(res)
                        .build();
            }

            case TASK:
            default: {
                CreateTaskRequest taskReq = CreateTaskRequest.builder()
                        .title(draft.getTitle() != null && !draft.getTitle().isBlank() ? draft.getTitle() : draft.getRawInput())
                        .description(draft.getDescription())
                        .category(draft.getCategory() != null ? draft.getCategory() : "WORK")
                        .priority(safeTaskPriority(draft.getPriority()))
                        .dueDate(draft.getDate())
                        .dueTime(draft.getTime())
                        .build();
                TaskResponse res = taskService.createTask(userId, taskReq);
                return UniversalAddConfirmResponse.builder()
                        .entityId(res.getId())
                        .domain(UniversalAddDomain.TASK)
                        .title(res.getTitle())
                        .message("Task successfully created")
                        .entityDetails(res)
                        .build();
            }
        }
    }

    private TaskPriority safeTaskPriority(String p) {
        if (p == null) return TaskPriority.MEDIUM;
        try {
            return TaskPriority.valueOf(p.toUpperCase());
        } catch (Exception e) {
            return TaskPriority.MEDIUM;
        }
    }

    private EventPriority safeEventPriority(String p) {
        if (p == null) return EventPriority.MEDIUM;
        try {
            return EventPriority.valueOf(p.toUpperCase());
        } catch (Exception e) {
            return EventPriority.MEDIUM;
        }
    }

    private GoalPriority safeGoalPriority(String p) {
        if (p == null) return GoalPriority.MEDIUM;
        try {
            return GoalPriority.valueOf(p.toUpperCase());
        } catch (Exception e) {
            return GoalPriority.MEDIUM;
        }
    }

    private HabitFrequency safeHabitFrequency(String f) {
        if (f == null) return HabitFrequency.DAILY;
        try {
            return HabitFrequency.valueOf(f.toUpperCase());
        } catch (Exception e) {
            return HabitFrequency.DAILY;
        }
    }

    @Override
    public UniversalAddResponse scanDocument(UUID userId, org.springframework.web.multipart.MultipartFile file, String hint) {
        com.livo.api.modules.ocr.dto.OcrScanResult ocrResult = ocrService.scanFile(file, hint);

        UniversalAddDomain domain = UniversalAddDomain.EXPENSE;
        if ("TASK_LIST".equalsIgnoreCase(ocrResult.getDocumentType())) {
            domain = UniversalAddDomain.TASK;
        } else if ("TICKET".equalsIgnoreCase(ocrResult.getDocumentType())) {
            domain = UniversalAddDomain.EVENT;
        }

        StringBuilder descBuilder = new StringBuilder();
        if (ocrResult.getMerchant() != null) {
            descBuilder.append("Merchant: ").append(ocrResult.getMerchant()).append("\n");
        }
        if (ocrResult.getLineItems() != null && !ocrResult.getLineItems().isEmpty()) {
            descBuilder.append("Items:\n");
            for (com.livo.api.modules.ocr.dto.OcrLineItem item : ocrResult.getLineItems()) {
                descBuilder.append(" - ").append(item.getName());
                if (item.getPrice() != null) {
                    descBuilder.append(" (").append(item.getPrice()).append(")");
                }
                descBuilder.append("\n");
            }
        }
        if (ocrResult.getTaxAmount() != null) {
            descBuilder.append("Tax: ").append(ocrResult.getTaxAmount()).append("\n");
        }

        ParsedEntityDraft draft = ParsedEntityDraft.builder()
                .domain(domain)
                .title(ocrResult.getTitle() != null && !ocrResult.getTitle().isBlank() ? ocrResult.getTitle() : "Scanned Receipt")
                .description(descBuilder.toString().trim())
                .category(ocrResult.getCategory() != null ? ocrResult.getCategory() : "FOOD")
                .amount(ocrResult.getTotalAmount() != null ? ocrResult.getTotalAmount() : BigDecimal.ZERO)
                .currency(ocrResult.getCurrency() != null ? ocrResult.getCurrency() : "INR")
                .date(ocrResult.getDate() != null ? ocrResult.getDate() : LocalDate.now())
                .time(ocrResult.getTime())
                .confidenceScore(ocrResult.getConfidenceScore())
                .rawInput(ocrResult.getRawText())
                .priority("MEDIUM")
                .build();

        String explanation = String.format("OCR extracted %s (%s %s) from '%s' via %s engine",
                domain,
                draft.getAmount(),
                draft.getCurrency(),
                file.getOriginalFilename(),
                ocrResult.getEngine());

        return UniversalAddResponse.builder()
                .rawInput(ocrResult.getRawText())
                .detectedDomain(domain)
                .confidence(ocrResult.getConfidenceScore())
                .parsedDraft(draft)
                .explanation(explanation)
                .build();
    }
}
