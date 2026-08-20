package com.shopmanagement.jyotishservice.engine.varga;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.shopmanagement.jyotishservice.engine.astro.ZodiacCatalog;

/**
 * Navamsha (D9) mapping regression tests.
 *
 * <p><b>Tolerances:</b> exact sign index; longitude within ±0.01° of the analytically expected
 * value for boundary and mid-part cases (no ephemeris involved — pure geometric mapping).
 */
class NavamshaVargaMapperTest {

  private static final double LON_TOL = 0.01;
  private final NavamshaVargaMapper mapper = NavamshaVargaMapper.INSTANCE;

  @Test
  void aries0_mapsToAries0() {
    assertLonSign(0.0, 0.0, 0);
  }

  @Test
  void ariesFirstNavamshaMid_mapsToAries15() {
    // 0–3°20' of Aries → Aries; midpoint 1°40' → Aries 15°
    assertLonSign(1.0 + 40.0 / 60.0, 15.0, 0);
  }

  @Test
  void ariesSecondNavamshaStart_mapsToTaurus0() {
    // 3°20' Aries → start of 2nd navamsha → Taurus 0°
    assertLonSign(NavamshaVargaMapper.NAVAMSHA_SPAN, 30.0, 1);
  }

  @Test
  void ariesLastNavamsha_mapsToSagittarius() {
    // 9th part of Aries (movable start Aries): signs Ar..Sg → Sagittarius (8)
    double lon = 8 * NavamshaVargaMapper.NAVAMSHA_SPAN + 0.001;
    double mapped = mapper.mapLongitude(lon);
    assertEquals(8, ZodiacCatalog.signIndex(mapped));
  }

  @Test
  void taurus0_fixedStartsAtCapricorn() {
    // Taurus fixed → start Capricorn (9)
    assertLonSign(30.0, 270.0, 9);
  }

  @Test
  void gemini0_dualStartsAtLibra() {
    // Gemini dual → start Libra (6)
    assertLonSign(60.0, 180.0, 6);
  }

  @Test
  void cancer0_movableStartsAtCancer() {
    assertLonSign(90.0, 90.0, 3);
  }

  @Test
  void leoMidFifthNavamsha_mapsTowardLeo() {
    // Leo fixed, start Aries (0); 5th part (index 4) → Leo
    double degInSign = 4 * NavamshaVargaMapper.NAVAMSHA_SPAN + NavamshaVargaMapper.NAVAMSHA_SPAN / 2.0;
    double d1 = 120.0 + degInSign; // Leo = 120°
    double mapped = mapper.mapLongitude(d1);
    assertEquals(4, ZodiacCatalog.signIndex(mapped)); // Leo
    assertEquals(15.0, ZodiacCatalog.degreeInSign(mapped), LON_TOL);
  }

  @Test
  void navamshaIndexInSignBoundaries() {
    assertEquals(0, NavamshaVargaMapper.navamshaIndexInSign(0.0));
    assertEquals(0, NavamshaVargaMapper.navamshaIndexInSign(NavamshaVargaMapper.NAVAMSHA_SPAN - 1e-9));
    assertEquals(1, NavamshaVargaMapper.navamshaIndexInSign(NavamshaVargaMapper.NAVAMSHA_SPAN));
    assertEquals(8, NavamshaVargaMapper.navamshaIndexInSign(29.999));
  }

  @Test
  void fullCircleCoversAllSigns() {
    boolean[] seen = new boolean[12];
    for (int i = 0; i < 108; i++) {
      double d1 = i * NavamshaVargaMapper.NAVAMSHA_SPAN;
      seen[ZodiacCatalog.signIndex(mapper.mapLongitude(d1))] = true;
    }
    for (boolean s : seen) {
      assertTrue(s);
    }
  }

  private void assertLonSign(double d1, double expectedLon, int expectedSign) {
    double mapped = mapper.mapLongitude(d1);
    assertEquals(expectedSign, ZodiacCatalog.signIndex(mapped));
    assertEquals(expectedLon, mapped, LON_TOL);
  }
}
