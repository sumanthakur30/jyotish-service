package com.shopmanagement.jyotishservice.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class BirthProfileApi {

  private BirthProfileApi() {}

  public record BirthDetailsRequest(
      @NotNull LocalDate birthDate,
      LocalTime birthTime,
      Boolean birthTimeUnknown,
      /** EXACT | APPROXIMATE | UNKNOWN */
      @Size(max = 16) String birthTimeAccuracy,
      Integer uncertaintyMinutes,
      Boolean dstObserved,
      @NotBlank @Size(max = 64) String timeZone) {}

  public record BirthLocationRequest(
      @NotBlank @Size(max = 256) String placeName,
      @Size(max = 8) String countryCode,
      @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") BigDecimal latitude,
      @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") BigDecimal longitude,
      @NotBlank @Size(max = 64) String timeZone,
      Boolean coordsManual) {}

  public record UpsertProfileRequest(
      @NotBlank @Size(max = 256) String displayName,
      @Size(max = 32) String gender,
      @Size(max = 64) String clientRef,
      String notes,
      @NotNull @Valid BirthDetailsRequest details,
      @NotNull @Valid BirthLocationRequest location) {}

  public record BirthDetailsResponse(
      LocalDate birthDate,
      LocalTime birthTime,
      boolean birthTimeUnknown,
      String birthTimeAccuracy,
      Integer uncertaintyMinutes,
      boolean dstObserved,
      String timeZone) {}

  public record BirthLocationResponse(
      String placeName,
      String countryCode,
      BigDecimal latitude,
      BigDecimal longitude,
      String timeZone,
      boolean coordsManual) {}

  public record ProfileResponse(
      Long id,
      String displayName,
      String gender,
      String status,
      String clientRef,
      String notes,
      BirthDetailsResponse details,
      BirthLocationResponse location,
      Instant createdAt,
      Instant updatedAt) {}

  public record ProfileListResponse(List<ProfileResponse> items) {}

  public record PlaceSuggestion(
      String placeName,
      String countryCode,
      BigDecimal latitude,
      BigDecimal longitude,
      String timeZone) {}

  public record PlaceSearchResponse(List<PlaceSuggestion> items) {}

  public record WorkspaceBootstrapRequest(@Size(max = 128) String name) {}

  public record WorkspaceResponse(
      Long id,
      String tenantId,
      String name,
      String timezone,
      String locale,
      String ayanamsaCode,
      String zodiacSystem,
      String chartStyle) {}
}
