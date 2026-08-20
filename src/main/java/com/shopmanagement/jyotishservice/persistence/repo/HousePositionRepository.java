package com.shopmanagement.jyotishservice.persistence.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopmanagement.jyotishservice.persistence.entity.HousePositionEntity;

public interface HousePositionRepository extends JpaRepository<HousePositionEntity, Long> {

  List<HousePositionEntity> findByKundaliIdAndTenantIdOrderByHouseAsc(Long kundaliId, String tenantId);
}
