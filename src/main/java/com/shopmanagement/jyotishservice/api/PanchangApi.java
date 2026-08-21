package com.shopmanagement.jyotishservice.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class PanchangApi {

  private PanchangApi() {}

  /**
   * POST body for panchang. Canonical fields: {@code lat}, {@code lon}, {@code timezone}. Aliases:
   * {@code latitude}, {@code longitude}, {@code timeZone}.
   */
  public record PanchangRequestBody(
      @NotNull(message = "date is required (yyyy-MM-dd)") LocalDate date,
      @JsonAlias("latitude")
          @NotNull(message = "lat (or latitude) is required")
          @DecimalMin("-90.0")
          @DecimalMax("90.0")
          BigDecimal lat,
      @JsonAlias("longitude")
          @NotNull(message = "lon (or longitude) is required")
          @DecimalMin("-180.0")
          @DecimalMax("180.0")
          BigDecimal lon,
      @JsonAlias({"timeZone", "time_zone"})
          @NotBlank(message = "timezone (or timeZone) is required")
          @Size(max = 64)
          String timezone,
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

  public record MuhuratPeriodDto(
      String code, String name, Instant start, Instant end, String quality) {}

  public record MuhuratBundleDto(List<MuhuratPeriodDto> periods, String notes) {}

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
      MuhuratBundleDto muhurat,
      List<CatalogItem> catalog,
      List<ComingSoonFeature> comingSoon,
      String notes,
      String disclaimer) {}
}
