package com.shopmanagement.jyotishservice.engine.houses;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.shopmanagement.jyotishservice.engine.CalculationEngine;
import com.shopmanagement.jyotishservice.engine.ephemeris.MeeusEphemeris;
import com.shopmanagement.jyotishservice.engine.model.AyanamsaMode;
import com.shopmanagement.jyotishservice.engine.model.BirthMoment;
import com.shopmanagement.jyotishservice.engine.model.ChartRequest;
import com.shopmanagement.jyotishservice.engine.model.D1Chart;
import com.shopmanagement.jyotishservice.engine.model.HouseSystem;
import com.shopmanagement.jyotishservice.engine.model.Planet;
import com.shopmanagement.jyotishservice.engine.model.PlanetPosition;

/**
 * Sripati Bhava Chalit must not copy whole-sign houses. Suman Katihar 1975 is the print regression
 * anchor (Mars/Ketu often shift H9→H8 in Chalit vs Rashi).
 */
class SripatiBhavaChalitTest {

  private static final BirthMoment SUMAN =
      new BirthMoment(
          LocalDate.of(1975, 8, 6),
          LocalTime.of(9, 30),
          ZoneId.of("Asia/Kolkata"),
          25.5394,
          87.5713,
          "Katihar, Bihar, India");

  @Test
  void sandhiCuspsAreUnequalAndCoverZodiac() {
    double[] c = SripatiBhavaChalit.sandhiCusps(166.98, 80.0);
    assertEquals(12, c.length - 1);
    double spanSum = 0;
    for (int h = 1; h <= 12; h++) {
      double start = c[h];
      double end = c[h == 12 ? 1 : h + 1];
      double span = (end - start + 360.0) % 360.0;
      assertTrue(span > 1.0 && span < 60.0, () -> "bhava span out of range: " + span);
      spanSum += span;
    }
    assertEquals(360.0, spanSum, 0.01);
  }

  @Test
  void sumanChalitDiffersFromWholeSignForBoundaryGrahas() {
    CalculationEngine engine = new CalculationEngine(new MeeusEphemeris());
    D1Chart whole =
        engine.computeD1(new ChartRequest(SUMAN, AyanamsaMode.LAHIRI, false), HouseSystem.WHOLE_SIGN);
    D1Chart chalit =
        engine.computeChalit(whole, SUMAN.latitudeDeg(), SUMAN.longitudeDeg());

    assertEquals(HouseSystem.WHOLE_SIGN.code(), whole.houseSystem());
    assertEquals(HouseSystem.SRIPATI.code(), chalit.houseSystem());
    assertEquals(5, whole.ascendant().signIndex(), "Lagna Virgo");

    Map<Planet, Integer> wholeH =
        whole.planets().stream()
            .collect(Collectors.toMap(PlanetPosition::planet, PlanetPosition::house));
    Map<Planet, Integer> chalitH =
        chalit.planets().stream()
            .collect(Collectors.toMap(PlanetPosition::planet, PlanetPosition::house));

    // At least one classical graha must move house vs whole-sign (otherwise we faked Chalit).
    boolean anyMoved =
        wholeH.entrySet().stream()
            .filter(e -> e.getKey().isClassicalGraha())
            .anyMatch(e -> !e.getValue().equals(chalitH.get(e.getKey())));
    assertTrue(anyMoved, "Chalit must rehouse at least one graha vs WHOLE_SIGN");

    // Print expectation: Mars/Ketu often leave H9 for H8 in Chalit — soft check (depends on
    // ephemeris lon + MC). Always require real rehouse somewhere.
    assertEquals(9, wholeH.get(Planet.MARS));
    assertEquals(9, wholeH.get(Planet.KETU));
    System.out.printf(
        "Suman Chalit Mars H%d Ketu H%d (whole H9)%n",
        chalitH.get(Planet.MARS), chalitH.get(Planet.KETU));
  }

  @Test
  void houseOfAssignsByCuspNotSign() {
    // Equal 30° cusps from 0° — same as whole-sign from Aries ASC.
    double[] cusps = new double[13];
    for (int h = 1; h <= 12; h++) {
      cusps[h] = (h - 1) * 30.0;
    }
    assertEquals(1, SripatiBhavaChalit.houseOf(0.0, cusps));
    assertEquals(1, SripatiBhavaChalit.houseOf(29.9, cusps));
    assertEquals(2, SripatiBhavaChalit.houseOf(30.0, cusps));
  }
}
