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
 * Known-date checks for Tithi / Nakshatra. Values are relative to Meeus + Lahiri engine (V1.6) —
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

    assertEquals("V1.6", r.engineVersion());
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
    assertTrue(
        r.comingSoon().stream().anyMatch(c -> "CHOGHADIYA".equals(c.code())));
    assertTrue(r.comingSoon().stream().anyMatch(c -> "RAHU_KAAL".equals(c.code())));
  }

  @Test
  void knownDelhi2024Jan1_tithiAndNakshatraStable() {
    // Regression anchors for Meeus+Lahiri at Delhi sunrise on 2024-01-01.
    PanchangResult r =
        engine.computePanchang(
            new PanchangRequest(
                LocalDate.of(2024, 1, 1),
                28.6139,
                77.2090,
                "Asia/Kolkata",
                "Delhi, India",
                AyanamsaMode.LAHIRI));

    assertEquals("V1.6", r.engineVersion());
    // Krishna/Shukla + tithi name must be consistent with index
    assertEquals(PanchangCatalog.tithiName(r.tithi().index()), r.tithi().name());
    assertTrue(r.tithi().progress() >= 0 && r.tithi().progress() < 1.0001);
    // Nakshatra name matches catalog
    assertEquals(
        com.shopmanagement.jyotishservice.engine.astro.ZodiacCatalog.nakshatraName(
            r.nakshatra().index()),
        r.nakshatra().name());
    // Fixed known indices for this engine (update only if Meeus/ayanamsa intentionally changes)
    assertEquals(19, r.tithi().index(), "expected tithi index for 2024-01-01 Delhi sunrise");
    assertEquals("Krishna", r.tithi().paksha());
    assertEquals("Panchami", r.tithi().name());
    assertEquals(9, r.nakshatra().index(), "expected nakshatra index (Magha) for 2024-01-01");
    assertEquals("Magha", r.nakshatra().name());
  }

  @Test
  void catalogListsComingSoonStubs() {
    assertTrue(PanchangRegistry.comingSoon().size() >= 3);
    assertTrue(
        PanchangRegistry.catalog().stream()
            .anyMatch(f -> f.implemented() && "PANCHANG_CORE".equals(f.code())));
  }
}
