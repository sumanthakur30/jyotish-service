package com.shopmanagement.jyotishservice.engine.yoga;

import java.util.List;

import com.shopmanagement.jyotishservice.engine.astro.ZodiacCatalog;
import com.shopmanagement.jyotishservice.engine.model.Planet;
import com.shopmanagement.jyotishservice.engine.model.PlanetPosition;

/**
 * Budhaditya: Sun and Mercury in the same whole-sign (conjunction). Strength FULL if both also in a
 * Lagna kendra; otherwise MODERATE.
 */
public final class BudhadityaYogaDetector implements YogaDetector {

  public static final BudhadityaYogaDetector INSTANCE = new BudhadityaYogaDetector();
  public static final String RULE_ID = "BUDHADITYA_SUN_MERCURY_SAME_SIGN_D1_V1";

  private BudhadityaYogaDetector() {}

  @Override
  public YogaCode code() {
    return YogaCode.BUDHADITYA;
  }

  @Override
  public YogaHit detect(YogaContext ctx) {
    PlanetPosition sun =
        ctx.d1(Planet.SUN).orElseThrow(() -> new IllegalStateException("Sun required"));
    PlanetPosition mer =
        ctx.d1(Planet.MERCURY).orElseThrow(() -> new IllegalStateException("Mercury required"));
    List<String> planets = List.of("SUN", "MERCURY");
    List<Integer> houses = List.of(sun.house(), mer.house());
    String where =
        "Sun in "
            + ZodiacCatalog.signName(sun.signIndex())
            + " (H"
            + sun.house()
            + "), Mercury in "
            + ZodiacCatalog.signName(mer.signIndex())
            + " (H"
            + mer.house()
            + "). Rule: same whole-sign conjunction.";

    if (!YogaChartMath.conjunct(sun.signIndex(), mer.signIndex())) {
      return YogaHit.absent(
          code(), planets, houses, where + " Not conjunct. Pattern not present.", RULE_ID);
    }
    boolean bothKendra =
        YogaChartMath.isKendraHouse(sun.house()) && YogaChartMath.isKendraHouse(mer.house());
    YogaStrength strength = bothKendra ? YogaStrength.FULL : YogaStrength.MODERATE;
    return YogaHit.present(
        code(),
        strength,
        planets,
        houses,
        where
            + " Present ("
            + strength.displayName()
            + "). Descriptive intelligence/communication pattern only — not a prediction.",
        RULE_ID);
  }
}
