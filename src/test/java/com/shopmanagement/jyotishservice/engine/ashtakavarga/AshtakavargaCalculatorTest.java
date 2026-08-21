package com.shopmanagement.jyotishservice.engine.ashtakavarga;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import com.shopmanagement.jyotishservice.engine.model.Planet;

class AshtakavargaCalculatorTest {

  @Test
  void delhiSampleHasTwelveSignsAndPlausibleSavTotal() {
    D1Chart d1 =
        new CalculationEngine()
            .computeD1(
                new ChartRequest(
                    new BirthMoment(
                        LocalDate.of(1990, 8, 15),
                        LocalTime.of(10, 30),
                        ZoneId.of("Asia/Kolkata"),
                        28.6139,
                        77.2090,
                        "Delhi"),
                    AyanamsaMode.LAHIRI,
                    false));

    AshtakavargaCalculator.AshtakavargaResult r = AshtakavargaCalculator.compute(d1);
    assertEquals(7, r.bhinnashtakavarga().size());
    assertEquals(12, r.sarvashtakavarga().length);
    for (Planet p :
        new Planet[] {
          Planet.SUN, Planet.MOON, Planet.MARS, Planet.MERCURY, Planet.JUPITER, Planet.VENUS,
          Planet.SATURN
        }) {
      int[] row = r.bhinna(p);
      assertNotNull(row);
      assertEquals(12, row.length);
      int sum = 0;
      for (int v : row) {
        assertTrue(v >= 0 && v <= 8, p + " bindu out of range: " + v);
        sum += v;
      }
      assertTrue(sum >= 30 && sum <= 60, p + " BAV sum " + sum);
    }
    // Classical SAV total is typically ~337; allow 300–400 for structural sanity.
    assertTrue(
        r.totalBindus() >= 300 && r.totalBindus() <= 400,
        "SAV total out of range: " + r.totalBindus());
    int check = 0;
    for (int v : r.sarvashtakavarga()) {
      check += v;
    }
    assertEquals(r.totalBindus(), check);
    assertTrue(r.notes().toLowerCase().contains("rahu"));
  }
}
