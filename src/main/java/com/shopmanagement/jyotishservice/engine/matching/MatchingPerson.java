package com.shopmanagement.jyotishservice.engine.matching;

import java.util.List;
import java.util.Objects;

import com.shopmanagement.jyotishservice.engine.astro.ZodiacCatalog;
import com.shopmanagement.jyotishservice.engine.model.D1Chart;
import com.shopmanagement.jyotishservice.engine.model.Planet;
import com.shopmanagement.jyotishservice.engine.model.PlanetPosition;

/**
 * Chart inputs needed for Ashta Koota + Manglik. Built from D1 Moon / Mars / Lagna (whole-sign).
 */
public final class MatchingPerson {

  private final Long profileId;
  private final String displayName;
  private final int moonSignIndex;
  private final String moonSignName;
  private final int moonNakshatraIndex;
  private final String moonNakshatraName;
  private final int marsHouse;
  private final int marsSignIndex;
  private final int lagnaSignIndex;

  public MatchingPerson(
      Long profileId,
      String displayName,
      int moonSignIndex,
      String moonSignName,
      int moonNakshatraIndex,
      String moonNakshatraName,
      int marsHouse,
      int marsSignIndex,
      int lagnaSignIndex) {
    this.profileId = profileId;
    this.displayName = displayName;
    this.moonSignIndex = Math.floorMod(moonSignIndex, 12);
    this.moonSignName = moonSignName;
    this.moonNakshatraIndex = Math.floorMod(moonNakshatraIndex, 27);
    this.moonNakshatraName = moonNakshatraName;
    this.marsHouse = marsHouse;
    this.marsSignIndex = Math.floorMod(marsSignIndex, 12);
    this.lagnaSignIndex = Math.floorMod(lagnaSignIndex, 12);
  }

  public static MatchingPerson fromD1(Long profileId, String displayName, D1Chart chart) {
    Objects.requireNonNull(chart, "chart");
    return fromPositions(profileId, displayName, chart.ascendant().signIndex(), chart.planets());
  }

  public static MatchingPerson fromPositions(
      Long profileId, String displayName, int lagnaSignIndex, List<PlanetPosition> planets) {
    Objects.requireNonNull(planets, "planets");
    PlanetPosition moon =
        planets.stream()
            .filter(p -> p.planet() == Planet.MOON)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Moon position required for matching"));
    PlanetPosition mars =
        planets.stream()
            .filter(p -> p.planet() == Planet.MARS)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Mars position required for matching"));
    return new MatchingPerson(
        profileId,
        displayName,
        moon.signIndex(),
        moon.signName(),
        moon.nakshatraIndex(),
        moon.nakshatraName(),
        mars.house(),
        mars.signIndex(),
        lagnaSignIndex);
  }

  /** Test helper: build from Moon nakshatra + sign and Mars house only. */
  public static MatchingPerson forTest(
      String name, int moonSignIndex, int moonNakshatraIndex, int marsHouse) {
    return new MatchingPerson(
        null,
        name,
        moonSignIndex,
        ZodiacCatalog.signName(moonSignIndex),
        moonNakshatraIndex,
        ZodiacCatalog.nakshatraName(moonNakshatraIndex),
        marsHouse,
        moonSignIndex,
        0);
  }

  public Long profileId() {
    return profileId;
  }

  public String displayName() {
    return displayName;
  }

  public int moonSignIndex() {
    return moonSignIndex;
  }

  public String moonSignName() {
    return moonSignName;
  }

  public int moonNakshatraIndex() {
    return moonNakshatraIndex;
  }

  public String moonNakshatraName() {
    return moonNakshatraName;
  }

  public int marsHouse() {
    return marsHouse;
  }

  public int marsSignIndex() {
    return marsSignIndex;
  }

  public int lagnaSignIndex() {
    return lagnaSignIndex;
  }
}
