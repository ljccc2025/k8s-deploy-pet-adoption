package com.petadoption.adoption.application;

import com.petadoption.adoption.client.PetCatalogClient;
import com.petadoption.common.events.AdoptionEvents;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class AdoptionApplicationService {
  private final AdoptionApplicationRepository repository;
  private final AdoptionOutboxEventRepository outboxRepository;
  private final PetCatalogClient petCatalogClient;
  private final PostCommitActionRunner postCommitActionRunner;
  private final AdoptionOutboxDispatcher outboxDispatcher;

  AdoptionApplicationService(
      AdoptionApplicationRepository repository,
      AdoptionOutboxEventRepository outboxRepository,
      PetCatalogClient petCatalogClient,
      PostCommitActionRunner postCommitActionRunner,
      AdoptionOutboxDispatcher outboxDispatcher) {
    this.repository = repository;
    this.outboxRepository = outboxRepository;
    this.petCatalogClient = petCatalogClient;
    this.postCommitActionRunner = postCommitActionRunner;
    this.outboxDispatcher = outboxDispatcher;
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
    AdoptionApplication saved = saveNewApplication(application);
    enqueue(AdoptionEvents.SUBMITTED, saved, "PENDING");
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
    enqueue(AdoptionEvents.APPROVED, application, "ADOPTED");
    return AdoptionApplicationResponse.from(application);
  }

  @Transactional
  AdoptionApplicationResponse reject(UUID id, UUID reviewerId, ReviewApplicationRequest request) {
    AdoptionApplication application = find(id);
    application.reject(reviewerId, trimOptional(request == null ? null : request.reviewComment()), LocalDateTime.now());
    enqueue(AdoptionEvents.REJECTED, application, "AVAILABLE");
    return AdoptionApplicationResponse.from(application);
  }

  @Transactional
  AdoptionApplicationResponse cancel(UUID id, UUID userId) {
    AdoptionApplication application = find(id);
    application.cancel(userId, LocalDateTime.now());
    enqueue(AdoptionEvents.CANCELLED, application, "AVAILABLE");
    return AdoptionApplicationResponse.from(application);
  }

  private AdoptionApplication find(UUID id) {
    return repository.findById(id).orElseThrow(AdoptionApplicationNotFoundException::new);
  }

  private AdoptionApplication saveNewApplication(AdoptionApplication application) {
    try {
      return repository.saveAndFlush(application);
    } catch (DataIntegrityViolationException exception) {
      throw new InvalidAdoptionStateException("pet already has an active adoption application");
    }
  }

  private void enqueue(String eventType, AdoptionApplication application, String petStatusUpdate) {
    outboxRepository.save(AdoptionOutboxEvent.create(eventType, application, petStatusUpdate));
    postCommitActionRunner.runAfterCommit(outboxDispatcher::dispatchPending);
  }

  private static String trimOptional(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.trim();
  }
}
