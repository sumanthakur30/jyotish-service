package com.shopmanagement.jyotishservice.engine.yoga;

import com.shopmanagement.jyotishservice.engine.model.Planet;

/**
 * Bhadra (Panch Mahapurusha): Mercury in own sign (Gemini/Virgo) or exaltation (Virgo) and in a
 * kendra from Lagna. Strength: FULL if exalted, MODERATE if own sign.
 */
public final class BhadraYogaDetector implements YogaDetector {

  public static final BhadraYogaDetector INSTANCE = new BhadraYogaDetector();
  public static final String RULE_ID = "BHADRA_MERCURY_OWN_EXALT_KENDRA_D1_V1";

  private BhadraYogaDetector() {}

  @Override
  public YogaCode code() {
    return YogaCode.BHADRA;
  }

  @Override
  public YogaHit detect(YogaContext ctx) {
    return MahapurushaSupport.detect(
        code(),
        Planet.MERCURY,
        ctx,
        RULE_ID,
        "Bhadra requires Mercury in own or exaltation sign in a Lagna kendra (whole-sign D1).");
  }
}
