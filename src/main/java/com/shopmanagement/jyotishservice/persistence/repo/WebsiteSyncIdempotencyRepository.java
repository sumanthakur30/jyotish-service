package com.shopmanagement.jyotishservice.persistence.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopmanagement.jyotishservice.persistence.entity.WebsiteSyncIdempotencyEntity;

public interface WebsiteSyncIdempotencyRepository
    extends JpaRepository<WebsiteSyncIdempotencyEntity, Long> {

  Optional<WebsiteSyncIdempotencyEntity> findByTenantIdAndIdempotencyKey(
      String tenantId, String idempotencyKey);
}
