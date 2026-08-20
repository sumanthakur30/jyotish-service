package com.shopmanagement.jyotishservice.persistence.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopmanagement.jyotishservice.persistence.entity.DivisionalHousePositionEntity;

public interface DivisionalHousePositionRepository
    extends JpaRepository<DivisionalHousePositionEntity, Long> {

  List<DivisionalHousePositionEntity> findByDivisionalChartIdAndTenantIdOrderByHouseAsc(
      Long divisionalChartId, String tenantId);
}
