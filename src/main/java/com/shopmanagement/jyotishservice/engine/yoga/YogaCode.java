package com.shopmanagement.jyotishservice.engine.yoga;

import java.util.Locale;

/**
 * Catalog of named yogas. Only codes with a registered {@link YogaDetector} are evaluated; others
 * appear as Coming Soon in the API catalog (no fake presence).
 */
public enum YogaCode {
  GAJAKESARI("GAJAKESARI", "Gajakesari Yoga", YogaCategory.CHANDRA),
  DHARMA_KARMADHIPATI("DHARMA_KARMADHIPATI", "Dharma-Karmadhipati Raja Yoga", YogaCategory.RAJA),
  DHANA_2_11("DHANA_2_11", "Dhana Yoga (2nd–11th lords)", YogaCategory.DHANA),
  RUCHAKA("RUCHAKA", "Ruchaka Yoga", YogaCategory.MAHAPURUSHA),
  BHADRA("BHADRA", "Bhadra Yoga", YogaCategory.MAHAPURUSHA),
  // Catalog stubs — Coming Soon (no detectors)
  HAMSA("HAMSA", "Hamsa Yoga", YogaCategory.MAHAPURUSHA),
  MALAVYA("MALAVYA", "Malavya Yoga", YogaCategory.MAHAPURUSHA),
  SASA("SASA", "Sasa Yoga", YogaCategory.MAHAPURUSHA),
  NEECHA_BHANGA("NEECHA_BHANGA", "Neecha Bhanga Raja Yoga", YogaCategory.RAJA),
  VIPARITA_RAJA("VIPARITA_RAJA", "Viparita Raja Yoga", YogaCategory.RAJA),
  KEMADRUMA("KEMADRUMA", "Kemadruma Yoga", YogaCategory.CHANDRA),
  BUDHADITYA("BUDHADITYA", "Budhaditya Yoga", YogaCategory.OTHER);

  private final String code;
  private final String displayName;
  private final YogaCategory category;

  YogaCode(String code, String displayName, YogaCategory category) {
    this.code = code;
    this.displayName = displayName;
    this.category = category;
  }

  public String code() {
    return code;
  }

  public String displayName() {
    return displayName;
  }

  public YogaCategory category() {
    return category;
  }

  public static YogaCode parse(String raw) {
    if (raw == null || raw.isBlank()) {
      throw new IllegalArgumentException("Yoga code is required");
    }
    String n = raw.trim().toUpperCase(Locale.ROOT);
    for (YogaCode c : values()) {
      if (c.code.equals(n) || c.name().equals(n)) {
        return c;
      }
    }
    throw new IllegalArgumentException("Unknown yoga: " + raw);
  }
}
