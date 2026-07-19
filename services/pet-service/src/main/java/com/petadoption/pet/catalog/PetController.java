package com.petadoption.pet.catalog;

import com.petadoption.common.api.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
class PetController {
  private final PetService petService;

  PetController(PetService petService) {
    this.petService = petService;
  }

  @GetMapping("/api/v1/pets")
  ApiResponse<List<PetResponse>> list(@RequestParam(name = "status", required = false) String status) {
    return ApiResponse.success(petService.list(status));
  }

  @GetMapping("/api/v1/pets/{id}")
  ApiResponse<PetResponse> get(@PathVariable("id") UUID id) {
    return ApiResponse.success(petService.get(id));
  }

  @PostMapping("/api/v1/admin/pets")
  ApiResponse<PetResponse> create(@Valid @RequestBody PetRequest request) {
    return ApiResponse.success(petService.create(request));
  }

  @PutMapping("/api/v1/admin/pets/{id}")
  ApiResponse<PetResponse> update(@PathVariable("id") UUID id, @Valid @RequestBody PetRequest request) {
    return ApiResponse.success(petService.update(id, request));
  }

  @PatchMapping("/api/v1/internal/pets/{id}/adoption-status")
  ApiResponse<PetResponse> updateAdoptionStatus(
      @PathVariable("id") UUID id,
      @Valid @RequestBody UpdateAdoptionStatusRequest request) {
    return ApiResponse.success(petService.updateAdoptionStatus(id, request));
  }
}
