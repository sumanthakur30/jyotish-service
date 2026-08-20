package com.shopmanagement.jyotishservice.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.shopmanagement.jyotishservice.api.AppointmentApi.AppointmentResponse;
import com.shopmanagement.jyotishservice.api.AppointmentApi.UpsertAppointmentRequest;
import com.shopmanagement.jyotishservice.filter.TenantContextFilter;
import com.shopmanagement.jyotishservice.persistence.entity.JyotishAppointmentEntity;
import com.shopmanagement.jyotishservice.persistence.entity.JyotishClientEntity;
import com.shopmanagement.jyotishservice.persistence.repo.JyotishAppointmentRepository;

@Service
public class AppointmentService {

  private static final Set<String> STATUSES =
      Set.of("SCHEDULED", "CONFIRMED", "COMPLETED", "CANCELLED", "NO_SHOW");
  private static final Set<String> PAYMENT_STATUSES = Set.of("UNPAID", "PENDING", "PAID", "WAIVED");

  private final JyotishAppointmentRepository appointmentRepository;
  private final ClientService clientService;

  public AppointmentService(
      JyotishAppointmentRepository appointmentRepository, ClientService clientService) {
    this.appointmentRepository = appointmentRepository;
    this.clientService = clientService;
  }

  @Transactional
  public AppointmentResponse create(UpsertAppointmentRequest request) {
    String tenantId = requireTenant();
    JyotishClientEntity client = clientService.requireClient(request.clientId(), tenantId);
    JyotishAppointmentEntity appt = new JyotishAppointmentEntity();
    appt.setTenantId(tenantId);
    applyFields(appt, request);
    appt = appointmentRepository.save(appt);
    return toResponse(appt, client.getName());
  }

  @Transactional
  public AppointmentResponse update(Long id, UpsertAppointmentRequest request) {
    String tenantId = requireTenant();
    JyotishAppointmentEntity appt = requireAppointment(id, tenantId);
    JyotishClientEntity client = clientService.requireClient(request.clientId(), tenantId);
    applyFields(appt, request);
    appt = appointmentRepository.save(appt);
    return toResponse(appt, client.getName());
  }

  @Transactional(readOnly = true)
  public AppointmentResponse get(Long id) {
    String tenantId = requireTenant();
    JyotishAppointmentEntity appt = requireAppointment(id, tenantId);
    JyotishClientEntity client = clientService.requireClient(appt.getClientId(), tenantId);
    return toResponse(appt, client.getName());
  }

  @Transactional(readOnly = true)
  public List<AppointmentResponse> search(
      Long clientId, LocalDate fromDate, LocalDate toDate, String status) {
    String tenantId = requireTenant();
    String normalizedStatus = blankToNull(status);
    if (normalizedStatus != null) {
      normalizedStatus = normalizedStatus.toUpperCase(Locale.ROOT);
      if (!STATUSES.contains(normalizedStatus)) {
        throw new IllegalArgumentException("Invalid appointment status: " + status);
      }
    }
    List<JyotishAppointmentEntity> rows =
        appointmentRepository.search(tenantId, clientId, fromDate, toDate, normalizedStatus);
    List<AppointmentResponse> out = new ArrayList<>(rows.size());
    for (JyotishAppointmentEntity appt : rows) {
      JyotishClientEntity client = clientService.requireClient(appt.getClientId(), tenantId);
      out.add(toResponse(appt, client.getName()));
    }
    return out;
  }

  @Transactional
  public void softDelete(Long id) {
    String tenantId = requireTenant();
    JyotishAppointmentEntity appt = requireAppointment(id, tenantId);
    appt.setDeletedAt(Instant.now());
    appointmentRepository.save(appt);
  }

  private JyotishAppointmentEntity requireAppointment(Long id, String tenantId) {
    return appointmentRepository
        .findByIdAndTenantIdAndDeletedAtIsNull(id, tenantId)
        .orElseThrow(
            () ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Appointment not found for this tenant"));
  }

  private static void applyFields(JyotishAppointmentEntity appt, UpsertAppointmentRequest request) {
    appt.setClientId(request.clientId());
    appt.setAppointmentDate(request.appointmentDate());
    appt.setAppointmentTime(request.appointmentTime());
    appt.setConsultationType(request.consultationType().trim());
    String status =
        request.status() == null || request.status().isBlank()
            ? "SCHEDULED"
            : request.status().trim().toUpperCase(Locale.ROOT);
    if (!STATUSES.contains(status)) {
      throw new IllegalArgumentException("Invalid appointment status: " + request.status());
    }
    appt.setStatus(status);
    String payment = blankToNull(request.paymentStatus());
    if (payment != null) {
      payment = payment.toUpperCase(Locale.ROOT);
      if (!PAYMENT_STATUSES.contains(payment)) {
        throw new IllegalArgumentException("Invalid payment status: " + request.paymentStatus());
      }
    }
    appt.setPaymentStatus(payment);
    appt.setNotes(request.notes());
  }

  private static AppointmentResponse toResponse(JyotishAppointmentEntity appt, String clientName) {
    return new AppointmentResponse(
        appt.getId(),
        appt.getClientId(),
        clientName,
        appt.getAppointmentDate(),
        appt.getAppointmentTime(),
        appt.getConsultationType(),
        appt.getStatus(),
        appt.getPaymentStatus(),
        appt.getNotes(),
        appt.getCreatedAt(),
        appt.getUpdatedAt());
  }

  private static String requireTenant() {
    String tenantId = TenantContextFilter.getCurrentTenantId();
    if (tenantId == null || tenantId.isBlank()) {
      throw new IllegalArgumentException("Missing tenant context header: X-Tenant-Id");
    }
    return tenantId;
  }

  private static String blankToNull(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.trim();
  }
}
