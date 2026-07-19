package com.petadoption.auth.security;

import java.util.UUID;

public record AuthenticatedToken(UUID userId, String email, String role) {}
