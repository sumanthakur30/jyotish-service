package com.shopmanagement.jyotishservice.engine.ephemeris;

import com.shopmanagement.jyotishservice.engine.astro.AstroMath;

/** Sidereal time helpers (Meeus). */
public final class SiderealTime {

  private SiderealTime() {}

  /**
   * Greenwich mean sidereal time in hours [0, 24).
   *
   * @param julianDayUt Julian Day UT
   */
  public static double greenwichMeanSiderealTimeHours(double julianDayUt) {
    double t = AstroMath.centuriesJ2000(julianDayUt);
    double gmst =
        280.46061837
            + 360.98564736629 * (julianDayUt - 2451545.0)
            + 0.000387933 * t * t
            - (t * t * t) / 38710000.0;
    return AstroMath.norm360(gmst) / 15.0;
  }
}
