package com.petadoption.user.profile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpsertProfileRequest(
    @NotBlank @Size(max = 100) String displayName,
    @NotBlank @Size(max = 30) String phone,
    @NotBlank @Size(max = 100) String city,
    @NotBlank @Size(max = 255) String housing) {}
