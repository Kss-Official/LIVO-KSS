package com.livo.api.modules.attachment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.livo.api.modules.attachment.dto.RegisterAttachmentRequest;
import com.livo.api.modules.attachment.entity.enums.AttachmentEntityType;
import com.livo.api.modules.attachment.repository.AttachmentRepository;
import com.livo.api.modules.auth.dto.UserSyncRequest;
import com.livo.api.modules.auth.service.AuthService;
import com.livo.api.modules.goal.dto.CreateGoalRequest;
import com.livo.api.modules.goal.service.GoalService;
import com.livo.api.modules.task.dto.CreateTaskRequest;
import com.livo.api.modules.task.service.TaskService;
import com.livo.api.modules.trip.dto.CreateTripRequest;
import com.livo.api.modules.trip.service.TripService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AttachmentManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private GoalService goalService;

    @Autowired
    private TripService tripService;

    @Autowired
    private AttachmentRepository attachmentRepository;

    private static final byte[] VALID_PDF_BYTES = "%PDF-1.4 sample pdf content".getBytes(java.nio.charset.StandardCharsets.UTF_8);
    private static final byte[] VALID_PNG_BYTES = new byte[]{(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n', 0, 0, 0, 13};
    private static final byte[] VALID_JPEG_BYTES = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 16, 'J', 'F', 'I', 'F'};
    private static final byte[] VALID_WEBP_BYTES = new byte[]{'R', 'I', 'F', 'F', 32, 0, 0, 0, 'W', 'E', 'B', 'P', 'V', 'P', '8'};

    private UUID userId;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        String uid = "attachment_user_" + UUID.randomUUID();
        String email = "attach_" + UUID.randomUUID() + "@livo.test";

        var authResponse = authService.syncUser(UserSyncRequest.builder()
                .firebaseUid(uid)
                .email(email)
                .fullName("Media Manager")
                .timezone("Asia/Kolkata")
                .build());

        this.userId = authResponse.getUser().getId();
        this.jwtToken = authResponse.getAccessToken();
    }

    @Test
    @DisplayName("Should upload media files and query by entity")
    void testUploadAndQueryMultipartAttachments() throws Exception {
        // 1. Create Task
        var task = taskService.createTask(userId, CreateTaskRequest.builder()
                .title("Submit Tax Returns")
                .build());

        // 2. Create Trip
        var trip = tripService.createTrip(userId, CreateTripRequest.builder()
                .title("Conference Flight")
                .destination("Mumbai")
                .startDate(LocalDate.now().plusDays(10))
                .endDate(LocalDate.now().plusDays(12))
                .build());

        // 3. Upload PDF Receipt to Task
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file",
                "tax_statement.pdf",
                "application/pdf",
                VALID_PDF_BYTES
        );

        mockMvc.perform(multipart("/api/v1/attachments/upload")
                        .file(pdfFile)
                        .param("entityType", "TASK")
                        .param("entityId", task.getId().toString())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fileName").value("tax_statement.pdf"))
                .andExpect(jsonPath("$.data.mimeType").value("application/pdf"))
                .andExpect(jsonPath("$.data.entityType").value("TASK"))
                .andExpect(jsonPath("$.data.entityId").value(task.getId().toString()));

        // 4. Upload PNG Ticket to Trip
        MockMultipartFile pngFile = new MockMultipartFile(
                "file",
                "boarding_pass.png",
                "image/png",
                VALID_PNG_BYTES
        );

        mockMvc.perform(multipart("/api/v1/attachments/upload")
                        .file(pngFile)
                        .param("entityType", "TRIP")
                        .param("entityId", trip.getId().toString())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.fileName").value("boarding_pass.png"))
                .andExpect(jsonPath("$.data.mimeType").value("image/png"));

        // 5. Query all attachments
        mockMvc.perform(get("/api/v1/attachments")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));

        // 6. Query attachments for specific task
        mockMvc.perform(get("/api/v1/attachments/entity/TASK/" + task.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].fileName").value("tax_statement.pdf"));
    }

    @Test
    @DisplayName("Should register pre-uploaded CDN media metadata")
    void testRegisterPreUploadedAttachment() throws Exception {
        var goal = goalService.createGoal(userId, CreateGoalRequest.builder()
                .title("Build Investment Portfolio")
                .relatedArea("FINANCE")
                .build());

        RegisterAttachmentRequest request = RegisterAttachmentRequest.builder()
                .entityType(AttachmentEntityType.GOAL)
                .entityId(goal.getId())
                .storageKey("livo/goals/portfolio_breakdown.pdf")
                .fileUrl("https://res.cloudinary.com/livo/image/upload/v1/portfolio_breakdown.pdf")
                .fileName("portfolio_breakdown.pdf")
                .mimeType("application/pdf")
                .fileSizeBytes(524288L) // 512 KB
                .build();

        mockMvc.perform(post("/api/v1/attachments")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fileName").value("portfolio_breakdown.pdf"))
                .andExpect(jsonPath("$.data.formattedSize").value("512.0 KB"))
                .andExpect(jsonPath("$.data.entityType").value("GOAL"));
    }

    @Test
    @DisplayName("Should delete attachment and return 404 afterwards")
    void testDeleteAttachmentAnd404() throws Exception {
        var task = taskService.createTask(userId, CreateTaskRequest.builder()
                .title("Task with Attachment")
                .build());

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "invoice.webp",
                "image/webp",
                VALID_WEBP_BYTES
        );

        MvcResult uploadResult = mockMvc.perform(multipart("/api/v1/attachments/upload")
                        .file(file)
                        .param("entityType", "TASK")
                        .param("entityId", task.getId().toString())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isCreated())
                .andReturn();

        UUID attachmentId = UUID.fromString(objectMapper.readTree(uploadResult.getResponse().getContentAsString())
                .path("data").path("id").asText());

        // Delete attachment
        mockMvc.perform(delete("/api/v1/attachments/" + attachmentId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());

        // Fetch -> 404
        mockMvc.perform(get("/api/v1/attachments/" + attachmentId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());

        // Verify soft-delete in DB
        assertThat(attachmentRepository.findByIdAndUserIdAndDeletedAtIsNull(attachmentId, userId)).isEmpty();
    }

    @Test
    @DisplayName("Should validate MIME types, magic bytes, file sizes, and HTTPS URLs")
    void testValidationMimeTypeAndSize() throws Exception {
        var task = taskService.createTask(userId, CreateTaskRequest.builder()
                .title("Validation Task")
                .build());

        // 1. Unsupported MIME type (.txt / text/plain)
        MockMultipartFile txtFile = new MockMultipartFile(
                "file",
                "notes.txt",
                "text/plain",
                "Some plain text".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/attachments/upload")
                        .file(txtFile)
                        .param("entityType", "TASK")
                        .param("entityId", task.getId().toString())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isBadRequest());

        // 2. Spoofed MIME type: declared image/png, but body is bash script
        MockMultipartFile spoofedFile = new MockMultipartFile(
                "file",
                "malicious.png",
                "image/png",
                "#!/bin/bash\necho hacked".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/attachments/upload")
                        .file(spoofedFile)
                        .param("entityType", "TASK")
                        .param("entityId", task.getId().toString())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("File content signature does not match")));

        // 3. Mismatched MIME type: declared image/png, but body is valid PDF
        MockMultipartFile mismatchedFile = new MockMultipartFile(
                "file",
                "disguised.png",
                "image/png",
                VALID_PDF_BYTES
        );

        mockMvc.perform(multipart("/api/v1/attachments/upload")
                        .file(mismatchedFile)
                        .param("entityType", "TASK")
                        .param("entityId", task.getId().toString())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("MIME type mismatch")));

        // 4. Valid JPEG upload with proper magic bytes
        MockMultipartFile validJpeg = new MockMultipartFile(
                "file",
                "avatar.jpg",
                "image/jpeg",
                VALID_JPEG_BYTES
        );

        mockMvc.perform(multipart("/api/v1/attachments/upload")
                        .file(validJpeg)
                        .param("entityType", "TASK")
                        .param("entityId", task.getId().toString())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.mimeType").value("image/jpeg"));

        // 5. Empty file
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.png",
                "image/png",
                new byte[0]
        );

        mockMvc.perform(multipart("/api/v1/attachments/upload")
                        .file(emptyFile)
                        .param("entityType", "TASK")
                        .param("entityId", task.getId().toString())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isBadRequest());

        // 6. Register non-HTTPS URL
        RegisterAttachmentRequest insecureUrl = RegisterAttachmentRequest.builder()
                .entityType(AttachmentEntityType.TASK)
                .entityId(task.getId())
                .storageKey("insecure_key")
                .fileUrl("http://insecure-cdn.com/file.png")
                .fileName("file.png")
                .mimeType("image/png")
                .fileSizeBytes(1024L)
                .build();

        mockMvc.perform(post("/api/v1/attachments")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(insecureUrl)))
                .andExpect(status().isBadRequest());

        // 7. Register non-Cloudinary HTTPS URL (untrusted external domain)
        RegisterAttachmentRequest untrustedHostUrl = RegisterAttachmentRequest.builder()
                .entityType(AttachmentEntityType.TASK)
                .entityId(task.getId())
                .storageKey("external_key")
                .fileUrl("https://untrusted-third-party.com/file.png")
                .fileName("file.png")
                .mimeType("image/png")
                .fileSizeBytes(1024L)
                .build();

        mockMvc.perform(post("/api/v1/attachments")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(untrustedHostUrl)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Only Cloudinary CDN")));
    }

    @Test
    @DisplayName("Should enforce polymorphic entity verification and cross-user isolation")
    void testPolymorphicOwnershipAndUserIsolation() throws Exception {
        // 1. Non-existent entity attachment attempt
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "doc.pdf",
                "application/pdf",
                VALID_PDF_BYTES
        );

        mockMvc.perform(multipart("/api/v1/attachments/upload")
                        .file(file)
                        .param("entityType", "TASK")
                        .param("entityId", UUID.randomUUID().toString())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());

        // 2. Cross-user isolation
        var user1Task = taskService.createTask(userId, CreateTaskRequest.builder()
                .title("User 1 Secret Task")
                .build());

        MvcResult uploadResult = mockMvc.perform(multipart("/api/v1/attachments/upload")
                        .file(file)
                        .param("entityType", "TASK")
                        .param("entityId", user1Task.getId().toString())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isCreated())
                .andReturn();

        UUID attachmentId = UUID.fromString(objectMapper.readTree(uploadResult.getResponse().getContentAsString())
                .path("data").path("id").asText());

        // Create second user
        String otherUid = "other_media_user_" + UUID.randomUUID();
        var otherAuth = authService.syncUser(UserSyncRequest.builder()
                .firebaseUid(otherUid)
                .email("other_media_" + UUID.randomUUID() + "@livo.test")
                .fullName("Other Media User")
                .build());
        String otherToken = otherAuth.getAccessToken();

        // User 2 cannot access User 1's attachment
        mockMvc.perform(get("/api/v1/attachments/" + attachmentId)
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isNotFound());

        // User 2 cannot delete User 1's attachment
        mockMvc.perform(delete("/api/v1/attachments/" + attachmentId)
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isNotFound());

        // User 2 cannot upload an attachment linked to User 1's task
        mockMvc.perform(multipart("/api/v1/attachments/upload")
                        .file(file)
                        .param("entityType", "TASK")
                        .param("entityId", user1Task.getId().toString())
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isNotFound());
    }
}
