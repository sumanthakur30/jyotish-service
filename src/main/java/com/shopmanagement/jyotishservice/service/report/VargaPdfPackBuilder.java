package com.shopmanagement.jyotishservice.service.report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.shopmanagement.jyotishservice.engine.CalculationEngine;
import com.shopmanagement.jyotishservice.engine.CalculationEngine.PlanetLongitude;
import com.shopmanagement.jyotishservice.engine.model.Planet;
import com.shopmanagement.jyotishservice.engine.model.PlanetPosition;
import com.shopmanagement.jyotishservice.engine.model.VargaChart;
import com.shopmanagement.jyotishservice.engine.varga.VargaCode;
import com.shopmanagement.jyotishservice.persistence.entity.KundaliSnapshotEntity;
import com.shopmanagement.jyotishservice.persistence.entity.PlanetaryPositionEntity;

/** Builds printable NI frames for a 2×2 / 4×4 Shodashavarga pack from persisted D1. */
public final class VargaPdfPackBuilder {

  private static final VargaCode[] PACK = {
    VargaCode.D1,
    VargaCode.D9,
    VargaCode.D2,
    VargaCode.D10,
    VargaCode.D3,
    VargaCode.D4,
    VargaCode.D7,
    VargaCode.D12
  };

  private VargaPdfPackBuilder() {}

  public static List<VargaChartFrame> build(
      CalculationEngine engine, KundaliSnapshotEntity snap, List<PlanetaryPositionEntity> planets) {
    if (snap.getAscendantLongitude() == null || planets == null || planets.isEmpty()) {
      return List.of();
    }
    double lagna = snap.getAscendantLongitude().doubleValue();
    List<PlanetLongitude> longs = new ArrayList<>();
    for (PlanetaryPositionEntity p : planets) {
      Planet planet;
      try {
        planet = Planet.valueOf(p.getPlanetCode().trim().toUpperCase(Locale.ROOT));
      } catch (Exception ex) {
        continue;
      }
      if (planet == Planet.ASCENDANT) {
        continue;
      }
      Double speed = p.getSpeedDegPerDay() != null ? p.getSpeedDegPerDay().doubleValue() : null;
      longs.add(new PlanetLongitude(planet, p.getLongitudeDeg().doubleValue(), p.isRetrograde(), speed));
    }

    List<VargaChartFrame> frames = new ArrayList<>();
    for (VargaCode code : PACK) {
      VargaChart chart = engine.computeVargaFromLongitudes(code, lagna, longs);
      frames.add(toFrame(chart));
    }
    return frames;
  }

  private static VargaChartFrame toFrame(VargaChart chart) {
    Map<String, Integer> houses = new HashMap<>();
    if (chart.ascendant() != null) {
      houses.put("ASCENDANT", chart.ascendant().house());
    }
    for (PlanetPosition p : chart.planets()) {
      houses.put(p.planet().name(), p.house());
    }
    int lagnaSign = chart.ascendant() != null ? chart.ascendant().signIndex() : 0;
    String title = chart.varga().code() + " " + chart.varga().displayName();
    return new VargaChartFrame(title, lagnaSign, houses);
  }
}
