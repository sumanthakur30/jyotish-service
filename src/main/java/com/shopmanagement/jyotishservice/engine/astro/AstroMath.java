package com.shopmanagement.jyotishservice.engine.astro;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/** Julian Day and angle helpers for the calculation engine. */
public final class AstroMath {

  private AstroMath() {}

  /** Julian Day (UT) from an Instant. */
  public static double julianDayUt(Instant instant) {
    ZonedDateTime utc = instant.atZone(ZoneOffset.UTC);
    int y = utc.getYear();
    int m = utc.getMonthValue();
    double d =
        utc.getDayOfMonth()
            + (utc.getHour() + utc.getMinute() / 60.0 + utc.getSecond() / 3600.0) / 24.0;
    if (m <= 2) {
      y -= 1;
      m += 12;
    }
    int a = y / 100;
    int b = 2 - a + a / 4;
    return Math.floor(365.25 * (y + 4716))
        + Math.floor(30.6001 * (m + 1))
        + d
        + b
        - 1524.5;
  }

  public static double norm360(double deg) {
    double x = deg % 360.0;
    if (x < 0) {
      x += 360.0;
    }
    return x;
  }

  public static double toRad(double deg) {
    return Math.toRadians(deg);
  }

  public static double toDeg(double rad) {
    return Math.toDegrees(rad);
  }

  public static double sind(double deg) {
    return Math.sin(toRad(deg));
  }

  public static double cosd(double deg) {
    return Math.cos(toRad(deg));
  }

  public static double tand(double deg) {
    return Math.tan(toRad(deg));
  }

  /** Julian centuries from J2000.0. */
  public static double centuriesJ2000(double jd) {
    return (jd - 2451545.0) / 36525.0;
  }
}
