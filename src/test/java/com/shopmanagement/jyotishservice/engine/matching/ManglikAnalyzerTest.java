package com.shopmanagement.jyotishservice.engine.matching;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ManglikAnalyzerTest {

  @Test
  void marsInSeventhIsPresentWhenNoCancel() {
    // Mars house 7, Mars in Gemini (2), Jupiter elsewhere — no cancel
    MatchingPerson p = MatchingPerson.forTest("P", 0, 0, 7, 2, 5);
    ManglikAssessment a = ManglikAnalyzer.assess(p);
    assertEquals(ManglikStatus.PRESENT, a.status());
    assertTrue(a.present());
    assertFalse(a.cancelled());
    assertEquals(7, a.marsHouse());
    assertTrue(a.reasoning().contains("house 7"));
    assertFalse(a.cancellationsComingSoon());
    assertTrue(a.appliedCancellations().isEmpty());
  }

  @Test
  void marsOwnSignCancels() {
    MatchingPerson p = MatchingPerson.forTest("P", 0, 0, 7, 0, 5); // Aries Mars
    ManglikAssessment a = ManglikAnalyzer.assess(p);
    assertEquals(ManglikStatus.CANCELLED, a.status());
    assertTrue(a.cancelled());
    assertFalse(a.present());
    assertTrue(a.appliedCancellations().stream().anyMatch(r -> "MARS_OWN_SIGN".equals(r.code())));
  }

  @Test
  void marsExaltedCancels() {
    MatchingPerson p = MatchingPerson.forTest("P", 0, 0, 1, 9, 3); // Capricorn
    ManglikAssessment a = ManglikAnalyzer.assess(p);
    assertEquals(ManglikStatus.CANCELLED, a.status());
    assertTrue(a.appliedCancellations().stream().anyMatch(r -> "MARS_EXALTED".equals(r.code())));
  }

  @Test
  void jupiterWithMarsCancels() {
    MatchingPerson p = MatchingPerson.forTest("P", 0, 0, 8, 2, 2);
    ManglikAssessment a = ManglikAnalyzer.assess(p);
    assertEquals(ManglikStatus.CANCELLED, a.status());
    assertTrue(
        a.appliedCancellations().stream().anyMatch(r -> "JUPITER_WITH_MARS".equals(r.code())));
  }

  @Test
  void mutualManglikCancelsBoth() {
    MatchingPerson a = MatchingPerson.forTest("A", 0, 0, 7, 2, 5);
    MatchingPerson b = MatchingPerson.forTest("B", 1, 1, 8, 3, 6);
    MatchingReport report = MatchingRegistry.compute(a, b, "V1.7");
    assertTrue(report.manglikA().cancelled());
    assertTrue(report.manglikB().cancelled());
    assertTrue(
        report.manglikA().appliedCancellations().stream()
            .anyMatch(r -> "MUTUAL_MANGLIK".equals(r.code())));
  }

  @Test
  void marsInFirstSecondFourthEighthTwelfthArePresent() {
    for (int house : new int[] {1, 2, 4, 8, 12}) {
      ManglikAssessment a =
          ManglikAnalyzer.assess(MatchingPerson.forTest("P", 0, 0, house, 2, 5));
      assertTrue(a.placementManglik(), "expected placement for house " + house);
      assertEquals(ManglikStatus.PRESENT, a.status(), "expected present for house " + house);
    }
  }

  @Test
  void marsInThirdFifthSixthNinthTenthEleventhAreAbsent() {
    for (int house : new int[] {3, 5, 6, 9, 10, 11}) {
      ManglikAssessment a =
          ManglikAnalyzer.assess(MatchingPerson.forTest("P", 0, 0, house, 2, 5));
      assertEquals(ManglikStatus.ABSENT, a.status(), "expected absent for house " + house);
      assertFalse(a.present());
    }
  }

  @Test
  void relevantHousesDocumented() {
    assertEquals(java.util.List.of(1, 2, 4, 7, 8, 12), ManglikAnalyzer.RELEVANT_HOUSES);
  }
}
