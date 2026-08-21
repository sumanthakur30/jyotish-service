package com.shopmanagement.jyotishservice.engine.ephemeris;

import com.shopmanagement.jyotishservice.engine.model.Planet;

/**
 * Provider of tropical ecliptic longitudes for grahas.
 *
 * <p>Implementations: {@link MeeusEphemeris} (default, pure Java) and {@link SwissEphemerisProvider}
 * (optional Thomas Mack / Swiss Ephemeris JAR via {@code jyotish.ephemeris.provider=SWISS}).
 */
public interface EphemerisProvider {

  /** Stable provider id for status / notes ({@code MEEUS} or {@code SWISS}). */
  String code();

  TropicalBody position(Planet planet, double julianDayUt);

  /** True tropical ecliptic longitude of the local Ascendant (Lagna precursor). */
  double tropicalAscendant(double julianDayUt, double latitudeDeg, double longitudeDeg);
}
