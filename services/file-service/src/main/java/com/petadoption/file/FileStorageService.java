package com.petadoption.file;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
class FileStorageService {
  private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

  private final FileMetadataRepository fileMetadataRepository;
  private final Path storageRoot;

  FileStorageService(
      FileMetadataRepository fileMetadataRepository,
      @Value("${file.storage.root:./uploads/file-service}") String storageRoot) {
    this.fileMetadataRepository = fileMetadataRepository;
    this.storageRoot = Path.of(storageRoot).toAbsolutePath().normalize();
  }

  FileMetadataResponse store(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("file must not be empty");
    }

    UUID id = UUID.randomUUID();
    String storagePath = id.toString();
    Path target = storageRoot.resolve(storagePath).normalize();
    if (!target.startsWith(storageRoot)) {
      throw new IllegalArgumentException("invalid storage path");
    }

    try {
      Files.createDirectories(storageRoot);
      file.transferTo(target);
    } catch (IOException exception) {
      throw new UncheckedIOException("failed to store file", exception);
    }

    FileMetadata metadata = new FileMetadata(
        id,
        originalName(file),
        contentType(file),
        storagePath,
        LocalDateTime.now());
    return FileMetadataResponse.from(fileMetadataRepository.save(metadata));
  }

  FileMetadataResponse get(UUID id) {
    return fileMetadataRepository.findById(id)
        .map(FileMetadataResponse::from)
        .orElseThrow(FileNotFoundException::new);
  }

  private String originalName(MultipartFile file) {
    String originalName = file.getOriginalFilename();
    if (originalName == null || originalName.isBlank()) {
      return "file";
    }
    return originalName;
  }

  private String contentType(MultipartFile file) {
    String contentType = file.getContentType();
    if (contentType == null || contentType.isBlank()) {
      return DEFAULT_CONTENT_TYPE;
    }
    return contentType;
  }
}
