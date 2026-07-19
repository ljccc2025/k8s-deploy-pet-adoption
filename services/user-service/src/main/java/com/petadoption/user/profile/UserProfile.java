package com.petadoption.user.profile;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_profiles", schema = "user_schema")
class UserProfile {
  @Id
  @Column(name = "user_id")
  private UUID userId;

  @Column(name = "display_name", nullable = false, length = 100)
  private String displayName;

  @Column(nullable = false, length = 30)
  private String phone;

  @Column(nullable = false, length = 100)
  private String city;

  @Column(nullable = false, length = 255)
  private String housing;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  protected UserProfile() {
  }

  UserProfile(UUID userId) {
    this.userId = userId;
  }

  void update(String displayName, String phone, String city, String housing, LocalDateTime updatedAt) {
    this.displayName = displayName;
    this.phone = phone;
    this.city = city;
    this.housing = housing;
    this.updatedAt = updatedAt;
  }

  UUID userId() {
    return userId;
  }

  String displayName() {
    return displayName;
  }

  String phone() {
    return phone;
  }

  String city() {
    return city;
  }

  String housing() {
    return housing;
  }

  LocalDateTime updatedAt() {
    return updatedAt;
  }
}
