package com.petadoption.adoption.application;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
    prefix = "adoption.outbox",
    name = "scheduling-enabled",
    havingValue = "true",
    matchIfMissing = true)
class AdoptionOutboxScheduler {
  private final AdoptionOutboxDispatcher dispatcher;

  AdoptionOutboxScheduler(AdoptionOutboxDispatcher dispatcher) {
    this.dispatcher = dispatcher;
  }

  @Scheduled(fixedDelayString = "${adoption.outbox.dispatch-fixed-delay-ms:5000}")
  void dispatchPending() {
    dispatcher.dispatchPending();
  }
}
