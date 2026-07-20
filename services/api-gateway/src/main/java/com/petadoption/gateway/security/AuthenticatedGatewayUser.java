package com.petadoption.gateway.security;

import java.util.UUID;

record AuthenticatedGatewayUser(UUID userId, String role) {
}
