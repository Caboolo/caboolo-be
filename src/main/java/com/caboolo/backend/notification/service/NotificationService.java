package com.caboolo.backend.notification.service;

import com.caboolo.backend.core.idgen.SequenceGenerator;
import com.caboolo.backend.notification.domain.UserFcmToken;
import com.caboolo.backend.notification.repository.UserFcmTokenRepository;
import com.google.firebase.messaging.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NotificationService {

    private final UserFcmTokenRepository fcmTokenRepository;
    private final SequenceGenerator sequenceGenerator;

    public NotificationService(UserFcmTokenRepository fcmTokenRepository, SequenceGenerator sequenceGenerator) {
        this.fcmTokenRepository = fcmTokenRepository;
        this.sequenceGenerator = sequenceGenerator;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Token Management
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Register or refresh an FCM token for a user.
     * If the token already exists for this user, it's a no-op.
     * If the token belongs to a different user (device transferred), reassign it.
     */
    @Transactional
    public void registerToken(String userId, String fcmToken) {
        // Check if this exact (userId, token) pair already exists
        Optional<UserFcmToken> existing = fcmTokenRepository.findByUserIdAndFcmToken(userId, fcmToken);
        if (existing.isPresent()) {
            log.debug("FCM token already registered for user: {}", userId);
            return;
        }

        // If the token exists under a different user (device was logged out/in), remove old entry
        fcmTokenRepository.deleteByFcmToken(fcmToken);

        UserFcmToken tokenEntity = UserFcmToken.Builder.userFcmToken()
                .withUserFcmTokenId(sequenceGenerator.nextId())
                .withUserId(userId)
                .withFcmToken(fcmToken)
                .build();
        fcmTokenRepository.save(tokenEntity);
        log.info("Registered FCM token for user: {}", userId);
    }

    /**
     * Remove a specific FCM token (e.g., on logout).
     */
    @Transactional
    public void removeToken(String userId, String fcmToken) {
        fcmTokenRepository.deleteByUserIdAndFcmToken(userId, fcmToken);
        log.info("Removed FCM token for user: {}", userId);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Send Notifications
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Send a push notification to a single user (all their devices).
     */
    public void sendToUser(String userId, String title, String body, Map<String, String> data) {
        List<UserFcmToken> tokens = fcmTokenRepository.findByUserId(userId);
        if (tokens.isEmpty()) {
            log.debug("No FCM tokens for user: {} — skipping notification", userId);
            return;
        }

        List<String> fcmTokens = tokens.stream()
                .map(UserFcmToken::getFcmToken)
                .collect(Collectors.toList());

        sendToTokens(fcmTokens, title, body, data);
    }

    /**
     * Send a push notification to multiple users (all their devices).
     */
    public void sendToUsers(Collection<String> userIds, String title, String body, Map<String, String> data) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }

        List<UserFcmToken> tokens = fcmTokenRepository.findByUserIdIn(userIds);
        if (tokens.isEmpty()) {
            log.debug("No FCM tokens found for users: {} — skipping notification", userIds);
            return;
        }

        List<String> fcmTokens = tokens.stream()
                .map(UserFcmToken::getFcmToken)
                .collect(Collectors.toList());

        sendToTokens(fcmTokens, title, body, data);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Internal
    // ─────────────────────────────────────────────────────────────────────────

    private void sendToTokens(List<String> fcmTokens, String title, String body, Map<String, String> data) {
        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        for (String token : fcmTokens) {
            try {
                Message message = Message.builder()
                        .setToken(token)
                        .setNotification(notification)
                        .putAllData(data != null ? data : Map.of())
                        .build();

                String response = FirebaseMessaging.getInstance().send(message);
                log.debug("FCM sent successfully: {}", response);
            } catch (FirebaseMessagingException e) {
                handleFcmError(token, e);
            }
        }
    }

    /**
     * Handle FCM errors — silently remove invalid/expired tokens.
     */
    private void handleFcmError(String token, FirebaseMessagingException e) {
        MessagingErrorCode errorCode = e.getMessagingErrorCode();
        if (errorCode == MessagingErrorCode.UNREGISTERED
                || errorCode == MessagingErrorCode.INVALID_ARGUMENT) {
            log.warn("FCM token invalid/expired — removing: {}", token);
            fcmTokenRepository.deleteByFcmToken(token);
        } else {
            log.error("FCM send failed for token {}: {} (code: {})", token, e.getMessage(), errorCode);
        }
    }
}
