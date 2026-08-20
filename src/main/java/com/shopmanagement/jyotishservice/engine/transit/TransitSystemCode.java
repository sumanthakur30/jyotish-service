package com.shopmanagement.jyotishservice.engine.transit;

import java.util.Locale;

/** Catalog of transit / Gochar systems. */
public enum TransitSystemCode {
  GOCHAR("GOCHAR", "Gochar (natal overlay)"),
  SADE_SATI("SADE_SATI", "Sade Sati analysis");

  private final String code;
  private final String displayName;

  TransitSystemCode(String code, String displayName) {
    this.code = code;
    this.displayName = displayName;
  }

  public String code() {
    return code;
  }

  public String displayName() {
    return displayName;
  }

  public static TransitSystemCode parse(String raw) {
    if (raw == null || raw.isBlank()) {
      return GOCHAR;
    }
    String key = raw.trim().toUpperCase(Locale.ROOT).replace('-', '_');
    for (TransitSystemCode c : values()) {
      if (c.code.equals(key) || c.name().equals(key)) {
        return c;
      }
    }
    throw new IllegalArgumentException("Unknown transit system: " + raw);
  }
}
