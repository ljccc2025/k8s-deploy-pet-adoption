package com.petadoption.pet.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pets", schema = "pet_schema")
class Pet {
  @Id
  private UUID id;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(nullable = false, length = 50)
  private String type;

  @Column(nullable = false, length = 20)
  private String gender;

  @Column(name = "age_months", nullable = false)
  private int ageMonths;

  @Column(nullable = false, length = 100)
  private String city;

  @Column(name = "health_status", nullable = false, length = 255)
  private String healthStatus;

  @Enumerated(EnumType.STRING)
  @Column(name = "adoption_status", nullable = false, length = 32)
  private AdoptionStatus adoptionStatus;

  @Column(name = "image_url", length = 500)
  private String imageUrl;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String description;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  protected Pet() {
  }

  Pet(UUID id, LocalDateTime createdAt) {
    this.id = id;
    this.createdAt = createdAt;
    this.updatedAt = createdAt;
  }

  void update(
      String name,
      String type,
      String gender,
      int ageMonths,
      String city,
      String healthStatus,
      AdoptionStatus adoptionStatus,
      String imageUrl,
      String description,
      LocalDateTime updatedAt) {
    this.name = name;
    this.type = type;
    this.gender = gender;
    this.ageMonths = ageMonths;
    this.city = city;
    this.healthStatus = healthStatus;
    this.adoptionStatus = adoptionStatus;
    this.imageUrl = imageUrl;
    this.description = description;
    this.updatedAt = updatedAt;
  }

  void updateAdoptionStatus(AdoptionStatus adoptionStatus, LocalDateTime updatedAt) {
    this.adoptionStatus = adoptionStatus;
    this.updatedAt = updatedAt;
  }

  UUID id() {
    return id;
  }

  String name() {
    return name;
  }

  String type() {
    return type;
  }

  String gender() {
    return gender;
  }

  int ageMonths() {
    return ageMonths;
  }

  String city() {
    return city;
  }

  String healthStatus() {
    return healthStatus;
  }

  AdoptionStatus adoptionStatus() {
    return adoptionStatus;
  }

  String imageUrl() {
    return imageUrl;
  }

  String description() {
    return description;
  }

  LocalDateTime createdAt() {
    return createdAt;
  }

  LocalDateTime updatedAt() {
    return updatedAt;
  }
}
