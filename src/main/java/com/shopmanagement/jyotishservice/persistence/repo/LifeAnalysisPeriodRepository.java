package com.shopmanagement.jyotishservice.persistence.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopmanagement.jyotishservice.persistence.entity.LifeAnalysisPeriodEntity;

public interface LifeAnalysisPeriodRepository extends JpaRepository<LifeAnalysisPeriodEntity, Long> {

  List<LifeAnalysisPeriodEntity> findByKundaliIdAndTenantIdOrderBySortOrderAscFromDateAsc(
      Long kundaliId, String tenantId);

  List<LifeAnalysisPeriodEntity> findByKundaliIdAndTenantIdAndCategoryOrderBySortOrderAscFromDateAsc(
      Long kundaliId, String tenantId, String category);

  Optional<LifeAnalysisPeriodEntity> findByIdAndTenantId(Long id, String tenantId);
}
