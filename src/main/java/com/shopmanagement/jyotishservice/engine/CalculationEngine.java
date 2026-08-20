package com.shopmanagement.jyotishservice.engine;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

import com.shopmanagement.jyotishservice.engine.astro.AyanamsaCalculator;
import com.shopmanagement.jyotishservice.engine.astro.AstroMath;
import com.shopmanagement.jyotishservice.engine.astro.ZodiacCatalog;
import com.shopmanagement.jyotishservice.engine.ephemeris.EphemerisProvider;
import com.shopmanagement.jyotishservice.engine.ephemeris.MeeusEphemeris;
import com.shopmanagement.jyotishservice.engine.ephemeris.TropicalBody;
import com.shopmanagement.jyotishservice.engine.model.AyanamsaMode;
import com.shopmanagement.jyotishservice.engine.model.ChartRequest;
import com.shopmanagement.jyotishservice.engine.model.D1Chart;
import com.shopmanagement.jyotishservice.engine.model.HouseCusp;
import com.shopmanagement.jyotishservice.engine.model.Planet;
import com.shopmanagement.jyotishservice.engine.model.PlanetPosition;

/**
 * Pure calculation engine (no Spring). V1.0 sidereal D1 via {@link MeeusEphemeris} + configurable
 * ayanamsa. Whole-sign houses. Combust is stubbed false (Coming Soon).
 */
public final class CalculationEngine {

  public static final String VERSION = "V1.0";

  private static final EnumSet<Planet> CHART_PLANETS =
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

  private final EphemerisProvider ephemeris;

  public CalculationEngine() {
    this(new MeeusEphemeris());
  }

  public CalculationEngine(EphemerisProvider ephemeris) {
    this.ephemeris = Objects.requireNonNull(ephemeris, "ephemeris");
  }

  public String version() {
    return VERSION;
  }

  public D1Chart computeD1(ChartRequest request) {
    Objects.requireNonNull(request, "request");
    double jd = AstroMath.julianDayUt(request.birth().toZonedDateTime().toInstant());
    AyanamsaMode mode = request.ayanamsa();
    double ayanamsa = AyanamsaCalculator.degrees(jd, mode);

    double tropicalAsc =
        ephemeris.tropicalAscendant(
            jd, request.birth().latitudeDeg(), request.birth().longitudeDeg());
    double siderealAsc = AstroMath.norm360(tropicalAsc - ayanamsa);
    int lagnaSign = ZodiacCatalog.signIndex(siderealAsc);

    PlanetPosition ascendant = toPosition(Planet.ASCENDANT, siderealAsc, lagnaSign, null, false);

    List<PlanetPosition> planets = new ArrayList<>();
    for (Planet planet : CHART_PLANETS) {
      TropicalBody body = ephemeris.position(planet, jd);
      double sidereal = AstroMath.norm360(body.longitudeDeg() - ayanamsa);
      boolean retrograde = body.speedDegPerDay() < 0;
      planets.add(toPosition(planet, sidereal, lagnaSign, body.speedDegPerDay(), retrograde));
    }

    List<HouseCusp> houses = new ArrayList<>(12);
    for (int h = 1; h <= 12; h++) {
      int signIndex = Math.floorMod(lagnaSign + h - 1, 12);
      double cusp = signIndex * 30.0;
      houses.add(new HouseCusp(h, signIndex, ZodiacCatalog.signName(signIndex), cusp));
    }

    String notes =
        request.birthTimeUnknown()
            ? "Birth time unknown — Lagna and houses use noon local time; treat as approximate."
            : "D1 whole-sign; combust Coming Soon; Meeus tropical + "
                + mode.name()
                + " ayanamsa.";

    return new D1Chart(
        VERSION,
        mode,
        ayanamsa,
        jd,
        planets,
        houses,
        ascendant,
        "WHOLE_SIGN",
        notes);
  }

  private static PlanetPosition toPosition(
      Planet planet,
      double siderealLon,
      int lagnaSign,
      Double speed,
      boolean retrograde) {
    int signIndex = ZodiacCatalog.signIndex(siderealLon);
    int nak = ZodiacCatalog.nakshatraIndex(siderealLon);
    int house = planet == Planet.ASCENDANT ? 1 : ZodiacCatalog.wholeSignHouse(lagnaSign, signIndex);
    return new PlanetPosition(
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
        false,
        speed);
  }
}
