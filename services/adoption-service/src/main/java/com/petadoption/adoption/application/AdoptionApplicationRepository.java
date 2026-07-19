package com.petadoption.adoption.application;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface AdoptionApplicationRepository extends JpaRepository<AdoptionApplication, UUID> {
  List<AdoptionApplication> findAllByOrderByCreatedAtDesc();

  List<AdoptionApplication> findByUserIdOrderByCreatedAtDesc(UUID userId);

  boolean existsByActivePetId(UUID petId);
}
