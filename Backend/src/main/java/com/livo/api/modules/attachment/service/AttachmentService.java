package com.livo.api.modules.attachment.service;

import com.livo.api.modules.attachment.dto.AttachmentResponse;
import com.livo.api.modules.attachment.dto.RegisterAttachmentRequest;
import com.livo.api.modules.attachment.entity.enums.AttachmentEntityType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface AttachmentService {

    AttachmentResponse uploadAttachment(UUID userId, MultipartFile file, AttachmentEntityType entityType, UUID entityId);

    AttachmentResponse registerAttachment(UUID userId, RegisterAttachmentRequest request);

    List<AttachmentResponse> getAttachments(UUID userId, AttachmentEntityType entityType, UUID entityId);

    AttachmentResponse getAttachment(UUID userId, UUID id);

    void deleteAttachment(UUID userId, UUID id);

    List<AttachmentResponse> getAttachmentsForEntity(UUID userId, AttachmentEntityType entityType, UUID entityId);
}
