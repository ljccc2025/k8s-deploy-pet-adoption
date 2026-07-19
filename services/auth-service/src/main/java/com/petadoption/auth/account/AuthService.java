package com.petadoption.auth.account;

import com.petadoption.auth.security.JwtTokenService;
import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class AuthService {
  private static final String USER_ROLE = "USER";

  private final AccountRepository accountRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenService jwtTokenService;
  private final Clock clock;

  AuthService(
      AccountRepository accountRepository,
      PasswordEncoder passwordEncoder,
      JwtTokenService jwtTokenService,
      Clock clock) {
    this.accountRepository = accountRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtTokenService = jwtTokenService;
    this.clock = clock;
  }

  @Transactional
  void register(RegisterRequest request) {
    String email = normalizeEmail(request.email());
    if (accountRepository.existsByEmail(email)) {
      throw new DuplicateAccountException();
    }

    Account account = new Account(
        UUID.randomUUID(),
        email,
        passwordEncoder.encode(request.password()),
        normalizeRole(request.role()),
        Instant.now(clock));
    accountRepository.save(account);
  }

  @Transactional(readOnly = true)
  TokenResponse login(LoginRequest request) {
    String email = normalizeEmail(request.email());
    Account account = accountRepository.findByEmail(email)
        .orElseThrow(InvalidCredentialsException::new);
    if (!passwordEncoder.matches(request.password(), account.passwordHash())) {
      throw new InvalidCredentialsException();
    }
    return new TokenResponse(jwtTokenService.issueToken(account.id(), account.email(), account.role()));
  }

  private static String normalizeEmail(String email) {
    if (email == null || email.isBlank()) {
      throw new IllegalArgumentException("email is required");
    }
    return email.trim().toLowerCase(Locale.ROOT);
  }

  private static String normalizeRole(String role) {
    if (role == null || role.isBlank()) {
      throw new IllegalArgumentException("role is required");
    }
    String normalizedRole = role.trim().toUpperCase(Locale.ROOT);
    if (!USER_ROLE.equals(normalizedRole)) {
      throw new IllegalArgumentException("public registration only supports USER role");
    }
    return normalizedRole;
  }
}
