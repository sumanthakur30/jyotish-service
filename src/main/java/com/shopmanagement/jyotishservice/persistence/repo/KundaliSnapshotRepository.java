package com.shopmanagement.jyotishservice.persistence.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopmanagement.jyotishservice.persistence.entity.KundaliSnapshotEntity;

public interface KundaliSnapshotRepository extends JpaRepository<KundaliSnapshotEntity, Long> {

  Optional<KundaliSnapshotEntity> findByIdAndTenantId(Long id, String tenantId);

  List<KundaliSnapshotEntity> findByTenantIdAndBirthProfileIdOrderByCreatedAtDesc(
      String tenantId, Long birthProfileId);
}
