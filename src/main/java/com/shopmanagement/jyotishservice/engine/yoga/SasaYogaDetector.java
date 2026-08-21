package com.shopmanagement.jyotishservice.engine.yoga;

import com.shopmanagement.jyotishservice.engine.model.Planet;

/** Sasa (Panch Mahapurusha): Saturn own/exalt in Lagna kendra. */
public final class SasaYogaDetector implements YogaDetector {

  public static final SasaYogaDetector INSTANCE = new SasaYogaDetector();
  public static final String RULE_ID = "SASA_SATURN_OWN_EXALT_KENDRA_D1_V1";

  private SasaYogaDetector() {}

  @Override
  public YogaCode code() {
    return YogaCode.SASA;
  }

  @Override
  public YogaHit detect(YogaContext ctx) {
    return MahapurushaSupport.detect(
        code(),
        Planet.SATURN,
        ctx,
        RULE_ID,
        "Sasa requires Saturn in own or exaltation sign in a Lagna kendra (whole-sign D1).");
  }
}
