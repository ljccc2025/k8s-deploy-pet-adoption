package com.petadoption.adoption;

import static org.assertj.core.api.Assertions.assertThat;

import com.petadoption.adoption.application.AdoptionOutboxDispatcher;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;

@SpringBootTest
class AdoptionServiceApplicationTests {
  @Autowired ApplicationContext applicationContext;

  @Test
  void contextLoads() {
  }

  @Test
  void outboxDispatcherDoesNotOwnScheduledMethod() {
    boolean hasScheduledMethod = Arrays.stream(AdoptionOutboxDispatcher.class.getDeclaredMethods())
        .anyMatch(method -> method.isAnnotationPresent(Scheduled.class));

    assertThat(hasScheduledMethod).isFalse();
  }

  @Test
  void testProfileDisablesOutboxScheduler() {
    assertThat(applicationContext.containsBean("adoptionOutboxScheduler")).isFalse();
  }
}
