package com.caboolo.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Firebase configuration for FCM push notifications ONLY.
 * Firebase Authentication is NOT used — authentication is handled via 2Factor + JWT.
 *
 * <p>Startup validation is performed eagerly so that any misconfiguration
 * (missing service-account file, wrong project, bad credentials) is surfaced
 * immediately in the logs instead of failing silently at first send-time.</p>
 */
@Slf4j
@Configuration
public class FirebaseMessagingConfig {

    @Value("${firebase.credentials.path:}")
    private String credentialsPath;

    // ─────────────────────────────────────────────────────────────────────────
    // Bean – initialise Firebase Admin SDK and expose FirebaseMessaging
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Creates and validates the {@link FirebaseMessaging} bean at startup.
     *
     * <p>Fail-fast behaviour: if the credentials path is configured but the SDK
     * cannot be initialised, an {@link IllegalStateException} is thrown so the
     * application refuses to start rather than running with broken push
     * notifications.</p>
     *
     * @return a fully-initialised {@link FirebaseMessaging} instance, or
     *         {@code null} when Firebase is intentionally disabled (no path set).
     */
    @Bean
    public FirebaseMessaging firebaseMessaging() {
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("  FIREBASE STARTUP DIAGNOSTIC");
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        // ── 1. Credentials path ──────────────────────────────────────────────
        if (credentialsPath == null || credentialsPath.trim().isEmpty()) {
            log.warn("[Firebase] ✗ FIREBASE_CREDENTIALS_PATH is not set.");
            log.warn("[Firebase]   Push notifications will be DISABLED.");
            log.warn("[Firebase]   Set firebase.credentials.path (or the env var FIREBASE_CREDENTIALS_PATH) to enable FCM.");
            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            return null;
        }

        log.info("[Firebase] Credentials path resolved to: {}", credentialsPath);

        // ── 2. File existence check ──────────────────────────────────────────
        File serviceAccountFile = new File(credentialsPath);
        if (!serviceAccountFile.exists()) {
            log.error("[Firebase] ✗ Service account file NOT FOUND at: {}", serviceAccountFile.getAbsolutePath());
            log.error("[Firebase]   Ensure the file exists and the path in application-local.properties / env var is correct.");
            throw new IllegalStateException(
                "[Firebase] Service account file not found: " + serviceAccountFile.getAbsolutePath() +
                ". Fix FIREBASE_CREDENTIALS_PATH and restart."
            );
        }
        if (!serviceAccountFile.canRead()) {
            log.error("[Firebase] ✗ Service account file EXISTS but is NOT READABLE: {}", serviceAccountFile.getAbsolutePath());
            throw new IllegalStateException(
                "[Firebase] Service account file is not readable: " + serviceAccountFile.getAbsolutePath()
            );
        }
        log.info("[Firebase] ✓ Service account file exists and is readable: {}", serviceAccountFile.getAbsolutePath());
        log.info("[Firebase]   File size: {} bytes", serviceAccountFile.length());

        // ── 3. Parse credentials and extract project info ────────────────────
        GoogleCredentials credentials;
        try (FileInputStream serviceAccountStream = new FileInputStream(serviceAccountFile)) {
            credentials = GoogleCredentials.fromStream(serviceAccountStream);
            log.info("[Firebase] ✓ GoogleCredentials parsed successfully from service account file.");
        } catch (IOException e) {
            log.error("[Firebase] ✗ Failed to parse service account JSON. The file may be malformed or not a valid service account key.", e);
            throw new IllegalStateException(
                "[Firebase] Could not parse service account credentials. Check the file contents.", e
            );
        }

        // Log project ID from credentials if available
        if (credentials instanceof ServiceAccountCredentials saCreds) {
            log.info("[Firebase]   Service Account: {}", saCreds.getClientEmail());
            log.info("[Firebase]   Project ID:      {}", saCreds.getProjectId());
            log.info("[Firebase]   Client ID:       {}", saCreds.getClientId());

            if (saCreds.getProjectId() == null || saCreds.getProjectId().isBlank()) {
                log.warn("[Firebase] ✗ Project ID is EMPTY in service account credentials.");
                log.warn("[Firebase]   This usually means the service-account JSON is incomplete or from the wrong project.");
            } else {
                log.info("[Firebase] ✓ Firebase project ID confirmed: {}", saCreds.getProjectId());
            }
        } else {
            log.warn("[Firebase]   Credentials type: {} (not a ServiceAccountCredentials — project ID cannot be confirmed)",
                credentials.getClass().getSimpleName());
        }

        // ── 4. Initialise (or reuse) FirebaseApp ─────────────────────────────
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                log.info("[Firebase] Initialising Firebase Admin SDK...");

                // Re-open the file for FirebaseOptions (stream was closed above)
                try (FileInputStream optionsStream = new FileInputStream(serviceAccountFile)) {
                    FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(optionsStream))
                        .build();
                    FirebaseApp app = FirebaseApp.initializeApp(options);
                    log.info("[Firebase] ✓ Firebase Admin SDK initialised successfully. App name: {}", app.getName());
                }
            } else {
                log.info("[Firebase] ✓ Firebase Admin SDK already initialised (reusing existing app). " +
                         "App count: {}", FirebaseApp.getApps().size());
            }
        } catch (Exception e) {
            log.error("[Firebase] ✗ Firebase Admin SDK FAILED to initialise.", e);
            throw new IllegalStateException("[Firebase] Firebase Admin SDK initialisation failed. See cause.", e);
        }

        // ── 5. Obtain FirebaseMessaging instance ─────────────────────────────
        FirebaseMessaging messagingInstance;
        try {
            messagingInstance = FirebaseMessaging.getInstance();
            log.info("[Firebase] ✓ FirebaseMessaging bean created successfully.");
        } catch (Exception e) {
            log.error("[Firebase] ✗ Could not obtain FirebaseMessaging instance after SDK init.", e);
            throw new IllegalStateException("[Firebase] Failed to get FirebaseMessaging instance.", e);
        }

        // ── 6. APNs note ─────────────────────────────────────────────────────
        log.info("[Firebase]   APNs (iOS push) is configured on the Firebase Console, not in the backend.");
        log.info("[Firebase]   If iOS notifications are failing, verify APNs Auth Key or Certificate in:");
        log.info("[Firebase]   Firebase Console → Project Settings → Cloud Messaging → Apple app configuration");

        log.info("[Firebase] ✓ FCM is READY. Push notifications are enabled.");
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        return messagingInstance;
    }
}
