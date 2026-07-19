package com.petadoption.user.profile;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class UserProfileService {
  private final UserProfileRepository profileRepository;
  private final Clock clock;

  UserProfileService(UserProfileRepository profileRepository, Clock clock) {
    this.profileRepository = profileRepository;
    this.clock = clock;
  }

  @Transactional
  UserProfileResponse upsert(UUID userId, UpsertProfileRequest request) {
    if (userId == null) {
      throw new IllegalArgumentException("user id is required");
    }

    UserProfile profile = profileRepository.findById(userId)
        .orElseGet(() -> new UserProfile(userId));
    profile.update(
        requireText(request.displayName(), "displayName"),
        requireText(request.phone(), "phone"),
        requireText(request.city(), "city"),
        requireText(request.housing(), "housing"),
        LocalDateTime.now(clock));
    return UserProfileResponse.from(profileRepository.save(profile));
  }

  @Transactional(readOnly = true)
  UserProfileResponse get(UUID userId) {
    if (userId == null) {
      throw new IllegalArgumentException("user id is required");
    }
    return profileRepository.findById(userId)
        .map(UserProfileResponse::from)
        .orElseThrow(UserProfileNotFoundException::new);
  }

  private static String requireText(String value, String fieldName) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(fieldName + " is required");
    }
    return value.trim();
  }
}
