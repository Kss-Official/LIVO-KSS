package com.livo.api.modules.travelhealth;

import com.livo.api.modules.health.entity.HealthEntryEntity;
import com.livo.api.modules.health.entity.enums.HealthIntensity;
import com.livo.api.modules.health.entity.enums.HealthType;
import com.livo.api.modules.health.repository.HealthEntryRepository;
import com.livo.api.modules.trip.entity.ItineraryItemEntity;
import com.livo.api.modules.trip.entity.TripEntity;
import com.livo.api.modules.trip.entity.enums.AccommodationType;
import com.livo.api.modules.trip.entity.enums.TravelMode;
import com.livo.api.modules.trip.entity.enums.TravelWith;
import com.livo.api.modules.trip.entity.enums.TripType;
import com.livo.api.modules.trip.repository.ItineraryItemRepository;
import com.livo.api.modules.trip.repository.TripRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class TravelHealthDomainIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private ItineraryItemRepository itineraryItemRepository;

    @Autowired
    private HealthEntryRepository healthEntryRepository;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        testUser = UserEntity.builder()
                .firebaseUid("test_part9_fb_" + UUID.randomUUID())
                .email("test.part9." + UUID.randomUUID() + "@example.com")
                .fullName("Part 9 Travel Health User")
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
                itineraryItemRepository.deleteAll(itineraryItemRepository.findAllByUserIdAndDeletedAtIsNull(u.getId()));
                tripRepository.deleteAll(tripRepository.findAllByUserIdAndDeletedAtIsNull(u.getId()));
                healthEntryRepository.deleteAll(healthEntryRepository.findAllByUserIdAndDeletedAtIsNull(u.getId()));
                userRepository.delete(u);
            });
        }
    }

    @Test
    @DisplayName("Test 1: TripEntity persistence, enum mapping, budget, and date range query")
    void testTripPersistence() {
        LocalDate start = LocalDate.now().plusDays(10);
        LocalDate end = start.plusDays(7);

        TripEntity trip = TripEntity.builder()
                .title("Kyoto Autumn Expedition")
                .destination("Kyoto, Japan")
                .startDate(start)
                .endDate(end)
                .tripType(TripType.LEISURE)
                .travelMode(TravelMode.FLIGHT)
                .accommodationType(AccommodationType.HOTEL)
                .accommodationNotes("Ryokan in Gion district")
                .travelWith(TravelWith.SOLO)
                .budgetAmount(new BigDecimal("150000.00"))
                .currency("INR")
                .notes("Visit Fushimi Inari and Arashiyama bamboo grove")
                .build();
        trip.setUserId(testUser.getId());
        trip = tripRepository.saveAndFlush(trip);

        Optional<TripEntity> found = tripRepository.findByIdAndUserIdAndDeletedAtIsNull(trip.getId(), testUser.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Kyoto Autumn Expedition");
        assertThat(found.get().getDestination()).isEqualTo("Kyoto, Japan");
        assertThat(found.get().getTripType()).isEqualTo(TripType.LEISURE);
        assertThat(found.get().getTravelMode()).isEqualTo(TravelMode.FLIGHT);
        assertThat(found.get().getBudgetAmount()).isEqualByComparingTo(new BigDecimal("150000.00"));

        // Type query
        List<TripEntity> leisureTrips = tripRepository.findAllByUserIdAndTripTypeAndDeletedAtIsNull(testUser.getId(), TripType.LEISURE);
        assertThat(leisureTrips).hasSize(1);

        // Soft delete
        trip.markDeleted();
        tripRepository.saveAndFlush(trip);
        assertThat(tripRepository.findByIdAndUserIdAndDeletedAtIsNull(trip.getId(), testUser.getId())).isEmpty();
    }

    @Test
    @DisplayName("Test 2: ItineraryItemEntity persistence, timeline ordering, and soft delete")
    void testItineraryItemPersistence() {
        LocalDate day1 = LocalDate.now().plusDays(5);
        TripEntity trip = TripEntity.builder()
                .title("Goa Weekend Getaway")
                .destination("Goa, India")
                .startDate(day1)
                .endDate(day1.plusDays(3))
                .build();
        trip.setUserId(testUser.getId());
        trip = tripRepository.saveAndFlush(trip);

        ItineraryItemEntity morningActivity = ItineraryItemEntity.builder()
                .tripId(trip.getId())
                .itemDate(day1)
                .itemTime(LocalTime.of(9, 30))
                .title("Breakfast at German Bakery")
                .location("Anjuna")
                .notes("Order croissants and cappuccino")
                .build();
        morningActivity.setUserId(testUser.getId());
        morningActivity = itineraryItemRepository.saveAndFlush(morningActivity);

        ItineraryItemEntity eveningActivity = ItineraryItemEntity.builder()
                .tripId(trip.getId())
                .itemDate(day1)
                .itemTime(LocalTime.of(17, 0))
                .title("Sunset at Vagator Beach")
                .location("Vagator")
                .notes("Cliff top photography")
                .build();
        eveningActivity.setUserId(testUser.getId());
        eveningActivity = itineraryItemRepository.saveAndFlush(eveningActivity);

        List<ItineraryItemEntity> items = itineraryItemRepository.findAllByTripIdAndUserIdAndDeletedAtIsNullOrderByItemDateAscItemTimeAsc(
                trip.getId(), testUser.getId()
        );
        assertThat(items).hasSize(2);
        assertThat(items.get(0).getTitle()).isEqualTo("Breakfast at German Bakery");
        assertThat(items.get(1).getTitle()).isEqualTo("Sunset at Vagator Beach");

        // Soft delete morningActivity
        morningActivity.markDeleted();
        itineraryItemRepository.saveAndFlush(morningActivity);
        List<ItineraryItemEntity> remaining = itineraryItemRepository.findAllByTripIdAndUserIdAndDeletedAtIsNullOrderByItemDateAscItemTimeAsc(
                trip.getId(), testUser.getId()
        );
        assertThat(remaining).hasSize(1);
        assertThat(remaining.get(0).getTitle()).isEqualTo("Sunset at Vagator Beach");
    }

    @Test
    @DisplayName("Test 3: HealthEntryEntity persistence, JSONB metrics mapping, and health type query")
    void testHealthEntryPersistence() {
        LocalDate today = LocalDate.now();
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("calories_burned", 520);
        metrics.put("avg_heart_rate", 148);
        metrics.put("peak_heart_rate", 172);

        HealthEntryEntity workout = HealthEntryEntity.builder()
                .title("Morning High-Intensity Interval Training")
                .description("Cardio and bodyweight strength circuit")
                .healthType(HealthType.WORKOUT)
                .entryDate(today)
                .entryTime(LocalTime.of(7, 15))
                .durationMins(45)
                .intensity(HealthIntensity.HIGH)
                .metricsJson(metrics)
                .notes("Felt energized throughout the session")
                .build();
        workout.setUserId(testUser.getId());
        workout = healthEntryRepository.saveAndFlush(workout);

        Optional<HealthEntryEntity> found = healthEntryRepository.findByIdAndUserIdAndDeletedAtIsNull(workout.getId(), testUser.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Morning High-Intensity Interval Training");
        assertThat(found.get().getHealthType()).isEqualTo(HealthType.WORKOUT);
        assertThat(found.get().getIntensity()).isEqualTo(HealthIntensity.HIGH);
        assertThat(found.get().getDurationMins()).isEqualTo(45);
        assertThat(found.get().getMetricsJson()).containsEntry("calories_burned", 520);
        assertThat(found.get().getMetricsJson()).containsEntry("avg_heart_rate", 148);

        // Filter by health type
        List<HealthEntryEntity> workouts = healthEntryRepository.findAllByUserIdAndHealthTypeAndDeletedAtIsNull(
                testUser.getId(), HealthType.WORKOUT
        );
        assertThat(workouts).hasSize(1);

        // Date range query
        List<HealthEntryEntity> dateRange = healthEntryRepository.findAllByUserIdAndEntryDateBetweenAndDeletedAtIsNullOrderByEntryDateDesc(
                testUser.getId(), today.minusDays(1), today.plusDays(1)
        );
        assertThat(dateRange).hasSize(1);

        // Soft delete
        workout.markDeleted();
        healthEntryRepository.saveAndFlush(workout);
        assertThat(healthEntryRepository.findByIdAndUserIdAndDeletedAtIsNull(workout.getId(), testUser.getId())).isEmpty();
    }
}
