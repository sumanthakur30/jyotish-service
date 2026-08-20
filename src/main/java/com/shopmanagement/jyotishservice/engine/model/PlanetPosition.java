package com.shopmanagement.jyotishservice.engine.model;

/** One graha (or Ascendant) placement in the sidereal D1 chart. */
public final class PlanetPosition {

  private final Planet planet;
  private final double longitudeDeg;
  private final int signIndex;
  private final String signName;
  private final double degreeInSign;
  private final int house;
  private final int nakshatraIndex;
  private final String nakshatraName;
  private final int pada;
  private final boolean retrograde;
  /** Combust is stubbed false in V1.0 — see Coming Soon notes. */
  private final boolean combust;
  private final Double speedDegPerDay;

  public PlanetPosition(
      Planet planet,
      double longitudeDeg,
      int signIndex,
      String signName,
      double degreeInSign,
      int house,
      int nakshatraIndex,
      String nakshatraName,
      int pada,
      boolean retrograde,
      boolean combust,
      Double speedDegPerDay) {
    this.planet = planet;
    this.longitudeDeg = longitudeDeg;
    this.signIndex = signIndex;
    this.signName = signName;
    this.degreeInSign = degreeInSign;
    this.house = house;
    this.nakshatraIndex = nakshatraIndex;
    this.nakshatraName = nakshatraName;
    this.pada = pada;
    this.retrograde = retrograde;
    this.combust = combust;
    this.speedDegPerDay = speedDegPerDay;
  }

  public Planet planet() {
    return planet;
  }

  public double longitudeDeg() {
    return longitudeDeg;
  }

  public int signIndex() {
    return signIndex;
  }

  public String signName() {
    return signName;
  }

  public double degreeInSign() {
    return degreeInSign;
  }

  public int house() {
    return house;
  }

  public int nakshatraIndex() {
    return nakshatraIndex;
  }

  public String nakshatraName() {
    return nakshatraName;
  }

  public int pada() {
    return pada;
  }

  public boolean retrograde() {
    return retrograde;
  }

  public boolean combust() {
    return combust;
  }

  public Double speedDegPerDay() {
    return speedDegPerDay;
  }
}
