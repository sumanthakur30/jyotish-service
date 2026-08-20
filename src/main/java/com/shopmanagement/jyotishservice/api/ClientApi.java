package com.shopmanagement.jyotishservice.api;

import java.time.Instant;
import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class ClientApi {

  private ClientApi() {}

  public record UpsertClientRequest(
      @NotBlank @Size(max = 256) String name,
      @Size(max = 32) String mobile,
      @Email @Size(max = 256) String email,
      String notes,
      List<Long> birthProfileIds) {}

  public record ClientResponse(
      Long id,
      String name,
      String mobile,
      String email,
      String notes,
      List<Long> birthProfileIds,
      Instant createdAt,
      Instant updatedAt) {}

  public record ClientListResponse(List<ClientResponse> items) {}

  public record CrmDashboardResponse(long totalClients, long todaysAppointments) {}
}
