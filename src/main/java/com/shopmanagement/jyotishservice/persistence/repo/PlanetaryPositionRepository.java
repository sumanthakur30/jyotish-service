package com.shopmanagement.jyotishservice.persistence.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopmanagement.jyotishservice.persistence.entity.PlanetaryPositionEntity;

public interface PlanetaryPositionRepository extends JpaRepository<PlanetaryPositionEntity, Long> {

  List<PlanetaryPositionEntity> findByKundaliIdAndTenantIdOrderByPlanetCodeAsc(
      Long kundaliId, String tenantId);
}
