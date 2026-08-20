package com.shopmanagement.jyotishservice.engine.dasha;

import java.util.Locale;

/** Catalog of dasha systems. Only registered calculators are implemented. */
public enum DashaSystemCode {
  VIMSHOTTARI("VIMSHOTTARI", "Vimshottari"),
  YOGINI("YOGINI", "Yogini"),
  CHARA("CHARA", "Chara"),
  ASHTOTTARI("ASHTOTTARI", "Ashtottari");

  private final String code;
  private final String displayName;

  DashaSystemCode(String code, String displayName) {
    this.code = code;
    this.displayName = displayName;
  }

  public String code() {
    return code;
  }

  public String displayName() {
    return displayName;
  }

  public static DashaSystemCode parse(String raw) {
    if (raw == null || raw.isBlank()) {
      throw new IllegalArgumentException("Dasha system code is required");
    }
    String n = raw.trim().toUpperCase(Locale.ROOT);
    for (DashaSystemCode c : values()) {
      if (c.code.equals(n) || c.name().equals(n)) {
        return c;
      }
    }
    throw new IllegalArgumentException("Unknown dasha system: " + raw);
  }
}
