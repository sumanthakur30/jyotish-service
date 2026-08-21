package com.shopmanagement.jyotishservice.engine.shadbala;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import com.shopmanagement.jyotishservice.engine.model.D1Chart;
import com.shopmanagement.jyotishservice.engine.model.Planet;
import com.shopmanagement.jyotishservice.engine.model.PlanetPosition;

/**
 * Honest <strong>partial</strong> Shadbala. Only Naisargika, Dig, and a Sthana subset are scored.
 * Other classical components are listed as {@link ComponentStatus#COMING_SOON} with null scores —
 * this engine never invents a fake complete Shadbala total.
 *
 * <p>Sthana subset formula (documented):
 *
 * <ul>
 *   <li>Uchcha: exaltation sign → 60 virupas; debilitation → 0; otherwise → 30
 *   <li>House: kendra (1,4,7,10) → 60; trikona (5,9) → 45; dusthana (6,8,12) → 15; else → 30
 *   <li>Sthana partial = average of the two (equal weight)
 * </ul>
 */
public final class ShadbalaCalculator {

  private static final Planet[] GRAHAS = {
    Planet.SUN, Planet.MOON, Planet.MARS, Planet.MERCURY, Planet.JUPITER, Planet.VENUS, Planet.SATURN
  };

  /** Classical Naisargika Bala (virupas). */
  private static final Map<Planet, Double> NAISARGIKA =
      Map.of(
          Planet.SUN, 60.0,
          Planet.MOON, 51.43,
          Planet.VENUS, 42.85,
          Planet.JUPITER, 34.28,
          Planet.MERCURY, 25.70,
          Planet.MARS, 17.14,
          Planet.SATURN, 8.57);

  /** Dig Bala strong house (whole-sign). */
  private static final Map<Planet, Integer> DIG_HOUSE =
      Map.of(
          Planet.SUN, 10,
          Planet.MOON, 4,
          Planet.MARS, 10,
          Planet.MERCURY, 1,
          Planet.JUPITER, 1,
          Planet.VENUS, 4,
          Planet.SATURN, 7);

  private static final Map<Planet, Integer> EXALTATION =
      Map.of(
          Planet.SUN, 0,
          Planet.MOON, 1,
          Planet.MARS, 9,
          Planet.MERCURY, 5,
          Planet.JUPITER, 3,
          Planet.VENUS, 11,
          Planet.SATURN, 6);

  private ShadbalaCalculator() {}

  public static ShadbalaReport compute(D1Chart d1) {
    Objects.requireNonNull(d1, "d1");
    EnumMap<Planet, PlanetPosition> byPlanet = new EnumMap<>(Planet.class);
    for (PlanetPosition p : d1.planets()) {
      byPlanet.put(p.planet(), p);
    }

    List<PlanetShadbala> rows = new ArrayList<>();
    for (Planet planet : GRAHAS) {
      PlanetPosition pos = byPlanet.get(planet);
      if (pos == null) {
        continue;
      }
      rows.add(scorePlanet(planet, pos));
    }

    return new ShadbalaReport(
        List.copyOf(rows),
        "PARTIAL — only Naisargika, Dig, and Sthana (exaltation/debilitation + kendra/trikona/"
            + "dusthana) are implemented. Remaining Shadbala components are Coming Soon; do not"
            + " treat partialTotalVirupas as a full classical Shadbala.");
  }

  private static PlanetShadbala scorePlanet(Planet planet, PlanetPosition pos) {
    List<ShadbalaComponent> components = new ArrayList<>();

    double naisargika = NAISARGIKA.getOrDefault(planet, 0.0);
    components.add(
        new ShadbalaComponent(
            "NAISARGIKA", "Naisargika Bala", ComponentStatus.READY, naisargika, "Fixed classical"));

    Integer digHouse = DIG_HOUSE.get(planet);
    double dig = (digHouse != null && pos.house() == digHouse) ? 60.0 : 0.0;
    components.add(
        new ShadbalaComponent(
            "DIG",
            "Dig Bala",
            ComponentStatus.READY,
            dig,
            "Whole-sign: 60 if in directional house "
                + digHouse
                + ", else 0 (longitude interpolation Coming Soon)"));

    double uchcha = uchchaBala(planet, pos.signIndex());
    double house = houseStrength(pos.house());
    double sthana = (uchcha + house) / 2.0;
    components.add(
        new ShadbalaComponent(
            "STHANA_SUBSET",
            "Sthana Bala (subset)",
            ComponentStatus.READY,
            sthana,
            String.format(
                Locale.ROOT,
                "Uchcha=%.0f house=%.0f; avg only. Saptavargaja / other Sthana Coming Soon",
                uchcha,
                house)));

    components.add(comingSoon("KALA", "Kala Bala"));
    components.add(comingSoon("CHESTA", "Chesta Bala"));
    components.add(comingSoon("DRIK", "Drik Bala"));
    components.add(comingSoon("STHANA_FULL", "Remaining Sthana (Saptavargaja etc.)"));

    List<String> ready = new ArrayList<>();
    List<String> soon = new ArrayList<>();
    double partial = 0;
    for (ShadbalaComponent c : components) {
      if (c.status() == ComponentStatus.READY) {
        ready.add(c.code());
        if (c.virupas() != null) {
          partial += c.virupas();
        }
      } else {
        soon.add(c.code());
      }
    }

    return new PlanetShadbala(
        planet,
        planet.displayName(),
        pos.signIndex(),
        pos.house(),
        List.copyOf(components),
        List.copyOf(ready),
        List.copyOf(soon),
        partial,
        "PARTIAL total (READY components only)");
  }

  private static ShadbalaComponent comingSoon(String code, String name) {
    return new ShadbalaComponent(code, name, ComponentStatus.COMING_SOON, null, "Not implemented");
  }

  private static double uchchaBala(Planet planet, int signIndex) {
    Integer exalt = EXALTATION.get(planet);
    if (exalt == null) {
      return 30.0;
    }
    int debil = Math.floorMod(exalt + 6, 12);
    if (signIndex == exalt) {
      return 60.0;
    }
    if (signIndex == debil) {
      return 0.0;
    }
    return 30.0;
  }

  private static double houseStrength(int house) {
    if (house == 1 || house == 4 || house == 7 || house == 10) {
      return 60.0;
    }
    if (house == 5 || house == 9) {
      return 45.0;
    }
    if (house == 6 || house == 8 || house == 12) {
      return 15.0;
    }
    return 30.0;
  }

  public enum ComponentStatus {
    READY,
    COMING_SOON
  }

  public record ShadbalaComponent(
      String code, String displayName, ComponentStatus status, Double virupas, String note) {}

  public record PlanetShadbala(
      Planet planet,
      String planetName,
      int signIndex,
      int house,
      List<ShadbalaComponent> components,
      List<String> implementedComponents,
      List<String> comingSoonComponents,
      double partialTotalVirupas,
      String notes) {}

  public record ShadbalaReport(List<PlanetShadbala> planets, String notes) {
    public ShadbalaReport {
      planets = List.copyOf(planets);
      notes = notes == null ? "" : notes;
    }
  }
}
