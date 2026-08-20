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
import com.shopmanagement.jyotishservice.engine.dasha.DashaCalculator;
import com.shopmanagement.jyotishservice.engine.dasha.DashaRegistry;
import com.shopmanagement.jyotishservice.engine.dasha.DashaSystemCode;
import com.shopmanagement.jyotishservice.engine.dasha.DashaTimeline;
import com.shopmanagement.jyotishservice.engine.model.VargaChart;
import com.shopmanagement.jyotishservice.engine.varga.VargaCode;
import com.shopmanagement.jyotishservice.engine.varga.VargaMapper;
import com.shopmanagement.jyotishservice.engine.varga.VargaRegistry;

/**
 * Pure calculation engine (no Spring). V1.2 sidereal D1 via {@link MeeusEphemeris} + configurable
 * ayanamsa, Parashara Vargas (D2/D3/D9/D10) via {@link VargaRegistry}, and Vimshottari dasha via
 * {@link DashaRegistry}. Whole-sign houses. Combust is stubbed false (Coming Soon).
 */
public final class CalculationEngine {

  /** Bumped to V1.2 when Vimshottari dasha algorithm surface was added (Phase 4). */
  public static final String VERSION = "V1.2";

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

    List<HouseCusp> houses = wholeSignHouses(lagnaSign);

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

  /**
   * Build a divisional chart from an existing D1 result by mapping longitudes through the
   * registered {@link VargaMapper}. Does not re-run ephemeris.
   */
  public VargaChart computeVarga(D1Chart d1, VargaCode code) {
    Objects.requireNonNull(d1, "d1");
    Objects.requireNonNull(code, "code");
    if (code == VargaCode.D1) {
      return new VargaChart(
          VargaCode.D1,
          VERSION,
          d1.planets(),
          d1.houses(),
          d1.ascendant(),
          d1.houseSystem(),
          "D1 Rashi (identity varga).");
    }
    VargaMapper mapper = VargaRegistry.requireMapper(code);
    return project(d1, code, mapper);
  }

  /**
   * Project raw D1 longitudes (planets + lagna) into a varga without a full {@link D1Chart}
   * object — used when reconstructing from persisted D1 rows.
   */
  public VargaChart computeVargaFromLongitudes(
      VargaCode code,
      double lagnaLongitudeDeg,
      List<PlanetLongitude> planetLongitudes) {
    Objects.requireNonNull(code, "code");
    Objects.requireNonNull(planetLongitudes, "planetLongitudes");
    VargaMapper mapper = VargaRegistry.requireMapper(code);

    double vLagnaLon = mapper.mapLongitude(lagnaLongitudeDeg);
    int vLagnaSign = ZodiacCatalog.signIndex(vLagnaLon);
    PlanetPosition ascendant =
        toPosition(Planet.ASCENDANT, vLagnaLon, vLagnaSign, null, false);

    List<PlanetPosition> planets = new ArrayList<>();
    for (PlanetLongitude pl : planetLongitudes) {
      if (pl.planet() == Planet.ASCENDANT) {
        continue;
      }
      double vLon = mapper.mapLongitude(pl.longitudeDeg());
      planets.add(
          toPosition(pl.planet(), vLon, vLagnaSign, pl.speedDegPerDay(), pl.retrograde()));
    }

    return new VargaChart(
        code,
        VERSION,
        planets,
        wholeSignHouses(vLagnaSign),
        ascendant,
        "WHOLE_SIGN",
        notesFor(code));
  }

  private VargaChart project(D1Chart d1, VargaCode code, VargaMapper mapper) {
    double vLagnaLon = mapper.mapLongitude(d1.ascendant().longitudeDeg());
    int vLagnaSign = ZodiacCatalog.signIndex(vLagnaLon);
    PlanetPosition ascendant =
        toPosition(Planet.ASCENDANT, vLagnaLon, vLagnaSign, null, false);

    List<PlanetPosition> planets = new ArrayList<>();
    for (PlanetPosition p : d1.planets()) {
      double vLon = mapper.mapLongitude(p.longitudeDeg());
      planets.add(
          toPosition(p.planet(), vLon, vLagnaSign, p.speedDegPerDay(), p.retrograde()));
    }

    return new VargaChart(
        code,
        VERSION,
        planets,
        wholeSignHouses(vLagnaSign),
        ascendant,
        "WHOLE_SIGN",
        notesFor(code));
  }

  /**
   * Compute a dasha timeline from Moon's sidereal longitude at birth. Unimplemented systems throw
   * via {@link DashaRegistry#requireCalculator}.
   */
  public DashaTimeline computeDasha(
      DashaSystemCode system, double moonLongitudeDeg, java.time.Instant birthAt) {
    DashaCalculator calc = DashaRegistry.requireCalculator(system);
    return calc.compute(moonLongitudeDeg, birthAt, VERSION);
  }

  private static String notesFor(VargaCode code) {
    return code.code()
        + " "
        + code.displayName()
        + " from D1 longitudes (Parashara); whole-sign houses from varga Lagna; engine "
        + VERSION
        + ".";
  }

  private static List<HouseCusp> wholeSignHouses(int lagnaSign) {
    List<HouseCusp> houses = new ArrayList<>(12);
    for (int h = 1; h <= 12; h++) {
      int signIndex = Math.floorMod(lagnaSign + h - 1, 12);
      double cusp = signIndex * 30.0;
      houses.add(new HouseCusp(h, signIndex, ZodiacCatalog.signName(signIndex), cusp));
    }
    return houses;
  }

  static PlanetPosition toPosition(
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

  /** Lightweight D1 longitude carrier for varga projection from persistence. */
  public record PlanetLongitude(
      Planet planet, double longitudeDeg, boolean retrograde, Double speedDegPerDay) {}
}
