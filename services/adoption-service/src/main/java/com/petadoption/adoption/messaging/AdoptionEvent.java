package com.petadoption.adoption.messaging;

import java.time.Instant;
import java.util.UUID;

public record AdoptionEvent(
    String eventType,
    UUID applicationId,
    UUID petId,
    UUID userId,
    Instant occurredAt) {
}
