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
      @Size(max = 32) String ayanamsaCode,
      /**
       * When true, also compute and persist Sripati Bhava Chalit ({@code CHALIT}) on generate. D1
       * itself always remains {@code WHOLE_SIGN}.
       */
      Boolean includeChalit) {}

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

  /** Catalog entry for GET .../charts — implemented vs Coming Soon. */
  public record ChartCatalogItem(
      String vargaCode,
      String displayName,
      int divisions,
      boolean implemented,
      boolean computed,
      String status) {}

  public record ChartListResponse(Long kundaliId, List<ChartCatalogItem> charts) {}

  public record VargaChartResponse(
      Long kundaliId,
      Long chartId,
      String vargaCode,
      String displayName,
      String calculationEngineVersion,
      String houseSystem,
      PlanetDto ascendant,
      List<PlanetDto> planets,
      List<HouseDto> houses,
      String notes,
      boolean comingSoon,
      Instant createdAt) {}

  /** Catalog entry for GET .../dasha — implemented vs Coming Soon. */
  public record DashaCatalogItem(
      String systemCode, String displayName, boolean implemented, String status) {}

  public record DashaPeriodDto(
      String level,
      String lordCode,
      String lordName,
      String mahaLordCode,
      String antarLordCode,
      String pratyantarLordCode,
      Instant startAt,
      Instant endAt,
      Long remainingDays,
      boolean current,
      List<DashaPeriodDto> children) {}

  public record DashaCurrentDto(
      DashaPeriodDto maha, DashaPeriodDto antar, DashaPeriodDto pratyantar) {}

  public record DashaResponse(
      Long kundaliId,
      String systemCode,
      String displayName,
      String calculationEngineVersion,
      int moonNakshatraIndex,
      String moonNakshatraName,
      String birthMahadashaLord,
      BigDecimal balanceAtBirthYears,
      BigDecimal elapsedAtBirthYears,
      DashaCurrentDto current,
      List<DashaPeriodDto> timeline,
      List<DashaCatalogItem> catalog,
      String notes,
      String interpretationPlaceholder,
      Instant asOf) {}

  /** Catalog entry for GET .../yogas — implemented vs Coming Soon. */
  public record YogaCatalogItem(
      String yogaCode,
      String displayName,
      String categoryCode,
      String categoryName,
      boolean implemented,
      String status) {}

  public record YogaDto(
      String yogaCode,
      String displayName,
      String categoryCode,
      String categoryName,
      boolean present,
      String strengthCode,
      String strengthLabel,
      List<String> planets,
      List<Integer> houses,
      String explanation,
      String ruleId) {}

  public record YogaListResponse(
      Long kundaliId,
      String calculationEngineVersion,
      String categoryFilter,
      List<YogaDto> yogas,
      List<YogaCatalogItem> catalog,
      String notes,
      String disclaimer) {}

  public record DoshaDto(
      String doshaCode,
      String displayName,
      String displayNameHi,
      String status,
      String severityCode,
      List<String> planets,
      List<Integer> houses,
      List<String> conditions,
      String explanation,
      String ruleId,
      boolean implemented) {}

  public record DoshaCatalogItem(
      String doshaCode, String displayName, boolean implemented, String status) {}

  public record DoshaListResponse(
      Long kundaliId,
      String calculationEngineVersion,
      List<DoshaDto> doshas,
      List<DoshaCatalogItem> catalog,
      String notes,
      String disclaimer) {}

  /** GET .../ashtakavarga */
  public record AshtakavargaPlanetDto(String planetCode, String planetName, List<Integer> bindus) {}

  public record AshtakavargaResponse(
      Long kundaliId,
      String calculationEngineVersion,
      List<AshtakavargaPlanetDto> bhinnashtakavarga,
      List<Integer> sarvashtakavarga,
      int totalBindus,
      String notes,
      String disclaimer) {}

  /** GET .../shadbala — PARTIAL scores only. */
  public record ShadbalaComponentDto(
      String code, String displayName, String status, BigDecimal virupas, String note) {}

  public record ShadbalaPlanetDto(
      String planetCode,
      String planetName,
      int signIndex,
      int house,
      List<ShadbalaComponentDto> components,
      List<String> implementedComponents,
      List<String> comingSoonComponents,
      BigDecimal partialTotalVirupas,
      String notes) {}

  public record ShadbalaResponse(
      Long kundaliId,
      String calculationEngineVersion,
      String completeness,
      List<ShadbalaPlanetDto> planets,
      String notes,
      String disclaimer) {}
}
