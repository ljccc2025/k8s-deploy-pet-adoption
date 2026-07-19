package com.petadoption.adoption.application;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface AdoptionOutboxEventRepository extends JpaRepository<AdoptionOutboxEvent, UUID> {
  List<AdoptionOutboxEvent> findByProcessedAtIsNullOrderByOccurredAtAsc();
}
