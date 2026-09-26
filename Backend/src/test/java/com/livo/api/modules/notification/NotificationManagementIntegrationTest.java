package com.livo.api.modules.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.livo.api.modules.auth.dto.UserSyncRequest;
import com.livo.api.modules.auth.service.AuthService;
import com.livo.api.modules.notification.dto.CreateNotificationRequest;
import com.livo.api.modules.notification.entity.enums.NotificationType;
import com.livo.api.modules.notification.repository.NotificationRepository;
import com.livo.api.modules.notification.scheduler.DailyBriefingScheduler;
import com.livo.api.modules.notification.scheduler.MidnightRolloverScheduler;
import com.livo.api.modules.notification.scheduler.TaskReminderScheduler;
import com.livo.api.modules.task.dto.CreateTaskRequest;
import com.livo.api.modules.task.service.TaskService;
import com.livo.api.modules.notification.service.PushNotificationService;
import com.livo.api.modules.user.entity.UserDeviceTokenEntity;
import com.livo.api.modules.user.entity.UserPreferenceEntity;
import com.livo.api.modules.user.entity.enums.DevicePlatform;
import com.livo.api.modules.user.repository.UserDeviceTokenRepository;
import com.livo.api.modules.user.repository.UserPreferenceRepository;
import com.livo.api.modules.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NotificationManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserDeviceTokenRepository userDeviceTokenRepository;

    @Autowired
    private TaskReminderScheduler taskReminderScheduler;

    @Autowired
    private DailyBriefingScheduler dailyBriefingScheduler;

    @Autowired
    private MidnightRolloverScheduler midnightRolloverScheduler;

    @Autowired
    private PushNotificationService pushNotificationService;

    @Autowired
    private UserPreferenceRepository userPreferenceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    private UUID userId;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        String uid = "notif_user_" + UUID.randomUUID();
        String email = "notif_" + UUID.randomUUID() + "@livo.test";

        var authResponse = authService.syncUser(UserSyncRequest.builder()
                .firebaseUid(uid)
                .email(email)
                .fullName("Alex Notif")
                .timezone("Asia/Kolkata")
                .build());

        this.userId = authResponse.getUser().getId();
        this.jwtToken = authResponse.getAccessToken();
    }

    @Test
    @DisplayName("Should create and query notifications with unread and type filtering")
    void testCreateAndFilterNotifications() throws Exception {
        // 1. Create a REMINDER notification
        CreateNotificationRequest req1 = CreateNotificationRequest.builder()
                .type(NotificationType.REMINDER)
                .title("Standup in 10 mins")
                .body("Join Google Meet for project sync")
                .relatedEntityType("TASK")
                .relatedEntityId(UUID.randomUUID())
                .sendPush(false)
                .build();

        mockMvc.perform(post("/api/v1/notifications")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Standup in 10 mins"))
                .andExpect(jsonPath("$.data.type").value("REMINDER"))
                .andExpect(jsonPath("$.data.read").value(false));

        // 2. Create an AI_RECOMMENDATION notification
        CreateNotificationRequest req2 = CreateNotificationRequest.builder()
                .type(NotificationType.AI_RECOMMENDATION)
                .title("Focus Window Identified")
                .body("You are most productive between 10 AM and 12 PM")
                .sendPush(false)
                .build();

        mockMvc.perform(post("/api/v1/notifications")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.type").value("AI_RECOMMENDATION"));

        // 3. Query all notifications
        mockMvc.perform(get("/api/v1/notifications")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2));

        // 4. Query by type = REMINDER
        mockMvc.perform(get("/api/v1/notifications")
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("type", "REMINDER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].type").value("REMINDER"));

        // 5. Query by non-existent type = BUDGET
        mockMvc.perform(get("/api/v1/notifications")
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("type", "BUDGET"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("Should track unread counts, mark single as read, and mark all as read")
    void testUnreadCountAndMarkAsRead() throws Exception {
        // 1. Create two unread notifications
        CreateNotificationRequest req1 = CreateNotificationRequest.builder()
                .type(NotificationType.STREAK)
                .title("7-Day Streak Achieved!")
                .body("Keep it going tomorrow.")
                .sendPush(false)
                .build();

        MvcResult result1 = mockMvc.perform(post("/api/v1/notifications")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isCreated())
                .andReturn();

        String notifId1 = objectMapper.readTree(result1.getResponse().getContentAsString())
                .path("data").path("id").asText();

        CreateNotificationRequest req2 = CreateNotificationRequest.builder()
                .type(NotificationType.SYSTEM)
                .title("App Update")
                .body("Version 2.0 is now live.")
                .sendPush(false)
                .build();

        mockMvc.perform(post("/api/v1/notifications")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isCreated());

        // 2. Check unread count (should be 2)
        mockMvc.perform(get("/api/v1/notifications/unread-count")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.unreadCount").value(2));

        // 3. Mark first notification as read via POST
        mockMvc.perform(post("/api/v1/notifications/" + notifId1 + "/read")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.read").value(true))
                .andExpect(jsonPath("$.data.readAt").isNotEmpty());

        // 4. Check unread count (now 1)
        mockMvc.perform(get("/api/v1/notifications/unread-count")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.unreadCount").value(1));

        // 5. Query unreadOnly = true (only second one returned)
        mockMvc.perform(get("/api/v1/notifications")
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("unreadOnly", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("App Update"));

        // 6. Mark all as read via PATCH
        mockMvc.perform(patch("/api/v1/notifications/read-all")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());

        // 7. Check unread count (now 0)
        mockMvc.perform(get("/api/v1/notifications/unread-count")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.unreadCount").value(0));
    }

    @Test
    @DisplayName("Should soft delete notification and handle 404 for deleted items")
    void testSoftDeleteNotification() throws Exception {
        // 1. Create notification
        CreateNotificationRequest req = CreateNotificationRequest.builder()
                .type(NotificationType.TRIP)
                .title("Trip to Paris")
                .body("Flight departs tomorrow at 6 AM")
                .sendPush(false)
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/notifications")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        String notifId = objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("id").asText();

        // 2. Delete notification
        mockMvc.perform(delete("/api/v1/notifications/" + notifId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Notification deleted successfully"));

        // 3. Trying to mark deleted notification as read returns 404
        mockMvc.perform(post("/api/v1/notifications/" + notifId + "/read")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());

        // 4. Feed does not contain deleted notification
        mockMvc.perform(get("/api/v1/notifications")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("Should enforce multi-tenant isolation across users")
    void testMultiTenantIsolation() throws Exception {
        // User A creates notification
        CreateNotificationRequest req = CreateNotificationRequest.builder()
                .type(NotificationType.REMINDER)
                .title("Secret User A Alert")
                .body("Confidential notification")
                .sendPush(false)
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/notifications")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        String notifId = objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("id").asText();

        // Setup User B
        String uidB = "user_b_" + UUID.randomUUID();
        String emailB = "user_b_" + UUID.randomUUID() + "@livo.test";
        var authB = authService.syncUser(UserSyncRequest.builder()
                .firebaseUid(uidB)
                .email(emailB)
                .fullName("User B")
                .timezone("UTC")
                .build());
        String tokenB = authB.getAccessToken();

        // User B cannot see User A's notification
        mockMvc.perform(get("/api/v1/notifications")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));

        // User B cannot mark User A's notification as read (returns 404)
        mockMvc.perform(post("/api/v1/notifications/" + notifId + "/read")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());

        // User B cannot delete User A's notification (returns 404)
        mockMvc.perform(delete("/api/v1/notifications/" + notifId)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should execute task reminder and daily briefing schedulers without error")
    void testSchedulersExecution() throws Exception {
        jdbcTemplate.execute("UPDATE tasks SET due_time = date_trunc('second', due_time) WHERE due_time IS NOT NULL");

        // Create a task due today within 20 minutes
        LocalTime dueTime = LocalTime.now().plusMinutes(20).withNano(0);
        taskService.createTask(userId, CreateTaskRequest.builder()
                .title("Review Pull Request")
                .dueDate(LocalDate.now())
                .dueTime(dueTime)
                .build());

        // Trigger TaskReminderScheduler
        taskReminderScheduler.checkUpcomingTaskDeadlines();

        // Trigger DailyBriefingScheduler morning & evening
        dailyBriefingScheduler.sendMorningBriefings();
        dailyBriefingScheduler.sendEveningReviews();

        // Verify that notification entries were created for the user
        var notifications = notificationRepository.findAllByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(userId);
        assertThat(notifications).isNotEmpty();
        assertThat(notifications.stream().anyMatch(n -> n.getTitle().contains("Review Pull Request") || n.getTitle().contains("Good morning"))).isTrue();
    }

    @Test
    @DisplayName("Should test FCM push notification dispatch in fallback simulation mode")
    void testPushNotificationWithDeviceToken() throws Exception {
        // Register an active device token for user
        userDeviceTokenRepository.save(UserDeviceTokenEntity.builder()
                .userId(userId)
                .deviceToken("fcm_test_token_" + UUID.randomUUID())
                .platform(DevicePlatform.ANDROID)
                .isActive(true)
                .build());

        // Create notification with sendPush = true
        CreateNotificationRequest pushReq = CreateNotificationRequest.builder()
                .type(NotificationType.OVERDUE)
                .title("Urgent Task Overdue")
                .body("Please check overdue tasks immediately")
                .sendPush(true)
                .build();

        mockMvc.perform(post("/api/v1/notifications")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pushReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.type").value("OVERDUE"));
    }

    @Test
    @DisplayName("Should correctly evaluate quiet hours boundary conditions")
    void testIsQuietHoursBoundaries() {
        LocalTime quietStart = LocalTime.of(22, 0);
        LocalTime quietEnd = LocalTime.of(7, 0);

        // Overnight window: [22:00, 07:00)
        assertThat(PushNotificationService.isQuietHours(LocalTime.of(21, 59, 59), quietStart, quietEnd)).isFalse();
        assertThat(PushNotificationService.isQuietHours(LocalTime.of(22, 0, 0), quietStart, quietEnd)).isTrue();
        assertThat(PushNotificationService.isQuietHours(LocalTime.of(23, 30, 0), quietStart, quietEnd)).isTrue();
        assertThat(PushNotificationService.isQuietHours(LocalTime.of(0, 0, 0), quietStart, quietEnd)).isTrue();
        assertThat(PushNotificationService.isQuietHours(LocalTime.of(3, 0, 0), quietStart, quietEnd)).isTrue();
        assertThat(PushNotificationService.isQuietHours(LocalTime.of(6, 59, 59), quietStart, quietEnd)).isTrue();
        assertThat(PushNotificationService.isQuietHours(LocalTime.of(7, 0, 0), quietStart, quietEnd)).isFalse();
        assertThat(PushNotificationService.isQuietHours(LocalTime.of(12, 0, 0), quietStart, quietEnd)).isFalse();

        // Same-day window: [13:00, 15:00)
        LocalTime sameDayStart = LocalTime.of(13, 0);
        LocalTime sameDayEnd = LocalTime.of(15, 0);
        assertThat(PushNotificationService.isQuietHours(LocalTime.of(12, 59, 59), sameDayStart, sameDayEnd)).isFalse();
        assertThat(PushNotificationService.isQuietHours(LocalTime.of(13, 0, 0), sameDayStart, sameDayEnd)).isTrue();
        assertThat(PushNotificationService.isQuietHours(LocalTime.of(14, 0, 0), sameDayStart, sameDayEnd)).isTrue();
        assertThat(PushNotificationService.isQuietHours(LocalTime.of(14, 59, 59), sameDayStart, sameDayEnd)).isTrue();
        assertThat(PushNotificationService.isQuietHours(LocalTime.of(15, 0, 0), sameDayStart, sameDayEnd)).isFalse();

        // Equal boundaries or nulls
        assertThat(PushNotificationService.isQuietHours(LocalTime.of(12, 0), LocalTime.of(0, 0), LocalTime.of(0, 0))).isFalse();
        assertThat(PushNotificationService.isQuietHours(null, quietStart, quietEnd)).isFalse();
        assertThat(PushNotificationService.isQuietHours(LocalTime.of(12, 0), null, quietEnd)).isFalse();
        assertThat(PushNotificationService.isQuietHours(LocalTime.of(12, 0), quietStart, null)).isFalse();
    }

    @Test
    @DisplayName("Should suppress push notification when notifications are disabled in user preferences")
    void testPushSuppressedWhenNotificationsDisabled() {
        // Disable notifications for user
        UserPreferenceEntity pref = userPreferenceRepository.findByUserId(userId)
                .orElseGet(() -> UserPreferenceEntity.builder().userId(userId).build());
        pref.setNotificationsEnabled(false);
        userPreferenceRepository.save(pref);

        // Register active device token
        userDeviceTokenRepository.save(UserDeviceTokenEntity.builder()
                .userId(userId)
                .deviceToken("token_disabled_test_" + UUID.randomUUID())
                .platform(DevicePlatform.ANDROID)
                .isActive(true)
                .build());

        // Evaluation must be false
        assertThat(pushNotificationService.isPushAllowed(userId)).isFalse();

        // Dispatch must return false (suppressed)
        boolean dispatched = pushNotificationService.dispatchPush(userId, "Test Title", "Test Body", Map.of(), Instant.now());
        assertThat(dispatched).isFalse();
    }

    @Test
    @DisplayName("Should suppress push notification when current local time falls within quiet hours")
    void testPushSuppressedDuringQuietHoursInUserTimezone() {
        // Ensure notifications enabled, quiet hours 22:00 to 07:00
        UserPreferenceEntity pref = userPreferenceRepository.findByUserId(userId)
                .orElseGet(() -> UserPreferenceEntity.builder().userId(userId).build());
        pref.setNotificationsEnabled(true);
        pref.setNotificationQuietStart(LocalTime.of(22, 0));
        pref.setNotificationQuietEnd(LocalTime.of(7, 0));
        userPreferenceRepository.save(pref);

        userDeviceTokenRepository.save(UserDeviceTokenEntity.builder()
                .userId(userId)
                .deviceToken("token_quiet_test_" + UUID.randomUUID())
                .platform(DevicePlatform.ANDROID)
                .isActive(true)
                .build());

        // Instant at 23:30 Asia/Kolkata (falls in quiet hours)
        Instant quietInstant = ZonedDateTime.of(2026, 9, 23, 23, 30, 0, 0, ZoneId.of("Asia/Kolkata")).toInstant();

        assertThat(pushNotificationService.isPushAllowed(userId, quietInstant)).isFalse();
        boolean dispatched = pushNotificationService.dispatchPush(userId, "Late Night Alert", "Should be muted", Map.of(), quietInstant);
        assertThat(dispatched).isFalse();
    }

    @Test
    @DisplayName("Should allow push notification when local time is outside quiet hours and notifications are enabled")
    void testPushAllowedOutsideQuietHours() {
        UserPreferenceEntity pref = userPreferenceRepository.findByUserId(userId)
                .orElseGet(() -> UserPreferenceEntity.builder().userId(userId).build());
        pref.setNotificationsEnabled(true);
        pref.setNotificationQuietStart(LocalTime.of(22, 0));
        pref.setNotificationQuietEnd(LocalTime.of(7, 0));
        userPreferenceRepository.save(pref);

        userDeviceTokenRepository.save(UserDeviceTokenEntity.builder()
                .userId(userId)
                .deviceToken("token_active_test_" + UUID.randomUUID())
                .platform(DevicePlatform.ANDROID)
                .isActive(true)
                .build());

        // Instant at 14:00 Asia/Kolkata (outside quiet hours)
        Instant daytimeInstant = ZonedDateTime.of(2026, 9, 23, 14, 0, 0, 0, ZoneId.of("Asia/Kolkata")).toInstant();

        assertThat(pushNotificationService.isPushAllowed(userId, daytimeInstant)).isTrue();
        boolean dispatched = pushNotificationService.dispatchPush(userId, "Afternoon Alert", "Stay focused", Map.of(), daytimeInstant);
        assertThat(dispatched).isTrue();
    }

    @Test
    @DisplayName("Should correctly evaluate quiet hours according to user specific timezone")
    void testTimezoneAwareQuietHours() {
        // User NY in America/New_York
        String uidNY = "ny_user_" + UUID.randomUUID();
        String emailNY = "ny_" + UUID.randomUUID() + "@livo.test";
        var authNY = authService.syncUser(UserSyncRequest.builder()
                .firebaseUid(uidNY)
                .email(emailNY)
                .fullName("NY User")
                .timezone("America/New_York")
                .build());
        UUID userNYId = authNY.getUser().getId();

        // 03:00 UTC = 23:00 EDT (11:00 PM previous day in New York) -> Quiet hours [22:00, 07:00]
        Instant utcInstant = ZonedDateTime.of(2026, 9, 23, 3, 0, 0, 0, ZoneId.of("UTC")).toInstant();

        // NY user: 23:00 local time -> suppressed
        assertThat(pushNotificationService.isPushAllowed(userNYId, utcInstant)).isFalse();

        // Indian user: 03:00 UTC = 08:30 AM IST (Asia/Kolkata) -> allowed
        assertThat(pushNotificationService.isPushAllowed(userId, utcInstant)).isTrue();
    }

    @Test
    @DisplayName("Should retain in-app database notification even when push is muted by quiet hours")
    void testInAppNotificationRetainedWhenPushMuted() throws Exception {
        // Set quiet hours spanning current time
        UserPreferenceEntity pref = userPreferenceRepository.findByUserId(userId)
                .orElseGet(() -> UserPreferenceEntity.builder().userId(userId).build());
        pref.setNotificationsEnabled(false); // completely disabled
        userPreferenceRepository.save(pref);

        // POST /api/v1/notifications with sendPush = true
        CreateNotificationRequest req = CreateNotificationRequest.builder()
                .type(NotificationType.SYSTEM)
                .title("Muted Push Alert")
                .body("Notification body saved in database")
                .sendPush(true)
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/notifications")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("Muted Push Alert"))
                .andReturn();

        // Query notifications to ensure it is in the database and visible in inbox
        mockMvc.perform(get("/api/v1/notifications")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.title == 'Muted Push Alert')]").exists());
    }

    @Test
    @DisplayName("TaskReminderScheduler: Should dispatch reminders matching user timezone local window")
    void testTaskReminderSchedulerTimezoneAware() {
        // User in America/New_York
        String uidNY = "ny_remind_" + UUID.randomUUID();
        String emailNY = "ny_remind_" + UUID.randomUUID() + "@livo.test";
        var authNY = authService.syncUser(UserSyncRequest.builder()
                .firebaseUid(uidNY)
                .email(emailNY)
                .fullName("NY Reminder User")
                .timezone("America/New_York")
                .build());
        UUID userNYId = authNY.getUser().getId();

        LocalDate nyToday = LocalDate.now(ZoneId.of("America/New_York"));
        LocalTime taskTime = LocalTime.of(14, 0); // 2:00 PM in New York

        taskService.createTask(userNYId, CreateTaskRequest.builder()
                .title("NY Client Meeting")
                .dueDate(nyToday)
                .dueTime(taskTime)
                .build());

        // Instant at 13:45 New York local time (task is due in 15 minutes, well within 30 min window)
        Instant ny1345 = ZonedDateTime.of(nyToday, LocalTime.of(13, 45), ZoneId.of("America/New_York")).toInstant();

        taskReminderScheduler.checkUpcomingTaskDeadlines(ny1345);

        var notifications = notificationRepository.findAllByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(userNYId);
        assertThat(notifications).isNotEmpty();
        assertThat(notifications.stream().anyMatch(n -> n.getTitle().contains("NY Client Meeting"))).isTrue();
    }

    @Test
    @DisplayName("MidnightRolloverScheduler: Should only rollover users whose local time is midnight hour")
    void testMidnightRolloverSchedulerTimezoneAware() {
        // User in America/New_York
        String uidNY = "ny_roll_" + UUID.randomUUID();
        String emailNY = "ny_roll_" + UUID.randomUUID() + "@livo.test";
        var authNY = authService.syncUser(UserSyncRequest.builder()
                .firebaseUid(uidNY)
                .email(emailNY)
                .fullName("NY Rollover User")
                .timezone("America/New_York")
                .build());
        UUID userNYId = authNY.getUser().getId();

        // Create overdue tasks for Indian user and NY user
        LocalDate istYesterday = LocalDate.now(ZoneId.of("Asia/Kolkata")).minusDays(1);
        taskService.createTask(userId, CreateTaskRequest.builder()
                .title("Indian Overdue Task")
                .dueDate(istYesterday)
                .build());

        LocalDate nyYesterday = LocalDate.now(ZoneId.of("America/New_York")).minusDays(1);
        taskService.createTask(userNYId, CreateTaskRequest.builder()
                .title("NY Overdue Task")
                .dueDate(nyYesterday)
                .build());

        // Reference instant when local time is 00:15 in India (18:45 UTC previous day)
        // In New York, this instant corresponds to 14:45 (2:45 PM), NOT midnight!
        Instant indianMidnight = ZonedDateTime.of(LocalDate.now(ZoneId.of("Asia/Kolkata")), LocalTime.of(0, 15), ZoneId.of("Asia/Kolkata")).toInstant();

        midnightRolloverScheduler.executeTimezoneAwareRollover(indianMidnight);

        // Indian user MUST be rolled over and notified
        var istNotifs = notificationRepository.findAllByUserIdAndTypeAndDeletedAtIsNullOrderByCreatedAtDesc(userId, NotificationType.OVERDUE);
        assertThat(istNotifs).isNotEmpty();
        assertThat(istNotifs.get(0).getTitle()).isEqualTo("Overdue Tasks Reminder");

        // NY user MUST NOT be rolled over at 2:45 PM
        var nyNotifs = notificationRepository.findAllByUserIdAndTypeAndDeletedAtIsNullOrderByCreatedAtDesc(userNYId, NotificationType.OVERDUE);
        assertThat(nyNotifs).isEmpty();
    }

    @Test
    @DisplayName("DailyBriefingScheduler: Should dispatch morning briefing at 8:00 AM in user timezone")
    void testDailyBriefingSchedulerTimezoneAware() {
        // User in Europe/London
        String uidLondon = "london_brief_" + UUID.randomUUID();
        String emailLondon = "london_brief_" + UUID.randomUUID() + "@livo.test";
        var authLondon = authService.syncUser(UserSyncRequest.builder()
                .firebaseUid(uidLondon)
                .email(emailLondon)
                .fullName("London Brief User")
                .timezone("Europe/London")
                .build());
        UUID londonUserId = authLondon.getUser().getId();

        LocalDate londonToday = LocalDate.now(ZoneId.of("Europe/London"));
        taskService.createTask(londonUserId, CreateTaskRequest.builder()
                .title("London Morning Standup")
                .dueDate(londonToday)
                .build());

        // Create reference instant at 08:15 AM London local time
        Instant londonEightAm = ZonedDateTime.of(londonToday, LocalTime.of(8, 15), ZoneId.of("Europe/London")).toInstant();

        dailyBriefingScheduler.dispatchBriefingsForInstant(londonEightAm);

        // London user MUST receive morning briefing
        var londonNotifs = notificationRepository.findAllByUserIdAndTypeAndDeletedAtIsNullOrderByCreatedAtDesc(londonUserId, NotificationType.SYSTEM);
        assertThat(londonNotifs).isNotEmpty();
        assertThat(londonNotifs.get(0).getTitle()).contains("Good morning");
    }
}
