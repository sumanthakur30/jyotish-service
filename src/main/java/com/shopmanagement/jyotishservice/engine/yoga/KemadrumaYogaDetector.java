package com.shopmanagement.jyotishservice.engine.yoga;

import java.util.ArrayList;
import java.util.List;

import com.shopmanagement.jyotishservice.engine.astro.ZodiacCatalog;
import com.shopmanagement.jyotishservice.engine.model.Planet;
import com.shopmanagement.jyotishservice.engine.model.PlanetPosition;

/**
 * Kemadruma (simplified classical screen): from the Moon, houses 2 and 12 have no classical planet
 * (Sun–Saturn). Nodes are ignored. Descriptive pattern only.
 */
public final class KemadrumaYogaDetector implements YogaDetector {

  public static final KemadrumaYogaDetector INSTANCE = new KemadrumaYogaDetector();
  public static final String RULE_ID = "KEMADRUMA_MOON_2_12_EMPTY_D1_V1";

  private KemadrumaYogaDetector() {}

  @Override
  public YogaCode code() {
    return YogaCode.KEMADRUMA;
  }

  @Override
  public YogaHit detect(YogaContext ctx) {
    PlanetPosition moon =
        ctx.d1(Planet.MOON).orElseThrow(() -> new IllegalStateException("Moon required"));
    int moonSign = moon.signIndex();
    List<String> neighbors = new ArrayList<>();
    for (PlanetPosition p : ctx.d1Planets()) {
      Planet pl = p.planet();
      if (pl == Planet.MOON || pl == Planet.ASCENDANT || pl == Planet.RAHU || pl == Planet.KETU) {
        continue;
      }
      int fromMoon = Math.floorMod(p.signIndex() - moonSign, 12) + 1;
      if (fromMoon == 2 || fromMoon == 12) {
        neighbors.add(pl.displayName() + " (from Moon house " + fromMoon + ")");
      }
    }
    List<String> planets = List.of("MOON");
    List<Integer> houses = List.of(moon.house());
    String where =
        "Moon in "
            + ZodiacCatalog.signName(moonSign)
            + " (house "
            + moon.house()
            + "). Rule: no Sun–Saturn planet in 2nd or 12th from Moon (nodes ignored).";

    if (!neighbors.isEmpty()) {
      return YogaHit.absent(
          code(),
          planets,
          houses,
          where + " Occupied by: " + String.join(", ", neighbors) + ". Not present.",
          RULE_ID);
    }
    return YogaHit.present(
        code(),
        YogaStrength.MODERATE,
        planets,
        houses,
        where
            + " 2nd and 12th from Moon are empty of classical planets. Pattern note only — not a"
            + " prediction of loneliness or misfortune.",
        RULE_ID);
  }
}
