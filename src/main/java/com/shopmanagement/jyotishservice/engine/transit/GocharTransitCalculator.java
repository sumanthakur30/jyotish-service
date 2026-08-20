package com.shopmanagement.jyotishservice.engine.transit;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

import com.shopmanagement.jyotishservice.engine.astro.AyanamsaCalculator;
import com.shopmanagement.jyotishservice.engine.astro.AstroMath;
import com.shopmanagement.jyotishservice.engine.astro.ZodiacCatalog;
import com.shopmanagement.jyotishservice.engine.ephemeris.EphemerisProvider;
import com.shopmanagement.jyotishservice.engine.ephemeris.TropicalBody;
import com.shopmanagement.jyotishservice.engine.model.Planet;
import com.shopmanagement.jyotishservice.engine.model.PlanetPosition;

/**
 * Standard Gochar: sidereal planet positions at the transit instant, whole-sign houses from natal
 * Lagna. Same graha set as D1 (Sun–Saturn, Rahu, Ketu).
 */
public final class GocharTransitCalculator implements TransitCalculator {

  public static final GocharTransitCalculator INSTANCE = new GocharTransitCalculator();

  private static final EnumSet<Planet> TRANSIT_PLANETS =
      EnumSet.of(
          Planet.SUN,
          Planet.MOON,
          Planet.MARS,
          Planet.MERCURY,
          Planet.JUPITER,
          Planet.VENUS,
          Planet.SATURN,
          Planet.RAHU,
          Planet.KETU);

  private GocharTransitCalculator() {}

  @Override
  public TransitSystemCode system() {
    return TransitSystemCode.GOCHAR;
  }

  @Override
  public TransitChart compute(
      TransitRequest request,
      List<PlanetPosition> natalPlanets,
      EphemerisProvider ephemeris,
      String engineVersion) {
    Objects.requireNonNull(request, "request");
    Objects.requireNonNull(natalPlanets, "natalPlanets");
    Objects.requireNonNull(ephemeris, "ephemeris");
    Objects.requireNonNull(engineVersion, "engineVersion");

    double jd =
        AstroMath.julianDayUt(
            request.transitDate().atTime(request.transitTime()).atZone(request.zoneId()).toInstant());
    double ayanamsa = AyanamsaCalculator.degrees(jd, request.ayanamsa());
    int lagnaSign = request.natalLagnaSignIndex();

    List<TransitPlanetPosition> transit = new ArrayList<>(TRANSIT_PLANETS.size());
    for (Planet planet : TRANSIT_PLANETS) {
      TropicalBody body = ephemeris.position(planet, jd);
      double sidereal = AstroMath.norm360(body.longitudeDeg() - ayanamsa);
      boolean retrograde = body.speedDegPerDay() < 0;
      transit.add(toTransitPosition(planet, sidereal, lagnaSign, body.speedDegPerDay(), retrograde));
    }

    List<NatalTransitRow> rows = TransitComparer.compare(natalPlanets, transit);
    String notes =
        "Gochar V1.5 — sidereal positions at "
            + request.transitDate()
            + " "
            + request.transitTime()
            + " "
            + request.zoneId()
            + "; whole-sign houses from natal Lagna; "
            + request.ayanamsa().name()
            + " ayanamsa. Sade Sati Coming Soon.";

    return new TransitChart(
        TransitSystemCode.GOCHAR,
        engineVersion,
        request.transitDate(),
        request.transitTime(),
        request.ayanamsa(),
        ayanamsa,
        jd,
        lagnaSign,
        rows,
        notes);
  }

  static TransitPlanetPosition toTransitPosition(
      Planet planet, double siderealLon, int natalLagnaSign, Double speed, boolean retrograde) {
    int signIndex = ZodiacCatalog.signIndex(siderealLon);
    int nak = ZodiacCatalog.nakshatraIndex(siderealLon);
    int house = ZodiacCatalog.wholeSignHouse(natalLagnaSign, signIndex);
    return new TransitPlanetPosition(
        planet,
        siderealLon,
        signIndex,
        ZodiacCatalog.signName(signIndex),
        ZodiacCatalog.degreeInSign(siderealLon),
        house,
        nak,
        ZodiacCatalog.nakshatraName(nak),
        ZodiacCatalog.pada(siderealLon),
        retrograde,
        speed);
  }
}
