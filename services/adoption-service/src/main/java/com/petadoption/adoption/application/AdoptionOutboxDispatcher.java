package com.petadoption.adoption.application;

import com.petadoption.adoption.client.PetCatalogClient;
import com.petadoption.adoption.messaging.AdoptionEventPublisher;
import java.time.Instant;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdoptionOutboxDispatcher {
  private final AdoptionOutboxEventRepository repository;
  private final PetCatalogClient petCatalogClient;
  private final AdoptionEventPublisher eventPublisher;

  AdoptionOutboxDispatcher(
      AdoptionOutboxEventRepository repository,
      PetCatalogClient petCatalogClient,
      AdoptionEventPublisher eventPublisher) {
    this.repository = repository;
    this.petCatalogClient = petCatalogClient;
    this.eventPublisher = eventPublisher;
  }

  @Scheduled(fixedDelayString = "${adoption.outbox.dispatch-fixed-delay-ms:5000}")
  void dispatchOnSchedule() {
    dispatchPending();
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public synchronized void dispatchPending() {
    repository.findByProcessedAtIsNullOrderByOccurredAtAsc()
        .forEach(this::dispatch);
  }

  private void dispatch(AdoptionOutboxEvent event) {
    try {
      if (event.petStatusUpdate() != null) {
        petCatalogClient.updateAdoptionStatus(event.petId(), event.petStatusUpdate());
      }
      eventPublisher.publish(event.toAdoptionEvent());
      event.markProcessed(Instant.now());
    } catch (Exception exception) {
      event.markFailed(exception);
    }
    repository.save(event);
  }
}
