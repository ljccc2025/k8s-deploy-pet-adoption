package com.petadoption.pet.catalog;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface PetRepository extends JpaRepository<Pet, UUID> {
  List<Pet> findAllByOrderByCreatedAtDesc();

  List<Pet> findByAdoptionStatusOrderByCreatedAtDesc(AdoptionStatus adoptionStatus);
}
