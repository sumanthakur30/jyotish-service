package com.shopmanagement.jyotishservice.engine.panchang;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import com.shopmanagement.jyotishservice.engine.astro.AstroMath;
import com.shopmanagement.jyotishservice.engine.ephemeris.EphemerisProvider;
import com.shopmanagement.jyotishservice.engine.ephemeris.SiderealTime;
import com.shopmanagement.jyotishservice.engine.model.Planet;

/**
 * Sunrise / sunset from tropical Sun longitude (Meeus) + geometric altitude −0.833° (disk +
 * refraction). Polar day/night returns unavailable.
 */
public final class SolarEvents {

  /** Standard upper-limb + refraction horizon for civil sunrise/sunset. */
  private static final double HORIZON_ALT_DEG = -0.833;

  private SolarEvents() {}

  public static Result compute(
      LocalDate date,
      double latitudeDeg,
      double longitudeDeg,
      ZoneId zone,
      EphemerisProvider ephemeris) {
    ZonedDateTime noonLocal = date.atTime(12, 0).atZone(zone);
    double jdNoon = AstroMath.julianDayUt(noonLocal.toInstant());

    SunEquatorial eq = equatorial(ephemeris, jdNoon);
    double cosH0 =
        (AstroMath.sind(HORIZON_ALT_DEG) - AstroMath.sind(latitudeDeg) * AstroMath.sind(eq.dec))
            / (AstroMath.cosd(latitudeDeg) * AstroMath.cosd(eq.dec));

    if (cosH0 < -1.0 || cosH0 > 1.0) {
      String note =
          cosH0 < -1.0
              ? "Sun below horizon all day (polar night)."
              : "Sun above horizon all day (polar day).";
      return new Result(
          PanchangResult.SolarEvent.unavailable(note),
          PanchangResult.SolarEvent.unavailable(note),
          noonLocal.toInstant());
    }

    double H0 = AstroMath.toDeg(Math.acos(cosH0)); // degrees of hour angle
    Instant transit = solarTransit(jdNoon, longitudeDeg, eq.ra, zone, date);
    Instant rise = transit.minusSeconds(Math.round(H0 / 15.0 * 3600.0));
    Instant set = transit.plusSeconds(Math.round(H0 / 15.0 * 3600.0));

    // One refinement pass at estimated times
    rise = refineCrossing(ephemeris, rise, latitudeDeg, longitudeDeg, true);
    set = refineCrossing(ephemeris, set, latitudeDeg, longitudeDeg, false);

    return new Result(
        PanchangResult.SolarEvent.of(LocalTime.ofInstant(rise, zone), rise),
        PanchangResult.SolarEvent.of(LocalTime.ofInstant(set, zone), set),
        rise);
  }

  private static Instant solarTransit(
      double jdApprox, double longitudeDeg, double raDeg, ZoneId zone, LocalDate date) {
    // Iterate from local noon to find when local sidereal ≈ RA
    Instant guess = date.atTime(12, 0).atZone(zone).toInstant();
    for (int i = 0; i < 4; i++) {
      double jd = AstroMath.julianDayUt(guess);
      double gstHours = SiderealTime.greenwichMeanSiderealTimeHours(jd);
      double lstDeg = AstroMath.norm360(gstHours * 15.0 + longitudeDeg);
      double ha = AstroMath.norm360(lstDeg - raDeg + 180.0) - 180.0; // signed hour angle deg
      guess = guess.minusSeconds(Math.round(ha / 15.0 * 3600.0));
    }
    return guess;
  }

  private static Instant refineCrossing(
      EphemerisProvider ephemeris,
      Instant guess,
      double lat,
      double lon,
      boolean rising) {
    Instant t = guess;
    for (int i = 0; i < 6; i++) {
      double alt = altitude(ephemeris, t, lat, lon);
      double err = alt - HORIZON_ALT_DEG;
      // Sun moves ~15°/hour in hour angle; d(alt)/dt ≈ cos(lat)cos(dec)sin(H)*15 °/h
      // Use fixed ~0.25°/min near horizon as coarse Newton step
      double minutes = err / (rising ? 0.25 : -0.25);
      t = t.plusSeconds(Math.round(minutes * 60.0));
    }
    return t;
  }

  static double altitude(
      EphemerisProvider ephemeris, Instant instant, double latitudeDeg, double longitudeDeg) {
    double jd = AstroMath.julianDayUt(instant);
    SunEquatorial eq = equatorial(ephemeris, jd);
    double gstHours = SiderealTime.greenwichMeanSiderealTimeHours(jd);
    double lstDeg = AstroMath.norm360(gstHours * 15.0 + longitudeDeg);
    double ha = AstroMath.norm360(lstDeg - eq.ra + 180.0) - 180.0;
    return AstroMath.toDeg(
        Math.asin(
            AstroMath.sind(latitudeDeg) * AstroMath.sind(eq.dec)
                + AstroMath.cosd(latitudeDeg) * AstroMath.cosd(eq.dec) * AstroMath.cosd(ha)));
  }

  private static SunEquatorial equatorial(EphemerisProvider ephemeris, double jd) {
    double lon = ephemeris.position(Planet.SUN, jd).longitudeDeg();
    double t = AstroMath.centuriesJ2000(jd);
    double eps =
        23.439291 - 0.0130042 * t - 0.00000016 * t * t + 0.000000504 * t * t * t;
    double ra =
        AstroMath.norm360(
            AstroMath.toDeg(
                Math.atan2(AstroMath.cosd(eps) * AstroMath.sind(lon), AstroMath.cosd(lon))));
    double dec = AstroMath.toDeg(Math.asin(AstroMath.sind(eps) * AstroMath.sind(lon)));
    return new SunEquatorial(ra, dec);
  }

  private record SunEquatorial(double ra, double dec) {}

  public record Result(
      PanchangResult.SolarEvent sunrise,
      PanchangResult.SolarEvent sunset,
      Instant asOfFallback) {}
}
