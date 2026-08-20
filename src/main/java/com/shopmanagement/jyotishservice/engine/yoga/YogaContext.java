package com.shopmanagement.jyotishservice.engine.yoga;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.shopmanagement.jyotishservice.engine.model.Planet;
import com.shopmanagement.jyotishservice.engine.model.PlanetPosition;
import com.shopmanagement.jyotishservice.engine.model.VargaChart;

/**
 * Input snapshot for yoga detectors. D1 is required; D9 is optional for future / optional
 * reinforcement (Phase 5 detectors use D1 primarily).
 */
public final class YogaContext {

  private final int lagnaSignIndex;
  private final List<PlanetPosition> d1Planets;
  private final Map<Planet, PlanetPosition> d1ByPlanet;
  private final VargaChart d9; // nullable

  public YogaContext(int lagnaSignIndex, List<PlanetPosition> d1Planets, VargaChart d9) {
    this.lagnaSignIndex = lagnaSignIndex;
    this.d1Planets = List.copyOf(Objects.requireNonNull(d1Planets, "d1Planets"));
    this.d1ByPlanet = YogaChartMath.indexByPlanet(this.d1Planets);
    this.d9 = d9;
  }

  public int lagnaSignIndex() {
    return lagnaSignIndex;
  }

  public List<PlanetPosition> d1Planets() {
    return d1Planets;
  }

  public Optional<PlanetPosition> d1(Planet planet) {
    return Optional.ofNullable(d1ByPlanet.get(planet));
  }

  public Optional<VargaChart> d9() {
    return Optional.ofNullable(d9);
  }

  public Planet lordOfHouse(int house) {
    return YogaChartMath.lordOfHouse(lagnaSignIndex, house);
  }
}
