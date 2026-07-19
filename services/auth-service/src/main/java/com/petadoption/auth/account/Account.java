package com.petadoption.auth.account;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "accounts", schema = "auth_schema")
class Account {
  @Id
  private UUID id;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @Column(nullable = false)
  private String role;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected Account() {
  }

  Account(UUID id, String email, String passwordHash, String role, Instant createdAt) {
    this.id = id;
    this.email = email;
    this.passwordHash = passwordHash;
    this.role = role;
    this.createdAt = createdAt;
  }

  String email() {
    return email;
  }

  UUID id() {
    return id;
  }

  String passwordHash() {
    return passwordHash;
  }

  String role() {
    return role;
  }
}
