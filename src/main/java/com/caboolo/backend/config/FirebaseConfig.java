package com.caboolo.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initialize() {
        try {
            String firebaseConfig = System.getenv("FIREBASE_CREDENTIALS");
            
            if (firebaseConfig == null || firebaseConfig.trim().isEmpty()) {
                System.err.println("Firebase credentials not found in environment variable FIREBASE_CREDENTIALS.");
                return;
            }

            InputStream serviceAccount = new java.io.ByteArrayInputStream(firebaseConfig.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("Firebase Admin SDK initialized successfully.");
            }
        } catch (Exception e) {
            System.err.println("Firebase initialization failed: " + e.getMessage());
        }
    }
}
