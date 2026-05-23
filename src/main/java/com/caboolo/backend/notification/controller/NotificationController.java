package com.caboolo.backend.notification.controller;

import com.caboolo.backend.core.controller.BaseController;
import com.caboolo.backend.core.dto.RestEntity;
import com.caboolo.backend.notification.dto.FcmTokenRequestDto;
import com.caboolo.backend.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/notification")
public class NotificationController extends BaseController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * POST /api/v1/notification/fcm-token?userId=...
     *
     * Register or refresh an FCM device token for the given user.
     * Called by the mobile app on login / app startup / token refresh.
     */
    @PostMapping("/fcm-token")
    public RestEntity<Void> registerFcmToken(
            @RequestParam String userId,
            @Valid @RequestBody FcmTokenRequestDto request) {
        log.info("Registering FCM token for user: {}, deviceId: {}", userId, request.getDeviceId());
        notificationService.registerToken(userId, request);
        return successResponse("FCM token registered successfully");
    }

    /**
     * DELETE /api/v1/notification/fcm-token?userId=...&deviceId=...
     *
     * Remove an FCM token (e.g., on logout).
     */
    @DeleteMapping("/fcm-token")
    public RestEntity<Void> removeFcmToken(
            @RequestParam String userId,
            @RequestParam String deviceId) {
        log.info("Removing FCM token for user: {}, deviceId: {}", userId, deviceId);
        notificationService.removeToken(userId, deviceId);
        return successResponse("FCM token removed successfully");
    }
}
