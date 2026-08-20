package com.shopmanagement.jyotishservice.engine.model;

/** Graha set for D1 (Rashi) V1.0. */
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
}
