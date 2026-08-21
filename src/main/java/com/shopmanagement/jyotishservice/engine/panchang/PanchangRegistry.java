package com.shopmanagement.jyotishservice.engine.panchang;

import java.util.List;

/** Catalog of Panchang-related features (implemented + Coming Soon stubs). */
public final class PanchangRegistry {

  private PanchangRegistry() {}

  public static List<PanchangResult.PanchangFeature> catalog() {
    return List.of(
        feature("PANCHANG_CORE", "Tithi · Vara · Nakshatra · Yoga · Karana", true),
        feature("SUNRISE_SUNSET", "Sunrise / Sunset", true),
        feature("MOONRISE_MOONSET", "Moonrise / Moonset", false),
        feature("CHOGHADIYA", "Choghadiya", true),
        feature("RAHU_KAAL", "Rahu Kaal", true),
        feature("YAMAGANDA", "Yamaganda", true),
        feature("GULIKA", "Gulika Kaal", true),
        feature("HORA", "Planetary Hora", true),
        feature("ABHIJIT", "Abhijit Muhurat", true));
  }

  public static List<PanchangResult.PanchangFeature> comingSoon() {
    return catalog().stream().filter(f -> !f.implemented()).toList();
  }

  private static PanchangResult.PanchangFeature feature(
      String code, String displayName, boolean implemented) {
    return new PanchangResult.PanchangFeature(
        code, displayName, implemented, implemented ? "READY" : "COMING_SOON");
  }
}
