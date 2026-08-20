package com.shopmanagement.jyotishservice.engine.varga;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/**
 * Registered divisional chart codes. Implemented codes have a {@link VargaMapper}; others are
 * catalogued as Coming Soon so the UI/API can list them without rewriting per chart.
 */
public enum VargaCode {
  D1("Rashi", 1, true),
  D2("Hora", 2, true),
  D3("Drekkana", 3, true),
  D4("Chaturthamsha", 4, false),
  D7("Saptamsha", 7, false),
  D9("Navamsha", 9, true),
  D10("Dasamsha", 10, true),
  D12("Dwadasamsha", 12, false),
  D16("Shodashamsha", 16, false),
  D20("Vimshamsha", 20, false),
  D24("Chaturvimshamsha", 24, false),
  D27("Nakshatramsha", 27, false),
  D30("Trimshamsha", 30, false),
  D40("Khavedamsha", 40, false),
  D45("Akshavedamsha", 45, false),
  D60("Shashtyamsha", 60, false);

  private final String displayName;
  private final int divisions;
  private final boolean implemented;

  VargaCode(String displayName, int divisions, boolean implemented) {
    this.displayName = displayName;
    this.divisions = divisions;
    this.implemented = implemented;
  }

  public String code() {
    return name();
  }

  public String displayName() {
    return displayName;
  }

  public int divisions() {
    return divisions;
  }

  public boolean implemented() {
    return implemented;
  }

  public static Optional<VargaCode> tryParse(String raw) {
    if (raw == null || raw.isBlank()) {
      return Optional.empty();
    }
    String key = raw.trim().toUpperCase(Locale.ROOT);
    return Arrays.stream(values()).filter(v -> v.name().equals(key)).findFirst();
  }

  public static VargaCode parse(String raw) {
    return tryParse(raw)
        .orElseThrow(() -> new IllegalArgumentException("Unknown varga code: " + raw));
  }
}
