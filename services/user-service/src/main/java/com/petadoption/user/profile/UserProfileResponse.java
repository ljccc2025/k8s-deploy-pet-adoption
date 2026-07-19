package com.petadoption.user.profile;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserProfileResponse(
    UUID userId,
    String displayName,
    String phone,
    String city,
    String housing,
    LocalDateTime updatedAt) {
  static UserProfileResponse from(UserProfile profile) {
    return new UserProfileResponse(
        profile.userId(),
        profile.displayName(),
        profile.phone(),
        profile.city(),
        profile.housing(),
        profile.updatedAt());
  }
}
