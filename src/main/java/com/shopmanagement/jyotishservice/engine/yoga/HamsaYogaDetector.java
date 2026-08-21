package com.shopmanagement.jyotishservice.engine.yoga;

import com.shopmanagement.jyotishservice.engine.model.Planet;

/** Hamsa (Panch Mahapurusha): Jupiter own/exalt in Lagna kendra. */
public final class HamsaYogaDetector implements YogaDetector {

  public static final HamsaYogaDetector INSTANCE = new HamsaYogaDetector();
  public static final String RULE_ID = "HAMSA_JUPITER_OWN_EXALT_KENDRA_D1_V1";

  private HamsaYogaDetector() {}

  @Override
  public YogaCode code() {
    return YogaCode.HAMSA;
  }

  @Override
  public YogaHit detect(YogaContext ctx) {
    return MahapurushaSupport.detect(
        code(),
        Planet.JUPITER,
        ctx,
        RULE_ID,
        "Hamsa requires Jupiter in own or exaltation sign in a Lagna kendra (whole-sign D1).");
  }
}
