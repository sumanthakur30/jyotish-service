package com.shopmanagement.jyotishservice.engine.transit;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.shopmanagement.jyotishservice.engine.model.Planet;
import com.shopmanagement.jyotishservice.engine.model.PlanetPosition;

/**
 * Builds natal-vs-transit comparison rows. Pure helpers — no ephemeris calls.
 */
public final class TransitComparer {

  private TransitComparer() {}

  /**
   * Pair each transit planet with its natal counterpart (if present). Sign/house “changed” flags
   * compare natal placement to transit placement.
   */
  public static List<NatalTransitRow> compare(
      List<PlanetPosition> natalPlanets, List<TransitPlanetPosition> transitPlanets) {
    Objects.requireNonNull(natalPlanets, "natalPlanets");
    Objects.requireNonNull(transitPlanets, "transitPlanets");

    Map<Planet, PlanetPosition> natalByPlanet = new EnumMap<>(Planet.class);
    for (PlanetPosition p : natalPlanets) {
      if (p.planet() != Planet.ASCENDANT) {
        natalByPlanet.put(p.planet(), p);
      }
    }

    List<NatalTransitRow> rows = new ArrayList<>(transitPlanets.size());
    for (TransitPlanetPosition t : transitPlanets) {
      PlanetPosition n = natalByPlanet.get(t.planet());
      if (n == null) {
        rows.add(new NatalTransitRow(t.planet(), null, null, null, null, t, false, false));
        continue;
      }
      boolean signChanged = n.signIndex() != t.signIndex();
      boolean houseChanged = n.house() != t.house();
      rows.add(
          new NatalTransitRow(
              t.planet(),
              n.longitudeDeg(),
              n.signIndex(),
              n.signName(),
              n.house(),
              t,
              signChanged,
              houseChanged));
    }
    return rows;
  }

  /** True when transit and natal signs differ (null natal → false). */
  public static boolean signChanged(Integer natalSignIndex, int transitSignIndex) {
    return natalSignIndex != null && natalSignIndex != transitSignIndex;
  }

  /** True when transit and natal whole-sign houses differ (null natal → false). */
  public static boolean houseChanged(Integer natalHouse, int transitHouse) {
    return natalHouse != null && natalHouse != transitHouse;
  }
}
