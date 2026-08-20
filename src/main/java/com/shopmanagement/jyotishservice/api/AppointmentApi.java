package com.shopmanagement.jyotishservice.api;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class AppointmentApi {

  private AppointmentApi() {}

  public record UpsertAppointmentRequest(
      @NotNull Long clientId,
      @NotNull LocalDate appointmentDate,
      @NotNull LocalTime appointmentTime,
      @NotBlank @Size(max = 64) String consultationType,
      @Size(max = 32) String status,
      @Size(max = 32) String paymentStatus,
      String notes) {}

  public record AppointmentResponse(
      Long id,
      Long clientId,
      String clientName,
      LocalDate appointmentDate,
      LocalTime appointmentTime,
      String consultationType,
      String status,
      String paymentStatus,
      String notes,
      Instant createdAt,
      Instant updatedAt) {}

  public record AppointmentListResponse(List<AppointmentResponse> items) {}
}
