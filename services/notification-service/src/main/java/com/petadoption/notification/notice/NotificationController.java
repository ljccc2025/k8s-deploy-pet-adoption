package com.petadoption.notification.notice;

import com.petadoption.common.api.ApiResponse;
import com.petadoption.common.security.AuthHeaders;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
class NotificationController {
  private final NotificationService notificationService;

  NotificationController(NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  @GetMapping("/api/v1/notifications/me")
  ApiResponse<List<NotificationResponse>> listMine(@RequestHeader(AuthHeaders.USER_ID) String userId) {
    return ApiResponse.success(notificationService.listMine(parseUserId(userId)));
  }

  @PostMapping("/api/v1/notifications/{id}/read")
  ApiResponse<NotificationResponse> markRead(
      @PathVariable("id") UUID id,
      @RequestHeader(AuthHeaders.USER_ID) String userId) {
    return ApiResponse.success(notificationService.markRead(id, parseUserId(userId)));
  }

  private static UUID parseUserId(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(AuthHeaders.USER_ID + " is required");
    }
    return UUID.fromString(value);
  }
}
