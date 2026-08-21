package com.shopmanagement.jyotishservice.persistence.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopmanagement.jyotishservice.persistence.entity.ShadbalaResultEntity;

public interface ShadbalaResultRepository extends JpaRepository<ShadbalaResultEntity, Long> {

  Optional<ShadbalaResultEntity> findByKundaliIdAndTenantId(Long kundaliId, String tenantId);

  void deleteByKundaliIdAndTenantId(Long kundaliId, String tenantId);
}
