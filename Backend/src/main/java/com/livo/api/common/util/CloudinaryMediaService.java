package com.livo.api.common.util;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryMediaService {

    @Nullable
    private final Cloudinary cloudinary;

    @Data
    @Builder
    public static class UploadResult {
        private String storageKey;
        private String fileUrl;
        private long bytes;
        private String format;
    }

    public UploadResult upload(byte[] fileBytes, String fileName, String mimeType, String folder) throws IOException {
        if (cloudinary != null) {
            Map<?, ?> uploadParams = ObjectUtils.asMap(
                    "folder", folder,
                    "use_filename", true,
                    "unique_filename", true,
                    "resource_type", "auto"
            );

            Map<?, ?> result = cloudinary.uploader().upload(fileBytes, uploadParams);
            String storageKey = (String) result.get("public_id");
            String secureUrl = (String) result.get("secure_url");
            Object bytesObj = result.get("bytes");
            long bytes = (bytesObj instanceof Number num) ? num.longValue() : fileBytes.length;
            String format = (String) result.get("format");

            log.info("Uploaded media to Cloudinary: publicId={}, secureUrl={}", storageKey, secureUrl);
            return UploadResult.builder()
                    .storageKey(storageKey)
                    .fileUrl(secureUrl)
                    .bytes(bytes)
                    .format(format)
                    .build();
        } else {
            // Dev/Testing fallback: Generate clean HTTPS simulated Cloudinary URL
            String uniqueId = UUID.randomUUID().toString().replace("-", "");
            String sanitizedFileName = fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
            String storageKey = folder + "/" + uniqueId + "_" + sanitizedFileName;
            String mockUrl = "https://res.cloudinary.com/livo-cdn/image/upload/v1/" + storageKey;

            log.info("Simulated media upload (fallback mode): storageKey={}, url={}", storageKey, mockUrl);
            return UploadResult.builder()
                    .storageKey(storageKey)
                    .fileUrl(mockUrl)
                    .bytes(fileBytes.length)
                    .format(mimeType.contains("/") ? mimeType.substring(mimeType.lastIndexOf('/') + 1) : "bin")
                    .build();
        }
    }

    public void delete(String storageKey) {
        if (cloudinary != null && storageKey != null && !storageKey.isBlank()) {
            try {
                cloudinary.uploader().destroy(storageKey, ObjectUtils.emptyMap());
                log.info("Deleted media from Cloudinary: publicId={}", storageKey);
            } catch (IOException e) {
                log.warn("Failed to delete media from Cloudinary {}: {}", storageKey, e.getMessage());
            }
        }
    }
}
