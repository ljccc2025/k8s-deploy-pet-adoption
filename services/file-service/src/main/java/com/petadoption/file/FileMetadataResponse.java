package com.petadoption.file;

import java.time.LocalDateTime;
import java.util.UUID;

record FileMetadataResponse(
    UUID id,
    String originalName,
    String contentType,
    String storagePath,
    LocalDateTime createdAt) {
  static FileMetadataResponse from(FileMetadata metadata) {
    return new FileMetadataResponse(
        metadata.id(),
        metadata.originalName(),
        metadata.contentType(),
        metadata.storagePath(),
        metadata.createdAt());
  }
}
