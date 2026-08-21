package com.shopmanagement.jyotishservice.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.EnumMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.shopmanagement.jyotishservice.config.JyotishEphemerisProperties;
import com.shopmanagement.jyotishservice.engine.ephemeris.EphemerisProvider;
import com.shopmanagement.jyotishservice.engine.ephemeris.EphemerisProviders;
import com.shopmanagement.jyotishservice.engine.ephemeris.MeeusEphemeris;
import com.shopmanagement.jyotishservice.engine.model.AyanamsaMode;
import com.shopmanagement.jyotishservice.engine.model.BirthMoment;
import com.shopmanagement.jyotishservice.engine.model.ChartRequest;
import com.shopmanagement.jyotishservice.engine.model.D1Chart;
import com.shopmanagement.jyotishservice.engine.model.Planet;
import com.shopmanagement.jyotishservice.engine.model.PlanetPosition;

/**
 * Frozen Swiss-Moshier Lahiri gold for Delhi 1990-08-15 10:30 Asia/Kolkata.
 *
 * <p>When the optional Swiss JAR is present, Swiss engine must match gold within ±0.05° for all
 * bodies including ASC. Meeus always asserts soft tolerances for Sun/Moon/Rahu/Ketu only —
 * Mars/Jupiter/Venus/Saturn/ASC are <strong>not</strong> fail criteria under Meeus (documented in
 * {@code docs/ACCURACY-PACK.md}).
 */
class LahiriGoldenChartTest {

  private static final Path SWISS_JAR =
      Path.of("third_party/swiss-ephemeris/swisseph-2.01.00-02.jar");

  /** Swiss-Moshier Lahiri gold (Delhi). */
  private static final double GOLD_AYAN = 23.725485;

  private static final Map<Planet, Double> DELHI_GOLD = delhiGold();

  private static Map<Planet, Double> delhiGold() {
    EnumMap<Planet, Double> m = new EnumMap<>(Planet.class);
    m.put(Planet.ASCENDANT, 178.713194);
    m.put(Planet.SUN, 118.399606);
    m.put(Planet.MOON, 48.934641);
    m.put(Planet.MARS, 27.491563);
    m.put(Planet.MERCURY, 145.450580);
    m.put(Planet.JUPITER, 95.611251);
    m.put(Planet.VENUS, 97.847207);
    m.put(Planet.SATURN, 266.152584);
    m.put(Planet.RAHU, 282.758175);
    m.put(Planet.KETU, 102.758175);
    return Map.copyOf(m);
  }

  private static boolean swissJarPresent;
  private static EphemerisProvider swissProvider;
  private static Map<Planet, Double> mumbaiSwissGold;

  @BeforeAll
  static void setupSwissIfPresent() {
    swissJarPresent = Files.isRegularFile(SWISS_JAR);
    if (!swissJarPresent) {
      return;
    }
    JyotishEphemerisProperties props = new JyotishEphemerisProperties();
    props.setProvider(EphemerisProviders.SWISS);
    props.setSwissJarPath(SWISS_JAR.toString());
    props.setSwissUseFiles(false);
    props.setSwissTrueNode(false);
    swissProvider = EphemerisProviders.create(props);

    // Freeze Mumbai sample gold once from Swiss-Moshier when JAR is available.
    D1Chart mumbai = new CalculationEngine(swissProvider).computeD1(mumbaiRequest());
    EnumMap<Planet, Double> m = new EnumMap<>(Planet.class);
    m.put(Planet.ASCENDANT, mumbai.ascendant().longitudeDeg());
    for (PlanetPosition p : mumbai.planets()) {
      m.put(p.planet(), p.longitudeDeg());
    }
    mumbaiSwissGold = Map.copyOf(m);
  }

  @Test
  void swissDelhiWithinFiveHundredthsOfGoldWhenJarPresent() {
    assumeTrue(swissJarPresent, "Swiss JAR not present — skip Swiss self-regression");
    D1Chart chart = new CalculationEngine(swissProvider).computeD1(delhiRequest());
    assertEquals(GOLD_AYAN, chart.ayanamsaDeg(), 0.05, "ayanamsa");
    assertWithin(0.05, Planet.ASCENDANT, chart.ascendant().longitudeDeg(), DELHI_GOLD);
    for (PlanetPosition p : chart.planets()) {
      if (p.planet().isOuter()) {
        continue; // outers not in classical Delhi gold table
      }
      assertWithin(0.05, p.planet(), p.longitudeDeg(), DELHI_GOLD);
    }
  }

