package com.shopmanagement.jyotishservice.engine.astro;

import com.shopmanagement.jyotishservice.engine.model.AyanamsaMode;

/**
 * Sidereal ayanamsa models for Vedic D1.
 *
 * <p>Default Lahiri/Chitrapaksha uses the widely published polynomial referenced against the
 * Lahiri official value near J2000 (~23.85°). Raman and KP are approximate offsets for
 * configurability — Swiss Ephemeris SIDM tables can replace these in a later engine version.
 */
public final class AyanamsaCalculator {

  private AyanamsaCalculator() {}

  public static double degrees(double julianDayUt, AyanamsaMode mode) {
    double lahiri = lahiriDegrees(julianDayUt);
    return switch (mode == null ? AyanamsaMode.LAHIRI : mode) {
      case LAHIRI -> lahiri;
      case RAMAN -> AstroMath.norm360(lahiri - 1.4);
      case KP -> AstroMath.norm360(lahiri - 0.07);
    };
  }

  /**
   * Lahiri ayanamsa (degrees).
   *
   * <p>Standard Jyotish polynomial (centuries from 1900.0 / JD 2415020.0):
   * {@code 22.460148 + 1.396042·T + 0.000308·T²}. Yields ≈23.85° at J2000 (matches published
   * Lahiri/Chitrapaksha).
   */
  public static double lahiriDegrees(double julianDayUt) {
    double t = (julianDayUt - 2415020.0) / 36525.0;
    return AstroMath.norm360(22.460148 + 1.396042 * t + 0.000308 * t * t);
  }
}
