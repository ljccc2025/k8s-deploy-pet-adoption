package com.petadoption.gateway.security;

import org.springframework.stereotype.Component;

@Component
class RouteSecurity {
  boolean isPublicPath(String path) {
    return path.startsWith("/api/v1/auth/")
        || path.equals("/api/v1/pets")
        || path.startsWith("/api/v1/pets/");
  }
}
