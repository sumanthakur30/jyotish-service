package com.shopmanagement.jyotishservice.engine.matching;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ManglikAnalyzerTest {

  @Test
  void marsInSeventhIsPresent() {
    MatchingPerson p = MatchingPerson.forTest("P", 0, 0, 7);
    ManglikAssessment a = ManglikAnalyzer.assess(p);
    assertEquals(ManglikStatus.PRESENT, a.status());
    assertTrue(a.present());
    assertEquals(7, a.marsHouse());
    assertTrue(a.reasoning().contains("house 7"));
    assertTrue(a.cancellationsComingSoon());
    assertTrue(a.cancellationsNote().contains("Coming Soon"));
  }

  @Test
  void marsInFirstSecondFourthEighthTwelfthArePresent() {
    for (int house : new int[] {1, 2, 4, 8, 12}) {
      ManglikAssessment a = ManglikAnalyzer.assess(MatchingPerson.forTest("P", 0, 0, house));
      assertTrue(a.present(), "expected present for house " + house);
    }
  }

  @Test
  void marsInThirdFifthSixthNinthTenthEleventhAreAbsent() {
    for (int house : new int[] {3, 5, 6, 9, 10, 11}) {
      ManglikAssessment a = ManglikAnalyzer.assess(MatchingPerson.forTest("P", 0, 0, house));
      assertEquals(ManglikStatus.ABSENT, a.status(), "expected absent for house " + house);
      assertFalse(a.present());
    }
  }

  @Test
  void relevantHousesDocumented() {
    assertEquals(java.util.List.of(1, 2, 4, 7, 8, 12), ManglikAnalyzer.RELEVANT_HOUSES);
  }
}
