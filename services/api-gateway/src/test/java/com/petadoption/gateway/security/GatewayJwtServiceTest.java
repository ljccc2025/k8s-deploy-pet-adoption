package com.petadoption.gateway.security;

import static org.assertj.core.api.Assertions.assertThat;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;

class GatewayJwtServiceTest {
  private static final String SECRET = "change-me-local-dev-secret-at-least-32-bytes";
  private static final String ISSUER = "pet-adoption-auth-service";

  private final GatewayJwtService jwtService = new GatewayJwtService(SECRET, ISSUER);

  @Test
  void authenticateParsesAuthServiceCompatibleToken() {
    UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    String token = issueToken(userId, "ADMIN", ISSUER);

    Optional<AuthenticatedGatewayUser> user = jwtService.authenticate(token);

    assertThat(user).isPresent();
    assertThat(user.orElseThrow().userId()).isEqualTo(userId);
    assertThat(user.orElseThrow().role()).isEqualTo("ADMIN");
  }

  @Test
  void authenticateRejectsTokenFromDifferentIssuer() {
    String token = issueToken(UUID.randomUUID(), "USER", "another-issuer");

    assertThat(jwtService.authenticate(token)).isEmpty();
  }

  private static String issueToken(UUID userId, String role, String issuer) {
    Instant issuedAt = Instant.parse("2030-07-20T00:00:00Z");
    return Jwts.builder()
        .subject(userId.toString())
        .issuer(issuer)
        .issuedAt(Date.from(issuedAt))
        .expiration(Date.from(issuedAt.plusSeconds(3600)))
        .claim("userId", userId.toString())
        .claim("email", "user@example.com")
        .claim("role", role)
        .signWith(signingKey(), Jwts.SIG.HS256)
        .compact();
  }

  private static SecretKey signingKey() {
    try {
      byte[] digest = MessageDigest.getInstance("SHA-256").digest(SECRET.getBytes(StandardCharsets.UTF_8));
      return Keys.hmacShaKeyFor(digest);
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 is not available", exception);
    }
  }
}
