package com.shopmanagement.jyotishservice.engine.matching;

/** Ashta Koota (eightfold) compatibility factors. Max points sum to 36. */
public enum KootaCode {
  VARNA("VARNA", "Varna", 1),
  VASHYA("VASHYA", "Vashya", 2),
  TARA("TARA", "Tara", 3),
  YONI("YONI", "Yoni", 4),
  GRAHA_MAITRI("GRAHA_MAITRI", "Graha Maitri", 5),
  GANA("GANA", "Gana", 6),
  BHAKOOT("BHAKOOT", "Bhakoot", 7),
  NADI("NADI", "Nadi", 8);

  private final String code;
  private final String displayName;
  private final int maxPoints;

  KootaCode(String code, String displayName, int maxPoints) {
    this.code = code;
    this.displayName = displayName;
    this.maxPoints = maxPoints;
  }

  public String code() {
    return code;
  }

  public String displayName() {
    return displayName;
  }

  public int maxPoints() {
    return maxPoints;
  }

  public static int totalMax() {
    int sum = 0;
    for (KootaCode k : values()) {
      sum += k.maxPoints;
    }
    return sum;
  }

  public static KootaCode fromCode(String raw) {
    if (raw == null || raw.isBlank()) {
      throw new IllegalArgumentException("Koota code required");
    }
    String n = raw.trim().toUpperCase().replace('-', '_').replace(' ', '_');
    for (KootaCode k : values()) {
      if (k.code.equals(n) || k.name().equals(n)) {
        return k;
      }
    }
    throw new IllegalArgumentException("Unknown koota: " + raw);
  }
}
