package com.shopmanagement.jyotishservice.engine.yoga;

import java.util.ArrayList;
import java.util.List;

import com.shopmanagement.jyotishservice.engine.astro.ZodiacCatalog;
import com.shopmanagement.jyotishservice.engine.model.Planet;
import com.shopmanagement.jyotishservice.engine.model.PlanetPosition;

/**
 * Dharma-Karmadhipati Raja Yoga: lords of the 9th and 10th houses are associated on D1.
 *
 * <p>Association (documented): same sign (conjunction) or mutual kendra (offsets 0/3/6/9). Strength
 * not graded in V1 — omit. Responsible language only.
 */
public final class DharmaKarmadhipatiYogaDetector implements YogaDetector {

  public static final DharmaKarmadhipatiYogaDetector INSTANCE =
      new DharmaKarmadhipatiYogaDetector();
  public static final String RULE_ID = "RAJA_9_10_LORDS_ASSOCIATION_D1_V1";

  private DharmaKarmadhipatiYogaDetector() {}

  @Override
  public YogaCode code() {
    return YogaCode.DHARMA_KARMADHIPATI;
  }

  @Override
  public YogaHit detect(YogaContext ctx) {
    Planet lord9 = ctx.lordOfHouse(9);
    Planet lord10 = ctx.lordOfHouse(10);
    PlanetPosition p9 =
        ctx.d1(lord9)
            .orElseThrow(() -> new IllegalStateException("9th lord missing: " + lord9));
    PlanetPosition p10 =
        ctx.d1(lord10)
            .orElseThrow(() -> new IllegalStateException("10th lord missing: " + lord10));

    List<String> planets = distinctPlanets(lord9, lord10);
    List<Integer> houses = List.of(p9.house(), p10.house());

    boolean assoc = YogaChartMath.associated(p9.signIndex(), p10.signIndex());
    String base =
        "9th lord "
            + lord9.displayName()
            + " in "
            + ZodiacCatalog.signName(p9.signIndex())
            + " (house "
            + p9.house()
            + "); 10th lord "
            + lord10.displayName()
            + " in "
            + ZodiacCatalog.signName(p10.signIndex())
            + " (house "
            + p10.house()
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
            + " Lords are associated. Classical texts link 9–10 connection with vocation and"
            + " purpose themes; this flags a geometric pattern only — not guaranteed status or"
            + " success.",
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
