package com.livo.api.modules.attachment.dto;

import com.livo.api.modules.attachment.entity.enums.AttachmentEntityType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterAttachmentRequest {

    @NotNull(message = "Entity type is required")
    private AttachmentEntityType entityType;

    @NotNull(message = "Entity ID is required")
    private UUID entityId;

    @NotBlank(message = "Storage key is required")
    @Size(max = 255, message = "Storage key cannot exceed 255 characters")
    private String storageKey;

    @NotBlank(message = "File URL is required")
    @Pattern(regexp = "^https://.*", message = "File URL must start with https://")
    private String fileUrl;

    @NotBlank(message = "File name is required")
    @Size(max = 255, message = "File name cannot exceed 255 characters")
    private String fileName;

    @NotBlank(message = "MIME type is required")
    @Pattern(
            regexp = "^(image/jpeg|image/png|image/webp|application/pdf)$",
            message = "MIME type must be one of: image/jpeg, image/png, image/webp, application/pdf"
    )
    private String mimeType;

    @NotNull(message = "File size in bytes is required")
    @Min(value = 1, message = "File size must be greater than zero")
    @Max(value = 10485760, message = "File size cannot exceed 10MB (10485760 bytes)")
    private Long fileSizeBytes;
}
