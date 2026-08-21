package com.shopmanagement.jyotishservice.engine.shadbala;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import com.shopmanagement.jyotishservice.engine.shadbala.ShadbalaCalculator.ComponentStatus;
import com.shopmanagement.jyotishservice.engine.shadbala.ShadbalaCalculator.PlanetShadbala;
import com.shopmanagement.jyotishservice.engine.shadbala.ShadbalaCalculator.ShadbalaReport;

class ShadbalaCalculatorTest {

  @Test
  void partialReportNeverClaimsFullTotal() {
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

    ShadbalaReport report = ShadbalaCalculator.compute(d1);
    assertEquals(7, report.planets().size());
    assertTrue(report.notes().contains("PARTIAL"));

    PlanetShadbala sun =
        report.planets().stream().filter(p -> p.planet() == Planet.SUN).findFirst().orElseThrow();
    assertTrue(sun.implementedComponents().contains("NAISARGIKA"));
    assertTrue(sun.implementedComponents().contains("DIG"));
    assertTrue(sun.implementedComponents().contains("STHANA_SUBSET"));
    assertFalse(sun.comingSoonComponents().isEmpty());
    assertTrue(sun.notes().contains("PARTIAL"));

    double readySum = 0;
    for (var c : sun.components()) {
      if (c.status() == ComponentStatus.READY) {
        assertTrue(c.virupas() != null);
        readySum += c.virupas();
      } else {
        assertEquals(ComponentStatus.COMING_SOON, c.status());
        assertNull(c.virupas());
      }
    }
    assertEquals(readySum, sun.partialTotalVirupas(), 1e-6);
    assertEquals(60.0, sun.components().stream()
        .filter(c -> "NAISARGIKA".equals(c.code()))
        .findFirst()
        .orElseThrow()
        .virupas(), 1e-6);
  }
}
