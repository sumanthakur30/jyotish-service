package com.shopmanagement.jyotishservice.api;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Life Analysis API contracts — notes are Jyotish-authored; indicators are calculated separately. */
public final class LifeAnalysisApi {

  private LifeAnalysisApi() {}

  public record DashboardResponse(
      Long kundaliId,
      List<CategorySummary> categories,
      /** Shared calculated strip (same Dasha/Gochar for every card). Null lords if no stored rows. */
      CalculatedTimelineStrip calculatedTimeline) {}

  public record CategorySummary(
      String category,
      String labelEn,
      String labelHi,
      String status,
      Instant updatedAt,
      boolean includeInReport,
      /**
       * Short date line for the card, e.g. {@code Jupiter / Saturn · until 2027-03-12}. From stored
       * Vimshottari only — never invented.
       */
      String currentDashaLine,
      Instant currentDashaEndAt) {}

  public record AnalysisDetailResponse(
      Long id,
      Long kundaliId,
      String category,
      String subCategory,
      String status,
      String pastNotes,
      String presentNotes,
      String futureNotes,
      String importantPeriodsNotes,
      String advice,
      String jyotishNotes,
      Map<String, String> sections,
      boolean includeInReport,
      List<IndicatorItem> indicators,
      String healthDisclaimerEn,
      String healthDisclaimerHi,
      Instant createdAt,
      Instant updatedAt,
      String updatedBy,
      /** Calculated Dasha/Gochar + optional topic periods — separate from Jyotish notes. */
      CalculatedTimelineStrip calculatedTimeline) {}

  public record IndicatorItem(String code, String label, String value, String source) {}

  /** One stored Vimshottari (or similar) period row exposed as facts only. */
  public record CalculatedDashaPeriod(
      String levelCode,
      String lordCode,
      String lordName,
      String mahaLordCode,
      String mahaLordName,
      String antarLordCode,
      String antarLordName,
      Instant startAt,
      Instant endAt,
      String basis) {}

  public record CurrentDashaStrip(
      String systemCode,
      CalculatedDashaPeriod maha,
      CalculatedDashaPeriod antar,
      /** e.g. Jupiter / Saturn · until 2027-03-12 */
      String summaryLine) {}

  public record GocharPlanetFact(
      String planetCode, String planetName, String signName, Integer house) {}

  public record GocharAsOf(
      LocalDate transitDate,
      List<GocharPlanetFact> planets,
      /** Short factual summary, e.g. JU Aries H5; SA Capricorn H2 */
      String summaryLine) {}

  /**
   * Date-wise calculated strip. All dates come from persisted Dasha / Gochar (or topic period rows).
   * Never invents outcomes or prediction text.
   */
  public record CalculatedTimelineStrip(
      Instant asOf,
      CurrentDashaStrip currentDasha,
      List<CalculatedDashaPeriod> upcomingDasha,
      GocharAsOf gocharAsOf,
      List<PeriodDto> topicPeriods) {}

  public record UpsertAnalysisRequest(
      String status,
      String pastNotes,
      String presentNotes,
      String futureNotes,
      String importantPeriodsNotes,
      String advice,
      String jyotishNotes,
      Map<String, String> sections,
      Boolean includeInReport,
      String updatedBy,
      /** When true, also append a consultation history row. */
      Boolean recordConsultation,
      String consultationObservation,
      String consultationDashaSnapshot,
      String consultationGocharSnapshot,
      String consultationAdvice,
      LocalDate followUpDate) {}

  public record PeriodDto(
      Long id,
      String category,
      LocalDate fromDate,
      LocalDate toDate,
      String topic,
      String observation,
      String calculationBasis,
      String status,
      int sortOrder) {}

  public record UpsertPeriodRequest(
      @NotBlank String category,
      LocalDate fromDate,
      LocalDate toDate,
      @NotBlank String topic,
      String observation,
      String calculationBasis,
      String status,
      Integer sortOrder,
      String updatedBy) {}

  public record PeriodListResponse(Long kundaliId, List<PeriodDto> periods) {}

  public record HistoryItem(
      Long id, String fieldName, String oldValue, String newValue, String updatedBy, Instant createdAt) {}

  public record HistoryListResponse(Long lifeAnalysisId, List<HistoryItem> items) {}

  public record ConsultationItem(
      Long id,
      String category,
      String observation,
      String dashaSnapshot,
      String gocharSnapshot,
      String advice,
      LocalDate followUpDate,
      String createdBy,
      Instant createdAt) {}

  public record ConsultationListResponse(Long kundaliId, List<ConsultationItem> items) {}

  public record SearchHit(
      String category, String field, String snippet, Long analysisId) {}

  public record SearchResponse(Long kundaliId, String query, List<SearchHit> hits) {}

  public record ReportSelectionRequest(@NotNull List<String> categories) {}
}
