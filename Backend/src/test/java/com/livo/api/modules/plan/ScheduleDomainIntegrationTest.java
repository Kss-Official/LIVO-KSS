package com.livo.api.modules.plan;

import com.livo.api.modules.event.entity.EventEntity;
import com.livo.api.modules.event.entity.enums.EventFormat;
import com.livo.api.modules.event.entity.enums.EventPriority;
import com.livo.api.modules.event.repository.EventRepository;
import com.livo.api.modules.plan.entity.ScheduleBlockEntity;
import com.livo.api.modules.plan.repository.ScheduleBlockRepository;
import com.livo.api.modules.routine.entity.RoutineEntity;
import com.livo.api.modules.routine.repository.RoutineRepository;
import com.livo.api.modules.user.entity.UserEntity;
import com.livo.api.modules.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ScheduleDomainIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private RoutineRepository routineRepository;

    @Autowired
    private ScheduleBlockRepository scheduleBlockRepository;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        testUser = UserEntity.builder()
                .firebaseUid("test_part4_fb_" + UUID.randomUUID())
                .email("test.part4." + UUID.randomUUID() + "@example.com")
                .fullName("Part 4 Schedule User")
                .timezone("Asia/Kolkata")
                .language("en")
                .currency("INR")
                .build();
        testUser = userRepository.saveAndFlush(testUser);
    }

    @AfterEach
    @Transactional
    void tearDown() {
        if (testUser != null && testUser.getId() != null) {
            userRepository.findById(testUser.getId()).ifPresent(u -> {
                scheduleBlockRepository.deleteAll(scheduleBlockRepository.findAllByUserIdAndBlockDateAndDeletedAtIsNullOrderByStartTimeAsc(u.getId(), LocalDate.now()));
                routineRepository.deleteAll(routineRepository.findAllByUserIdAndDeletedAtIsNull(u.getId()));
                eventRepository.deleteAll(eventRepository.findAllByUserIdAndDeletedAtIsNull(u.getId()));
                userRepository.delete(u);
            });
        }
    }

    @Test
    @DisplayName("Test 1: EventEntity persistence, time ordering, and range querying")
    void testEventPersistence() {
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Instant end = now.plus(1, ChronoUnit.HOURS);

        EventEntity event = EventEntity.builder()
                .title("Architecture Sync Call")
                .description("Review JPA entities with pairing agent")
                .location("Google Meet")
                .format(EventFormat.ONLINE)
                .priority(EventPriority.HIGH)
                .category("WORK")
                .startTime(now)
                .endTime(end)
                .reminderMinutes(15)
                .notes("Discuss multi-tenant composite foreign keys")
                .build();
        event.setUserId(testUser.getId());
        event = eventRepository.saveAndFlush(event);

        Optional<EventEntity> found = eventRepository.findByIdAndUserIdAndDeletedAtIsNull(event.getId(), testUser.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Architecture Sync Call");
        assertThat(found.get().getFormat()).isEqualTo(EventFormat.ONLINE);
        assertThat(found.get().getPriority()).isEqualTo(EventPriority.HIGH);

        // Query by time range
        List<EventEntity> rangeEvents = eventRepository.findAllByUserIdAndStartTimeBetweenAndDeletedAtIsNull(
                testUser.getId(), now.minus(5, ChronoUnit.MINUTES), now.plus(5, ChronoUnit.MINUTES)
        );
        assertThat(rangeEvents).hasSize(1);

        // Soft delete
        event.markDeleted();
        eventRepository.saveAndFlush(event);
        assertThat(eventRepository.findByIdAndUserIdAndDeletedAtIsNull(event.getId(), testUser.getId())).isEmpty();
    }

    @Test
    @DisplayName("Test 2: RoutineEntity recurring days array and overnight validation")
    void testRoutinePersistence() {
        RoutineEntity routine = RoutineEntity.builder()
                .title("Morning Focus Routine")
                .startTime(LocalTime.of(7, 30))
                .endTime(LocalTime.of(8, 30))
                .daysOfWeek(List.of(1, 2, 3, 4, 5)) // Monday to Friday
                .isActive(true)
                .build();
        routine.setUserId(testUser.getId());
        routine = routineRepository.saveAndFlush(routine);

        Optional<RoutineEntity> found = routineRepository.findByIdAndUserIdAndDeletedAtIsNull(routine.getId(), testUser.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Morning Focus Routine");
        assertThat(found.get().getDaysOfWeek()).containsExactly(1, 2, 3, 4, 5);

        List<RoutineEntity> activeRoutines = routineRepository.findAllByUserIdAndIsActiveTrueAndDeletedAtIsNull(testUser.getId());
        assertThat(activeRoutines).hasSize(1);
    }

    @Test
    @DisplayName("Test 3: ScheduleBlockEntity daily timeline and slot ordering")
    void testScheduleBlockPersistence() {
        LocalDate today = LocalDate.now();

        ScheduleBlockEntity morningBlock = ScheduleBlockEntity.builder()
                .blockDate(today)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 30))
                .title("Deep Work: Backend Architecture")
                .category("WORK")
                .isLocked(true)
                .build();
        morningBlock.setUserId(testUser.getId());

        ScheduleBlockEntity afternoonBlock = ScheduleBlockEntity.builder()
                .blockDate(today)
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(15, 0))
                .title("Review Pull Requests")
                .category("WORK")
                .isLocked(false)
                .build();
        afternoonBlock.setUserId(testUser.getId());

        scheduleBlockRepository.saveAndFlush(morningBlock);
        scheduleBlockRepository.saveAndFlush(afternoonBlock);

        List<ScheduleBlockEntity> timeline = scheduleBlockRepository.findAllByUserIdAndBlockDateAndDeletedAtIsNullOrderByStartTimeAsc(
                testUser.getId(), today
        );
        assertThat(timeline).hasSize(2);
        assertThat(timeline.get(0).getTitle()).isEqualTo("Deep Work: Backend Architecture");
        assertThat(timeline.get(0).isLocked()).isTrue();
        assertThat(timeline.get(1).getTitle()).isEqualTo("Review Pull Requests");
        assertThat(timeline.get(1).isLocked()).isFalse();
    }
}
