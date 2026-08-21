package com.shopmanagement.jyotishservice.persistence.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopmanagement.jyotishservice.persistence.entity.LifeAnalysisConsultationEntity;

public interface LifeAnalysisConsultationRepository
    extends JpaRepository<LifeAnalysisConsultationEntity, Long> {

  List<LifeAnalysisConsultationEntity> findByKundaliIdAndTenantIdOrderByCreatedAtDesc(
      Long kundaliId, String tenantId);
}
