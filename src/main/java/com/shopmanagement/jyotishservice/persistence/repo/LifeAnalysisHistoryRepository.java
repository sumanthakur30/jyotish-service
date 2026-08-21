package com.shopmanagement.jyotishservice.persistence.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopmanagement.jyotishservice.persistence.entity.LifeAnalysisHistoryEntity;

public interface LifeAnalysisHistoryRepository extends JpaRepository<LifeAnalysisHistoryEntity, Long> {

  List<LifeAnalysisHistoryEntity> findByLifeAnalysisIdAndTenantIdOrderByCreatedAtDesc(
      Long lifeAnalysisId, String tenantId);
}
