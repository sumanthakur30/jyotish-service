package com.shopmanagement.jyotishservice.engine.transit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.shopmanagement.jyotishservice.engine.CalculationEngine;
import com.shopmanagement.jyotishservice.engine.model.AyanamsaMode;
import com.shopmanagement.jyotishservice.engine.model.BirthMoment;
import com.shopmanagement.jyotishservice.engine.model.ChartRequest;
import com.shopmanagement.jyotishservice.engine.model.D1Chart;
import com.shopmanagement.jyotishservice.engine.model.Planet;

class GocharTransitCalculatorTest {

  private final CalculationEngine engine = new CalculationEngine();

  @Test
  void gocharUsesNatalLagnaHousesAndNinePlanets() {
    BirthMoment birth =
        new BirthMoment(
            LocalDate.of(1990, 8, 15),
            LocalTime.of(10, 30),
            ZoneId.of("Asia/Kolkata"),
            28.6139,
            77.2090,
            "Delhi, India");
    D1Chart d1 = engine.computeD1(new ChartRequest(birth, AyanamsaMode.LAHIRI, false));

    TransitRequest request =
        new TransitRequest(
            LocalDate.of(2026, 8, 20),
            LocalTime.NOON,
            ZoneId.of("Asia/Kolkata"),
            28.6139,
            77.2090,
            AyanamsaMode.LAHIRI,
            d1.ascendant().signIndex());

    TransitChart chart = engine.computeTransit(TransitSystemCode.GOCHAR, request, d1.planets());

    assertEquals("V1.7", chart.engineVersion());
    assertEquals(TransitSystemCode.GOCHAR, chart.system());
    assertEquals(9, chart.rows().size());
    assertEquals(d1.ascendant().signIndex(), chart.natalLagnaSignIndex());
    assertTrue(chart.ayanamsaDeg() > 20 && chart.ayanamsaDeg() < 28);

    NatalTransitRow saturn =
        chart.rows().stream().filter(r -> r.planet() == Planet.SATURN).findFirst().orElseThrow();
    assertEquals(Planet.SATURN, saturn.planet());
    assertTrue(saturn.natalHouse() != null && saturn.natalHouse() >= 1 && saturn.natalHouse() <= 12);
    assertTrue(saturn.transit().house() >= 1 && saturn.transit().house() <= 12);
    assertEquals(
        TransitComparer.signChanged(saturn.natalSignIndex(), saturn.transit().signIndex()),
        saturn.signChanged());
    assertEquals(
        TransitComparer.houseChanged(saturn.natalHouse(), saturn.transit().house()),
        saturn.houseChanged());

    int moonSign =
        d1.planets().stream()
            .filter(p -> p.planet() == Planet.MOON)
            .findFirst()
            .orElseThrow()
            .signIndex();
    var sade = SadeSatiCalculator.analyze(moonSign, saturn.transit().signIndex());
    assertTrue(sade.phase() != null);
  }

  @Test
  void sadeSatiHasNoDedicatedTransitCalculatorYet() {
    // Sade Sati is attached on Gochar API responses via SadeSatiCalculator, not TransitRegistry.
    assertFalse(TransitRegistry.isImplemented(TransitSystemCode.SADE_SATI));
    assertThrows(
        IllegalArgumentException.class,
        () -> TransitRegistry.requireCalculator(TransitSystemCode.SADE_SATI));
  }

  @Test
  void registryListsGocharReady() {
    assertTrue(TransitRegistry.isImplemented(TransitSystemCode.GOCHAR));
    assertEquals(2, TransitRegistry.all().length);
  }
}
