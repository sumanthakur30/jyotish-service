package com.shopmanagement.jyotishservice.engine.varga;

/**
 * Maps a sidereal D1 longitude (0–360°) into the corresponding divisional longitude (0–360°).
 *
 * <p>Adding a new varga = register a mapper in {@link VargaRegistry}; no UI rewrite required.
 */
@FunctionalInterface
public interface VargaMapper {

  /**
   * @param d1LongitudeDeg sidereal ecliptic longitude in the Rashi (D1) chart
   * @return sidereal longitude in the divisional chart (same 0–360° circle)
   */
  double mapLongitude(double d1LongitudeDeg);
}
