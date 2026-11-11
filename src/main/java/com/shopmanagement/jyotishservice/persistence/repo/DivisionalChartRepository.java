package com.shopmanagement.jyotishservice.persistence.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopmanagement.jyotishservice.persistence.entity.DivisionalChartEntity;

public interface DivisionalChartRepository extends JpaRepository<DivisionalChartEntity, Long> {

  List<DivisionalChartEntity> findByKundaliIdAndTenantIdOrderByVargaCodeAsc(
      Long kundaliId, String tenantId);

  Optional<DivisionalChartEntity> findByKundaliIdAndTenantIdAndVargaCode(
      Long kundaliId, String tenantId, String vargaCode);

  @org.springframework.data.jpa.repository.Modifying
  @org.springframework.transaction.annotation.Transactional
  void deleteByKundaliIdAndTenantId(Long kundaliId, String tenantId);
}
