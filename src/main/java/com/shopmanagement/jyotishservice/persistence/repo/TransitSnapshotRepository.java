package com.shopmanagement.jyotishservice.persistence.repo;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopmanagement.jyotishservice.persistence.entity.TransitSnapshotEntity;

public interface TransitSnapshotRepository extends JpaRepository<TransitSnapshotEntity, Long> {

  Optional<TransitSnapshotEntity> findByKundaliIdAndTenantIdAndTransitDate(
      Long kundaliId, String tenantId, LocalDate transitDate);

  Optional<TransitSnapshotEntity> findByIdAndTenantId(Long id, String tenantId);

  Optional<TransitSnapshotEntity> findFirstByKundaliIdAndTenantIdOrderByTransitDateDescCreatedAtDesc(
      Long kundaliId, String tenantId);
}
