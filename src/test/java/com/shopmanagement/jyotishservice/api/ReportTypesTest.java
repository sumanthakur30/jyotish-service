package com.shopmanagement.jyotishservice.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ReportTypesTest {

  @Test
  void resolvesCanonicalUnchanged() {
    assertEquals(ReportTypes.BASIC_KUNDALI, ReportTypes.resolve("BASIC_KUNDALI"));
    assertEquals(ReportTypes.MATCHING, ReportTypes.resolve("matching"));
    assertEquals(ReportTypes.DASHA_SUMMARY, ReportTypes.resolve("DASHA_SUMMARY"));
    assertEquals(ReportTypes.TRANSIT, ReportTypes.resolve("transit"));
  }

  @Test
  void resolvesBasicKundaliAliases() {
    assertEquals(ReportTypes.BASIC_KUNDALI, ReportTypes.resolve("KUNDALI_SUMMARY"));
    assertEquals(ReportTypes.BASIC_KUNDALI, ReportTypes.resolve("kundali"));
    assertEquals(ReportTypes.BASIC_KUNDALI, ReportTypes.resolve("BASIC"));
  }

  @Test
  void blankTypeRejected() {
    assertThrows(IllegalArgumentException.class, () -> ReportTypes.resolve("  "));
    assertThrows(IllegalArgumentException.class, () -> ReportTypes.resolve(null));
  }

  @Test
  void unknownRemainsUppercasedForCallerRejection() {
    assertEquals("NOPE", ReportTypes.resolve("nope"));
    assertFalse(ReportTypes.isCanonical("NOPE"));
    assertTrue(ReportTypes.isCanonical(ReportTypes.BASIC_KUNDALI));
  }
}
