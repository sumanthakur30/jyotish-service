package com.shopmanagement.jyotishservice.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;

import com.shopmanagement.jyotishservice.engine.model.AyanamsaMode;
import com.shopmanagement.jyotishservice.engine.model.BirthMoment;
import com.shopmanagement.jyotishservice.engine.model.ChartRequest;
import com.shopmanagement.jyotishservice.engine.model.D1Chart;
import com.shopmanagement.jyotishservice.engine.model.Planet;

class CalculationEngineTest {

  private final CalculationEngine engine = new CalculationEngine();

  @Test
  void versionIsV1_6() {
    assertEquals("V1.6", engine.version());
  }

  @Test
  void computeD1DelhiSampleHasNinePlanetsAndAscendant() {
    BirthMoment birth =
        new BirthMoment(
            LocalDate.of(1990, 8, 15),
            LocalTime.of(10, 30),
            ZoneId.of("Asia/Kolkata"),
            28.6139,
            77.2090,
            "Delhi, India");
    D1Chart chart = engine.computeD1(new ChartRequest(birth, AyanamsaMode.LAHIRI, false));

    assertEquals("V1.6", chart.engineVersion());
    assertEquals(9, chart.planets().size());
    assertEquals(12, chart.houses().size());
    assertEquals(Planet.ASCENDANT, chart.ascendant().planet());
    assertEquals(1, chart.ascendant().house());
    assertTrue(chart.ayanamsaDeg() > 20 && chart.ayanamsaDeg() < 28);
    assertFalse(chart.planets().isEmpty());
    assertTrue(
        chart.planets().stream().anyMatch(p -> p.planet() == Planet.MOON && p.nakshatraName() != null));
  }

  @Test
  void computeD9FromD1HasWholeSignHousesAndMappedLagna() {
    BirthMoment birth =
        new BirthMoment(
            LocalDate.of(1990, 8, 15),
            LocalTime.of(10, 30),
            ZoneId.of("Asia/Kolkata"),
            28.6139,
            77.2090,
            "Delhi, India");
    D1Chart d1 = engine.computeD1(new ChartRequest(birth, AyanamsaMode.LAHIRI, false));
    var d9 = engine.computeVarga(d1, com.shopmanagement.jyotishservice.engine.varga.VargaCode.D9);

    assertEquals("D9", d9.varga().code());
    assertEquals(9, d9.planets().size());
    assertEquals(12, d9.houses().size());
    assertEquals(1, d9.ascendant().house());
    assertEquals("V1.6", d9.engineVersion());
  }
}
