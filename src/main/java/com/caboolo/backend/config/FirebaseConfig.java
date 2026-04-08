package com.caboolo.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;

@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${firebase.credentials.path}")
    private String credentialsPath;

    @PostConstruct
    public void initialize() {
        log.info("Initializing Firebase Admin SDK...");
        try {
            if (credentialsPath == null || credentialsPath.trim().isEmpty()) {
                log.error("Firebase credentials path NOT found. Please set FIREBASE_CREDENTIALS_PATH.");
                return;
            }

            log.debug("Loading Firebase credentials from: {}", credentialsPath);
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(new FileInputStream(credentialsPath)))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("Firebase Admin SDK initialized successfully.");
            } else {
                log.info("Firebase Admin SDK already initialized.");
            }
        } catch (Exception e) {
            log.error("Firebase initialization failed: {}", e.getMessage(), e);
        }
    }
}
