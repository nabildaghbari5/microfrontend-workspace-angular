package com.talentcloud.notification_ms.controller;

import com.talentcloud.notification_ms.client.UserClient;
import com.talentcloud.notification_ms.model.Notification;
import com.talentcloud.notification_ms.model.NotificationState;
import com.talentcloud.notification_ms.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.*;
@CrossOrigin(origins = "http://localhost:5173")
@Slf4j
@RestController
@RequestMapping("/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final UserClient userClient;

    @GetMapping
    public List<Notification> getAuthenticatedUserNotifications(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmailHeader
    ) {
        log.info("🔍 Fetching notifications - UserId: {}, Email: {}", userIdHeader, userEmailHeader);

        boolean hasUserId = userIdHeader != null && !userIdHeader.isBlank();
        boolean hasEmail = userEmailHeader != null && !userEmailHeader.isBlank();

        // 🔁 Try to fetch email if missing and userId is present
        if (hasUserId && !hasEmail) {
            try {
                Map<String, String> emailMap = userClient.getEmailByUserId(userIdHeader);
                if (emailMap != null && emailMap.containsKey("email")) {
                    userEmailHeader = emailMap.get("email");
                    hasEmail = true;
                    log.info("📩 Retrieved email '{}' from auth-ms for userId '{}'", userEmailHeader, userIdHeader);
                }
            } catch (Exception e) {
                log.warn("⚠️ Failed to retrieve email for userId {} from auth-ms: {}", userIdHeader, e.getMessage());
            }
        }

        if (!hasUserId && !hasEmail) {
            throw new UnauthorizedAccessException("User ID or Email must be provided in header");
        }

        List<Notification> result = new ArrayList<>();

        if (hasUserId) {
            List<Notification> byUserId = notificationRepository.findByUserIdOrderByCreatedAtDesc(userIdHeader);
            log.info("📦 Fetched {} notifications by userId", byUserId.size());
            result.addAll(byUserId);
        }

        if (hasEmail) {
            List<Notification> byEmail = notificationRepository.findByEmailOrderByCreatedAtDesc(userEmailHeader.trim().toLowerCase());
            log.info("📦 Fetched {} notifications by email", byEmail.size());

            for (Notification emailNotif : byEmail) {
                if (emailNotif.getUserId() == null || !hasUserId || !emailNotif.getUserId().equals(userIdHeader)) {
                    result.add(emailNotif); // avoid duplicate
                }
            }
        }

        result.sort(Comparator.comparing(Notification::getCreatedAt).reversed());
        log.info("📊 Total combined notifications: {}", result.size());

        return result;
    }

    @PostMapping("/{notificationId}/mark-as-read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAsRead(
            @PathVariable Long notificationId,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmailHeader
    ) {
        log.info("📝 Marking notification {} as read - UserId: {}, Email: {}", notificationId, userIdHeader, userEmailHeader);

        notificationRepository.findById(notificationId).ifPresentOrElse(notification -> {
            boolean isAuthorized = false;

            if (userIdHeader != null && notification.getUserId() != null && notification.getUserId().equals(userIdHeader)) {
                isAuthorized = true;
                log.info("✅ Authorized by userId");
            } else if (userEmailHeader != null && notification.getEmail() != null &&
                    notification.getEmail().equalsIgnoreCase(userEmailHeader)) {
                isAuthorized = true;
                log.info("✅ Authorized by email");
            }

            if (!isAuthorized) {
                log.warn("🚫 Unauthorized access for notification {}", notificationId);
                throw new UnauthorizedAccessException("You are not allowed to modify this notification");
            }

            notification.setState(NotificationState.READ);
            notificationRepository.save(notification);
            log.info("✅ Notification {} marked as read", notificationId);
        }, () -> {
            log.warn("📭 Notification {} not found", notificationId);
            throw new NotificationNotFoundException("Notification not found");
        });
    }

    @GetMapping("/unread-count")
    public long getUnreadCount(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmailHeader
    ) {
        log.info("🔢 Counting unread notifications - UserId: {}, Email: {}", userIdHeader, userEmailHeader);

        boolean hasUserId = userIdHeader != null && !userIdHeader.isBlank();
        boolean hasEmail = userEmailHeader != null && !userEmailHeader.isBlank();

        if (hasUserId && !hasEmail) {
            try {
                Map<String, String> emailMap = userClient.getEmailByUserId(userIdHeader);
                if (emailMap != null && emailMap.containsKey("email")) {
                    userEmailHeader = emailMap.get("email");
                    hasEmail = true;
                    log.info("📩 Retrieved email '{}' from auth-ms for userId '{}'", userEmailHeader, userIdHeader);
                }
            } catch (Exception e) {
                log.warn("⚠️ Failed to retrieve email for userId {} from auth-ms: {}", userIdHeader, e.getMessage());
            }
        }

        if (!hasUserId && !hasEmail) {
            throw new UnauthorizedAccessException("User ID or Email must be provided in header");
        }

        long count = notificationRepository.countByUserIdOrEmailAndState(userIdHeader, userEmailHeader, NotificationState.UNREAD);
        log.info("📊 Unread notifications count: {}", count);
        return count;
    }

    @GetMapping("/debug")
    public Map<String, Object> debugNotifications(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmailHeader
    ) {
        log.info("🐛 Debug notifications - UserId: {}, Email: {}", userIdHeader, userEmailHeader);

        Map<String, Object> debugInfo = new HashMap<>();

        if (userIdHeader != null && !userIdHeader.isBlank()) {
            long countByUserId = notificationRepository.countByUserId(userIdHeader);
            List<Notification> byUserId = notificationRepository.findByUserIdOrderByCreatedAtDesc(userIdHeader);
            debugInfo.put("countByUserId", countByUserId);
            debugInfo.put("notificationsByUserId", byUserId.size());
        }

        if (userEmailHeader != null && !userEmailHeader.isBlank()) {
            long countByEmail = notificationRepository.countByEmail(userEmailHeader);
            List<Notification> byEmail = notificationRepository.findByEmailOrderByCreatedAtDesc(userEmailHeader);
            debugInfo.put("countByEmail", countByEmail);
            debugInfo.put("notificationsByEmail", byEmail.size());
        }

        if ((userIdHeader != null && !userIdHeader.isBlank()) ||
                (userEmailHeader != null && !userEmailHeader.isBlank())) {
            List<Notification> combined = notificationRepository.findByUserIdOrEmailOrderByCreatedAtDesc(
                    userIdHeader, userEmailHeader);
            debugInfo.put("combinedQuery", combined.size());
        }

        long totalCount = notificationRepository.count();
        debugInfo.put("totalNotificationsInDB", totalCount);

        return debugInfo;
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(UnauthorizedAccessException.class)
    public String handleUnauthorized(UnauthorizedAccessException e) {
        return e.getMessage();
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotificationNotFoundException.class)
    public String handleNotificationNotFound(NotificationNotFoundException e) {
        return e.getMessage();
    }

    private static class UnauthorizedAccessException extends RuntimeException {
        public UnauthorizedAccessException(String message) {
            super(message);
        }
    }

    private static class NotificationNotFoundException extends RuntimeException {
        public NotificationNotFoundException(String message) {
            super(message);
        }
    }
}
