package com.shopmanagement.jyotishservice.engine.yoga;

import java.util.List;
import java.util.Objects;

/**
 * Result of evaluating one yoga rule against a chart. When {@code present} is false, planets/houses
 * may still list the factors checked; strength is only set when the detector defines a grading rule.
 */
public final class YogaHit {

  private final YogaCode code;
  private final boolean present;
  private final YogaStrength strength; // nullable
  private final List<String> planetCodes;
  private final List<Integer> houses;
  private final String explanation;
  private final String ruleId;

  public YogaHit(
      YogaCode code,
      boolean present,
      YogaStrength strength,
      List<String> planetCodes,
      List<Integer> houses,
      String explanation,
      String ruleId) {
    this.code = Objects.requireNonNull(code, "code");
    this.present = present;
    this.strength = strength;
    this.planetCodes = planetCodes == null ? List.of() : List.copyOf(planetCodes);
    this.houses = houses == null ? List.of() : List.copyOf(houses);
    this.explanation = Objects.requireNonNull(explanation, "explanation");
    this.ruleId = Objects.requireNonNull(ruleId, "ruleId");
  }

  public static YogaHit absent(
      YogaCode code, List<String> planetCodes, List<Integer> houses, String explanation, String ruleId) {
    return new YogaHit(code, false, null, planetCodes, houses, explanation, ruleId);
  }

  public static YogaHit present(
      YogaCode code,
      YogaStrength strength,
      List<String> planetCodes,
      List<Integer> houses,
      String explanation,
      String ruleId) {
    return new YogaHit(code, true, strength, planetCodes, houses, explanation, ruleId);
  }

  public YogaCode code() {
    return code;
  }

  public boolean present() {
    return present;
  }

  public YogaStrength strength() {
    return strength;
  }

  public List<String> planetCodes() {
    return planetCodes;
  }

  public List<Integer> houses() {
    return houses;
  }

  public String explanation() {
    return explanation;
  }

  public String ruleId() {
    return ruleId;
  }
}
