package com.petadoption.user.profile;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {
}
