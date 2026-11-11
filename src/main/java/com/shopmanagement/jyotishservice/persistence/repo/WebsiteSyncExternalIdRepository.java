package com.shopmanagement.jyotishservice.persistence.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopmanagement.jyotishservice.persistence.entity.WebsiteSyncExternalIdEntity;

public interface WebsiteSyncExternalIdRepository
    extends JpaRepository<WebsiteSyncExternalIdEntity, Long> {

  Optional<WebsiteSyncExternalIdEntity> findByTenantIdAndEntityTypeAndExternalId(
      String tenantId, String entityType, String externalId);
}
