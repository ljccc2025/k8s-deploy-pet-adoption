package com.petadoption.adoption.application;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

record SubmitApplicationRequest(
    @NotNull UUID petId,
    @NotBlank String reason,
    @NotBlank String experience) {
}
