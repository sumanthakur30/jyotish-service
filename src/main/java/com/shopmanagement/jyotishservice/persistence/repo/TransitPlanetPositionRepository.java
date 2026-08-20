package com.shopmanagement.jyotishservice.persistence.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopmanagement.jyotishservice.persistence.entity.TransitPlanetPositionEntity;

public interface TransitPlanetPositionRepository
    extends JpaRepository<TransitPlanetPositionEntity, Long> {

  List<TransitPlanetPositionEntity> findByTransitIdAndTenantIdOrderByPlanetCodeAsc(
      Long transitId, String tenantId);

  void deleteByTransitIdAndTenantId(Long transitId, String tenantId);
}
