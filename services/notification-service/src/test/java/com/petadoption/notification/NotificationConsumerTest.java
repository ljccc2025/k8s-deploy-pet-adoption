package com.petadoption.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.petadoption.common.security.AuthHeaders;
import com.petadoption.notification.events.AdoptionEvent;
import com.petadoption.notification.events.AdoptionNotificationConsumer;
import com.petadoption.notification.notice.Notification;
import com.petadoption.notification.notice.NotificationRepository;
import com.petadoption.notification.notice.NotificationService;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class NotificationConsumerTest {
  private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID OTHER_USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

  @Autowired NotificationService notificationService;
  @Autowired AdoptionNotificationConsumer consumer;
  @Autowired NotificationRepository notificationRepository;
  @Autowired MockMvc mockMvc;

  @BeforeEach
  void cleanDatabase() {
    notificationRepository.deleteAll();
  }

  @Test
  void createsNotificationFromApprovedAdoptionEvent() {
    AdoptionEvent event = event("adoption.approved", USER_ID);

    Notification notification = notificationService.createFrom(event);

    assertThat(notification.userId()).isEqualTo(event.userId());
    assertThat(notification.eventType()).isEqualTo("adoption.approved");
    assertThat(notification.message()).contains("通过");
  }

  @Test
  void duplicateEventDoesNotCreateDuplicateNotification() {
    AdoptionEvent event = event("adoption.approved", USER_ID);

    Notification first = notificationService.createFrom(event);
    Notification second = notificationService.createFrom(event);

    assertThat(second.id()).isEqualTo(first.id());
    assertThat(notificationRepository.count()).isEqualTo(1);
  }

  @Test
  void consumerPersistsNotificationFromAdoptionEvent() {
    consumer.handleAdoptionEvent(event("adoption.rejected", USER_ID));

    assertThat(notificationRepository.findAll())
        .singleElement()
        .satisfies(notification -> {
          assertThat(notification.userId()).isEqualTo(USER_ID);
          assertThat(notification.message()).contains("未通过");
        });
  }

  @Test
  void listMineReturnsOnlyCurrentUserNotificationsNewestFirst() throws Exception {
    notificationService.createFrom(event("adoption.approved", OTHER_USER_ID));
    AdoptionEvent older = new AdoptionEvent(
        "adoption.submitted",
        UUID.fromString("33333333-3333-3333-3333-333333333333"),
        UUID.randomUUID(),
        USER_ID,
        Instant.parse("2026-07-19T08:00:00Z"));
    AdoptionEvent newer = new AdoptionEvent(
        "adoption.rejected",
        UUID.fromString("44444444-4444-4444-4444-444444444444"),
        UUID.randomUUID(),
        USER_ID,
        Instant.parse("2026-07-19T09:00:00Z"));
    notificationService.createFrom(older);
    notificationService.createFrom(newer);

    mockMvc.perform(get("/api/v1/notifications/me")
        .header(AuthHeaders.USER_ID, USER_ID.toString()))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.length()").value(2))
      .andExpect(jsonPath("$.data[0].eventType").value("adoption.rejected"))
      .andExpect(jsonPath("$.data[1].eventType").value("adoption.submitted"));
  }

  @Test
  void markReadUpdatesOnlyOwnedNotification() throws Exception {
    Notification notification = notificationService.createFrom(event("adoption.approved", USER_ID));

    mockMvc.perform(post("/api/v1/notifications/{id}/read", notification.id())
        .header(AuthHeaders.USER_ID, USER_ID.toString()))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.id").value(notification.id().toString()))
      .andExpect(jsonPath("$.data.readAt").isNotEmpty());

    mockMvc.perform(post("/api/v1/notifications/{id}/read", notification.id())
        .header(AuthHeaders.USER_ID, OTHER_USER_ID.toString()))
      .andExpect(status().isNotFound());
  }

  @Test
  void notificationEndpointsRejectMissingOrInvalidUserHeader() throws Exception {
    mockMvc.perform(get("/api/v1/notifications/me"))
      .andExpect(status().isBadRequest());

    mockMvc.perform(get("/api/v1/notifications/me")
        .header(AuthHeaders.USER_ID, "not-a-uuid"))
      .andExpect(status().isBadRequest());
  }

  private static AdoptionEvent event(String eventType, UUID userId) {
    return new AdoptionEvent(
        eventType,
        UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
        UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
        userId,
        Instant.parse("2026-07-19T10:00:00Z"));
  }
}
