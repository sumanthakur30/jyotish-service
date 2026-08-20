package com.shopmanagement.jyotishservice.persistence.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopmanagement.jyotishservice.persistence.entity.DivisionalPlanetPositionEntity;

public interface DivisionalPlanetPositionRepository
    extends JpaRepository<DivisionalPlanetPositionEntity, Long> {

  List<DivisionalPlanetPositionEntity>
      findByDivisionalChartIdAndTenantIdOrderByPlanetCodeAsc(
          Long divisionalChartId, String tenantId);
}
