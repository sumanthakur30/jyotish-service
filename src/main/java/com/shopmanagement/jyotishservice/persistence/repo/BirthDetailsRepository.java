package com.shopmanagement.jyotishservice.persistence.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopmanagement.jyotishservice.persistence.entity.BirthDetailsEntity;

public interface BirthDetailsRepository extends JpaRepository<BirthDetailsEntity, Long> {

  Optional<BirthDetailsEntity> findByProfileIdAndTenantId(Long profileId, String tenantId);
}
