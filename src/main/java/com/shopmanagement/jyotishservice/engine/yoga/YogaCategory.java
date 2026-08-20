package com.shopmanagement.jyotishservice.engine.yoga;

import java.util.Locale;

/** High-level yoga catalog categories for UI grouping / Coming Soon. */
public enum YogaCategory {
  CHANDRA("CHANDRA", "Chandra Yogas"),
  RAJA("RAJA", "Raja Yogas"),
  DHANA("DHANA", "Dhana Yogas"),
  MAHAPURUSHA("MAHAPURUSHA", "Panch Mahapurusha"),
  OTHER("OTHER", "Other Classical Yogas");

  private final String code;
  private final String displayName;

  YogaCategory(String code, String displayName) {
    this.code = code;
    this.displayName = displayName;
  }

  public String code() {
    return code;
  }

  public String displayName() {
    return displayName;
  }

  public static YogaCategory parse(String raw) {
    if (raw == null || raw.isBlank()) {
      throw new IllegalArgumentException("Yoga category is required");
    }
    String n = raw.trim().toUpperCase(Locale.ROOT);
    for (YogaCategory c : values()) {
      if (c.code.equals(n) || c.name().equals(n)) {
        return c;
      }
    }
    throw new IllegalArgumentException("Unknown yoga category: " + raw);
  }
}
