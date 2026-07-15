package com.caboolo.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Firebase configuration for FCM push notifications ONLY.
 * Firebase Authentication is NOT used — authentication is handled via 2Factor + JWT.
 */
@Slf4j
@Configuration
public class FirebaseMessagingConfig {

    @Value("${firebase.credentials.path:}")
    private String credentialsPath;

    @PostConstruct
    public void initializeFirebase() {
        if (credentialsPath == null || credentialsPath.trim().isEmpty()) {
            log.warn("Firebase credentials path not configured. Push notifications will be unavailable. " +
                     "Set FIREBASE_CREDENTIALS_PATH environment variable to enable FCM.");
            return;
        }

        try {
            if (FirebaseApp.getApps().isEmpty()) {
                log.info("Initializing Firebase Admin SDK for FCM push notifications...");
                FileInputStream serviceAccount = new FileInputStream(credentialsPath);
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();
                FirebaseApp.initializeApp(options);
                log.info("Firebase Admin SDK initialized successfully for FCM messaging.");
            } else {
                log.info("Firebase Admin SDK already initialized.");
            }
        } catch (IOException e) {
            log.error("Failed to initialize Firebase Admin SDK. Push notifications will be unavailable.", e);
        }
    }

    @Bean
    public FirebaseMessaging firebaseMessaging() {
        if (FirebaseApp.getApps().isEmpty()) {
            log.warn("Firebase is not initialized. Returning null FirebaseMessaging bean.");
            return null;
        }
        return FirebaseMessaging.getInstance();
    }
}
