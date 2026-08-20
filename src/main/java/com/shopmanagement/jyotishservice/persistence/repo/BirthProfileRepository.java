package com.shopmanagement.jyotishservice.persistence.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.shopmanagement.jyotishservice.persistence.entity.BirthProfileEntity;

public interface BirthProfileRepository extends JpaRepository<BirthProfileEntity, Long> {

  Optional<BirthProfileEntity> findByIdAndTenantIdAndDeletedAtIsNull(Long id, String tenantId);

  List<BirthProfileEntity> findByTenantIdAndDeletedAtIsNullOrderByUpdatedAtDesc(String tenantId);

  @Query(
      """
      select p from BirthProfileEntity p
      where p.tenantId = :tenantId and p.deletedAt is null
        and (:includeArchived = true or p.status = 'ACTIVE')
        and (:q is null or :q = '' or lower(p.displayName) like lower(concat('%', cast(:q as string), '%')))
      order by p.updatedAt desc
      """)
  List<BirthProfileEntity> search(
      @Param("tenantId") String tenantId,
      @Param("q") String q,
      @Param("includeArchived") boolean includeArchived);
}
