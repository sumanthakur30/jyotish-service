package com.shopmanagement.jyotishservice.persistence.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopmanagement.jyotishservice.persistence.entity.BirthLocationEntity;

public interface BirthLocationRepository extends JpaRepository<BirthLocationEntity, Long> {

  Optional<BirthLocationEntity> findByProfileIdAndTenantId(Long profileId, String tenantId);
}
