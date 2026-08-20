package com.shopmanagement.jyotishservice.engine.matching;

/** Catalog of matching subsystems — register calculators in {@link MatchingRegistry}. */
public enum MatchingSystemCode {
  ASHTA_KOOTA("ASHTA_KOOTA", "Ashta Koota (Guna Milan)", true),
  MANGLIK("MANGLIK", "Manglik Dosha", true),
  /** Placeholder for future systems (e.g. Dasha Sandhi) — Coming Soon. */
  DASHA_SANDHI("DASHA_SANDHI", "Dasha Sandhi", false);

  private final String code;
  private final String displayName;
  private final boolean defaultImplemented;

  MatchingSystemCode(String code, String displayName, boolean defaultImplemented) {
    this.code = code;
    this.displayName = displayName;
    this.defaultImplemented = defaultImplemented;
  }

  public String code() {
    return code;
  }

  public String displayName() {
    return displayName;
  }

  /** Hint only — {@link MatchingRegistry#isImplemented} is authoritative. */
  public boolean defaultImplemented() {
    return defaultImplemented;
  }

  public static MatchingSystemCode fromCode(String raw) {
    if (raw == null || raw.isBlank()) {
      throw new IllegalArgumentException("Matching system code required");
    }
    String n = raw.trim().toUpperCase().replace('-', '_').replace(' ', '_');
    for (MatchingSystemCode c : values()) {
      if (c.code.equals(n) || c.name().equals(n)) {
        return c;
      }
    }
    throw new IllegalArgumentException("Unknown matching system: " + raw);
  }
}
