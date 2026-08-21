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
import com.shopmanagement.jyotishservice.engine.matching.MatchingPerson;
import com.shopmanagement.jyotishservice.engine.matching.MatchingRegistry;
import com.shopmanagement.jyotishservice.engine.matching.MatchingReport;
import com.shopmanagement.jyotishservice.engine.ashtakavarga.AshtakavargaCalculator;
import com.shopmanagement.jyotishservice.engine.panchang.PanchangCalculator;
import com.shopmanagement.jyotishservice.engine.panchang.PanchangRequest;
import com.shopmanagement.jyotishservice.engine.panchang.PanchangResult;
import com.shopmanagement.jyotishservice.engine.shadbala.ShadbalaCalculator;
import com.shopmanagement.jyotishservice.engine.transit.TransitChart;
import com.shopmanagement.jyotishservice.engine.transit.TransitRegistry;
import com.shopmanagement.jyotishservice.engine.transit.TransitRequest;
import com.shopmanagement.jyotishservice.engine.transit.TransitSystemCode;
import com.shopmanagement.jyotishservice.engine.yoga.YogaContext;
import com.shopmanagement.jyotishservice.engine.yoga.YogaRegistry;
import com.shopmanagement.jyotishservice.engine.yoga.YogaReport;

/**
 * Pure calculation engine (no Spring). V1.7 sidereal D1 via pluggable {@link EphemerisProvider}
 * (default {@link MeeusEphemeris}; optional Swiss) + configurable ayanamsa, Parashara Vargas
 * (D2/D3/D9/D10) via {@link VargaRegistry}, Vimshottari dasha via {@link DashaRegistry},
 * rule-based yogas via {@link YogaRegistry}, Kundali matching via {@link MatchingRegistry},
 * Gochar transit via {@link TransitRegistry}, Panchang + muhurat via {@link PanchangCalculator},
 * Ashtakavarga / partial Shadbala, and Sade Sati helpers. Whole-sign houses. Combust is stubbed
 * false (Coming Soon).
 */
public final class CalculationEngine {

  /** Bumped to V1.7 for accuracy pack, Ashtakavarga, partial Shadbala, muhurat, Manglik cancel, Sade Sati. */
  public static final String VERSION = "V1.7";

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
            : "D1 whole-sign; combust Coming Soon; "
                + ephemeris.code()
                + " tropical + "
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

  /**
   * Evaluate registered yoga detectors against D1 (optional D9 context for future rules). Catalog
   * stubs without detectors are omitted from the report — they appear as Coming Soon in the API.
   */
  public YogaReport computeYogas(D1Chart d1, VargaChart d9OrNull) {
    Objects.requireNonNull(d1, "d1");
    int lagnaSign = d1.ascendant().signIndex();
    YogaContext ctx = new YogaContext(lagnaSign, d1.planets(), d9OrNull);
    return new YogaReport(
        VERSION,
        YogaRegistry.evaluateAll(ctx),
        "Yoga detectors V1.3 from D1 whole-sign positions; unimplemented catalog entries are Coming"
            + " Soon (not stored as present). Patterns are descriptive — not predictions.");
  }

  /** Reconstruct yogas from persisted D1 planet rows + lagna sign. */
  public YogaReport computeYogasFromPositions(
      int lagnaSignIndex, List<PlanetPosition> d1Planets, VargaChart d9OrNull) {
    Objects.requireNonNull(d1Planets, "d1Planets");
    YogaContext ctx = new YogaContext(lagnaSignIndex, d1Planets, d9OrNull);
    return new YogaReport(
        VERSION,
        YogaRegistry.evaluateAll(ctx),
        "Yoga detectors V1.3 from stored D1 positions; patterns are descriptive — not predictions.");
  }

  /** Ashta Koota + Manglik for two D1-derived persons. */
  public MatchingReport computeMatching(MatchingPerson personA, MatchingPerson personB) {
    Objects.requireNonNull(personA, "personA");
    Objects.requireNonNull(personB, "personB");
    return MatchingRegistry.compute(personA, personB, VERSION);
  }

  /**
   * Gochar transit positions for a date, compared to natal D1 planets. Unimplemented systems throw
   * via {@link TransitRegistry#requireCalculator}.
   */
  public TransitChart computeTransit(
      TransitSystemCode system, TransitRequest request, List<PlanetPosition> natalPlanets) {
    return TransitRegistry.requireCalculator(system)
        .compute(request, natalPlanets, ephemeris, VERSION);
  }

  /**
   * Classical Panchang (Tithi–Karana + sunrise/sunset + muhurat extras) for a civil date and place.
   * Compute-only — no persistence.
   */
  public PanchangResult computePanchang(PanchangRequest request) {
    return PanchangCalculator.compute(request, ephemeris, VERSION);
  }

  /** Classical Bhinnashtakavarga + Sarvashtakavarga from D1 whole-sign positions. */
  public AshtakavargaCalculator.AshtakavargaResult computeAshtakavarga(D1Chart d1) {
    return AshtakavargaCalculator.compute(d1);
  }

  /**
   * Honest partial Shadbala (Naisargika + Dig + Sthana subset). Remaining components are Coming
   * Soon — never a fake full total.
   */
  public ShadbalaCalculator.ShadbalaReport computeShadbala(D1Chart d1) {
    return ShadbalaCalculator.compute(d1);
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
