package com.shopmanagement.jyotishservice.engine.ephemeris;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

import com.shopmanagement.jyotishservice.config.JyotishEphemerisProperties;
import com.shopmanagement.jyotishservice.engine.astro.AstroMath;
import com.shopmanagement.jyotishservice.engine.model.Planet;

/**
 * {@link EphemerisProvider} backed by Thomas Mack's pure-Java Swiss Ephemeris port ({@code
 * swisseph.SwissEph}).
 *
 * <p>Default mode is <strong>Moshier</strong> (no {@code .se1} files) when {@code
 * jyotish.ephemeris.swiss-ephe-path} is empty — still substantially more accurate than truncated
 * Meeus. Full Swiss Ephemeris files are optional via path + {@code swiss-use-files=true}.
 *
 * <p><b>License:</b> Swiss Ephemeris Free Edition is AGPL. Commercial SaaS may require Astrodienst
 * dual licensing — see Astrodienst Swiss Ephemeris license docs before enabling in production.
 *
 * <p>JNI packaging is intentionally avoided in this slice; the pure-Java JAR is loaded reflectively
 * so the default Meeus build has zero Swiss dependency.
 */
public final class SwissEphemerisProvider implements EphemerisProvider {

  private final SwissEphemerisSupport.Handle handle;

  private SwissEphemerisProvider(SwissEphemerisSupport.Handle handle) {
    this.handle = Objects.requireNonNull(handle, "handle");
  }

  public static SwissEphemerisProvider create(JyotishEphemerisProperties props) {
    return new SwissEphemerisProvider(SwissEphemerisSupport.probe(props));
  }

  @Override
  public String code() {
    return EphemerisProviders.SWISS;
  }

  /** True when this instance was configured to use Swiss Ephemeris data files. */
  public boolean usingFiles() {
    return handle.usingFiles();
  }

  @Override
  public TropicalBody position(Planet planet, double julianDayUt) {
    Objects.requireNonNull(planet, "planet");
    return switch (planet) {
      case SUN -> body(SwissEphemerisSupport.SE_SUN, julianDayUt);
      case MOON -> body(SwissEphemerisSupport.SE_MOON, julianDayUt);
      case MERCURY -> body(SwissEphemerisSupport.SE_MERCURY, julianDayUt);
      case VENUS -> body(SwissEphemerisSupport.SE_VENUS, julianDayUt);
      case MARS -> body(SwissEphemerisSupport.SE_MARS, julianDayUt);
      case JUPITER -> body(SwissEphemerisSupport.SE_JUPITER, julianDayUt);
      case SATURN -> body(SwissEphemerisSupport.SE_SATURN, julianDayUt);
      case URANUS -> body(SwissEphemerisSupport.SE_URANUS, julianDayUt);
      case NEPTUNE -> body(SwissEphemerisSupport.SE_NEPTUNE, julianDayUt);
      case PLUTO -> body(SwissEphemerisSupport.SE_PLUTO, julianDayUt);
      case RAHU -> {
        int node =
            handle.trueNode()
                ? SwissEphemerisSupport.SE_TRUE_NODE
                : SwissEphemerisSupport.SE_MEAN_NODE;
        yield body(node, julianDayUt);
      }
      case KETU -> {
        TropicalBody rahu = position(Planet.RAHU, julianDayUt);
        yield new TropicalBody(AstroMath.norm360(rahu.longitudeDeg() + 180.0), rahu.speedDegPerDay());
      }
      case ASCENDANT -> throw new IllegalArgumentException("Use tropicalAscendant() for Lagna");
    };
  }

  @Override
  public boolean supportsOuterPlanets() {
    return true;
  }

  @Override
  public double tropicalAscendant(double julianDayUt, double latitudeDeg, double longitudeDeg) {
    return ascmc(julianDayUt, latitudeDeg, longitudeDeg)[0];
  }

  @Override
  public double tropicalMidheaven(double julianDayUt, double latitudeDeg, double longitudeDeg) {
    return ascmc(julianDayUt, latitudeDeg, longitudeDeg)[1];
  }

  /** {@code [ASC, MC]} via swe_houses Placidus angles (ASC/MC independent of house system). */
  private double[] ascmc(double julianDayUt, double latitudeDeg, double longitudeDeg) {
    double[] cusps = new double[13];
    double[] ascmc = new double[10];
    synchronized (handle.swissEph()) {
      try {
        int rc =
            (Integer)
                handle
                    .houses()
                    .invoke(
                        handle.swissEph(),
                        julianDayUt,
                        0,
                        latitudeDeg,
                        longitudeDeg,
                        (int) 'P',
                        cusps,
                        ascmc);
        if (rc < 0) {
          throw new EphemerisUnavailableException(
              "Swiss Ephemeris swe_houses failed (rc=" + rc + ")");
        }
        return new double[] {AstroMath.norm360(ascmc[0]), AstroMath.norm360(ascmc[1])};
      } catch (InvocationTargetException ex) {
        throw new EphemerisUnavailableException(
            "Swiss Ephemeris swe_houses error: " + ex.getTargetException().getMessage(),
            ex.getTargetException());
      } catch (ReflectiveOperationException ex) {
        throw new EphemerisUnavailableException("Swiss Ephemeris swe_houses reflect failed", ex);
      }
    }
  }

  private TropicalBody body(int swissPlanetId, double jd) {
    double[] xx = new double[6];
    StringBuffer serr = new StringBuffer();
    synchronized (handle.swissEph()) {
      try {
        int rc =
            (Integer)
                handle
                    .calcUt()
                    .invoke(handle.swissEph(), jd, swissPlanetId, handle.iflag(), xx, serr);
        if (rc < 0) {
          String err = serr.length() > 0 ? serr.toString() : ("rc=" + rc);
          throw new EphemerisUnavailableException("Swiss Ephemeris swe_calc_ut failed: " + err);
        }
        return new TropicalBody(AstroMath.norm360(xx[0]), xx[3]);
      } catch (InvocationTargetException ex) {
        throw new EphemerisUnavailableException(
            "Swiss Ephemeris swe_calc_ut error: " + ex.getTargetException().getMessage(),
            ex.getTargetException());
      } catch (ReflectiveOperationException ex) {
        throw new EphemerisUnavailableException("Swiss Ephemeris swe_calc_ut reflect failed", ex);
      }
    }
  }
}
