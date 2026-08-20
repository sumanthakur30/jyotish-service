package com.shopmanagement.jyotishservice.engine.transit;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

import com.shopmanagement.jyotishservice.engine.model.AyanamsaMode;

/** Gochar result: transit positions + natal comparisons. */
public final class TransitChart {

  private final TransitSystemCode system;
  private final String engineVersion;
  private final LocalDate transitDate;
  private final LocalTime transitTime;
  private final AyanamsaMode ayanamsa;
  private final double ayanamsaDeg;
  private final double julianDayUt;
  private final int natalLagnaSignIndex;
  private final List<NatalTransitRow> rows;
  private final String notes;

  public TransitChart(
      TransitSystemCode system,
      String engineVersion,
      LocalDate transitDate,
      LocalTime transitTime,
      AyanamsaMode ayanamsa,
      double ayanamsaDeg,
      double julianDayUt,
      int natalLagnaSignIndex,
      List<NatalTransitRow> rows,
      String notes) {
    this.system = Objects.requireNonNull(system, "system");
    this.engineVersion = Objects.requireNonNull(engineVersion, "engineVersion");
    this.transitDate = Objects.requireNonNull(transitDate, "transitDate");
    this.transitTime = Objects.requireNonNull(transitTime, "transitTime");
    this.ayanamsa = Objects.requireNonNull(ayanamsa, "ayanamsa");
    this.ayanamsaDeg = ayanamsaDeg;
    this.julianDayUt = julianDayUt;
    this.natalLagnaSignIndex = natalLagnaSignIndex;
    this.rows = List.copyOf(Objects.requireNonNull(rows, "rows"));
    this.notes = notes;
  }

  public TransitSystemCode system() {
    return system;
  }

  public String engineVersion() {
    return engineVersion;
  }

  public LocalDate transitDate() {
    return transitDate;
  }

  public LocalTime transitTime() {
    return transitTime;
  }

  public AyanamsaMode ayanamsa() {
    return ayanamsa;
  }

  public double ayanamsaDeg() {
    return ayanamsaDeg;
  }

  public double julianDayUt() {
    return julianDayUt;
  }

  public int natalLagnaSignIndex() {
    return natalLagnaSignIndex;
  }

  public List<NatalTransitRow> rows() {
    return rows;
  }

  public String notes() {
    return notes;
  }
}
