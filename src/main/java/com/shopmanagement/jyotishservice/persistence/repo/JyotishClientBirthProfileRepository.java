package com.shopmanagement.jyotishservice.persistence.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.shopmanagement.jyotishservice.persistence.entity.JyotishClientBirthProfileEntity;

public interface JyotishClientBirthProfileRepository
    extends JpaRepository<JyotishClientBirthProfileEntity, Long> {

  List<JyotishClientBirthProfileEntity> findByClientIdAndTenantIdOrderByCreatedAtAsc(
      Long clientId, String tenantId);

  @Modifying(clearAutomatically = true)
  @Query(
      "delete from JyotishClientBirthProfileEntity l where l.clientId = :clientId and l.tenantId = :tenantId")
  void deleteByClientIdAndTenantId(
      @Param("clientId") Long clientId, @Param("tenantId") String tenantId);
}
