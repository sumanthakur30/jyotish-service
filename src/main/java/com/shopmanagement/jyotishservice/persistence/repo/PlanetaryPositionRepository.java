package com.shopmanagement.jyotishservice.persistence.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import com.shopmanagement.jyotishservice.persistence.entity.PlanetaryPositionEntity;

public interface PlanetaryPositionRepository extends JpaRepository<PlanetaryPositionEntity, Long> {

  List<PlanetaryPositionEntity> findByKundaliIdAndTenantIdOrderByPlanetCodeAsc(
      Long kundaliId, String tenantId);

  @Modifying
  @Transactional
  void deleteByKundaliIdAndTenantId(Long kundaliId, String tenantId);
}
