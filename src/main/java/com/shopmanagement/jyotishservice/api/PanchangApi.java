package com.shopmanagement.jyotishservice.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class PanchangApi {

  private PanchangApi() {}

  public record PanchangRequestBody(
      @NotNull LocalDate date,
      @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") BigDecimal lat,
      @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") BigDecimal lon,
      @NotBlank @Size(max = 64) String timezone,
      @Size(max = 256) String placeName,
      @Size(max = 32) String ayanamsaCode) {}

  public record LimbDto(
      int index,
      String name,
      String paksha,
      Integer pada,
      BigDecimal progress,
      String detail) {}

  public record SolarEventDto(
      boolean available, LocalTime localTime, Instant instant, String note) {}

  public record LunarEventDto(
      boolean available, LocalTime localTime, Instant instant, String note) {}

  public record CatalogItem(
      String code, String displayName, boolean implemented, String status) {}

  public record ComingSoonFeature(String code, String label) {}

  public record PanchangResponse(
      LocalDate date,
      String timeZone,
      String placeName,
      BigDecimal latitude,
      BigDecimal longitude,
      String ayanamsaCode,
      BigDecimal ayanamsaDeg,
      BigDecimal julianDayUt,
      Instant asOf,
      String calculationEngineVersion,
      LimbDto tithi,
      LimbDto vara,
      LimbDto nakshatra,
      LimbDto yoga,
      LimbDto karana,
      SolarEventDto sunrise,
      SolarEventDto sunset,
      LunarEventDto moonrise,
      LunarEventDto moonset,
      List<CatalogItem> catalog,
      List<ComingSoonFeature> comingSoon,
      String notes,
      String disclaimer) {}
}
