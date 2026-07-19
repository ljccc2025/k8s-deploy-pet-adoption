package com.petadoption.user.profile;

import com.petadoption.common.api.ApiResponse;
import com.petadoption.common.security.AuthHeaders;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
class UserProfileController {
  private final UserProfileService profileService;

  UserProfileController(UserProfileService profileService) {
    this.profileService = profileService;
  }

  @PutMapping("/me/profile")
  ApiResponse<UserProfileResponse> upsert(
      @RequestHeader(AuthHeaders.USER_ID) UUID userId,
      @RequestBody UpsertProfileRequest request) {
    return ApiResponse.success(profileService.upsert(userId, request));
  }

  @GetMapping("/me/profile")
  ApiResponse<UserProfileResponse> get(@RequestHeader(AuthHeaders.USER_ID) UUID userId) {
    return ApiResponse.success(profileService.get(userId));
  }
}
