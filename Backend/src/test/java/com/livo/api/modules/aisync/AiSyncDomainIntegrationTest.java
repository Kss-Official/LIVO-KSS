package com.livo.api.modules.aisync;

import com.livo.api.modules.ai.entity.AiAuditLogEntity;
import com.livo.api.modules.ai.entity.AiConversationEntity;
import com.livo.api.modules.ai.entity.AiMessageEntity;
import com.livo.api.modules.ai.entity.AiProposalEntity;
import com.livo.api.modules.ai.entity.AiRecommendationEntity;
import com.livo.api.modules.ai.entity.AiUsageDailyEntity;
import com.livo.api.modules.ai.entity.AiUsageDailyId;
import com.livo.api.modules.ai.entity.enums.AiAuditSource;
import com.livo.api.modules.ai.entity.enums.AiMessageRole;
import com.livo.api.modules.ai.entity.enums.AiPermissionLevel;
import com.livo.api.modules.ai.entity.enums.AiProposalStatus;
import com.livo.api.modules.ai.entity.enums.AiRecommendationPriority;
import com.livo.api.modules.ai.entity.enums.AiRecommendationType;
import com.livo.api.modules.ai.entity.enums.AiRelatedEntityType;
import com.livo.api.modules.ai.repository.AiAuditLogRepository;
import com.livo.api.modules.ai.repository.AiConversationRepository;
import com.livo.api.modules.ai.repository.AiMessageRepository;
import com.livo.api.modules.ai.repository.AiProposalRepository;
import com.livo.api.modules.ai.repository.AiRecommendationRepository;
import com.livo.api.modules.ai.repository.AiUsageDailyRepository;
import com.livo.api.modules.notification.entity.NotificationEntity;
import com.livo.api.modules.notification.entity.enums.NotificationType;
import com.livo.api.modules.notification.repository.NotificationRepository;
import com.livo.api.modules.sync.entity.OfflineMutationEntity;
import com.livo.api.modules.sync.entity.enums.OfflineMutationStatus;
import com.livo.api.modules.sync.repository.OfflineMutationRepository;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class AiSyncDomainIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AiConversationRepository aiConversationRepository;

    @Autowired
    private AiMessageRepository aiMessageRepository;

    @Autowired
    private AiRecommendationRepository aiRecommendationRepository;

    @Autowired
    private AiProposalRepository aiProposalRepository;

    @Autowired
    private AiAuditLogRepository aiAuditLogRepository;

    @Autowired
    private AiUsageDailyRepository aiUsageDailyRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private OfflineMutationRepository offlineMutationRepository;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        testUser = UserEntity.builder()
                .firebaseUid("test_part10_fb_" + UUID.randomUUID())
                .email("test.part10." + UUID.randomUUID() + "@example.com")
                .fullName("Part 10 AI Sync User")
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
                offlineMutationRepository.deleteAll(offlineMutationRepository.findAllByUserIdOrderByAppliedAtDesc(u.getId()));
                notificationRepository.deleteAll(notificationRepository.findAllByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(u.getId()));
                aiUsageDailyRepository.deleteAll(aiUsageDailyRepository.findAllByIdUserId(u.getId()));
                aiAuditLogRepository.deleteAll(aiAuditLogRepository.findAllByUserIdOrderByCreatedAtDesc(u.getId()));
                aiProposalRepository.deleteAll(aiProposalRepository.findAllByUserIdOrderByCreatedAtDesc(u.getId()));
                aiRecommendationRepository.deleteAll(aiRecommendationRepository.findAllByUserIdAndIsActiveTrueOrderByCreatedAtDesc(u.getId()));
                aiConversationRepository.deleteAll(aiConversationRepository.findAllByUserIdOrderByUpdatedAtDesc(u.getId()));
                userRepository.delete(u);
            });
        }
    }

    @Test
    @DisplayName("Test 1: AiConversationEntity and AiMessageEntity with JSON cards and array attachments")
    void testAiConversationAndMessages() {
        AiConversationEntity conv = AiConversationEntity.builder()
                .userId(testUser.getId())
                .title("Weekly Productivity Planning")
                .build();
        conv = aiConversationRepository.saveAndFlush(conv);

        Map<String, Object> cardData = new HashMap<>();
        cardData.put("type", "schedule_card");
        cardData.put("slot", "10:00 - 11:30 AM");

        AiMessageEntity msg = AiMessageEntity.builder()
                .conversationId(conv.getId())
                .userId(testUser.getId())
                .role(AiMessageRole.ASSISTANT)
                .content("I found a 90-minute focus slot tomorrow morning.")
                .operatingMode("PROACTIVE")
                .cardsJson(cardData)
                .suggestedRepliesJson(List.of("Schedule deep work", "Dismiss"))
                .attachmentIds(List.of(UUID.randomUUID()))
                .build();
        msg = aiMessageRepository.saveAndFlush(msg);

        List<AiMessageEntity> messages = aiMessageRepository.findAllByConversationIdAndUserIdOrderByCreatedAtAsc(conv.getId(), testUser.getId());
        assertThat(messages).hasSize(1);
        assertThat(messages.get(0).getContent()).contains("90-minute focus slot");
        assertThat(messages.get(0).getCardsJson()).containsEntry("slot", "10:00 - 11:30 AM");
        assertThat(messages.get(0).getSuggestedRepliesJson()).contains("Schedule deep work");
        assertThat(messages.get(0).getAttachmentIds()).hasSize(1);
    }

    @Test
    @DisplayName("Test 2: AiRecommendationEntity persistence, dedupe key, priority, and active query")
    void testAiRecommendations() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("suggested_start", "09:00");

        AiRecommendationEntity rec = AiRecommendationEntity.builder()
                .userId(testUser.getId())
                .type(AiRecommendationType.FREE_SLOT)
                .title("Open Morning Focus Block")
                .reason("You have no meetings between 9 AM and 11 AM.")
                .recommendationPriority(AiRecommendationPriority.HIGH)
                .permissionLevel(AiPermissionLevel.SUGGEST)
                .relatedEntityType(AiRelatedEntityType.TASK)
                .actionType("CREATE_SCHEDULE_BLOCK")
                .actionPayload(payload)
                .dedupeKey("free_slot_" + LocalDate.now())
                .isActive(true)
                .build();
        rec = aiRecommendationRepository.saveAndFlush(rec);

        Optional<AiRecommendationEntity> found = aiRecommendationRepository.findByUserIdAndDedupeKeyAndIsActiveTrue(
                testUser.getId(), "free_slot_" + LocalDate.now()
        );
        assertThat(found).isPresent();
        assertThat(found.get().getType()).isEqualTo(AiRecommendationType.FREE_SLOT);
        assertThat(found.get().getRecommendationPriority()).isEqualTo(AiRecommendationPriority.HIGH);
        assertThat(found.get().getActionPayload()).containsEntry("suggested_start", "09:00");

        List<AiRecommendationEntity> activeRecs = aiRecommendationRepository.findAllByUserIdAndIsActiveTrueOrderByCreatedAtDesc(testUser.getId());
        assertThat(activeRecs).hasSize(1);
    }

    @Test
    @DisplayName("Test 3: AiProposalEntity and AiAuditLogEntity mutation auditing")
    void testAiProposalAndAuditLog() {
        Map<String, Object> proposalData = new HashMap<>();
        proposalData.put("action", "RESCHEDULE_TASK");
        proposalData.put("new_date", LocalDate.now().plusDays(1).toString());

        AiProposalEntity proposal = AiProposalEntity.builder()
                .userId(testUser.getId())
                .actionType("RESCHEDULE_TASK")
                .proposalPayload(proposalData)
                .status(AiProposalStatus.PENDING)
                .build();
        proposal = aiProposalRepository.saveAndFlush(proposal);

        Map<String, Object> oldVal = Map.of("due_date", LocalDate.now().toString());
        Map<String, Object> newVal = Map.of("due_date", LocalDate.now().plusDays(1).toString());

        AiAuditLogEntity audit = AiAuditLogEntity.builder()
                .userId(testUser.getId())
                .proposalId(proposal.getId())
                .actionType("TASK_RESCHEDULE")
                .entityType("TASK")
                .entityId(UUID.randomUUID())
                .oldValue(oldVal)
                .newValue(newVal)
                .source(AiAuditSource.AI)
                .build();
        audit = aiAuditLogRepository.saveAndFlush(audit);

        Optional<AiProposalEntity> foundProposal = aiProposalRepository.findByIdAndUserId(proposal.getId(), testUser.getId());
        assertThat(foundProposal).isPresent();
        assertThat(foundProposal.get().getStatus()).isEqualTo(AiProposalStatus.PENDING);

        List<AiAuditLogEntity> logs = aiAuditLogRepository.findAllByUserIdAndProposalId(testUser.getId(), proposal.getId());
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getSource()).isEqualTo(AiAuditSource.AI);
    }

    @Test
    @DisplayName("Test 4: AiUsageDailyEntity composite primary key rate limiting")
    void testAiUsageDaily() {
        LocalDate today = LocalDate.now();
        AiUsageDailyId id = new AiUsageDailyId(testUser.getId(), today);

        AiUsageDailyEntity usage = AiUsageDailyEntity.builder()
                .id(id)
                .chatCount(15)
                .voiceCount(3)
                .build();
        usage = aiUsageDailyRepository.saveAndFlush(usage);

        Optional<AiUsageDailyEntity> found = aiUsageDailyRepository.findByIdUserIdAndIdUsageDate(testUser.getId(), today);
        assertThat(found).isPresent();
        assertThat(found.get().getChatCount()).isEqualTo(15);
        assertThat(found.get().getVoiceCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("Test 5: NotificationEntity persistence, unread count, and read status update")
    void testNotifications() {
        NotificationEntity notification = NotificationEntity.builder()
                .type(NotificationType.AI_RECOMMENDATION)
                .title("Smart Schedule Alert")
                .body("2 tasks are scheduled at the same time tomorrow.")
                .relatedEntityType("EVENT")
                .relatedEntityId(UUID.randomUUID())
                .build();
        notification.setUserId(testUser.getId());
        notification = notificationRepository.saveAndFlush(notification);

        long unreadCount = notificationRepository.countByUserIdAndReadAtIsNullAndDeletedAtIsNull(testUser.getId());
        assertThat(unreadCount).isEqualTo(1);

        // Mark read
        notification.setReadAt(Instant.now());
        notification = notificationRepository.saveAndFlush(notification);

        long updatedUnread = notificationRepository.countByUserIdAndReadAtIsNullAndDeletedAtIsNull(testUser.getId());
        assertThat(updatedUnread).isEqualTo(0);

        // Soft delete
        notification.markDeleted();
        notification = notificationRepository.saveAndFlush(notification);
        assertThat(notificationRepository.findByIdAndUserIdAndDeletedAtIsNull(notification.getId(), testUser.getId())).isEmpty();
    }

    @Test
    @DisplayName("Test 6: OfflineMutationEntity queued sync and idempotency check")
    void testOfflineMutations() {
        String idempotencyKey = "client_mut_" + UUID.randomUUID();
        Map<String, Object> payload = Map.of("title", "Buy Groceries", "priority", "HIGH");

        OfflineMutationEntity mutation = OfflineMutationEntity.builder()
                .userId(testUser.getId())
                .idempotencyKey(idempotencyKey)
                .domainEntity("TASK")
                .action("CREATE")
                .payload(payload)
                .clientTimestamp(Instant.now().minusSeconds(60))
                .entityId(UUID.randomUUID())
                .baseVersion(1L)
                .status(OfflineMutationStatus.APPLIED)
                .build();
        mutation = offlineMutationRepository.saveAndFlush(mutation);

        assertThat(offlineMutationRepository.existsByUserIdAndIdempotencyKey(testUser.getId(), idempotencyKey)).isTrue();
        Optional<OfflineMutationEntity> found = offlineMutationRepository.findByUserIdAndIdempotencyKey(testUser.getId(), idempotencyKey);
        assertThat(found).isPresent();
        assertThat(found.get().getDomainEntity()).isEqualTo("TASK");
        assertThat(found.get().getStatus()).isEqualTo(OfflineMutationStatus.APPLIED);
    }
}
