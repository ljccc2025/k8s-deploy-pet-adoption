package com.petadoption.pet.catalog;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

record PetRequest(
    @NotBlank @Size(max = 100) String name,
    @NotBlank @Size(max = 50) String type,
    @NotBlank @Size(max = 20) String gender,
    @NotNull @Min(0) Integer ageMonths,
    @NotBlank @Size(max = 100) String city,
    @NotBlank @Size(max = 255) String healthStatus,
    @NotBlank @Size(max = 32) String adoptionStatus,
    @Size(max = 500) String imageUrl,
    @NotBlank String description) {}
