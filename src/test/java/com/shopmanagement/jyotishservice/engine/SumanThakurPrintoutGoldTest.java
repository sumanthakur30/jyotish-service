package com.shopmanagement.jyotishservice.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.EnumMap;
import java.util.Map;

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
 * Desktop printout gold for SUMAN THAKUR — 6 Aug 1975, 09:30 IST, Katihar (87°30′E).
 *
 * <p>Values transcribed from the user's Nirayana Graha Spashta screenshot (Lahiri).
 */
class SumanThakurPrintoutGoldTest {

  private static final Path SWISS_JAR =
      Path.of("third_party/swiss-ephemeris/swisseph-2.01.00-02.jar");

  /** Sidereal longitudes from printout DMS. */
  private static final Map<Planet, Double> PRINTOUT = printout();

  private static Map<Planet, Double> printout() {
    EnumMap<Planet, Double> m = new EnumMap<>(Planet.class);
    m.put(Planet.ASCENDANT, dms(5, 16, 58, 32)); // Kanya
    m.put(Planet.SUN, dms(3, 19, 34, 41)); // Karka
    m.put(Planet.MOON, dms(3, 1, 9, 33));
    m.put(Planet.MARS, dms(1, 0, 59, 29)); // Vrisha
    m.put(Planet.MERCURY, dms(3, 24, 46, 48));
    m.put(Planet.JUPITER, dms(0, 1, 2, 52)); // Mesha
    m.put(Planet.VENUS, dms(4, 18, 11, 44)); // Simha
    m.put(Planet.SATURN, dms(3, 1, 49, 21));
    m.put(Planet.RAHU, dms(7, 4, 25, 2)); // Vrischika
    m.put(Planet.KETU, dms(1, 4, 25, 2));
    return Map.copyOf(m);
  }

  private static ChartRequest katihar() {
    return new ChartRequest(
        new BirthMoment(
            LocalDate.of(1975, 8, 6),
            LocalTime.of(9, 30),
            ZoneId.of("Asia/Kolkata"),
            25.5394,
            87.5,
            "Katihar, Bihar, India"),
        AyanamsaMode.LAHIRI,
        false);
  }

  @Test
  void meeusLagnaMatchesPrintoutSign() {
    D1Chart chart = new CalculationEngine(new MeeusEphemeris()).computeD1(katihar());
    dump("MEEUS", chart);
    assertEquals(5, chart.ascendant().signIndex(), "Lagna must be Virgo (printout कन्या), not Pisces");
    assertTrue(
        angularDelta(chart.ascendant().longitudeDeg(), PRINTOUT.get(Planet.ASCENDANT)) < 2.0,
        "Meeus Lagna should be within ~2° of printout 16:58 Virgo");
  }

  @Test
  void swissMatchesPrintoutWhenJarPresent() {
    if (!Files.isRegularFile(SWISS_JAR)) {
      System.out.println("Swiss JAR missing — skip print-grade compare: " + SWISS_JAR.toAbsolutePath());
      return;
    }
    JyotishEphemerisProperties props = new JyotishEphemerisProperties();
    props.setProvider(EphemerisProviders.SWISS);
    props.setSwissJarPath(SWISS_JAR.toString());
    props.setSwissUseFiles(false);
    EphemerisProvider swiss = EphemerisProviders.create(props);
    D1Chart chart = new CalculationEngine(swiss).computeD1(katihar());
    dump("SWISS-MOSHIER", chart);
    assertEquals(5, chart.ascendant().signIndex(), "Swiss Lagna Virgo");
    for (Map.Entry<Planet, Double> e : PRINTOUT.entrySet()) {
      double actual =
          e.getKey() == Planet.ASCENDANT
              ? chart.ascendant().longitudeDeg()
              : chart.planets().stream()
                  .filter(p -> p.planet() == e.getKey())
                  .mapToDouble(PlanetPosition::longitudeDeg)
                  .findFirst()
                  .orElseThrow();
      double delta = angularDelta(actual, e.getValue());
      assertTrue(
          delta < 0.15,
          () -> e.getKey() + " delta=" + delta + "° actual=" + actual + " printout=" + e.getValue());
    }
  }

  private static void dump(String label, D1Chart chart) {
    System.out.println("=== " + label + " vs printout (Suman Katihar 1975-08-06 09:30 IST) ===");
    System.out.printf(
        "  %-10s  engine=%s  printout=%s  Δ=%.3f°%n",
        "ASC",
        fmt(chart.ascendant().signName(), chart.ascendant().degreeInSign()),
        "Virgo 16:58:32",
        angularDelta(chart.ascendant().longitudeDeg(), PRINTOUT.get(Planet.ASCENDANT)));
    for (PlanetPosition p : chart.planets()) {
      if (p.planet().isOuter()) {
        continue;
      }
      Double gold = PRINTOUT.get(p.planet());
      if (gold == null) {
        continue;
      }
      System.out.printf(
          "  %-10s  engine=%s  Δ=%.3f°%n",
          p.planet().name(),
          fmt(p.signName(), p.degreeInSign()),
          angularDelta(p.longitudeDeg(), gold));
    }
  }

  private static String fmt(String sign, double deg) {
    int d = (int) Math.floor(deg);
    int m = (int) Math.floor((deg - d) * 60);
    int s = (int) Math.floor((((deg - d) * 60) - m) * 60);
    return String.format("%s %02d:%02d:%02d", sign, d, m, s);
  }

  private static double dms(int signIndex, int d, int m, int s) {
    return signIndex * 30.0 + d + m / 60.0 + s / 3600.0;
  }

  private static double angularDelta(double a, double b) {
    double d = Math.abs(a - b) % 360.0;
    return d > 180.0 ? 360.0 - d : d;
  }
}
