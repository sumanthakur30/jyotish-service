package com.shopmanagement.jyotishservice.engine.model;

import java.util.List;

/** Sidereal Rashi (D1) chart result from CalculationEngine V1.1. */
public final class D1Chart {

  private final String engineVersion;
  private final AyanamsaMode ayanamsaMode;
  private final double ayanamsaDeg;
  private final double julianDayUt;
  private final List<PlanetPosition> planets;
  private final List<HouseCusp> houses;
  private final PlanetPosition ascendant;
  private final String houseSystem;
  private final String notes;

  public D1Chart(
      String engineVersion,
      AyanamsaMode ayanamsaMode,
      double ayanamsaDeg,
      double julianDayUt,
      List<PlanetPosition> planets,
      List<HouseCusp> houses,
      PlanetPosition ascendant,
      String houseSystem,
      String notes) {
    this.engineVersion = engineVersion;
    this.ayanamsaMode = ayanamsaMode;
    this.ayanamsaDeg = ayanamsaDeg;
    this.julianDayUt = julianDayUt;
    this.planets = List.copyOf(planets);
    this.houses = List.copyOf(houses);
    this.ascendant = ascendant;
    this.houseSystem = houseSystem;
    this.notes = notes;
  }

  public String engineVersion() {
    return engineVersion;
  }

  public AyanamsaMode ayanamsaMode() {
    return ayanamsaMode;
  }

  public double ayanamsaDeg() {
    return ayanamsaDeg;
  }

  public double julianDayUt() {
    return julianDayUt;
  }

  public List<PlanetPosition> planets() {
    return planets;
  }

  public List<HouseCusp> houses() {
    return houses;
  }

  public PlanetPosition ascendant() {
    return ascendant;
  }

  public String houseSystem() {
    return houseSystem;
  }

  public String notes() {
    return notes;
  }
}
