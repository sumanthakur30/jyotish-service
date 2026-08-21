package com.shopmanagement.jyotishservice.engine.life;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/** Configurable life-analysis categories (Shodasha-style case topics). */
public enum LifeCategory {
  CAREER("Career", "करियर", List.of(10, 6, 2, 11)),
  JOB("Job", "नौकरी", List.of(10, 6)),
  BUSINESS("Business", "व्यवसाय", List.of(2, 7, 10, 11)),
  FINANCE("Finance", "वित्त", List.of(2, 11)),
  MARRIAGE("Marriage", "विवाह", List.of(7)),
  FAMILY("Family", "परिवार", List.of(4, 2)),
  CHILDREN("Children", "संतान", List.of(5)),
  EDUCATION("Education", "शिक्षा", List.of(4, 5, 9)),
  PROPERTY("Property", "संपत्ति", List.of(4)),
  FOREIGN("Foreign", "विदेश", List.of(12, 9, 7)),
  SPIRITUALITY("Spirituality", "आध्यात्मिकता", List.of(5, 9, 12)),
  HEALTH("Health", "स्वास्थ्य", List.of(1, 6, 8)),
  GENERAL("General", "सामान्य", List.of());

  private final String labelEn;
  private final String labelHi;
  private final List<Integer> indicatorHouses;

  LifeCategory(String labelEn, String labelHi, List<Integer> indicatorHouses) {
    this.labelEn = labelEn;
    this.labelHi = labelHi;
    this.indicatorHouses = List.copyOf(indicatorHouses);
  }

  public String code() {
    return name();
  }

  public String labelEn() {
    return labelEn;
  }

  public String labelHi() {
    return labelHi;
  }

  public List<Integer> indicatorHouses() {
    return indicatorHouses;
  }

  public static Optional<LifeCategory> tryParse(String raw) {
    if (raw == null || raw.isBlank()) {
      return Optional.empty();
    }
    return Arrays.stream(values())
        .filter(c -> c.name().equals(raw.trim().toUpperCase(Locale.ROOT)))
        .findFirst();
  }

  public static LifeCategory parse(String raw) {
    return tryParse(raw)
        .orElseThrow(() -> new IllegalArgumentException("Unknown life category: " + raw));
  }
}
