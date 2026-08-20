package com.shopmanagement.jyotishservice.engine.yoga;

import java.util.ArrayList;
import java.util.List;

import com.shopmanagement.jyotishservice.engine.astro.ZodiacCatalog;
import com.shopmanagement.jyotishservice.engine.model.Planet;
import com.shopmanagement.jyotishservice.engine.model.PlanetPosition;

/**
 * Dhana Yoga (one clear pattern): lords of the 2nd and 11th houses are associated on D1.
 *
 * <p>Association = conjunction or mutual kendra (same as Raja detector). Strength omitted. No
 * wealth promises — descriptive only.
 */
public final class Dhana211YogaDetector implements YogaDetector {

  public static final Dhana211YogaDetector INSTANCE = new Dhana211YogaDetector();
  public static final String RULE_ID = "DHANA_2_11_LORDS_ASSOCIATION_D1_V1";

  private Dhana211YogaDetector() {}

  @Override
  public YogaCode code() {
    return YogaCode.DHANA_2_11;
  }

  @Override
  public YogaHit detect(YogaContext ctx) {
    Planet lord2 = ctx.lordOfHouse(2);
    Planet lord11 = ctx.lordOfHouse(11);
    PlanetPosition p2 =
        ctx.d1(lord2)
            .orElseThrow(() -> new IllegalStateException("2nd lord missing: " + lord2));
    PlanetPosition p11 =
        ctx.d1(lord11)
            .orElseThrow(() -> new IllegalStateException("11th lord missing: " + lord11));

    List<String> planets = distinctPlanets(lord2, lord11);
    List<Integer> houses = List.of(p2.house(), p11.house());

    boolean assoc = YogaChartMath.associated(p2.signIndex(), p11.signIndex());
    String base =
        "2nd lord "
            + lord2.displayName()
            + " in "
            + ZodiacCatalog.signName(p2.signIndex())
            + " (house "
            + p2.house()
            + "); 11th lord "
            + lord11.displayName()
            + " in "
            + ZodiacCatalog.signName(p11.signIndex())
            + " (house "
            + p11.house()
            + "). Rule: association = conjunction or mutual kendra on D1.";

    if (!assoc) {
      return YogaHit.absent(
          code(),
          planets,
          houses,
          base + " Lords are not associated under this rule. Pattern note only — not a prediction.",
          RULE_ID);
    }

    return YogaHit.present(
        code(),
        null,
        planets,
        houses,
        base
            + " Lords are associated. Classical texts relate 2–11 links to resources and gains"
            + " themes; this flags geometry only — not a promise of wealth.",
        RULE_ID);
  }

  private static List<String> distinctPlanets(Planet a, Planet b) {
    List<String> out = new ArrayList<>(2);
    out.add(a.name());
    if (a != b) {
      out.add(b.name());
    }
    return List.copyOf(out);
  }
}
