package com.shopmanagement.jyotishservice.engine.transit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.shopmanagement.jyotishservice.engine.model.Planet;
import com.shopmanagement.jyotishservice.engine.model.PlanetPosition;

class TransitComparerTest {

  @Test
  void compareFlagsSignAndHouseChanges() {
    PlanetPosition natalMoon =
        new PlanetPosition(
            Planet.MOON, 45.0, 1, "Taurus", 15.0, 2, 3, "Rohini", 2, false, false, 13.0);
    PlanetPosition natalSun =
        new PlanetPosition(
            Planet.SUN, 120.0, 4, "Leo", 0.0, 5, 10, "Magha", 1, false, false, 1.0);

    TransitPlanetPosition transitMoon =
        new TransitPlanetPosition(
            Planet.MOON, 100.0, 3, "Cancer", 10.0, 4, 7, "Pushya", 1, false, 13.0);
    TransitPlanetPosition transitSun =
        new TransitPlanetPosition(
            Planet.SUN, 120.5, 4, "Leo", 0.5, 5, 10, "Magha", 1, false, 1.0);

    List<NatalTransitRow> rows =
        TransitComparer.compare(List.of(natalMoon, natalSun), List.of(transitMoon, transitSun));

    assertEquals(2, rows.size());
    NatalTransitRow moon = rows.stream().filter(r -> r.planet() == Planet.MOON).findFirst().orElseThrow();
    NatalTransitRow sun = rows.stream().filter(r -> r.planet() == Planet.SUN).findFirst().orElseThrow();

    assertTrue(moon.signChanged());
    assertTrue(moon.houseChanged());
    assertEquals(Integer.valueOf(1), moon.natalSignIndex());
    assertEquals(Integer.valueOf(2), moon.natalHouse());
    assertEquals(3, moon.transit().signIndex());
    assertEquals(4, moon.transit().house());

    assertFalse(sun.signChanged());
    assertFalse(sun.houseChanged());
  }

  @Test
  void helpersTreatNullNatalAsUnchanged() {
    assertFalse(TransitComparer.signChanged(null, 4));
    assertFalse(TransitComparer.houseChanged(null, 7));
    assertTrue(TransitComparer.signChanged(1, 4));
    assertTrue(TransitComparer.houseChanged(2, 7));
    assertFalse(TransitComparer.signChanged(4, 4));
  }
}
