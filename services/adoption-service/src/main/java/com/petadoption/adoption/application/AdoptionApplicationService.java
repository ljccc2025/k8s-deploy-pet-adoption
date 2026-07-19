package com.petadoption.adoption.application;

import com.petadoption.adoption.client.PetCatalogClient;
import com.petadoption.adoption.messaging.AdoptionEvent;
import com.petadoption.adoption.messaging.AdoptionEventPublisher;
import com.petadoption.common.events.AdoptionEvents;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class AdoptionApplicationService {
  private final AdoptionApplicationRepository repository;
  private final PetCatalogClient petCatalogClient;
  private final AdoptionEventPublisher eventPublisher;
  private final PostCommitActionRunner postCommitActionRunner;

  AdoptionApplicationService(
      AdoptionApplicationRepository repository,
      PetCatalogClient petCatalogClient,
      AdoptionEventPublisher eventPublisher,
      PostCommitActionRunner postCommitActionRunner) {
    this.repository = repository;
    this.petCatalogClient = petCatalogClient;
    this.eventPublisher = eventPublisher;
    this.postCommitActionRunner = postCommitActionRunner;
  }

  @Transactional
  AdoptionApplicationResponse submit(UUID userId, SubmitApplicationRequest request) {
    petCatalogClient.requireAvailable(request.petId());
    LocalDateTime now = LocalDateTime.now();
    AdoptionApplication application = new AdoptionApplication(
        UUID.randomUUID(),
        request.petId(),
        userId,
        request.reason().trim(),
        request.experience().trim(),
        now);
    AdoptionApplication saved = repository.save(application);
    postCommitActionRunner.runAfterCommit(() -> {
      petCatalogClient.updateAdoptionStatus(saved.petId(), "PENDING");
      publish(AdoptionEvents.SUBMITTED, saved);
    });
    return AdoptionApplicationResponse.from(saved);
  }

  @Transactional(readOnly = true)
  List<AdoptionApplicationResponse> listMine(UUID userId) {
    return repository.findByUserIdOrderByCreatedAtDesc(userId).stream()
        .map(AdoptionApplicationResponse::from)
        .toList();
  }

  @Transactional(readOnly = true)
  List<AdoptionApplicationResponse> listAll() {
    return repository.findAllByOrderByCreatedAtDesc().stream()
        .map(AdoptionApplicationResponse::from)
        .toList();
  }

  @Transactional
  AdoptionApplicationResponse approve(UUID id, UUID reviewerId) {
    AdoptionApplication application = find(id);
    application.approve(reviewerId, LocalDateTime.now());
    postCommitActionRunner.runAfterCommit(() -> {
      petCatalogClient.updateAdoptionStatus(application.petId(), "ADOPTED");
      publish(AdoptionEvents.APPROVED, application);
    });
    return AdoptionApplicationResponse.from(application);
  }

  @Transactional
  AdoptionApplicationResponse reject(UUID id, UUID reviewerId, ReviewApplicationRequest request) {
    AdoptionApplication application = find(id);
    application.reject(reviewerId, trimOptional(request == null ? null : request.reviewComment()), LocalDateTime.now());
    postCommitActionRunner.runAfterCommit(() -> publish(AdoptionEvents.REJECTED, application));
    return AdoptionApplicationResponse.from(application);
  }

  @Transactional
  AdoptionApplicationResponse cancel(UUID id, UUID userId) {
    AdoptionApplication application = find(id);
    application.cancel(userId, LocalDateTime.now());
    postCommitActionRunner.runAfterCommit(() -> publish(AdoptionEvents.CANCELLED, application));
    return AdoptionApplicationResponse.from(application);
  }

  private AdoptionApplication find(UUID id) {
    return repository.findById(id).orElseThrow(AdoptionApplicationNotFoundException::new);
  }

  private void publish(String eventType, AdoptionApplication application) {
    eventPublisher.publish(new AdoptionEvent(
        eventType,
        application.id(),
        application.petId(),
        application.userId(),
        Instant.now()));
  }

  private static String trimOptional(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.trim();
  }
}
