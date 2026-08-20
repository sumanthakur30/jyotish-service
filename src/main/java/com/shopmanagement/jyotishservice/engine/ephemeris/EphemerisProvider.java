package com.shopmanagement.jyotishservice.engine.ephemeris;

import com.shopmanagement.jyotishservice.engine.model.Planet;

/** Provider of tropical ecliptic longitudes for grahas. */
public interface EphemerisProvider {

  TropicalBody position(Planet planet, double julianDayUt);

  /** True tropical ecliptic longitude of the local Ascendant (Lagna precursor). */
  double tropicalAscendant(double julianDayUt, double latitudeDeg, double longitudeDeg);
}
