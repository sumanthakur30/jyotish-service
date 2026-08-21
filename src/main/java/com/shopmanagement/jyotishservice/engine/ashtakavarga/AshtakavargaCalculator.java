package com.shopmanagement.jyotishservice.engine.ashtakavarga;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.shopmanagement.jyotishservice.engine.model.D1Chart;
import com.shopmanagement.jyotishservice.engine.model.Planet;
import com.shopmanagement.jyotishservice.engine.model.PlanetPosition;

/**
 * Classical Parashara Bhinnashtakavarga + Sarvashtakavarga.
 *
 * <p>Rahu/Ketu are <strong>excluded</strong> from classical BAV (7 grahas + Lagna as
 * contributors). Each subject's BAV accumulates bindus from all eight contributors using the
 * standard house tables (houses counted from the contributor's sign).
 */
public final class AshtakavargaCalculator {

  private static final Planet[] BAV_PLANETS = {
    Planet.SUN, Planet.MOON, Planet.MARS, Planet.MERCURY, Planet.JUPITER, Planet.VENUS, Planet.SATURN
  };

  private AshtakavargaCalculator() {}

  public static AshtakavargaResult compute(D1Chart d1) {
    Objects.requireNonNull(d1, "d1");
    EnumMap<Planet, Integer> signs = new EnumMap<>(Planet.class);
    for (PlanetPosition p : d1.planets()) {
      if (!p.planet().isNode() && p.planet() != Planet.ASCENDANT) {
        signs.put(p.planet(), p.signIndex());
      }
    }
    int lagnaSign = d1.ascendant().signIndex();

    EnumMap<Planet, int[]> bhinna = new EnumMap<>(Planet.class);
    int[] sarva = new int[12];
    int total = 0;

    for (Planet subject : BAV_PLANETS) {
      int[] row = computeBhinna(subject, signs, lagnaSign);
      bhinna.put(subject, row);
      for (int i = 0; i < 12; i++) {
        sarva[i] += row[i];
        total += row[i];
      }
    }

    return new AshtakavargaResult(
        Map.copyOf(copyRows(bhinna)),
        Arrays.copyOf(sarva, 12),
        total,
        "Classical Parashara BAV (7 grahas + Lagna contributors). Rahu/Ketu excluded from classical"
            + " BAV. SAV total typically ~337.");
  }

  private static Map<Planet, int[]> copyRows(EnumMap<Planet, int[]> src) {
    EnumMap<Planet, int[]> out = new EnumMap<>(Planet.class);
    for (Map.Entry<Planet, int[]> e : src.entrySet()) {
      out.put(e.getKey(), Arrays.copyOf(e.getValue(), 12));
    }
    return out;
  }

  private static int[] computeBhinna(
      Planet subject, Map<Planet, Integer> signs, int lagnaSign) {
    int[] bav = new int[12];
    for (Planet contributor : BAV_PLANETS) {
      Integer sign = signs.get(contributor);
      if (sign == null) {
        continue;
      }
      addBindus(bav, sign, BavTables.houses(subject, contributor));
    }
    addBindus(bav, lagnaSign, BavTables.houses(subject, null));
    return bav;
  }

  private static void addBindus(int[] bav, int contributorSign, List<Integer> houses) {
    for (int h : houses) {
      int sign = Math.floorMod(contributorSign + h - 1, 12);
      bav[sign]++;
    }
  }

  /** Immutable result. BAV arrays are sign-indexed 0=Aries … 11=Pisces. */
  public record AshtakavargaResult(
      Map<Planet, int[]> bhinnashtakavarga,
      int[] sarvashtakavarga,
      int totalBindus,
      String notes) {

    public AshtakavargaResult {
      Objects.requireNonNull(bhinnashtakavarga, "bhinnashtakavarga");
      Objects.requireNonNull(sarvashtakavarga, "sarvashtakavarga");
      if (sarvashtakavarga.length != 12) {
        throw new IllegalArgumentException("sarvashtakavarga must have 12 entries");
      }
      EnumMap<Planet, int[]> copy = new EnumMap<>(Planet.class);
      for (Map.Entry<Planet, int[]> e : bhinnashtakavarga.entrySet()) {
        int[] row = e.getValue();
        if (row == null || row.length != 12) {
          throw new IllegalArgumentException("each BAV row must have 12 entries");
        }
        copy.put(e.getKey(), Arrays.copyOf(row, 12));
      }
      bhinnashtakavarga = Collections.unmodifiableMap(copy);
      sarvashtakavarga = Arrays.copyOf(sarvashtakavarga, 12);
      notes = notes == null ? "" : notes;
    }

    public int[] bhinna(Planet planet) {
      int[] row = bhinnashtakavarga.get(planet);
      return row == null ? null : Arrays.copyOf(row, 12);
    }
  }
}
