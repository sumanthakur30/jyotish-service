package com.shopmanagement.jyotishservice.engine.model;

import java.util.List;

import com.shopmanagement.jyotishservice.engine.varga.VargaCode;

/** One divisional (varga) chart computed from D1 longitudes. */
public final class VargaChart {

  private final VargaCode varga;
  private final String engineVersion;
  private final List<PlanetPosition> planets;
  private final List<HouseCusp> houses;
  private final PlanetPosition ascendant;
  private final String houseSystem;
  private final String notes;

  public VargaChart(
      VargaCode varga,
      String engineVersion,
      List<PlanetPosition> planets,
      List<HouseCusp> houses,
      PlanetPosition ascendant,
      String houseSystem,
      String notes) {
    this.varga = varga;
    this.engineVersion = engineVersion;
    this.planets = List.copyOf(planets);
    this.houses = List.copyOf(houses);
    this.ascendant = ascendant;
    this.houseSystem = houseSystem;
    this.notes = notes;
  }

  public VargaCode varga() {
    return varga;
  }

  public String engineVersion() {
    return engineVersion;
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
