package com.mertkacar.repository;

import com.mertkacar.model.UserEnterprise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserEnterpriseRepository extends JpaRepository<UserEnterprise, UUID> {
    Optional<UserEnterprise> findByKeycloakUserId(String keycloakUserId);
    Optional<UserEnterprise> findByEmail(String email);
}
