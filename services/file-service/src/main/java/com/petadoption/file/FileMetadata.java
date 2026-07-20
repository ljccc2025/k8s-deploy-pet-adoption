package com.petadoption.file;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "files", schema = "file_schema")
class FileMetadata {
  @Id
  private UUID id;

  @Column(name = "original_name", nullable = false, length = 255)
  private String originalName;

  @Column(name = "content_type", nullable = false, length = 100)
  private String contentType;

  @Column(name = "storage_path", nullable = false, length = 500)
  private String storagePath;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  protected FileMetadata() {
  }

  FileMetadata(
      UUID id,
      String originalName,
      String contentType,
      String storagePath,
      LocalDateTime createdAt) {
    this.id = id;
    this.originalName = originalName;
    this.contentType = contentType;
    this.storagePath = storagePath;
    this.createdAt = createdAt;
  }

  UUID id() {
    return id;
  }

  String originalName() {
    return originalName;
  }

  String contentType() {
    return contentType;
  }

  String storagePath() {
    return storagePath;
  }

  LocalDateTime createdAt() {
    return createdAt;
  }
}
