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

public final class KundaliApi {

  private KundaliApi() {}

  public record InlineBirthRequest(
      @NotBlank @Size(max = 256) String displayName,
      @NotNull LocalDate birthDate,
      LocalTime birthTime,
      Boolean birthTimeUnknown,
      @NotBlank @Size(max = 64) String timeZone,
      @NotBlank @Size(max = 256) String placeName,
      @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") BigDecimal latitude,
      @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") BigDecimal longitude) {}

  /**
   * Generate from an existing birth_profile id and/or inline birth details. At least one source is
   * required; profile id takes precedence for stored identity, inline may override ayanamsa only.
   */
  public record GenerateRequest(
      Long birthProfileId,
      @Valid InlineBirthRequest birth,
      @Size(max = 32) String ayanamsaCode) {}

  public record PlanetDto(
      String planetCode,
      String planetName,
      BigDecimal longitudeDeg,
      int signIndex,
      String signName,
      BigDecimal degreeInSign,
      int house,
      int nakshatraIndex,
      String nakshatraName,
      int pada,
      boolean retrograde,
      boolean combust,
      BigDecimal speedDegPerDay) {}

  public record HouseDto(
      int house, int signIndex, String signName, BigDecimal cuspLongitudeDeg) {}

  public record ComingSoonFeature(String code, String label) {}

  public record KundaliResponse(
      Long id,
      Long birthProfileId,
      String displayName,
      LocalDate birthDate,
      LocalTime birthTime,
      boolean birthTimeUnknown,
      String timeZone,
      String placeName,
      BigDecimal latitude,
      BigDecimal longitude,
      String ayanamsaCode,
      BigDecimal ayanamsaDeg,
      String zodiacSystem,
      String houseSystem,
      String chartStyle,
      String calculationEngineVersion,
      BigDecimal julianDayUt,
      PlanetDto ascendant,
      List<PlanetDto> planets,
      List<HouseDto> houses,
      String notes,
      List<ComingSoonFeature> comingSoon,
      Instant createdAt) {}

  public record PlanetListResponse(Long kundaliId, List<PlanetDto> planets) {}

  public record HouseListResponse(Long kundaliId, List<HouseDto> houses) {}
}
