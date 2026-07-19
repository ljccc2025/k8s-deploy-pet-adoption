package com.petadoption.notification;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class NotificationServiceConfig {
  @Bean
  Clock clock() {
    return Clock.systemUTC();
  }
}
