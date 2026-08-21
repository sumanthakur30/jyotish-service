package com.shopmanagement.jyotishservice.engine.ephemeris;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;

import com.shopmanagement.jyotishservice.engine.CalculationEngine;
import com.shopmanagement.jyotishservice.engine.model.AyanamsaMode;
import com.shopmanagement.jyotishservice.engine.model.BirthMoment;
import com.shopmanagement.jyotishservice.engine.model.ChartRequest;
import com.shopmanagement.jyotishservice.engine.model.D1Chart;

/**
 * Regression: Meeus atan2 args were both negated → ASC + 180° (Pisces vs Virgo at same deg-in-sign).
 */
class MeeusAscendantTest {

  @Test
  void sumanKatihar1975LagnaIsVirgoNotPisces() {
    BirthMoment birth =
        new BirthMoment(
            LocalDate.of(1975, 8, 6),
            LocalTime.of(9, 30),
            ZoneId.of("Asia/Kolkata"),
            25.5394,
            87.5713,
            "Katihar, Bihar, India");
    D1Chart chart =
        new CalculationEngine(new MeeusEphemeris())
            .computeD1(new ChartRequest(birth, AyanamsaMode.LAHIRI, false));

    // Computer Zone print: Lagna Virgo ~16°58′ Hasta (sign index 5).
    assertEquals(5, chart.ascendant().signIndex(), "Lagna sign Virgo");
    assertEquals("Virgo", chart.ascendant().signName());
    double deg = chart.ascendant().degreeInSign();
    assertTrue(deg > 15.5 && deg < 18.5, () -> "degree-in-sign near 16°58′, was " + deg);
  }
}
