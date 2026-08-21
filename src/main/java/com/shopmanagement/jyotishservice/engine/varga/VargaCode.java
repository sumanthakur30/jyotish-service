package com.shopmanagement.jyotishservice.engine.varga;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/**
 * Registered divisional chart codes (Shodashavarga). All codes are implemented via {@link
 * VargaRegistry} mappers.
 */
public enum VargaCode {
  D1("Rashi", 1),
  D2("Hora", 2),
  D3("Drekkana", 3),
  D4("Chaturthamsha", 4),
  D7("Saptamsha", 7),
  D9("Navamsha", 9),
  D10("Dasamsha", 10),
  D12("Dwadasamsha", 12),
  D16("Shodashamsha", 16),
  D20("Vimshamsha", 20),
  D24("Chaturvimshamsha", 24),
  D27("Nakshatramsha", 27),
  D30("Trimshamsha", 30),
  D40("Khavedamsha", 40),
  D45("Akshavedamsha", 45),
  D60("Shashtyamsha", 60);

  private final String displayName;
  private final int divisions;

  VargaCode(String displayName, int divisions) {
    this.displayName = displayName;
    this.divisions = divisions;
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

  /** Prefer {@link VargaRegistry#isImplemented(VargaCode)}; all Shodasha codes are mapped. */
  public boolean implemented() {
    return true;
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
