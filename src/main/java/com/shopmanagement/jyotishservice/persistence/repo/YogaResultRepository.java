package com.shopmanagement.jyotishservice.persistence.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopmanagement.jyotishservice.persistence.entity.YogaResultEntity;

public interface YogaResultRepository extends JpaRepository<YogaResultEntity, Long> {

  List<YogaResultEntity> findByKundaliIdAndTenantIdOrderByYogaCodeAsc(Long kundaliId, String tenantId);

  List<YogaResultEntity> findByKundaliIdAndTenantIdAndCategoryCodeOrderByYogaCodeAsc(
      Long kundaliId, String tenantId, String categoryCode);

  void deleteByKundaliIdAndTenantId(Long kundaliId, String tenantId);

  long countByKundaliIdAndTenantId(Long kundaliId, String tenantId);
}
