package com.shopmanagement.jyotishservice.engine.transit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SadeSatiCalculatorTest {

  @Test
  void peakWhenSaturnOnMoon() {
    var a = SadeSatiCalculator.analyze(3, 3); // Cancer
    assertEquals(SadeSatiCalculator.Phase.PEAK, a.phase());
    assertTrue(a.inSadeSati());
    assertEquals(0, a.signsFromMoon());
    assertEquals(1, a.houseFromMoon());
  }

  @Test
  void risingWhenSaturnInTwelfthFromMoon() {
    // Moon Aries (0), Saturn Pisces (11) → house 12
    var a = SadeSatiCalculator.analyze(0, 11);
    assertEquals(SadeSatiCalculator.Phase.RISING, a.phase());
    assertEquals(11, a.signsFromMoon());
    assertEquals(12, a.houseFromMoon());
  }

  @Test
  void settingWhenSaturnInSecondFromMoon() {
    var a = SadeSatiCalculator.analyze(0, 1);
    assertEquals(SadeSatiCalculator.Phase.SETTING, a.phase());
    assertEquals(1, a.signsFromMoon());
    assertEquals(2, a.houseFromMoon());
  }

  @Test
  void outsideNotInSadeSati() {
    var a = SadeSatiCalculator.analyze(0, 5);
    assertEquals(SadeSatiCalculator.Phase.NOT_IN_SADE_SATI, a.phase());
    assertFalse(a.inSadeSati());
  }
}
