package com.shopmanagement.jyotishservice.persistence.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopmanagement.jyotishservice.persistence.entity.LifeAnalysisEntity;

public interface LifeAnalysisRepository extends JpaRepository<LifeAnalysisEntity, Long> {

  List<LifeAnalysisEntity> findByKundaliIdAndTenantIdOrderByCategoryAsc(Long kundaliId, String tenantId);

  Optional<LifeAnalysisEntity> findByKundaliIdAndTenantIdAndCategoryAndSubCategory(
      Long kundaliId, String tenantId, String category, String subCategory);

  Optional<LifeAnalysisEntity> findByIdAndTenantId(Long id, String tenantId);
}
