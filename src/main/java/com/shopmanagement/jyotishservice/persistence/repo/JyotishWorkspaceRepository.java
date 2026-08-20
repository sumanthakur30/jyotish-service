package com.shopmanagement.jyotishservice.persistence.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopmanagement.jyotishservice.persistence.entity.JyotishWorkspaceEntity;

public interface JyotishWorkspaceRepository extends JpaRepository<JyotishWorkspaceEntity, Long> {

  Optional<JyotishWorkspaceEntity> findByTenantIdAndDeletedAtIsNull(String tenantId);
}
