package com.petadoption.notification.events;

import java.time.Instant;
import java.util.UUID;

public record AdoptionEvent(
    String eventType,
    UUID applicationId,
    UUID petId,
    UUID userId,
    Instant occurredAt) {
}
