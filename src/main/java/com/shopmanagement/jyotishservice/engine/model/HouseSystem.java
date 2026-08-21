package com.shopmanagement.jyotishservice.engine.model;

import java.util.Locale;
import java.util.Optional;

/**
 * House systems supported by the engine.
 *
 * <p><strong>Default:</strong> {@link #WHOLE_SIGN} — Lagna sign = house 1; used for D1 persistence,
 * yogas, manglik, Ashtakavarga, etc.
 *
 * <p><strong>Opt-in Chalit:</strong> {@link #SRIPATI} — Nirayana Bhava Chalit via classical Sripati
 * (unequal bhavas from ASC/MC quadrants). Exposed as chart {@code CHALIT}; never silently replaces
 * whole-sign D1.
 */
public enum HouseSystem {
  WHOLE_SIGN("Whole sign"),
  /** Sripati / Porphyry Bhava Chalit (ASC = cusp of bhava 1). */
  SRIPATI("Sripati Bhava Chalit");

  private final String displayName;

  HouseSystem(String displayName) {
    this.displayName = displayName;
  }

  public String code() {
    return name();
  }

  public String displayName() {
    return displayName;
  }

  /** API aliases: {@code BHAVA_CHALIT}, {@code CHALIT}, {@code SRIPATI}. */
  public static Optional<HouseSystem> tryParse(String raw) {
    if (raw == null || raw.isBlank()) {
      return Optional.empty();
    }
    String key = raw.trim().toUpperCase(Locale.ROOT).replace('-', '_');
    return switch (key) {
      case "WHOLE_SIGN", "WHOLE", "RASHI" -> Optional.of(WHOLE_SIGN);
      case "SRIPATI", "BHAVA_CHALIT", "CHALIT", "D1_CHALIT", "PORPHYRY" -> Optional.of(SRIPATI);
      default -> Optional.empty();
    };
  }

  public static HouseSystem parse(String raw) {
    return tryParse(raw)
        .orElseThrow(() -> new IllegalArgumentException("Unknown house system: " + raw));
  }
}
