package com.caboolo.backend.notification.service;

import com.caboolo.backend.core.idgen.SequenceGenerator;
import com.caboolo.backend.notification.domain.Notification;
import com.caboolo.backend.notification.domain.UserFcmToken;
import com.caboolo.backend.notification.dto.FcmTokenRequestDto;
import com.caboolo.backend.notification.enums.FcmTokenStatus;
import com.caboolo.backend.notification.repository.NotificationRepository;
import com.caboolo.backend.notification.repository.UserFcmTokenRepository;
import com.google.firebase.messaging.*;
import lombok.extern.slf4j.Slf4j;
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

    public NotificationService(UserFcmTokenRepository fcmTokenRepository, NotificationRepository notificationRepository, SequenceGenerator sequenceGenerator) {
        this.fcmTokenRepository = fcmTokenRepository;
        this.notificationRepository = notificationRepository;
        this.sequenceGenerator = sequenceGenerator;
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
        com.google.firebase.messaging.Notification notification = com.google.firebase.messaging.Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        // FCM multicast limit is 500, so we partition if necessary
        for (int i = 0; i < fcmTokens.size(); i += 500) {
            List<String> batchTokens = fcmTokens.subList(i, Math.min(fcmTokens.size(), i + 500));
            
            MulticastMessage message = MulticastMessage.builder()
                    .addAllTokens(batchTokens)
                    .setNotification(notification)
                    .putAllData(data != null ? data : Map.of())
                    .build();

            try {
                BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);
                
                if (response.getFailureCount() > 0) {
                    List<SendResponse> responses = response.getResponses();
                    List<String> failedTokens = new ArrayList<>();
                    
                    for (int j = 0; j < responses.size(); j++) {
                        if (!responses.get(j).isSuccessful() && responses.get(j).getException() != null) {
                            String errorCode = responses.get(j).getException().getMessagingErrorCode().name();
                            // Identify dead tokens
                            if ("UNREGISTERED".equals(errorCode) || "INVALID_ARGUMENT".equals(errorCode)) {
                                failedTokens.add(batchTokens.get(j));
                            }
                        }
                    }
                    
                    // Mark dead tokens as EXPIRED immediately
                    if (!failedTokens.isEmpty()) {
                        markTokensAsExpired(failedTokens);
                    }
                }
            } catch (FirebaseMessagingException e) {
                log.error("Failed to send FCM message via multicast", e);
            }
        }
    }
}