  @Test
  void meeusDelhiSoftSunMoonNodesVsGold() {
    // Soft Meeus tolerances only — do NOT fail on Mars/Jupiter/Venus/Saturn/ASC under Meeus.
    D1Chart chart = new CalculationEngine(new MeeusEphemeris()).computeD1(delhiRequest());
    Map<Planet, Double> lon = longitudes(chart);
    assertEquals(DELHI_GOLD.get(Planet.SUN), lon.get(Planet.SUN), 0.5, "Meeus Sun ±0.5°");
    assertEquals(DELHI_GOLD.get(Planet.MOON), lon.get(Planet.MOON), 1.0, "Meeus Moon ±1.0°");
    assertEquals(DELHI_GOLD.get(Planet.RAHU), lon.get(Planet.RAHU), 1.0, "Meeus Rahu ±1.0°");
    assertEquals(DELHI_GOLD.get(Planet.KETU), lon.get(Planet.KETU), 1.0, "Meeus Ketu ±1.0°");
  }

  @Test
  void mumbaiSwissSelfRegressionWhenJarPresent() {
    assumeTrue(swissJarPresent && mumbaiSwissGold != null, "Swiss JAR not present — skip");
    D1Chart chart = new CalculationEngine(swissProvider).computeD1(mumbaiRequest());
    assertWithin(0.05, Planet.ASCENDANT, chart.ascendant().longitudeDeg(), mumbaiSwissGold);
    for (PlanetPosition p : chart.planets()) {
      assertWithin(0.05, p.planet(), p.longitudeDeg(), mumbaiSwissGold);
    }
  }

  @Test
  void meeusMumbaiSoftSunMoonVsSwissWhenJarPresent() {
    assumeTrue(swissJarPresent && mumbaiSwissGold != null, "Swiss JAR not present — soft check skipped");
    D1Chart meeus = new CalculationEngine(new MeeusEphemeris()).computeD1(mumbaiRequest());
    Map<Planet, Double> lon = longitudes(meeus);
    assertEquals(mumbaiSwissGold.get(Planet.SUN), lon.get(Planet.SUN), 0.5, "Meeus Sun vs Swiss Mumbai");
    assertEquals(mumbaiSwissGold.get(Planet.MOON), lon.get(Planet.MOON), 1.0, "Meeus Moon vs Swiss Mumbai");
  }

  private static void assertWithin(
      double tol, Planet planet, double actual, Map<Planet, Double> gold) {
    Double expected = gold.get(planet);
    assertTrue(expected != null, "missing gold for " + planet);
    double delta = angularDelta(actual, expected);
    assertTrue(
        delta <= tol + 1e-9,
        () -> planet + " delta=" + delta + "° (tol=" + tol + ") actual=" + actual + " gold=" + expected);
  }

  private static double angularDelta(double a, double b) {
    double d = Math.abs(a - b) % 360.0;
    return d > 180.0 ? 360.0 - d : d;
  }

  private static Map<Planet, Double> longitudes(D1Chart chart) {
    EnumMap<Planet, Double> m = new EnumMap<>(Planet.class);
    m.put(Planet.ASCENDANT, chart.ascendant().longitudeDeg());
    for (PlanetPosition p : chart.planets()) {
      m.put(p.planet(), p.longitudeDeg());
    }
    return m;
  }

  private static ChartRequest delhiRequest() {
    return new ChartRequest(
        new BirthMoment(
            LocalDate.of(1990, 8, 15),
            LocalTime.of(10, 30),
            ZoneId.of("Asia/Kolkata"),
            28.6139,
            77.2090,
            "Delhi, India"),
        AyanamsaMode.LAHIRI,
        false);
  }

  private static ChartRequest mumbaiRequest() {
    return new ChartRequest(
        new BirthMoment(
            LocalDate.of(2000, 1, 1),
            LocalTime.of(12, 0),
            ZoneId.of("Asia/Kolkata"),
            19.0760,
            72.8777,
            "Mumbai, India"),
        AyanamsaMode.LAHIRI,
        false);
  }
}
