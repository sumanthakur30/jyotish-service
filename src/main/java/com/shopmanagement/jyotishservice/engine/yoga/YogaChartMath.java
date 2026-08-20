package com.shopmanagement.jyotishservice.engine.yoga;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.shopmanagement.jyotishservice.engine.model.Planet;
import com.shopmanagement.jyotishservice.engine.model.PlanetPosition;

/**
 * Shared helpers for yoga detectors: sign lords, exaltation, kendras, association. Whole-sign
 * house/sign model only (matches D1 engine).
 */
public final class YogaChartMath {

  /** Sign index 0–11 → classical lord (nodes have no lordship here). */
  private static final Planet[] SIGN_LORD = {
    Planet.MARS, // Aries
    Planet.VENUS, // Taurus
    Planet.MERCURY, // Gemini
    Planet.MOON, // Cancer
    Planet.SUN, // Leo
    Planet.MERCURY, // Virgo
    Planet.VENUS, // Libra
    Planet.MARS, // Scorpio
    Planet.JUPITER, // Sagittarius
    Planet.SATURN, // Capricorn
    Planet.SATURN, // Aquarius
    Planet.JUPITER // Pisces
  };

  /** Exaltation sign index; -1 if not used for that planet in Mahapurusha rules. */
  private static final Map<Planet, Integer> EXALTATION = new EnumMap<>(Planet.class);

  static {
    EXALTATION.put(Planet.SUN, 0); // Aries
    EXALTATION.put(Planet.MOON, 1); // Taurus
    EXALTATION.put(Planet.MARS, 9); // Capricorn
    EXALTATION.put(Planet.MERCURY, 5); // Virgo
    EXALTATION.put(Planet.JUPITER, 3); // Cancer
    EXALTATION.put(Planet.VENUS, 11); // Pisces
    EXALTATION.put(Planet.SATURN, 6); // Libra
  }

  private YogaChartMath() {}

  public static Planet lordOfSign(int signIndex) {
    return SIGN_LORD[Math.floorMod(signIndex, 12)];
  }

  /** Lord of whole-sign house {@code house} (1–12) given Lagna sign index. */
  public static Planet lordOfHouse(int lagnaSignIndex, int house) {
    if (house < 1 || house > 12) {
      throw new IllegalArgumentException("House must be 1–12");
    }
    int sign = Math.floorMod(lagnaSignIndex + house - 1, 12);
    return lordOfSign(sign);
  }

  public static boolean isOwnSign(Planet planet, int signIndex) {
    return lordOfSign(signIndex) == planet;
  }

  public static boolean isExalted(Planet planet, int signIndex) {
    Integer ex = EXALTATION.get(planet);
    return ex != null && ex == Math.floorMod(signIndex, 12);
  }

  /** Whole-sign house 1–12 from Lagna. */
  public static int houseFromLagna(int lagnaSignIndex, int planetSignIndex) {
    return Math.floorMod(planetSignIndex - lagnaSignIndex, 12) + 1;
  }

  public static boolean isKendraHouse(int house) {
    return house == 1 || house == 4 || house == 7 || house == 10;
  }

  /**
   * Mutual kendra: sign distance 0, 3, 6, or 9 (includes conjunction as 1st from self).
   */
  public static boolean mutualKendra(int signA, int signB) {
    int d = Math.floorMod(signA - signB, 12);
    return d == 0 || d == 3 || d == 6 || d == 9;
  }

  /** Same sign (conjunction under whole-sign). */
  public static boolean conjunct(int signA, int signB) {
    return Math.floorMod(signA, 12) == Math.floorMod(signB, 12);
  }

  /**
   * Association used by Raja / Dhana detectors: conjunction or mutual kendra. Documented in each
   * detector; not full Parashara aspect tables.
   */
  public static boolean associated(int signA, int signB) {
    return conjunct(signA, signB) || mutualKendra(signA, signB);
  }

  public static Optional<PlanetPosition> find(List<PlanetPosition> planets, Planet planet) {
    return planets.stream().filter(p -> p.planet() == planet).findFirst();
  }

  public static Map<Planet, PlanetPosition> indexByPlanet(List<PlanetPosition> planets) {
    Map<Planet, PlanetPosition> m = new EnumMap<>(Planet.class);
    for (PlanetPosition p : planets) {
      if (p.planet() != Planet.ASCENDANT) {
        m.put(p.planet(), p);
      }
    }
    return m;
  }
}
