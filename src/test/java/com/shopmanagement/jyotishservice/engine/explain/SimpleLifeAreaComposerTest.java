package com.shopmanagement.jyotishservice.engine.explain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.shopmanagement.jyotishservice.api.KundaliApi.HouseDto;
import com.shopmanagement.jyotishservice.api.KundaliApi.PlanetDto;
import com.shopmanagement.jyotishservice.api.SimpleOverviewApi.LifeAreaCard;
import com.shopmanagement.jyotishservice.engine.explain.SimpleExplanationComposer.LordPlacement;
import com.shopmanagement.jyotishservice.engine.explain.SimpleLifeAreaComposer.PeriodChapterExtras;
import com.shopmanagement.jyotishservice.engine.life.LifeCategory;

class SimpleLifeAreaComposerTest {

  @Test
  void careerAndMarriage_differInHousesAndPlanets() {
    List<HouseDto> houses =
        List.of(
            house(7, "Libra"),
            house(10, "Capricorn"),
            house(6, "Virgo"),
            house(2, "Taurus"),
            house(11, "Aquarius"));
    List<PlanetDto> planets =
        List.of(
            planet("SUN", "Capricorn", 10),
            planet("VENUS", "Taurus", 2),
            planet("JUPITER", "Pisces", 12),
            planet("SATURN", "Aquarius", 11),
            planet("MERCURY", "Capricorn", 10));

    LifeAreaCard career =
        SimpleLifeAreaComposer.compose(
            LifeCategory.CAREER,
            "NOT_STARTED",
            houses,
            planets,
            new LordPlacement("Aquarius", 11, "Shatabhisha"),
            new LordPlacement("Aquarius", 11, "Shatabhisha"),
            "RAHU",
            "RAHU");
    LifeAreaCard marriage =
        SimpleLifeAreaComposer.compose(
            LifeCategory.MARRIAGE,
            "NOT_STARTED",
            houses,
            planets,
            new LordPlacement("Aquarius", 11, "Shatabhisha"),
            new LordPlacement("Aquarius", 11, "Shatabhisha"),
            "RAHU",
            "RAHU");

    assertNotNull(career);
    assertNotNull(marriage);
    assertTrue(career.focusSummaryEn().contains("10th"));
    assertTrue(career.focusSummaryEn().toLowerCase().contains("capricorn"));
    assertTrue(marriage.focusSummaryEn().contains("7th"));
    assertTrue(marriage.focusSummaryEn().toLowerCase().contains("libra"));
    assertNotEquals(career.focusSummaryEn(), marriage.focusSummaryEn());
    assertTrue(
        career.relevantPlanetLinesEn().stream().anyMatch(l -> l.toLowerCase().contains("sun")));
    assertTrue(
        marriage.relevantPlanetLinesEn().stream()
            .anyMatch(l -> l.toLowerCase().contains("venus")));
    assertFalse(career.summaryParagraphsEn().isEmpty());
    assertFalse(marriage.summaryParagraphsHi().isEmpty());
    assertTrue(career.factBullets().stream().anyMatch(f -> "H10".equals(f.code())));
    assertTrue(marriage.factBullets().stream().anyMatch(f -> "H7".equals(f.code())));
    // Duplicate dasha strip fields must stay empty on cards
    assertTrue(career.currentDashaLine() == null);
    assertTrue(marriage.nextPeriodLineEn() == null);
  }

  @Test
  void education_uses45And9AndMercuryJupiter() {
    List<HouseDto> houses =
        List.of(house(4, "Cancer"), house(5, "Leo"), house(9, "Sagittarius"));
    List<PlanetDto> planets =
        List.of(planet("MERCURY", "Virgo", 6), planet("JUPITER", "Sagittarius", 9));

    LifeAreaCard edu =
        SimpleLifeAreaComposer.compose(
            LifeCategory.EDUCATION, "IN_PROGRESS", houses, planets, null, null, null, null);

    assertNotNull(edu);
    assertTrue(edu.focusSummaryEn().contains("4th") || edu.focusSummaryEn().contains("5th"));
    assertTrue(
        edu.relevantPlanetLinesEn().stream().anyMatch(l -> l.toLowerCase().contains("mercury")));
    assertTrue(edu.statusLineEn().toLowerCase().contains("progress"));
  }

  @Test
  void health_includesDisclaimerBullet() {
    List<HouseDto> houses = List.of(house(1, "Aries"), house(6, "Virgo"), house(8, "Scorpio"));
    LifeAreaCard health =
        SimpleLifeAreaComposer.compose(
            LifeCategory.HEALTH, "NOT_STARTED", houses, List.of(), null, null, null, null);
    assertNotNull(health);
    assertTrue(health.factBullets().stream().anyMatch(f -> "HEALTH_NOTE".equals(f.code())));
    String joined = String.join(" ", health.summaryParagraphsEn()).toLowerCase();
    assertTrue(joined.contains("medical") || joined.contains("traditional"));
  }

  @Test
  void chapterExtras_usesStoredPlacementOnly() {
    PeriodChapterExtras extras =
        SimpleLifeAreaComposer.chapterExtras(
            "MARS", new LordPlacement("Aries", 1, "Ashwini"));
    assertTrue(extras.placementLineEn().toLowerCase().contains("mars"));
    assertTrue(extras.placementLineEn().contains("house 1"));
    assertTrue(extras.glossEn().toLowerCase().contains("not a prediction"));
    assertFalse(extras.glossEn().toLowerCase().contains("will become"));
  }

  private static HouseDto house(int n, String sign) {
    return new HouseDto(n, 0, sign, BigDecimal.ZERO);
  }

  private static PlanetDto planet(String code, String sign, int house) {
    return new PlanetDto(
        code,
        code,
        BigDecimal.ZERO,
        0,
        sign,
        BigDecimal.ZERO,
        house,
        0,
        null,
        0,
        false,
        false,
        BigDecimal.ZERO);
  }
}
