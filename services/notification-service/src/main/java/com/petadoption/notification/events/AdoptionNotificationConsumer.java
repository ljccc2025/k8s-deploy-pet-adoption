package com.petadoption.notification.events;

import com.petadoption.notification.notice.NotificationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class AdoptionNotificationConsumer {
  private final NotificationService notificationService;

  AdoptionNotificationConsumer(NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  @RabbitListener(queues = "${notification.events.queue}")
  public void handleAdoptionEvent(AdoptionEvent event) {
    notificationService.createFrom(event);
  }
}
