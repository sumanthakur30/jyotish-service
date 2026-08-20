package com.shopmanagement.jyotishservice.engine.matching;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

class AshtaKootaCalculatorTest {

  @Test
  void sameNakshatraScoresNadiZeroAndFullSameGana() {
    // Ashwini (0) vs Ashwini — same Nadi → 0; same Gana → 6; Tara Janma → 0
    MatchingPerson a = MatchingPerson.forTest("A", 0, 0, 3); // Aries Moon, Ashwini
    MatchingPerson b = MatchingPerson.forTest("B", 0, 0, 5);
    Map<KootaCode, KootaScore> map = index(AshtaKootaCalculator.score(a, b));

    assertEquals(0, map.get(KootaCode.NADI).obtained());
    assertEquals(6, map.get(KootaCode.GANA).obtained());
    assertEquals(0, map.get(KootaCode.TARA).obtained()); // Janma
    assertEquals(4, map.get(KootaCode.YONI).obtained()); // same yoni same polarity
  }

  @Test
  void differentNadiAshwiniAndBharaniScoresFullNadi() {
    // Ashwini Adi vs Bharani Madhya → Nadi 8
    MatchingPerson a = MatchingPerson.forTest("A", 0, 0, 3);
    MatchingPerson b = MatchingPerson.forTest("B", 1, 1, 5); // Taurus / Bharani
    Map<KootaCode, KootaScore> map = index(AshtaKootaCalculator.score(a, b));
    assertEquals(8, map.get(KootaCode.NADI).obtained());
  }

  @Test
  void taraSampatScoresFullWhenBoyIsNextNakshatra() {
    // From Bharani(1) to Krittika(2) → offset 1 → Tara 2 Sampat → 3
    MatchingPerson groom = MatchingPerson.forTest("Groom", 0, 2, 3); // Krittika
    MatchingPerson bride = MatchingPerson.forTest("Bride", 1, 1, 5); // Bharani
    Map<KootaCode, KootaScore> map = index(AshtaKootaCalculator.score(groom, bride));
    assertEquals(3, map.get(KootaCode.TARA).obtained());
  }

  @Test
  void bhakootDoshaForSixthEighthSigns() {
    // Aries(0) → Virgo(5) is 6th → Bhakoot 0
    MatchingPerson a = MatchingPerson.forTest("A", 0, 0, 3);
    MatchingPerson b = MatchingPerson.forTest("B", 5, 12, 5); // Hasta in Virgo
    Map<KootaCode, KootaScore> map = index(AshtaKootaCalculator.score(a, b));
    assertEquals(0, map.get(KootaCode.BHAKOOT).obtained());
  }

  @Test
  void bhakootFullForTrineFriendlyOffset() {
    // Aries(0) → Leo(4) is 5th → classical 5/9 dosha → 0
    MatchingPerson a = MatchingPerson.forTest("A", 0, 0, 3);
    MatchingPerson b = MatchingPerson.forTest("B", 4, 10, 5); // Magha
    Map<KootaCode, KootaScore> map = index(AshtaKootaCalculator.score(a, b));
    assertEquals(0, map.get(KootaCode.BHAKOOT).obtained());
  }

  @Test
  void bhakootFullForSameSign() {
    MatchingPerson a = MatchingPerson.forTest("A", 3, 7, 3); // Cancer / Pushya
    MatchingPerson b = MatchingPerson.forTest("B", 3, 8, 5); // Cancer / Ashlesha
    Map<KootaCode, KootaScore> map = index(AshtaKootaCalculator.score(a, b));
    assertEquals(7, map.get(KootaCode.BHAKOOT).obtained());
  }

  @Test
  void varnaPointWhenGroomRankHigherOrEqual() {
    // Cancer Brahmin vs Gemini Shudra → 1
    MatchingPerson a = MatchingPerson.forTest("A", 3, 7, 3);
    MatchingPerson b = MatchingPerson.forTest("B", 2, 5, 5);
    Map<KootaCode, KootaScore> map = index(AshtaKootaCalculator.score(a, b));
    assertEquals(1, map.get(KootaCode.VARNA).obtained());

    // Reverse: Shudra groom vs Brahmin bride → 0
    Map<KootaCode, KootaScore> rev = index(AshtaKootaCalculator.score(b, a));
    assertEquals(0, rev.get(KootaCode.VARNA).obtained());
  }

  @Test
  void ganaDevaRakshasaScoresZero() {
    // Ashwini Deva vs Krittika Rakshasa
    MatchingPerson a = MatchingPerson.forTest("A", 0, 0, 3);
    MatchingPerson b = MatchingPerson.forTest("B", 0, 2, 5);
    Map<KootaCode, KootaScore> map = index(AshtaKootaCalculator.score(a, b));
    assertEquals(0, map.get(KootaCode.GANA).obtained());
  }

  @Test
  void totalNeverExceedsThirtySix() {
    MatchingPerson a = MatchingPerson.forTest("A", 3, 7, 1);
    MatchingPerson b = MatchingPerson.forTest("B", 8, 20, 10); // Sag / Purva Ashadha
    List<KootaScore> scores = AshtaKootaCalculator.score(a, b);
    assertEquals(8, scores.size());
    assertEquals(36, KootaCode.totalMax());
    assertTrue(AshtaKootaCalculator.totalObtained(scores) <= 36);
  }

  @Test
  void registryBuildsReportWithDisclaimer() {
    MatchingPerson a = MatchingPerson.forTest("A", 0, 0, 1);
    MatchingPerson b = MatchingPerson.forTest("B", 6, 14, 7);
    MatchingReport report = MatchingRegistry.compute(a, b, "V1.4");
    assertEquals(8, report.kootas().size());
    assertEquals(36, report.maxScore());
    assertTrue(report.disclaimer().contains("Traditional compatibility indicators"));
    assertTrue(report.disclaimer().toLowerCase().contains("do not determine"));
    assertFalse(report.summary().toLowerCase().contains("marriage will succeed"));
    assertFalse(report.summary().toLowerCase().contains("marriage will fail"));
    assertTrue(MatchingRegistry.isImplemented(MatchingSystemCode.ASHTA_KOOTA));
    assertTrue(MatchingRegistry.isImplemented(MatchingSystemCode.MANGLIK));
    assertFalse(MatchingRegistry.isImplemented(MatchingSystemCode.DASHA_SANDHI));
  }

  private static Map<KootaCode, KootaScore> index(List<KootaScore> scores) {
    return scores.stream().collect(Collectors.toMap(KootaScore::koota, Function.identity()));
  }
}
