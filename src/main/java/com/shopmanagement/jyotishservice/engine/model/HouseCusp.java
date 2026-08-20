package com.shopmanagement.jyotishservice.engine.model;

/** Whole-sign house cusp for Vedic D1 V1.0. */
public final class HouseCusp {

  private final int house;
  private final int signIndex;
  private final String signName;
  private final double cuspLongitudeDeg;

  public HouseCusp(int house, int signIndex, String signName, double cuspLongitudeDeg) {
    this.house = house;
    this.signIndex = signIndex;
    this.signName = signName;
    this.cuspLongitudeDeg = cuspLongitudeDeg;
  }

  public int house() {
    return house;
  }

  public int signIndex() {
    return signIndex;
  }

  public String signName() {
    return signName;
  }

  public double cuspLongitudeDeg() {
    return cuspLongitudeDeg;
  }
}
