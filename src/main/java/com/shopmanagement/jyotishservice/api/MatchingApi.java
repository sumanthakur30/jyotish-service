package com.shopmanagement.jyotishservice.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import jakarta.validation.constraints.NotNull;

public final class MatchingApi {

  private MatchingApi() {}

  public record MatchRequest(@NotNull Long profileIdA, @NotNull Long profileIdB) {}

  public record MatchingCatalogItem(
      String systemCode, String displayName, boolean implemented, String status) {}

  public record PersonSummaryDto(
      Long profileId,
      String displayName,
      Long kundaliId,
      int moonSignIndex,
      String moonSignName,
      int moonNakshatraIndex,
      String moonNakshatraName) {}

  public record KootaScoreDto(
      String kootaCode,
      String displayName,
      int obtained,
      int maxPoints,
      String explanation,
      String ruleId) {}

  public record ManglikDto(
      String status,
      String statusLabel,
      boolean present,
      int marsHouse,
      int marsSignIndex,
      String marsSignName,
      List<Integer> relevantHouses,
      String reasoning,
      boolean cancellationsComingSoon,
      String cancellationsNote) {}

  public record MatchingResponse(
      Long id,
      PersonSummaryDto personA,
      PersonSummaryDto personB,
      List<KootaScoreDto> kootas,
      int totalScore,
      int maxScore,
      BigDecimal percentage,
      ManglikDto manglikA,
      ManglikDto manglikB,
      String summary,
      String notes,
      String disclaimer,
      String calculationEngineVersion,
      List<MatchingCatalogItem> catalog,
      Instant createdAt) {}
}
