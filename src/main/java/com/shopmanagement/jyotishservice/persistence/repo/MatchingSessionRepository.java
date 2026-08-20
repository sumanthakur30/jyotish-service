package com.shopmanagement.jyotishservice.persistence.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopmanagement.jyotishservice.persistence.entity.MatchingSessionEntity;

public interface MatchingSessionRepository extends JpaRepository<MatchingSessionEntity, Long> {

  Optional<MatchingSessionEntity> findByIdAndTenantId(Long id, String tenantId);
}
