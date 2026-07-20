package com.petadoption.gateway.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
class GatewayJwtService {
  private final SecretKey key;
  private final String issuer;

  GatewayJwtService(@Value("${jwt.secret}") String secret, @Value("${jwt.issuer}") String issuer) {
    this.key = Keys.hmacShaKeyFor(sha256(secret));
    this.issuer = issuer;
  }

  Optional<AuthenticatedGatewayUser> authenticate(String token) {
    try {
      var claims = Jwts.parser()
          .verifyWith(key)
          .requireIssuer(issuer)
          .build()
          .parseSignedClaims(token)
          .getPayload();
      return Optional.of(new AuthenticatedGatewayUser(
          UUID.fromString(claims.getSubject()),
          claims.get("role", String.class)));
    } catch (IllegalArgumentException | JwtException exception) {
      return Optional.empty();
    }
  }

  private static byte[] sha256(String secret) {
    try {
      return MessageDigest.getInstance("SHA-256")
          .digest(secret.getBytes(StandardCharsets.UTF_8));
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 is not available", exception);
    }
  }
}
