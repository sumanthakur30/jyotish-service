package com.shopmanagement.jyotishservice.persistence.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import com.shopmanagement.jyotishservice.persistence.entity.HousePositionEntity;

public interface HousePositionRepository extends JpaRepository<HousePositionEntity, Long> {

  List<HousePositionEntity> findByKundaliIdAndTenantIdOrderByHouseAsc(Long kundaliId, String tenantId);

  @Modifying
  @Transactional
  void deleteByKundaliIdAndTenantId(Long kundaliId, String tenantId);
}
