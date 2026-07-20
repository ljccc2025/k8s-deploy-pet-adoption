package com.petadoption.file;

import com.petadoption.common.api.ApiResponse;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/files")
class FileController {
  private final FileStorageService fileStorageService;

  FileController(FileStorageService fileStorageService) {
    this.fileStorageService = fileStorageService;
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  ApiResponse<FileMetadataResponse> upload(@RequestPart("file") MultipartFile file) {
    return ApiResponse.success(fileStorageService.store(file));
  }

  @GetMapping("/{id}")
  ApiResponse<FileMetadataResponse> get(@PathVariable("id") UUID id) {
    return ApiResponse.success(fileStorageService.get(id));
  }
}
