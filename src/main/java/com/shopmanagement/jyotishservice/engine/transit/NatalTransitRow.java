package com.shopmanagement.jyotishservice.engine.transit;

import java.util.Objects;

import com.shopmanagement.jyotishservice.engine.model.Planet;

/** Natal vs transit comparison for one planet. */
public final class NatalTransitRow {

  private final Planet planet;
  private final Double natalLongitudeDeg;
  private final Integer natalSignIndex;
  private final String natalSignName;
  private final Integer natalHouse;
  private final TransitPlanetPosition transit;
  private final boolean signChanged;
  private final boolean houseChanged;

  public NatalTransitRow(
      Planet planet,
      Double natalLongitudeDeg,
      Integer natalSignIndex,
      String natalSignName,
      Integer natalHouse,
      TransitPlanetPosition transit,
      boolean signChanged,
      boolean houseChanged) {
    this.planet = Objects.requireNonNull(planet, "planet");
    this.natalLongitudeDeg = natalLongitudeDeg;
    this.natalSignIndex = natalSignIndex;
    this.natalSignName = natalSignName;
    this.natalHouse = natalHouse;
    this.transit = Objects.requireNonNull(transit, "transit");
    this.signChanged = signChanged;
    this.houseChanged = houseChanged;
  }

  public Planet planet() {
    return planet;
  }

  public Double natalLongitudeDeg() {
    return natalLongitudeDeg;
  }

  public Integer natalSignIndex() {
    return natalSignIndex;
  }

  public String natalSignName() {
    return natalSignName;
  }

  public Integer natalHouse() {
    return natalHouse;
  }

  public TransitPlanetPosition transit() {
    return transit;
  }

  public boolean signChanged() {
    return signChanged;
  }

  public boolean houseChanged() {
    return houseChanged;
  }
}
