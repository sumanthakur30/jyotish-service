package com.shopmanagement.jyotishservice.engine.yoga;

import com.shopmanagement.jyotishservice.engine.model.Planet;

/**
 * Ruchaka (Panch Mahapurusha): Mars in own sign (Aries/Scorpio) or exaltation (Capricorn) and in a
 * kendra from Lagna (houses 1/4/7/10). Strength: FULL if exalted, MODERATE if own sign.
 */
public final class RuchakaYogaDetector implements YogaDetector {

  public static final RuchakaYogaDetector INSTANCE = new RuchakaYogaDetector();
  public static final String RULE_ID = "RUCHAKA_MARS_OWN_EXALT_KENDRA_D1_V1";

  private RuchakaYogaDetector() {}

  @Override
  public YogaCode code() {
    return YogaCode.RUCHAKA;
  }

  @Override
  public YogaHit detect(YogaContext ctx) {
    return MahapurushaSupport.detect(
        code(),
        Planet.MARS,
        ctx,
        RULE_ID,
        "Ruchaka requires Mars in own or exaltation sign in a Lagna kendra (whole-sign D1).");
  }
}
