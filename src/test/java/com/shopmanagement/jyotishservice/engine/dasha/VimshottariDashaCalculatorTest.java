package com.shopmanagement.jyotishservice.engine.dasha;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;

import com.shopmanagement.jyotishservice.engine.astro.ZodiacCatalog;
import com.shopmanagement.jyotishservice.engine.model.Planet;

/**
 * Vimshottari balance + lord regressions (no ephemeris). Date math uses 365.25 d/y.
 */
class VimshottariDashaCalculatorTest {

  private final VimshottariDashaCalculator calc = VimshottariDashaCalculator.INSTANCE;

  @Test
  void ashwiniStart_ketuFullBalance() {
    // Moon at 0° → Ashwini start → Ketu MD, elapsed 0, balance 7y
    var bal = VimshottariDashaCalculator.balanceAtBirth(0.0);
    assertEquals(0, bal.nakshatraIndex());
    assertEquals("Ashwini", bal.nakshatraName());
    assertEquals(Planet.KETU, bal.lord());
    assertEquals(7.0, bal.fullYears(), 1e-9);
    assertEquals(0.0, bal.elapsedYears(), 1e-9);
    assertEquals(7.0, bal.balanceYears(), 1e-9);
  }

  @Test
  void ashwiniMid_ketuHalfBalance() {
    double mid = ZodiacCatalog.NAKSHATRA_SPAN / 2.0;
    var bal = VimshottariDashaCalculator.balanceAtBirth(mid);
    assertEquals(Planet.KETU, bal.lord());
    assertEquals(3.5, bal.balanceYears(), 1e-9);
    assertEquals(3.5, bal.elapsedYears(), 1e-9);
  }

  @Test
  void bharaniStart_venusFullBalance() {
    double lon = ZodiacCatalog.NAKSHATRA_SPAN; // start of Bharani
    var bal = VimshottariDashaCalculator.balanceAtBirth(lon);
    assertEquals(1, bal.nakshatraIndex());
    assertEquals("Bharani", bal.nakshatraName());
    assertEquals(Planet.VENUS, bal.lord());
    assertEquals(20.0, bal.balanceYears(), 1e-9);
  }

  @Test
  void maghaStart_ketuAgain() {
    // Magha = index 9 → Ketu
    double lon = 9 * ZodiacCatalog.NAKSHATRA_SPAN;
    assertEquals(Planet.KETU, VimshottariDashaCalculator.nakshatraLord(9));
    assertEquals(Planet.KETU, VimshottariDashaCalculator.balanceAtBirth(lon).lord());
  }

  @Test
  void firstMahadashaLord_matchesMoonNakshatra() {
    Instant birth = Instant.parse("1990-01-15T06:30:00Z");
    DashaTimeline tl = calc.compute(ZodiacCatalog.NAKSHATRA_SPAN * 5.25, birth, "V1.2");
    // Ardra = index 5 → Rahu
    assertEquals(Planet.RAHU, tl.birthMahadashaLord());
    assertEquals(Planet.RAHU, tl.mahadashas().get(0).lord());
    assertEquals(DashaLevel.MAHA, tl.mahadashas().get(0).level());
    assertFalse(tl.mahadashas().get(0).children().isEmpty());
    assertTrue(tl.mahadashas().get(0).children().get(0).children().size() > 0);
  }

  @Test
  void balanceDuration_matchesFirstMahadashaWallClock() {
    Instant birth = Instant.parse("2000-06-01T00:00:00Z");
    double lon = ZodiacCatalog.NAKSHATRA_SPAN / 4.0; // 25% into Ashwini → balance 5.25y
    var bal = VimshottariDashaCalculator.balanceAtBirth(lon);
    assertEquals(5.25, bal.balanceYears(), 1e-9);

    DashaTimeline tl = calc.compute(lon, birth, "V1.2");
    DashaPeriod first = tl.mahadashas().get(0);
    double years =
        ChronoUnit.MILLIS.between(first.startAt(), first.endAt())
            / (VimshottariDashaCalculator.DAYS_PER_YEAR * 24.0 * 3600.0 * 1000.0);
    assertEquals(bal.balanceYears(), years, 1e-6);
  }

  @Test
  void plusYears_dateMathRegression() {
    Instant start = Instant.parse("2020-01-01T00:00:00Z");
    Instant end = VimshottariDashaCalculator.plusYears(start, 1.0);
    long days = ChronoUnit.DAYS.between(start, end);
    assertEquals(365, days); // 365.25 rounded via millis → 365 full days + fraction
    // Exact millis check
    long expectedMillis = Math.round(365.25 * 24.0 * 3600.0 * 1000.0);
    assertEquals(expectedMillis, ChronoUnit.MILLIS.between(start, end));
  }

  @Test
  void cycleYears_sumTo120() {
    double sum = 0;
    for (Planet p : VimshottariDashaCalculator.LORD_ORDER) {
      sum += VimshottariDashaCalculator.fullYears(p);
    }
    assertEquals(120.0, sum, 1e-9);
  }

  @Test
  void registry_vimshottariImplemented_yoginiComingSoon() {
    assertTrue(DashaRegistry.isImplemented(DashaSystemCode.VIMSHOTTARI));
    assertFalse(DashaRegistry.isImplemented(DashaSystemCode.YOGINI));
    assertThrows(
        IllegalArgumentException.class,
        () -> DashaRegistry.requireCalculator(DashaSystemCode.YOGINI));
  }
}
