package com.livo.api.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Initializes Cloudinary client for receipt, photo, and document media management.
 * Gracefully defaults to null/mock mode if cloud credentials are not supplied.
 */
@Slf4j
@Configuration
public class CloudinaryConfig {

    @Value("${livo.cloudinary.url:}")
    private String cloudinaryUrl;

    @Value("${livo.cloudinary.cloud-name:}")
    private String cloudName;

    @Value("${livo.cloudinary.api-key:}")
    private String apiKey;

    @Value("${livo.cloudinary.api-secret:}")
    private String apiSecret;

    @Bean
    public Cloudinary cloudinary() {
        if (cloudinaryUrl != null && !cloudinaryUrl.isBlank()) {
            log.info("Cloudinary client initialized via CLOUDINARY_URL");
            return new Cloudinary(cloudinaryUrl.trim());
        } else if (cloudName != null && !cloudName.isBlank()
                && apiKey != null && !apiKey.isBlank()
                && apiSecret != null && !apiSecret.isBlank()) {
            log.info("Cloudinary client initialized with cloud-name: {}", cloudName);
            return new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", cloudName.trim(),
                    "api_key", apiKey.trim(),
                    "api_secret", apiSecret.trim(),
                    "secure", true
            ));
        } else {
            log.warn("No Cloudinary credentials configured! Running media services in fallback/local simulation mode. Uploaded assets will use simulated URLs and will not be persisted to Cloudinary CDN.");
            return null;
        }
    }
}
