package com.shopmanagement.jyotishservice.persistence.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopmanagement.jyotishservice.persistence.entity.KundaliReportEntity;

public interface KundaliReportRepository extends JpaRepository<KundaliReportEntity, Long> {

  Optional<KundaliReportEntity> findByIdAndTenantId(Long id, String tenantId);

  List<KundaliReportEntity> findByTenantIdAndKundaliIdOrderByGeneratedAtDesc(
      String tenantId, Long kundaliId);

  List<KundaliReportEntity> findByTenantIdAndMatchingIdOrderByGeneratedAtDesc(
      String tenantId, Long matchingId);
}
