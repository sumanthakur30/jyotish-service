package com.shopmanagement.jyotishservice.persistence.repo;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.shopmanagement.jyotishservice.persistence.entity.JyotishAppointmentEntity;

public interface JyotishAppointmentRepository extends JpaRepository<JyotishAppointmentEntity, Long> {

  Optional<JyotishAppointmentEntity> findByIdAndTenantIdAndDeletedAtIsNull(Long id, String tenantId);

  long countByTenantIdAndAppointmentDateAndDeletedAtIsNull(String tenantId, LocalDate appointmentDate);

  @Query(
      """
      select a from JyotishAppointmentEntity a
      where a.tenantId = :tenantId and a.deletedAt is null
        and (:clientId is null or a.clientId = :clientId)
        and (:fromDate is null or a.appointmentDate >= :fromDate)
        and (:toDate is null or a.appointmentDate <= :toDate)
        and (:status is null or :status = '' or a.status = :status)
      order by a.appointmentDate asc, a.appointmentTime asc
      """)
  List<JyotishAppointmentEntity> search(
      @Param("tenantId") String tenantId,
      @Param("clientId") Long clientId,
      @Param("fromDate") LocalDate fromDate,
      @Param("toDate") LocalDate toDate,
      @Param("status") String status);
}
