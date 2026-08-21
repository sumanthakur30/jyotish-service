package com.shopmanagement.jyotishservice.api;

import java.time.Instant;
import java.util.List;

/**
 * Customer Simple View contracts. All values are derived from stored D1 / Dasha / Yoga / Life
 * Analysis facts — never invented positions, dates, or predictions.
 */
public final class SimpleOverviewApi {

  private SimpleOverviewApi() {}

  public record SimpleOverviewResponse(
      Long kundaliId,
      String displayName,
      Instant asOf,
      boolean calculationNotAvailable,
      GlanceCard lagna,
      GlanceCard moonRashi,
      GlanceCard nakshatra,
      GlanceCard currentDashaGlance,
      CurrentLifePeriodCard currentLifePeriod,
      List<LifeAreaCard> lifeAreas,
      List<UpcomingItem> upcoming,
      List<PresentYogaFact> presentYogas,
      List<LordTheme> lordThemes,
      TechnicalDetails technical,
      String generalDisclaimerEn,
      String generalDisclaimerHi,
      String healthDisclaimerEn,
      String healthDisclaimerHi) {}

  public record GlanceCard(
      String code,
      boolean available,
      String valueEn,
      String valueHi,
      String whatIsThisEn,
      String whatIsThisHi) {}

  public record FactBullet(String code, String labelEn, String labelHi, String value) {}

  public record ExplainedBlock(
      boolean calculationNotAvailable,
      List<String> paragraphsEn,
      List<String> paragraphsHi,
      List<FactBullet> whyFacts) {}

  public record CurrentLifePeriodCard(
      String titleEn,
      String titleHi,
      Instant startAt,
      Instant endAt,
      String mahaLordCode,
      String mahaLordName,
      String antarLordCode,
      String antarLordName,
      ExplainedBlock explanation) {}

  /**
   * One life-area tile. Period dates live on the shared current-period banner — do not duplicate
   * identical dasha strips on every card. Topic text is templated from D1 houses/planets only.
   * Legacy dasha/next fields stay null for API stability (prefer section banner / upcoming list).
   */
  public record LifeAreaCard(
      String category,
      String labelEn,
      String labelHi,
      String status,
      /** Short factual status line (Jyotish notes state) — not a prediction. */
      String statusLineEn,
      String statusLineHi,
      String currentDashaLine,
      Instant currentDashaEndAt,
      Instant nextPeriodAt,
      String nextPeriodLineEn,
      String nextPeriodLineHi,
      /** e.g. "10th Capricorn · lord Saturn · Sun, Mercury" */
      String focusSummaryEn,
      String focusSummaryHi,
      /** 1–2 qualified sentences from chart facts (not Phaladesh). */
      List<String> summaryParagraphsEn,
      List<String> summaryParagraphsHi,
      /** Expandable Why? facts (houses, lords, key graha, optional dasha-lord house). */
      List<FactBullet> factBullets,
      List<String> relevantPlanetLinesEn,
      List<String> relevantPlanetLinesHi,
      List<Integer> focusHouses) {}

  /**
   * Upcoming chapter row — print-style: dates · lord · natal house/sign when D1 placement exists.
   */
  public record UpcomingItem(
      String levelCode,
      String labelEn,
      String labelHi,
      String lordCode,
      String lordName,
      String mahaLordCode,
      String mahaLordName,
      Instant startAt,
      Instant endAt,
      /** Natal sign of the period lord from stored D1; null if unknown. */
      String lordSignName,
      /** Natal house of the period lord from stored D1; 0 if unknown. */
      int lordHouse,
      /** e.g. "Rahu in Aquarius · house 11" */
      String placementLineEn,
      String placementLineHi,
      /** Short qualified gloss from placement + theme; never absolute Phaladesh. */
      String glossEn,
      String glossHi) {}

  public record PresentYogaFact(
      String yogaCode, String displayName, String strengthCode, List<String> planets) {}

  public record LordTheme(
      String lordCode,
      String nameEn,
      String nameHi,
      List<String> meaningEn,
      List<String> meaningHi) {}

  public record TechnicalDetails(
      String ayanamsaCode,
      String houseSystem,
      String calculationEngineVersion,
      String zodiacSystem,
      String dashaSystemCode) {}

  /** Explain a stored dasha period for Simple journey / “Understand this period”. */
  public record SimplePeriodExplainResponse(
      Long kundaliId,
      boolean calculationNotAvailable,
      String levelCode,
      String mahaLordCode,
      String mahaLordName,
      String antarLordCode,
      String antarLordName,
      Instant startAt,
      Instant endAt,
      ExplainedBlock explanation,
      String generalDisclaimerEn,
      String generalDisclaimerHi) {}
}
