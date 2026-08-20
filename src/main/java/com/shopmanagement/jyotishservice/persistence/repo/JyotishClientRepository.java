package com.shopmanagement.jyotishservice.persistence.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.shopmanagement.jyotishservice.persistence.entity.JyotishClientEntity;

public interface JyotishClientRepository extends JpaRepository<JyotishClientEntity, Long> {

  Optional<JyotishClientEntity> findByIdAndTenantIdAndDeletedAtIsNull(Long id, String tenantId);

  long countByTenantIdAndDeletedAtIsNull(String tenantId);

  @Query(
      """
      select c from JyotishClientEntity c
      where c.tenantId = :tenantId and c.deletedAt is null
        and (
          :q is null or :q = ''
          or lower(c.name) like lower(concat('%', cast(:q as string), '%'))
          or (c.mobile is not null and lower(c.mobile) like lower(concat('%', cast(:q as string), '%')))
        )
      order by c.updatedAt desc
      """)
  List<JyotishClientEntity> search(@Param("tenantId") String tenantId, @Param("q") String q);
}
