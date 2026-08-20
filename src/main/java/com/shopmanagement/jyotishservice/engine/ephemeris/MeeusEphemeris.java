package com.shopmanagement.jyotishservice.engine.ephemeris;

import com.shopmanagement.jyotishservice.engine.astro.AstroMath;
import com.shopmanagement.jyotishservice.engine.model.Planet;

/**
 * Pure-Java tropical ephemeris using Jean Meeus <em>Astronomical Algorithms</em> (2nd ed.) style
 * low-precision series for Sun/Moon/planets, plus mean lunar node for Rahu/Ketu.
 *
 * <p><b>Why not Swiss Ephemeris JNI?</b> SE Java ports exist but ship awkward packaging (manual JAR /
 * JitPack) and AGPL licensing friction for SaaS. V1.0 prefers a documented pure approach that runs
 * identically on Windows and Docker without native libs. Upgrade path: swap this class for a Swiss
 * Ephemeris-backed {@link EphemerisProvider} behind the same interface when licensed files are
 * available.
 *
 * <p>Expected accuracy vs Swiss Ephemeris (regression tolerances documented in tests): Sun ≈ ±0.5°,
 * Moon ≈ ±1.0°, Ascendant ≈ ±1.0°, other grahas ≈ ±1.5°.
 */
public final class MeeusEphemeris implements EphemerisProvider {

  @Override
  public TropicalBody position(Planet planet, double julianDayUt) {
    return switch (planet) {
      case SUN -> withSpeed(Planet.SUN, julianDayUt, this::sunLongitude);
      case MOON -> withSpeed(Planet.MOON, julianDayUt, this::moonLongitude);
      case MERCURY -> withSpeed(Planet.MERCURY, julianDayUt, jd -> planetLongitude(jd, MERCURY));
      case VENUS -> withSpeed(Planet.VENUS, julianDayUt, jd -> planetLongitude(jd, VENUS));
      case MARS -> withSpeed(Planet.MARS, julianDayUt, jd -> planetLongitude(jd, MARS));
      case JUPITER -> withSpeed(Planet.JUPITER, julianDayUt, jd -> planetLongitude(jd, JUPITER));
      case SATURN -> withSpeed(Planet.SATURN, julianDayUt, jd -> planetLongitude(jd, SATURN));
      case RAHU -> {
        double lon = AstroMath.norm360(meanAscendingNode(julianDayUt));
        double lon2 = AstroMath.norm360(meanAscendingNode(julianDayUt + 1.0));
        yield new TropicalBody(lon, lon2 - lon);
      }
      case KETU -> {
        double rahu = AstroMath.norm360(meanAscendingNode(julianDayUt));
        double ketu = AstroMath.norm360(rahu + 180.0);
        double rahu2 = AstroMath.norm360(meanAscendingNode(julianDayUt + 1.0));
        double ketu2 = AstroMath.norm360(rahu2 + 180.0);
        double speed = AstroMath.norm360(ketu2 - ketu + 180.0) - 180.0;
        yield new TropicalBody(ketu, speed);
      }
      case ASCENDANT -> throw new IllegalArgumentException("Use tropicalAscendant() for Lagna");
    };
  }

  @Override
  public double tropicalAscendant(double julianDayUt, double latitudeDeg, double longitudeDeg) {
    double t = AstroMath.centuriesJ2000(julianDayUt);
    // Mean obliquity of the ecliptic (Meeus)
    double eps =
        23.439291
            - 0.0130042 * t
            - 0.00000016 * t * t
            + 0.000000504 * t * t * t;

    double gst = SiderealTime.greenwichMeanSiderealTimeHours(julianDayUt);
    double lstHours = gst + longitudeDeg / 15.0;
    double ramc = AstroMath.norm360(lstHours * 15.0); // local sidereal degrees = RAMC

    double lat = latitudeDeg;
    double y = -AstroMath.cosd(ramc);
    double x = AstroMath.sind(ramc) * AstroMath.cosd(eps) + AstroMath.tand(lat) * AstroMath.sind(eps);
    double asc = AstroMath.toDeg(Math.atan2(y, x));
    return AstroMath.norm360(asc);
  }

  @FunctionalInterface
  private interface LonFn {
    double apply(double jd);
  }

  private TropicalBody withSpeed(Planet planet, double jd, LonFn fn) {
    double lon = AstroMath.norm360(fn.apply(jd));
    double lon2 = AstroMath.norm360(fn.apply(jd + 1.0));
    double raw = lon2 - lon;
    if (raw > 180) {
      raw -= 360;
    }
    if (raw < -180) {
      raw += 360;
    }
    return new TropicalBody(lon, raw);
  }

  /** Apparent Sun longitude (Meeus ch.25 simplified). */
  double sunLongitude(double jd) {
    double t = AstroMath.centuriesJ2000(jd);
    double l0 = AstroMath.norm360(280.46646 + 36000.76983 * t + 0.0003032 * t * t);
    double m = AstroMath.norm360(357.52911 + 35999.05029 * t - 0.0001537 * t * t);
    double c =
        (1.914602 - 0.004817 * t - 0.000014 * t * t) * AstroMath.sind(m)
            + (0.019993 - 0.000101 * t) * AstroMath.sind(2 * m)
            + 0.000289 * AstroMath.sind(3 * m);
    double trueLon = l0 + c;
    double omega = 125.04 - 1934.136 * t;
    // Approximate nutation in longitude + aberration
    double lambda = trueLon - 0.00569 - 0.00478 * AstroMath.sind(omega);
    return AstroMath.norm360(lambda);
  }

