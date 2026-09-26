package com.livo.api.modules.attachment.controller;

import com.livo.api.common.response.ApiResponse;
import com.livo.api.common.security.CurrentUser;
import com.livo.api.common.security.UserPrincipal;
import com.livo.api.modules.attachment.dto.AttachmentResponse;
import com.livo.api.modules.attachment.dto.RegisterAttachmentRequest;
import com.livo.api.modules.attachment.entity.enums.AttachmentEntityType;
import com.livo.api.modules.attachment.service.AttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/attachments")
@RequiredArgsConstructor
@Tag(name = "Attachments", description = "Endpoints for uploading and linking media, receipts, and files across all modules")
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload media file (receipt, image, pdf) and link to an entity")
    public ResponseEntity<ApiResponse<AttachmentResponse>> uploadAttachment(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam("file") MultipartFile file,
            @RequestParam("entityType") AttachmentEntityType entityType,
            @RequestParam("entityId") UUID entityId
    ) {
        AttachmentResponse response = attachmentService.uploadAttachment(currentUser.getId(), file, entityType, entityId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "File uploaded and attached successfully"));
    }

    @PostMapping
    @Operation(summary = "Register pre-uploaded CDN media metadata")
    public ResponseEntity<ApiResponse<AttachmentResponse>> registerAttachment(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody RegisterAttachmentRequest request
    ) {
        AttachmentResponse response = attachmentService.registerAttachment(currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Attachment registered successfully"));
    }

    @GetMapping
    @Operation(summary = "List attachments with optional entity filters")
    public ResponseEntity<ApiResponse<List<AttachmentResponse>>> getAttachments(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(required = false) AttachmentEntityType entityType,
            @RequestParam(required = false) UUID entityId
    ) {
        List<AttachmentResponse> response = attachmentService.getAttachments(currentUser.getId(), entityType, entityId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an attachment by ID")
    public ResponseEntity<ApiResponse<AttachmentResponse>> getAttachment(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID id
    ) {
        AttachmentResponse response = attachmentService.getAttachment(currentUser.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft-delete an attachment and cleanup media storage")
    public ResponseEntity<ApiResponse<Void>> deleteAttachment(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID id
    ) {
        attachmentService.deleteAttachment(currentUser.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(null, "Attachment deleted successfully"));
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    @Operation(summary = "Get all attachments linked to a specific entity")
    public ResponseEntity<ApiResponse<List<AttachmentResponse>>> getAttachmentsForEntity(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable AttachmentEntityType entityType,
            @PathVariable UUID entityId
    ) {
        List<AttachmentResponse> response = attachmentService.getAttachmentsForEntity(currentUser.getId(), entityType, entityId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
