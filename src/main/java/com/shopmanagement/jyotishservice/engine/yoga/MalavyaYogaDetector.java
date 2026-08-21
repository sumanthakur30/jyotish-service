package com.shopmanagement.jyotishservice.engine.yoga;

import com.shopmanagement.jyotishservice.engine.model.Planet;

/** Malavya (Panch Mahapurusha): Venus own/exalt in Lagna kendra. */
public final class MalavyaYogaDetector implements YogaDetector {

  public static final MalavyaYogaDetector INSTANCE = new MalavyaYogaDetector();
  public static final String RULE_ID = "MALAVYA_VENUS_OWN_EXALT_KENDRA_D1_V1";

  private MalavyaYogaDetector() {}

  @Override
  public YogaCode code() {
    return YogaCode.MALAVYA;
  }

  @Override
  public YogaHit detect(YogaContext ctx) {
    return MahapurushaSupport.detect(
        code(),
        Planet.VENUS,
        ctx,
        RULE_ID,
        "Malavya requires Venus in own or exaltation sign in a Lagna kendra (whole-sign D1).");
  }
}
