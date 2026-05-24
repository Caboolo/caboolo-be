package com.caboolo.backend.notification.controller;

import com.caboolo.backend.core.controller.BaseController;
import com.caboolo.backend.core.dto.RestEntity;
import com.caboolo.backend.notification.dto.FcmTokenRequestDto;
import com.caboolo.backend.notification.dto.NotificationResponseDto;
import com.caboolo.backend.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    /**
     * GET /api/v1/notification/list?userId=...
     *
     * Fetch all in-app notifications for a user, newest first.
     */
    @GetMapping("/list")
    public RestEntity<List<NotificationResponseDto>> getNotifications(
            @RequestParam String userId) {
        log.info("Fetching notifications for user: {}", userId);
        List<NotificationResponseDto> notifications = notificationService.getNotificationsForUser(userId);
        return successResponse(notifications, "Notifications fetched successfully");
    }

    /**
     * PATCH /api/v1/notification/{notificationId}/read?userId=...
     *
     * Mark a single notification as read. Returns 404 if the notification
     * does not exist or does not belong to the given user.
     */
    @PatchMapping("/{notificationId}/read")
    public RestEntity<Void> markAsRead(
            @PathVariable String notificationId,
            @RequestParam String userId) {
        log.info("Marking notification {} as read for user: {}", notificationId, userId);
        notificationService.markAsRead(notificationId, userId);
        return successResponse("Notification marked as read");
    }

    /**
     * PATCH /api/v1/notification/read-all?userId=...
     *
     * Mark all of a user's unread notifications as read.
     */
    @PatchMapping("/read-all")
    public RestEntity<Void> markAllAsRead(
            @RequestParam String userId) {
        log.info("Marking all notifications as read for user: {}", userId);
        notificationService.markAllAsRead(userId);
        return successResponse("All notifications marked as read");
    }
}

