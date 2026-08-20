package com.shopmanagement.jyotishservice.engine.model;

/** Configurable sidereal ayanamsa. Default Lahiri (Chitrapaksha). */
public enum AyanamsaMode {
  LAHIRI("Lahiri / Chitrapaksha"),
  RAMAN("B.V. Raman"),
  KP("Krishnamurti");

  private final String label;

  AyanamsaMode(String label) {
    this.label = label;
  }

  public String label() {
    return label;
  }

  public static AyanamsaMode fromCode(String code) {
    if (code == null || code.isBlank()) {
      return LAHIRI;
    }
    return switch (code.trim().toUpperCase()) {
      case "RAMAN" -> RAMAN;
      case "KP", "KRISHNAMURTI" -> KP;
      default -> LAHIRI;
    };
  }
}
