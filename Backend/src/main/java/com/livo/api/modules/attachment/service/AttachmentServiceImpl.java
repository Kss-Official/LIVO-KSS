package com.livo.api.modules.attachment.service;

import com.livo.api.common.exception.BadRequestException;
import com.livo.api.common.exception.ResourceNotFoundException;
import com.livo.api.common.util.CloudinaryMediaService;
import com.livo.api.modules.attachment.dto.AttachmentResponse;
import com.livo.api.modules.attachment.dto.RegisterAttachmentRequest;
import com.livo.api.modules.attachment.entity.AttachmentEntity;
import com.livo.api.modules.attachment.entity.enums.AttachmentEntityType;
import com.livo.api.modules.attachment.repository.AttachmentRepository;
import com.livo.api.modules.event.repository.EventRepository;
import com.livo.api.modules.finance.repository.TransactionRepository;
import com.livo.api.modules.goal.repository.GoalRepository;
import com.livo.api.modules.health.repository.HealthEntryRepository;
import com.livo.api.modules.task.repository.TaskRepository;
import com.livo.api.modules.trip.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttachmentServiceImpl implements AttachmentService {

    private static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024; // 10 MB
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "application/pdf"
    );

    private final AttachmentRepository attachmentRepository;
    private final TaskRepository taskRepository;
    private final GoalRepository goalRepository;
    private final EventRepository eventRepository;
    private final TripRepository tripRepository;
    private final TransactionRepository transactionRepository;
    private final HealthEntryRepository healthEntryRepository;
    private final CloudinaryMediaService cloudinaryMediaService;

    @Override
    @Transactional
    public AttachmentResponse uploadAttachment(UUID userId, MultipartFile file, AttachmentEntityType entityType, UUID entityId) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Uploaded file cannot be empty");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new BadRequestException("File size exceeds 10MB limit: " + file.getSize() + " bytes");
        }

        byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (IOException e) {
            throw new BadRequestException("Failed to read uploaded file: " + e.getMessage());
        }

        String detectedMimeType = detectMimeType(fileBytes);
        if (detectedMimeType == null || !ALLOWED_MIME_TYPES.contains(detectedMimeType)) {
            throw new BadRequestException("File content signature does not match any allowed format (JPEG, PNG, WEBP, PDF)");
        }

        String declaredContentType = file.getContentType();
        if (declaredContentType != null && !declaredContentType.isBlank()) {
            String normalizedDeclared = declaredContentType.toLowerCase().trim();
            if (normalizedDeclared.equals("image/jpg")) {
                normalizedDeclared = "image/jpeg";
            }
            if (!normalizedDeclared.equals(detectedMimeType)) {
                throw new BadRequestException(String.format(
                        "MIME type mismatch: declared '%s' but file content signature is '%s'",
                        declaredContentType, detectedMimeType
                ));
            }
        }

        verifyEntityOwnership(userId, entityType, entityId);

        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
        String folder = "livo/" + userId + "/" + entityType.name().toLowerCase();

        try {
            CloudinaryMediaService.UploadResult uploadResult = cloudinaryMediaService.upload(
                    fileBytes,
                    originalFilename,
                    detectedMimeType,
                    folder
            );

            AttachmentEntity entity = AttachmentEntity.builder()
                    .entityType(entityType)
                    .entityId(entityId)
                    .storageKey(uploadResult.getStorageKey())
                    .fileUrl(uploadResult.getFileUrl())
                    .fileName(originalFilename)
                    .mimeType(detectedMimeType)
                    .fileSizeBytes(uploadResult.getBytes())
                    .build();

            entity.setUserId(userId);
            entity.setVersion(1L);

            AttachmentEntity saved = attachmentRepository.save(entity);
            log.info("Uploaded and registered attachment {} for {} {} by user {}",
                    saved.getId(), entityType, entityId, userId);
            return AttachmentResponse.fromEntity(saved);
        } catch (Exception e) {
            log.error("Failed to process file upload for user {}: {}", userId, e.getMessage(), e);
            throw new BadRequestException("Failed to upload media file: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public AttachmentResponse registerAttachment(UUID userId, RegisterAttachmentRequest request) {
        verifyEntityOwnership(userId, request.getEntityType(), request.getEntityId());

        if (!ALLOWED_MIME_TYPES.contains(request.getMimeType().toLowerCase())) {
            throw new BadRequestException("Unsupported MIME type: " + request.getMimeType());
        }

        if (!request.getFileUrl().startsWith("https://")) {
            throw new BadRequestException("File URL must use HTTPS protocol: " + request.getFileUrl());
        }

        try {
            URI uri = URI.create(request.getFileUrl().trim());
            String host = uri.getHost();
            if (host == null || (!host.equalsIgnoreCase("res.cloudinary.com") && !host.toLowerCase().endsWith(".cloudinary.com"))) {
                throw new BadRequestException("File URL domain is not permitted. Only Cloudinary CDN (res.cloudinary.com) URLs are allowed: " + request.getFileUrl());
            }
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid file URL: " + request.getFileUrl());
        }

        if (request.getFileSizeBytes() <= 0 || request.getFileSizeBytes() > MAX_FILE_SIZE_BYTES) {
            throw new BadRequestException("Invalid file size: " + request.getFileSizeBytes() + " bytes");
        }

        AttachmentEntity entity = AttachmentEntity.builder()
                .entityType(request.getEntityType())
                .entityId(request.getEntityId())
                .storageKey(request.getStorageKey().trim())
                .fileUrl(request.getFileUrl().trim())
                .fileName(request.getFileName().trim())
                .mimeType(request.getMimeType().trim().toLowerCase())
                .fileSizeBytes(request.getFileSizeBytes())
                .build();

        entity.setUserId(userId);
        entity.setVersion(1L);

        AttachmentEntity saved = attachmentRepository.save(entity);
        log.info("Registered external attachment {} for {} {} by user {}",
                saved.getId(), request.getEntityType(), request.getEntityId(), userId);
        return AttachmentResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttachmentResponse> getAttachments(UUID userId, AttachmentEntityType entityType, UUID entityId) {
        List<AttachmentEntity> attachments;

        if (entityType != null && entityId != null) {
            attachments = attachmentRepository.findAllByUserIdAndEntityTypeAndEntityIdAndDeletedAtIsNull(userId, entityType, entityId);
        } else if (entityType != null) {
            attachments = attachmentRepository.findAllByUserIdAndEntityTypeAndDeletedAtIsNull(userId, entityType);
        } else {
            attachments = attachmentRepository.findAllByUserIdAndDeletedAtIsNull(userId);
        }

        return attachments.stream()
                .map(AttachmentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AttachmentResponse getAttachment(UUID userId, UUID id) {
        AttachmentEntity entity = attachmentRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found: " + id));
        return AttachmentResponse.fromEntity(entity);
    }

    @Override
    @Transactional
    public void deleteAttachment(UUID userId, UUID id) {
        AttachmentEntity entity = attachmentRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found: " + id));

        entity.setDeletedAt(Instant.now());
        entity.setVersion(entity.getVersion() != null ? entity.getVersion() + 1 : 1L);
        attachmentRepository.save(entity);

        // Clean up Cloudinary storage if configured
        cloudinaryMediaService.delete(entity.getStorageKey());
        log.info("Soft-deleted attachment {} for user {}", id, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttachmentResponse> getAttachmentsForEntity(UUID userId, AttachmentEntityType entityType, UUID entityId) {
        verifyEntityOwnership(userId, entityType, entityId);
        return attachmentRepository.findAllByUserIdAndEntityTypeAndEntityIdAndDeletedAtIsNull(userId, entityType, entityId)
                .stream()
                .map(AttachmentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    private void verifyEntityOwnership(UUID userId, AttachmentEntityType entityType, UUID entityId) {
        switch (entityType) {
            case TASK -> taskRepository.findByIdAndUserIdAndDeletedAtIsNull(entityId, userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + entityId));
            case GOAL -> goalRepository.findByIdAndUserIdAndDeletedAtIsNull(entityId, userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Goal not found: " + entityId));
            case EVENT -> eventRepository.findByIdAndUserIdAndDeletedAtIsNull(entityId, userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + entityId));
            case TRIP -> tripRepository.findByIdAndUserIdAndDeletedAtIsNull(entityId, userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Trip not found: " + entityId));
            case EXPENSE -> transactionRepository.findByIdAndUserIdAndDeletedAtIsNull(entityId, userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Expense/Transaction not found: " + entityId));
            case HEALTH -> healthEntryRepository.findByIdAndUserIdAndDeletedAtIsNull(entityId, userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Health entry not found: " + entityId));
        }
    }

    public static String detectMimeType(byte[] bytes) {
        if (bytes == null || bytes.length < 3) {
            return null;
        }

        // JPEG: FF D8 FF
        if ((bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8 && (bytes[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }

        // PNG: 89 50 4E 47 0D 0A 1A 0A
        if (bytes.length >= 8
                && (bytes[0] & 0xFF) == 0x89
                && bytes[1] == 0x50 // 'P'
                && bytes[2] == 0x4E // 'N'
                && bytes[3] == 0x47 // 'G'
                && bytes[4] == 0x0D // '\r'
                && bytes[5] == 0x0A // '\n'
                && bytes[6] == 0x1A
                && bytes[7] == 0x0A) {
            return "image/png";
        }

        // PDF: %PDF- (25 50 44 46 2D)
        if (bytes.length >= 5
                && bytes[0] == 0x25 // '%'
                && bytes[1] == 0x50 // 'P'
                && bytes[2] == 0x44 // 'D'
                && bytes[3] == 0x46 // 'F'
                && bytes[4] == 0x2D) { // '-'
            return "application/pdf";
        }

        // WEBP: RIFF....WEBP (52 49 46 46 .... 57 45 42 50)
        if (bytes.length >= 12
                && bytes[0] == 0x52 // 'R'
                && bytes[1] == 0x49 // 'I'
                && bytes[2] == 0x46 // 'F'
                && bytes[3] == 0x46 // 'F'
                && bytes[8] == 0x57 // 'W'
                && bytes[9] == 0x45 // 'E'
                && bytes[10] == 0x42 // 'B'
                && bytes[11] == 0x50) { // 'P'
            return "image/webp";
        }

        return null;
    }
}
