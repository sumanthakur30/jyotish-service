package com.shopmanagement.jyotishservice.engine.yoga;

import java.util.List;

import com.shopmanagement.jyotishservice.engine.astro.ZodiacCatalog;
import com.shopmanagement.jyotishservice.engine.model.Planet;
import com.shopmanagement.jyotishservice.engine.model.PlanetPosition;

/**
 * Gajakesari (Chandra yoga): Moon and Jupiter in mutual kendras (whole-sign: offsets 0/3/6/9).
 *
 * <p>Strength (rule-defined): FULL if both also occupy kendras from Lagna; otherwise PARTIAL when
 * present. Language stays descriptive — no fate claims.
 */
public final class GajakesariYogaDetector implements YogaDetector {

  public static final GajakesariYogaDetector INSTANCE = new GajakesariYogaDetector();
  public static final String RULE_ID = "GAJAKESARI_MUTUAL_KENDRA_D1_V1";

  private GajakesariYogaDetector() {}

  @Override
  public YogaCode code() {
    return YogaCode.GAJAKESARI;
  }

  @Override
  public YogaHit detect(YogaContext ctx) {
    PlanetPosition moon =
        ctx.d1(Planet.MOON)
            .orElseThrow(() -> new IllegalStateException("Moon required for Gajakesari"));
    PlanetPosition jupiter =
        ctx.d1(Planet.JUPITER)
            .orElseThrow(() -> new IllegalStateException("Jupiter required for Gajakesari"));

    List<String> planets = List.of(Planet.MOON.name(), Planet.JUPITER.name());
    int moonHouse = moon.house();
    int jupHouse = jupiter.house();
    List<Integer> houses = List.of(moonHouse, jupHouse);

    boolean mutual = YogaChartMath.mutualKendra(moon.signIndex(), jupiter.signIndex());
    if (!mutual) {
      return YogaHit.absent(
          code(),
          planets,
          houses,
          "Moon ("
              + ZodiacCatalog.signName(moon.signIndex())
              + ", house "
              + moonHouse
              + ") and Jupiter ("
              + ZodiacCatalog.signName(jupiter.signIndex())
              + ", house "
              + jupHouse
              + ") are not in mutual kendras on D1. Rule: whole-sign mutual kendra (1/4/7/10 from"
              + " each other). This notes a chart pattern only — not a prediction.",
          RULE_ID);
    }

    boolean bothLagnaKendra =
        YogaChartMath.isKendraHouse(moonHouse) && YogaChartMath.isKendraHouse(jupHouse);
    YogaStrength strength = bothLagnaKendra ? YogaStrength.FULL : YogaStrength.PARTIAL;

    return YogaHit.present(
        code(),
        strength,
        planets,
        houses,
        "Moon and Jupiter occupy mutual kendras on D1 (whole-sign). Moon in "
            + ZodiacCatalog.signName(moon.signIndex())
            + " (house "
            + moonHouse
            + "), Jupiter in "
            + ZodiacCatalog.signName(jupiter.signIndex())
            + " (house "
            + jupHouse
            + "). Strength "
            + strength.displayName()
            + " because "
            + (bothLagnaKendra
                ? "both also sit in kendras from Lagna."
                : "mutual kendra holds, but not both from Lagna kendras.")
            + " Descriptive pattern only — not a guarantee of outcomes.",
        RULE_ID);
  }
}
