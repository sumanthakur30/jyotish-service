package com.shopmanagement.jyotishservice.engine.panchang;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.shopmanagement.jyotishservice.engine.CalculationEngine;
import com.shopmanagement.jyotishservice.engine.model.AyanamsaMode;

/**
 * Known-date checks for Tithi / Nakshatra. Values are relative to Meeus + Lahiri engine (V1.7) —
 * not Swiss Ephemeris gold, but stable regression anchors.
 */
class PanchangCalculatorTest {

  private final CalculationEngine engine = new CalculationEngine();

  @Test
  void delhiIndependenceDay1990_hasTithiAndNakshatra() {
    PanchangResult r =
        engine.computePanchang(
            new PanchangRequest(
                LocalDate.of(1990, 8, 15),
                28.6139,
                77.2090,
                "Asia/Kolkata",
                "Delhi, India",
                AyanamsaMode.LAHIRI));

    assertEquals("V1.7", r.engineVersion());
    assertNotNull(r.tithi());
    assertNotNull(r.nakshatra());
    assertTrue(r.tithi().index() >= 0 && r.tithi().index() <= 29);
    assertTrue(r.nakshatra().index() >= 0 && r.nakshatra().index() <= 26);
    assertTrue(r.nakshatra().pada() >= 1 && r.nakshatra().pada() <= 4);
    assertEquals(PanchangCatalog.pakshaName(r.tithi().index()), r.tithi().paksha());
    assertEquals(PanchangCatalog.tithiName(r.tithi().index()), r.tithi().name());
    assertNotNull(r.yoga().name());
    assertNotNull(r.karana().name());
    assertNotNull(r.vara().name());
    assertTrue(r.sunrise().available());
    assertTrue(r.sunset().available());
    assertFalse(r.moonrise().available());
    assertNotNull(r.muhurat());
    assertTrue(r.muhurat().periods().stream().anyMatch(p -> "RAHU_KAAL".equals(p.code())));
    assertTrue(r.muhurat().periods().stream().anyMatch(p -> "ABHIJIT".equals(p.code())));
    assertTrue(r.comingSoon().stream().anyMatch(c -> "MOONRISE_MOONSET".equals(c.code())));
    assertFalse(r.comingSoon().stream().anyMatch(c -> "CHOGHADIYA".equals(c.code())));
    assertFalse(r.comingSoon().stream().anyMatch(c -> "RAHU_KAAL".equals(c.code())));
  }

  @Test
  void knownDelhi2024Jan1_tithiAndNakshatraStable() {
    PanchangResult r =
        engine.computePanchang(
            new PanchangRequest(
                LocalDate.of(2024, 1, 1),
                28.6139,
                77.2090,
                "Asia/Kolkata",
                "Delhi, India",
                AyanamsaMode.LAHIRI));

    assertEquals("V1.7", r.engineVersion());
    assertEquals(PanchangCatalog.tithiName(r.tithi().index()), r.tithi().name());
    assertTrue(r.tithi().progress() >= 0 && r.tithi().progress() < 1.0001);
    assertEquals(
        com.shopmanagement.jyotishservice.engine.astro.ZodiacCatalog.nakshatraName(
            r.nakshatra().index()),
        r.nakshatra().name());
    assertEquals(19, r.tithi().index(), "expected tithi index for 2024-01-01 Delhi sunrise");
    assertEquals("Krishna", r.tithi().paksha());
    assertEquals("Panchami", r.tithi().name());
    assertEquals(9, r.nakshatra().index(), "expected nakshatra index (Magha) for 2024-01-01");
    assertEquals("Magha", r.nakshatra().name());
  }

  @Test
  void catalogListsMuhuratReadyAndMoonComingSoon() {
    assertEquals(1, PanchangRegistry.comingSoon().size());
    assertTrue(
        PanchangRegistry.catalog().stream()
            .anyMatch(f -> f.implemented() && "RAHU_KAAL".equals(f.code())));
    assertTrue(
        PanchangRegistry.catalog().stream()
            .anyMatch(f -> f.implemented() && "CHOGHADIYA".equals(f.code())));
  }

  @Test
  void rahuKaalWeekdaySegments() {
    assertEquals(8, MuhuratCalculator.rahuKaalSegment(0));
    assertEquals(2, MuhuratCalculator.rahuKaalSegment(1));
    assertEquals(7, MuhuratCalculator.rahuKaalSegment(2));
    assertEquals(5, MuhuratCalculator.rahuKaalSegment(3));
    assertEquals(6, MuhuratCalculator.rahuKaalSegment(4));
    assertEquals(4, MuhuratCalculator.rahuKaalSegment(5));
    assertEquals(3, MuhuratCalculator.rahuKaalSegment(6));
  }
}
