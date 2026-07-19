package com.petadoption.auth.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {
  private final SecretKey key;
  private final String issuer;
  private final Duration accessTokenTtl;
  private final Clock clock;

  JwtTokenService(
      @Value("${jwt.secret}") String secret,
      @Value("${jwt.issuer}") String issuer,
      @Value("${jwt.access-token-ttl-minutes}") long accessTokenTtlMinutes,
      Clock clock) {
    this.key = Keys.hmacShaKeyFor(sha256(secret));
    this.issuer = issuer;
    this.accessTokenTtl = Duration.ofMinutes(accessTokenTtlMinutes);
    this.clock = clock;
  }

  public String issueToken(UUID userId, String email, String role) {
    Instant issuedAt = Instant.now(clock);
    Instant expiresAt = issuedAt.plus(accessTokenTtl);
    return Jwts.builder()
        .subject(userId.toString())
        .issuer(issuer)
        .issuedAt(Date.from(issuedAt))
        .expiration(Date.from(expiresAt))
        .claim("userId", userId.toString())
        .claim("email", email)
        .claim("role", role)
        .signWith(key, Jwts.SIG.HS256)
        .compact();
  }

  public AuthenticatedToken parseToken(String token) {
    var claims = Jwts.parser()
        .verifyWith(key)
        .requireIssuer(issuer)
        .build()
        .parseSignedClaims(token)
        .getPayload();
    return new AuthenticatedToken(
        UUID.fromString(claims.getSubject()),
        claims.get("email", String.class),
        claims.get("role", String.class));
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
