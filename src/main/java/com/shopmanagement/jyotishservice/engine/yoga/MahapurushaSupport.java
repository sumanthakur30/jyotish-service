package com.shopmanagement.jyotishservice.engine.yoga;

import java.util.List;

import com.shopmanagement.jyotishservice.engine.astro.ZodiacCatalog;
import com.shopmanagement.jyotishservice.engine.model.Planet;
import com.shopmanagement.jyotishservice.engine.model.PlanetPosition;

/** Shared Panch Mahapurusha own/exalt + Lagna-kendra check. */
final class MahapurushaSupport {

  private MahapurushaSupport() {}

  static YogaHit detect(
      YogaCode code, Planet planet, YogaContext ctx, String ruleId, String ruleSentence) {
    PlanetPosition pos =
        ctx.d1(planet)
            .orElseThrow(() -> new IllegalStateException(planet + " required for " + code.code()));

    List<String> planets = List.of(planet.name());
    List<Integer> houses = List.of(pos.house());
    boolean own = YogaChartMath.isOwnSign(planet, pos.signIndex());
    boolean exalt = YogaChartMath.isExalted(planet, pos.signIndex());
    boolean kendra = YogaChartMath.isKendraHouse(pos.house());
    boolean dignity = own || exalt;

    String where =
        planet.displayName()
            + " in "
            + ZodiacCatalog.signName(pos.signIndex())
            + " (house "
            + pos.house()
            + "). "
            + ruleSentence;

    if (!dignity || !kendra) {
      return YogaHit.absent(
          code,
          planets,
          houses,
          where
              + " Conditions not met (own/exalt="
              + dignity
              + ", Lagna kendra="
              + kendra
              + "). Pattern note only — not a character judgment.",
          ruleId);
    }

    YogaStrength strength = exalt ? YogaStrength.FULL : YogaStrength.MODERATE;
    return YogaHit.present(
        code,
        strength,
        planets,
        houses,
        where
            + " Present with strength "
            + strength.displayName()
            + " ("
            + (exalt ? "exaltation" : "own sign")
            + " in kendra). Descriptive dignity pattern only — not a prediction of status or"
            + " temperament.",
        ruleId);
  }
}
