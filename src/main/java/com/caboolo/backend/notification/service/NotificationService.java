package com.caboolo.backend.notification.service;

import com.caboolo.backend.core.idgen.SequenceGenerator;
import com.caboolo.backend.notification.domain.Notification;
import com.caboolo.backend.notification.domain.UserFcmToken;
import com.caboolo.backend.notification.dto.FcmTokenRequestDto;
import com.caboolo.backend.notification.dto.NotificationResponseDto;
import com.caboolo.backend.notification.converter.NotificationConverter;
import com.caboolo.backend.notification.enums.FcmTokenStatus;
import com.caboolo.backend.notification.repository.NotificationRepository;
import com.caboolo.backend.notification.repository.UserFcmTokenRepository;
import com.google.firebase.messaging.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NotificationService {

    private final UserFcmTokenRepository fcmTokenRepository;
    private final NotificationRepository notificationRepository;
    private final SequenceGenerator sequenceGenerator;
    private final NotificationConverter notificationConverter;

    @Autowired(required = false)
    private FirebaseMessaging firebaseMessaging;

    public NotificationService(
        UserFcmTokenRepository fcmTokenRepository,
        NotificationRepository notificationRepository,
        SequenceGenerator sequenceGenerator,
        NotificationConverter notificationConverter) {
        this.fcmTokenRepository = fcmTokenRepository;
        this.notificationRepository = notificationRepository;
        this.sequenceGenerator = sequenceGenerator;
        this.notificationConverter = notificationConverter;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Token Management
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public void registerToken(String userId, FcmTokenRequestDto request) {
        String token = request.getFcmToken();
        String deviceId = request.getDeviceId();

        // 1. Unbind this exact token from any other users/devices if it was transferred
        fcmTokenRepository.findByFcmToken(token).ifPresent(existing -> {
            if (!existing.getUserId().equals(userId) || !existing.getDeviceId().equals(deviceId)) {
                existing.setStatus(FcmTokenStatus.INACTIVE);
                fcmTokenRepository.save(existing);
            }
        });

        // 2. Upsert for the current user & device
        Optional<UserFcmToken> existingDeviceToken = fcmTokenRepository.findByUserIdAndDeviceId(userId, deviceId);

        if (existingDeviceToken.isPresent()) {
            UserFcmToken fcmToken = existingDeviceToken.get();
            fcmToken.setFcmToken(token);
            fcmToken.setDeviceType(request.getDeviceType());
            fcmToken.setAppVersion(request.getAppVersion());
            fcmToken.setStatus(FcmTokenStatus.ACTIVE);
            fcmToken.setLastUsedAt(LocalDateTime.now());
            fcmTokenRepository.save(fcmToken);
        } else {
            UserFcmToken newToken = UserFcmToken.Builder.userFcmToken()
                .withUserFcmTokenId(sequenceGenerator.nextId())
                .withUserId(userId)
                .withDeviceId(deviceId)
                .withFcmToken(token)
                .withDeviceType(request.getDeviceType())
                .withStatus(FcmTokenStatus.ACTIVE)
                .withAppVersion(request.getAppVersion())
                .withLastUsedAt(LocalDateTime.now())
                .build();
            fcmTokenRepository.save(newToken);
        }
        log.info("Registered FCM token for user: {}, deviceId: {}", userId, deviceId);
    }

    @Transactional
    public void removeToken(String userId, String deviceId) {
        fcmTokenRepository.findByUserIdAndDeviceId(userId, deviceId).ifPresent(token -> {
            token.setStatus(FcmTokenStatus.INACTIVE);
            fcmTokenRepository.save(token);
            log.info("Deactivated token for userId={}, deviceId={}", userId, deviceId);
        });
    }

    @Transactional
    public void cleanupExpiredTokens() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        fcmTokenRepository.deleteExpiredTokensOlderThan(cutoff);
        log.info("Cleaned up expired tokens older than {}", cutoff);
    }

    @Transactional
    public void markTokensAsExpired(List<String> tokens) {
        if (!tokens.isEmpty()) {
            fcmTokenRepository.updateStatusByTokens(tokens, FcmTokenStatus.EXPIRED);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Send Notifications
    // ─────────────────────────────────────────────────────────────────────────

    public void sendToUser(String userId, String title, String body, Map<String, String> data) {
        List<UserFcmToken> tokens = fcmTokenRepository.findAllByUserIdAndStatus(userId, FcmTokenStatus.ACTIVE);
        if (tokens.isEmpty()) {
            log.warn("[FCM] No active FCM tokens for user: {} — notification '{}' will NOT be delivered", userId, title);
            return;
        }

        log.info("[FCM] Sending notification '{}' to user: {} ({} active token(s))", title, userId, tokens.size());
        List<String> fcmTokens = tokens.stream()
            .map(UserFcmToken::getFcmToken)
            .collect(Collectors.toList());

        sendToTokens(fcmTokens, title, body, data);
    }

    public void sendToUsers(Collection<String> userIds, String title, String body, Map<String, String> data) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }

        List<UserFcmToken> tokens = fcmTokenRepository.findAllByUserIdInAndStatus(userIds, FcmTokenStatus.ACTIVE);
        if (tokens.isEmpty()) {
            log.debug("No active FCM tokens found for users: {} — skipping notification", userIds);
            return;
        }

        List<String> fcmTokens = tokens.stream()
            .map(UserFcmToken::getFcmToken)
            .collect(Collectors.toList());

        sendToTokens(fcmTokens, title, body, data);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Notification Listing & Read State
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<NotificationResponseDto> getNotificationsForUser(String userId) {
        return notificationRepository.findByUserIdOrderByDateCreatedDesc(userId)
            .stream()
            .map(notificationConverter::toResponseDto)
            .collect(Collectors.toList());
    }

    @Transactional
    public void markAsRead(String notificationId, String userId) {
        Notification notification = notificationRepository
            .findByNotificationIdAndUserId(notificationId, userId)
            .orElseThrow(() -> new EntityNotFoundException(
                "Notification not found: " + notificationId));
        if (!notification.isRead()) {
            notification.setRead(true);
            notificationRepository.save(notification);
            log.info("Marked notification {} as read for user {}", notificationId, userId);
        }
    }

    @Transactional
    public void markAllAsRead(String userId) {
        int updated = notificationRepository.markAllReadByUserId(userId);
        log.info("Marked {} notifications as read for user {}", updated, userId);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Internal
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public void saveInAppNotifications(Collection<Notification> notifications) {
        if (notifications == null || notifications.isEmpty()) {
            return;
        }
        notificationRepository.saveAll(notifications);
        log.info("Saved {} in-app notifications", notifications.size());
    }

    private void sendToTokens(List<String> fcmTokens, String title, String body, Map<String, String> data) {
        if (firebaseMessaging == null) {
            log.error("[FCM] ✗ FirebaseMessaging bean is NULL — Firebase Admin SDK was not initialised correctly.");
            log.error("[FCM]   Notification '{}' will NOT be delivered. Check startup logs for Firebase diagnostic output.", title);
            return;
        }

        com.google.firebase.messaging.Notification notification = com.google.firebase.messaging.Notification.builder()
            .setTitle(title)
            .setBody(body)
            .build();

        int totalBatches = (int) Math.ceil((double) fcmTokens.size() / 500);
        log.info("[FCM] Sending '{}' to {} token(s) in {} batch(es)", title, fcmTokens.size(), totalBatches);

        // FCM multicast limit is 500 tokens per batch
        for (int i = 0; i < fcmTokens.size(); i += 500) {
            int batchNumber = (i / 500) + 1;
            List<String> batchTokens = fcmTokens.subList(i, Math.min(fcmTokens.size(), i + 500));
            log.debug("[FCM] Dispatching batch {}/{} ({} tokens)", batchNumber, totalBatches, batchTokens.size());

            MulticastMessage message = MulticastMessage.builder()
                .addAllTokens(batchTokens)
                .setNotification(notification)
                .putAllData(data != null ? data : Map.of())
                .build();

            try {
                BatchResponse response = firebaseMessaging.sendEachForMulticast(message);
                log.info("[FCM] Batch {}/{} result — Success: {}, Failure: {}",
                    batchNumber, totalBatches, response.getSuccessCount(), response.getFailureCount());

                if (response.getFailureCount() > 0) {
                    List<SendResponse> responses = response.getResponses();
                    List<String> deadTokens = new ArrayList<>();

                    for (int j = 0; j < responses.size(); j++) {
                        SendResponse sr = responses.get(j);
                        if (!sr.isSuccessful() && sr.getException() != null) {
                            FirebaseMessagingException ex = sr.getException();
                            MessagingErrorCode errorCode = ex.getMessagingErrorCode();
                            String tokenSnippet = maskToken(batchTokens.get(j));

                            // ── Categorise & log the root cause ──────────────
                            if (errorCode == MessagingErrorCode.UNREGISTERED) {
                                log.warn("[FCM] Token {} → UNREGISTERED: app was uninstalled or token expired. Marking EXPIRED.",
                                    tokenSnippet);
                                deadTokens.add(batchTokens.get(j));

                            } else if (errorCode == MessagingErrorCode.INVALID_ARGUMENT) {
                                log.warn("[FCM] Token {} → INVALID_ARGUMENT: the FCM token itself is malformed. " +
                                         "Root cause: invalid/corrupt token. Marking EXPIRED.", tokenSnippet);
                                deadTokens.add(batchTokens.get(j));

                            } else if (errorCode == MessagingErrorCode.SENDER_ID_MISMATCH) {
                                log.error("[FCM] Token {} → SENDER_ID_MISMATCH: the FCM token was issued by a " +
                                          "DIFFERENT Firebase project / sender ID than the one in your service account. " +
                                          "Verify that the app and the service account belong to the same Firebase project.",
                                    tokenSnippet);

                            } else if (errorCode == MessagingErrorCode.QUOTA_EXCEEDED) {
                                log.error("[FCM] Token {} → QUOTA_EXCEEDED: FCM rate limit hit. " +
                                          "Reduce send frequency or request a quota increase in Firebase Console.",
                                    tokenSnippet);

                            } else if (errorCode == MessagingErrorCode.UNAVAILABLE) {
                                log.warn("[FCM] Token {} → UNAVAILABLE: FCM service temporarily unavailable. " +
                                         "Retry with exponential back-off.", tokenSnippet);

                            } else if (errorCode == MessagingErrorCode.INTERNAL) {
                                log.error("[FCM] Token {} → INTERNAL: unexpected Firebase server error. " +
                                          "This is a Firebase-side issue; retry later.", tokenSnippet);

                            } else {
                                log.error("[FCM] Token {} → {} (HTTP {}) — {}",
                                    tokenSnippet,
                                    errorCode,
                                    ex.getHttpResponse() != null ? ex.getHttpResponse().getStatusCode() : "N/A",
                                    ex.getMessage());
                            }
                        }
                    }

                    if (!deadTokens.isEmpty()) {
                        log.warn("[FCM] Marking {} dead/invalid FCM token(s) as EXPIRED", deadTokens.size());
                        markTokensAsExpired(deadTokens);
                    }
                }
            } catch (FirebaseMessagingException e) {
                MessagingErrorCode topLevelCode = e.getMessagingErrorCode();
                log.error("[FCM] ✗ Multicast batch {}/{} failed entirely.", batchNumber, totalBatches);
                log.error("[FCM]   Error code : {}", topLevelCode);
                log.error("[FCM]   HTTP status : {}",
                    e.getHttpResponse() != null ? e.getHttpResponse().getStatusCode() : "N/A");
                log.error("[FCM]   Message     : {}", e.getMessage());

                if (topLevelCode == MessagingErrorCode.SENDER_ID_MISMATCH) {
                    log.error("[FCM]   ROOT CAUSE: Firebase project mismatch. " +
                              "The service account and the mobile app belong to different Firebase projects.");
                } else if (topLevelCode == MessagingErrorCode.INVALID_ARGUMENT) {
                    log.error("[FCM]   ROOT CAUSE: All tokens in this batch are invalid/malformed.");
                } else if (topLevelCode == MessagingErrorCode.UNAVAILABLE) {
                    log.error("[FCM]   ROOT CAUSE: FCM service temporarily unavailable — retry with back-off.");
                } else if (topLevelCode == MessagingErrorCode.INTERNAL) {
                    log.error("[FCM]   ROOT CAUSE: Firebase internal error — this is a Firebase-side problem.");
                }

                log.error("[FCM]   Full exception:", e);
            }
        }
    }

    /**
     * Masks an FCM token for safe logging — shows only the first 8 and last 6 chars.
     * Example: "abc12345...xyz789"
     */
    private static String maskToken(String token) {
        if (token == null || token.length() < 20) {
            return "[short-token]";
        }
        return token.substring(0, 8) + "..." + token.substring(token.length() - 6);
    }
}
