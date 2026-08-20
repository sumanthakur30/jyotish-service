package com.shopmanagement.jyotishservice.engine.transit;

import java.util.List;

import com.shopmanagement.jyotishservice.engine.ephemeris.EphemerisProvider;
import com.shopmanagement.jyotishservice.engine.model.PlanetPosition;

/** Pluggable transit / Gochar calculator. */
public interface TransitCalculator {

  TransitSystemCode system();

  /**
   * Compute transit positions for the request, then compare against natal D1 planets (houses from
   * natal Lagna).
   */
  TransitChart compute(
      TransitRequest request,
      List<PlanetPosition> natalPlanets,
      EphemerisProvider ephemeris,
      String engineVersion);
}
