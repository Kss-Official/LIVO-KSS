package com.livo.api.modules.notification.controller;

import com.livo.api.common.response.ApiResponse;
import com.livo.api.common.security.CurrentUser;
import com.livo.api.common.security.UserPrincipal;
import com.livo.api.modules.notification.dto.CreateNotificationRequest;
import com.livo.api.modules.notification.dto.NotificationResponse;
import com.livo.api.modules.notification.dto.UnreadCountResponse;
import com.livo.api.modules.notification.entity.enums.NotificationType;
import com.livo.api.modules.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Endpoints for managing in-app notifications, FCM push alerts, and reminder feeds")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    @Operation(summary = "Create an in-app and/or push notification")
    public ResponseEntity<ApiResponse<NotificationResponse>> createNotification(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody CreateNotificationRequest request
    ) {
        NotificationResponse response = notificationService.createNotification(currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Notification created successfully"));
    }

    @GetMapping
    @Operation(summary = "Get user notifications feed with optional type and unread filters")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotifications(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(required = false) Boolean unreadOnly,
            @RequestParam(required = false) NotificationType type,
            @RequestParam(required = false) Integer limit
    ) {
        List<NotificationResponse> response = notificationService.getNotifications(currentUser.getId(), unreadOnly, type, limit);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get count of unread notifications for badge counters")
    public ResponseEntity<ApiResponse<UnreadCountResponse>> getUnreadCount(
            @CurrentUser UserPrincipal currentUser
    ) {
        UnreadCountResponse response = notificationService.getUnreadCount(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/read")
    @Operation(summary = "Mark a notification as read (POST)")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsReadPost(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID id
    ) {
        NotificationResponse response = notificationService.markAsRead(currentUser.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(response, "Notification marked as read"));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark a notification as read (PATCH)")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsReadPatch(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID id
    ) {
        NotificationResponse response = notificationService.markAsRead(currentUser.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(response, "Notification marked as read"));
    }

    @PostMapping("/read-all")
    @Operation(summary = "Mark all unread notifications as read (POST)")
    public ResponseEntity<ApiResponse<Void>> markAllAsReadPost(
            @CurrentUser UserPrincipal currentUser
    ) {
        notificationService.markAllAsRead(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(null, "All notifications marked as read"));
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Mark all unread notifications as read (PATCH)")
    public ResponseEntity<ApiResponse<Void>> markAllAsReadPatch(
            @CurrentUser UserPrincipal currentUser
    ) {
        notificationService.markAllAsRead(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(null, "All notifications marked as read"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft-delete a notification")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID id
    ) {
        notificationService.deleteNotification(currentUser.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(null, "Notification deleted successfully"));
    }
}
