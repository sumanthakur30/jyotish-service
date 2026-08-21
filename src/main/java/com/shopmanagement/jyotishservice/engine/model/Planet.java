package com.shopmanagement.jyotishservice.engine.model;

/** Graha set for D1 (Rashi). Classical nine + optional outer planets (Spashta only). */
public enum Planet {
  SUN("Sun"),
  MOON("Moon"),
  MARS("Mars"),
  MERCURY("Mercury"),
  JUPITER("Jupiter"),
  VENUS("Venus"),
  SATURN("Saturn"),
  RAHU("Rahu"),
  KETU("Ketu"),
  /** Western outer — Spashta only; never used in classical yogas. */
  URANUS("Uranus"),
  NEPTUNE("Neptune"),
  PLUTO("Pluto"),
  ASCENDANT("Ascendant");

  private final String displayName;

  Planet(String displayName) {
    this.displayName = displayName;
  }

  public String displayName() {
    return displayName;
  }

  public boolean isNode() {
    return this == RAHU || this == KETU;
  }

  /** Uranus / Neptune / Pluto — not part of classical nine-graha rules. */
  public boolean isOuter() {
    return this == URANUS || this == NEPTUNE || this == PLUTO;
  }

  public boolean isClassicalGraha() {
    return !isOuter() && this != ASCENDANT;
  }
}
