package com.shopmanagement.jyotishservice.persistence.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopmanagement.jyotishservice.persistence.entity.AshtakavargaResultEntity;

public interface AshtakavargaResultRepository extends JpaRepository<AshtakavargaResultEntity, Long> {

  Optional<AshtakavargaResultEntity> findByKundaliIdAndTenantId(Long kundaliId, String tenantId);

  void deleteByKundaliIdAndTenantId(Long kundaliId, String tenantId);
}
