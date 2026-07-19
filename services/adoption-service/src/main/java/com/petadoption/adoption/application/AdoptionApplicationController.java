package com.petadoption.adoption.application;

import com.petadoption.common.api.ApiResponse;
import com.petadoption.common.security.AuthHeaders;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
class AdoptionApplicationController {
  private final AdoptionApplicationService service;

  AdoptionApplicationController(AdoptionApplicationService service) {
    this.service = service;
  }

  @PostMapping("/api/v1/adoptions")
  ApiResponse<AdoptionApplicationResponse> submit(
      @RequestHeader(AuthHeaders.USER_ID) String userId,
      @Valid @RequestBody SubmitApplicationRequest request) {
    return ApiResponse.success(service.submit(parseUserId(userId), request));
  }

  @GetMapping("/api/v1/adoptions/me")
  ApiResponse<List<AdoptionApplicationResponse>> listMine(@RequestHeader(AuthHeaders.USER_ID) String userId) {
    return ApiResponse.success(service.listMine(parseUserId(userId)));
  }

  @GetMapping("/api/v1/admin/adoptions")
  ApiResponse<List<AdoptionApplicationResponse>> listAll() {
    return ApiResponse.success(service.listAll());
  }

  @PostMapping("/api/v1/admin/adoptions/{id}/approve")
  ApiResponse<AdoptionApplicationResponse> approve(
      @PathVariable("id") UUID id,
      @RequestHeader(AuthHeaders.USER_ID) String reviewerId) {
    return ApiResponse.success(service.approve(id, parseUserId(reviewerId)));
  }

  @PostMapping("/api/v1/admin/adoptions/{id}/reject")
  ApiResponse<AdoptionApplicationResponse> reject(
      @PathVariable("id") UUID id,
      @RequestHeader(AuthHeaders.USER_ID) String reviewerId,
      @RequestBody(required = false) ReviewApplicationRequest request) {
    return ApiResponse.success(service.reject(id, parseUserId(reviewerId), request));
  }

  @PostMapping("/api/v1/adoptions/{id}/cancel")
  ApiResponse<AdoptionApplicationResponse> cancel(
      @PathVariable("id") UUID id,
      @RequestHeader(AuthHeaders.USER_ID) String userId) {
    return ApiResponse.success(service.cancel(id, parseUserId(userId)));
  }

  private static UUID parseUserId(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(AuthHeaders.USER_ID + " is required");
    }
    return UUID.fromString(value);
  }
}
