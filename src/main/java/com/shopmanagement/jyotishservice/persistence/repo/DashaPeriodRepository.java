package com.shopmanagement.jyotishservice.persistence.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import com.shopmanagement.jyotishservice.persistence.entity.DashaPeriodEntity;

public interface DashaPeriodRepository extends JpaRepository<DashaPeriodEntity, Long> {

  List<DashaPeriodEntity> findByKundaliIdAndTenantIdAndSystemCodeOrderByStartAtAscSequenceNoAsc(
      Long kundaliId, String tenantId, String systemCode);

  boolean existsByKundaliIdAndTenantIdAndSystemCode(
      Long kundaliId, String tenantId, String systemCode);

  @Modifying
  @Transactional
  void deleteByKundaliIdAndTenantIdAndSystemCode(Long kundaliId, String tenantId, String systemCode);
}
