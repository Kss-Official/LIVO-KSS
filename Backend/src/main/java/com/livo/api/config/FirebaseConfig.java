package com.livo.api.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Initializes Firebase Admin SDK using service account credentials.
 * Gracefully defaults to mock/local mode if credentials are not configured.
 */
@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${firebase.credentials.path:serviceAccountKey.json}")
    private String credentialsPath;

    @PostConstruct
    public void initialize() {
        if (!FirebaseApp.getApps().isEmpty()) {
            return;
        }

        try {
            InputStream serviceAccount = null;
            File resolvedFile = null;

            if (credentialsPath != null && !credentialsPath.isBlank()) {
                File file = new File(credentialsPath);
                if (file.exists() && file.isFile()) {
                    resolvedFile = file;
                } else {
                    File backendFile = new File("Backend", credentialsPath);
                    if (backendFile.exists() && backendFile.isFile()) {
                        resolvedFile = backendFile;
                    }
                }
            }

            if (resolvedFile == null) {
                File fallback = new File("serviceAccountKey.json");
                if (fallback.exists() && fallback.isFile()) {
                    resolvedFile = fallback;
                }
            }

            if (resolvedFile != null) {
                serviceAccount = new FileInputStream(resolvedFile);
                credentialsPath = resolvedFile.getAbsolutePath();
            } else if (credentialsPath != null && !credentialsPath.isBlank()) {
                serviceAccount = getClass().getClassLoader().getResourceAsStream(credentialsPath);
            }

            if (serviceAccount != null) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();
                FirebaseApp.initializeApp(options);
                log.info("FirebaseApp initialized successfully with credentials from: {}", credentialsPath);
            } else {
                log.info("No Firebase credentials file configured. Remote Firebase token verification and FCM will run in fallback/dev mode.");
            }
        } catch (IOException e) {
            log.warn("Failed to initialize FirebaseApp from {}: {}. Running in fallback mode.", credentialsPath, e.getMessage());
        }
    }
}