  /** Apparent Moon longitude (Meeus ch.47 truncated). */
  double moonLongitude(double jd) {
    double t = AstroMath.centuriesJ2000(jd);
    double lp = AstroMath.norm360(218.3164477 + 481267.88123421 * t - 0.0015786 * t * t);
    double d = AstroMath.norm360(297.8501921 + 445267.1114034 * t - 0.0018819 * t * t);
    double m = AstroMath.norm360(357.5291092 + 35999.0502909 * t - 0.0001536 * t * t);
    double mp = AstroMath.norm360(134.9633964 + 477198.8675055 * t + 0.0087414 * t * t);
    double f = AstroMath.norm360(93.2720950 + 483202.0175233 * t - 0.0036539 * t * t);

    double lon =
        lp
            + 6.288774 * AstroMath.sind(mp)
            + 1.274027 * AstroMath.sind(2 * d - mp)
            + 0.658314 * AstroMath.sind(2 * d)
            + 0.213618 * AstroMath.sind(2 * mp)
            - 0.185116 * AstroMath.sind(m)
            - 0.114332 * AstroMath.sind(2 * f)
            + 0.058793 * AstroMath.sind(2 * d - 2 * mp)
            + 0.057066 * AstroMath.sind(2 * d - m - mp)
            + 0.053322 * AstroMath.sind(2 * d + mp)
            + 0.045758 * AstroMath.sind(2 * d - m)
            - 0.040923 * AstroMath.sind(m - mp)
            - 0.034720 * AstroMath.sind(d)
            - 0.030383 * AstroMath.sind(m + mp)
            + 0.015327 * AstroMath.sind(2 * d - 2 * f)
            - 0.012528 * AstroMath.sind(mp + 2 * f)
            + 0.010980 * AstroMath.sind(mp - 2 * f);
    return AstroMath.norm360(lon);
  }

  /** Mean ascending node of the Moon (Rahu tropical). Meeus. */
  double meanAscendingNode(double jd) {
    double t = AstroMath.centuriesJ2000(jd);
    return AstroMath.norm360(125.0445479 - 1934.1362891 * t + 0.0020762 * t * t);
  }

  private static final PlanetElements MERCURY =
      new PlanetElements(0.387098, 7.004986, 48.330893, 77.456119, 0.20563175, 174.794788);
  private static final PlanetElements VENUS =
      new PlanetElements(0.723330, 3.394662, 76.679920, 131.563703, 0.00677188, 50.416138);
  private static final PlanetElements MARS =
      new PlanetElements(1.523688, 1.849726, 49.558093, 336.060234, 0.09340062, 19.373041);
  private static final PlanetElements JUPITER =
      new PlanetElements(5.202561, 1.303270, 100.464441, 14.331309, 0.04849485, 20.020083);
  private static final PlanetElements SATURN =
      new PlanetElements(9.554747, 2.488878, 113.665524, 93.056787, 0.05550862, 317.020839);

  /**
   * Heliocentric → geocentric ecliptic longitude via Kepler + light geometric approximation
   * (sufficient for V1.0 Vedic D1 tolerances).
   */
  double planetLongitude(double jd, PlanetElements el) {
    double n = 0.9856076686 / (el.a * Math.sqrt(el.a)); // mean daily motion deg
    // Epoch elements are approx J2000; propagate mean anomaly
    double m0 = el.l0 - el.peri; // mean anomaly at J2000-ish
    double m = AstroMath.norm360(m0 + n * (jd - 2451545.0));

    // Solve Kepler (eccentric anomaly) iteratively
    double eRad = AstroMath.toRad(m);
    double ecc = el.e;
    double eAnom = eRad;
    for (int i = 0; i < 8; i++) {
      eAnom = eRad + ecc * Math.sin(eAnom);
    }
    double trueAnom =
        AstroMath.toDeg(
            2.0
                * Math.atan2(
                    Math.sqrt(1 + ecc) * Math.sin(eAnom / 2),
                    Math.sqrt(1 - ecc) * Math.cos(eAnom / 2)));
    double r = el.a * (1 - ecc * Math.cos(eAnom));

    double node = el.node;
    double incl = el.i;
    double helLon = AstroMath.norm360(trueAnom + el.peri);

    // Heliocentric ecliptic rectangular
    double u = helLon - node;
    double cosI = AstroMath.cosd(incl);
    double xh = r * (AstroMath.cosd(node) * AstroMath.cosd(u) - AstroMath.sind(node) * AstroMath.sind(u) * cosI);
    double yh = r * (AstroMath.sind(node) * AstroMath.cosd(u) + AstroMath.cosd(node) * AstroMath.sind(u) * cosI);

    // Earth heliocentric (from Sun geocentric inverse)
    double sunLon = sunLongitude(jd);
    // AU Earth-Sun ≈ 1
    double xe = -AstroMath.cosd(sunLon);
    double ye = -AstroMath.sind(sunLon);

    double xg = xh - xe;
    double yg = yh - ye;
    return AstroMath.norm360(AstroMath.toDeg(Math.atan2(yg, xg)));
  }

  private record PlanetElements(
      double a, double i, double node, double peri, double e, double l0) {}
}
