package com.petadoption.pet.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

record UpdateAdoptionStatusRequest(@NotBlank @Size(max = 32) String adoptionStatus) {}
