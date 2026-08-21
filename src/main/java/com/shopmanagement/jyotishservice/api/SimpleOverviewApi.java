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

  public record LifeAreaCard(
      String category,
      String labelEn,
      String labelHi,
      String status,
      String currentDashaLine,
      Instant currentDashaEndAt) {}

  public record UpcomingItem(
      String levelCode,
      String labelEn,
      String labelHi,
      String lordCode,
      String lordName,
      String mahaLordCode,
      String mahaLordName,
      Instant startAt,
      Instant endAt) {}

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
