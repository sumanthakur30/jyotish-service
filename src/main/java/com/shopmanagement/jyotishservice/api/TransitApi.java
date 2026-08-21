package com.shopmanagement.jyotishservice.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import jakarta.validation.constraints.NotNull;

public final class TransitApi {

  private TransitApi() {}

  public record TransitRequestBody(
      @NotNull Long kundaliId, LocalDate date, LocalTime time) {}

  public record TransitCatalogItem(
      String systemCode, String displayName, boolean implemented, String status) {}

  public record ComingSoonFeature(String code, String label) {}

  public record TransitPlanetDto(
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
      BigDecimal speedDegPerDay,
      BigDecimal natalLongitudeDeg,
      Integer natalSignIndex,
      String natalSignName,
      Integer natalHouse,
      boolean signChanged,
      boolean houseChanged) {}

  public record TransitResponse(
      Long id,
      Long kundaliId,
      LocalDate transitDate,
      LocalTime transitTime,
      String timeZone,
      String systemCode,
      String systemDisplayName,
      String calculationEngineVersion,
      String ayanamsaCode,
      BigDecimal ayanamsaDeg,
      BigDecimal julianDayUt,
      int natalLagnaSignIndex,
      List<TransitPlanetDto> planets,
      SadeSatiDto sadeSati,
      List<TransitCatalogItem> catalog,
      List<ComingSoonFeature> comingSoon,
      String notes,
      String disclaimer,
      Instant createdAt) {}

  public record SadeSatiDto(
      String phaseCode,
      String phaseLabel,
      int natalMoonSignIndex,
      String natalMoonSignName,
      int transitSaturnSignIndex,
      String transitSaturnSignName,
      int signsFromMoon,
      int houseFromMoon,
      boolean inSadeSati,
      String notes) {}
}
