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
            log.debug("No active FCM tokens for user: {} — skipping notification", userId);
            return;
        }

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
            log.warn("FirebaseMessaging is not initialized. Skipping push notifications for title: {}", title);
            return;
        }

        com.google.firebase.messaging.Notification notification = com.google.firebase.messaging.Notification.builder()
            .setTitle(title)
            .setBody(body)
            .build();

        // FCM multicast limit is 500 tokens per batch
        for (int i = 0; i < fcmTokens.size(); i += 500) {
            List<String> batchTokens = fcmTokens.subList(i, Math.min(fcmTokens.size(), i + 500));

            MulticastMessage message = MulticastMessage.builder()
                .addAllTokens(batchTokens)
                .setNotification(notification)
                .putAllData(data != null ? data : Map.of())
                .build();

            try {
                BatchResponse response = firebaseMessaging.sendEachForMulticast(message);
                log.info("FCM batch sent. Success: {}, Failure: {}", response.getSuccessCount(), response.getFailureCount());

                if (response.getFailureCount() > 0) {
                    List<SendResponse> responses = response.getResponses();
                    List<String> failedTokens = new ArrayList<>();

                    for (int j = 0; j < responses.size(); j++) {
                        if (!responses.get(j).isSuccessful() && responses.get(j).getException() != null) {
                            MessagingErrorCode errorCode = responses.get(j).getException().getMessagingErrorCode();
                            if (errorCode == MessagingErrorCode.UNREGISTERED
                                || errorCode == MessagingErrorCode.INVALID_ARGUMENT) {
                                failedTokens.add(batchTokens.get(j));
                            }
                        }
                    }

                    if (!failedTokens.isEmpty()) {
                        log.warn("Marking {} dead FCM tokens as EXPIRED", failedTokens.size());
                        markTokensAsExpired(failedTokens);
                    }
                }
            } catch (FirebaseMessagingException e) {
                log.error("Failed to send FCM multicast message batch", e);
            }
        }
    }
}
