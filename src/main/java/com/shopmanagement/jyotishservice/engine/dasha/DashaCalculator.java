package com.shopmanagement.jyotishservice.engine.dasha;

import java.time.Instant;

/**
 * Pluggable dasha system. Implementations are pure Java (no Spring) and registered in {@link
 * DashaRegistry}.
 */
public interface DashaCalculator {

  DashaSystemCode system();

  /**
   * Compute the timeline from Moon's sidereal longitude at birth.
   *
   * @param moonLongitudeDeg sidereal Moon longitude [0, 360)
   * @param birthAt birth instant (UT)
   * @param engineVersion stamp to embed in the result
   */
  DashaTimeline compute(double moonLongitudeDeg, Instant birthAt, String engineVersion);
}
