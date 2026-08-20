package com.shopmanagement.jyotishservice.engine.dasha;

import java.time.Instant;
import java.util.List;

import com.shopmanagement.jyotishservice.engine.model.Planet;

/** Full dasha result for one system, rooted at mahadasha periods. */
public final class DashaTimeline {

  private final DashaSystemCode system;
  private final String engineVersion;
  private final Instant birthAt;
  private final int moonNakshatraIndex;
  private final String moonNakshatraName;
  private final Planet birthMahadashaLord;
  private final double balanceAtBirthYears;
  private final double elapsedAtBirthYears;
  private final List<DashaPeriod> mahadashas;
  private final String notes;

  public DashaTimeline(
      DashaSystemCode system,
      String engineVersion,
      Instant birthAt,
      int moonNakshatraIndex,
      String moonNakshatraName,
      Planet birthMahadashaLord,
      double balanceAtBirthYears,
      double elapsedAtBirthYears,
      List<DashaPeriod> mahadashas,
      String notes) {
    this.system = system;
    this.engineVersion = engineVersion;
    this.birthAt = birthAt;
    this.moonNakshatraIndex = moonNakshatraIndex;
    this.moonNakshatraName = moonNakshatraName;
    this.birthMahadashaLord = birthMahadashaLord;
    this.balanceAtBirthYears = balanceAtBirthYears;
    this.elapsedAtBirthYears = elapsedAtBirthYears;
    this.mahadashas = List.copyOf(mahadashas);
    this.notes = notes;
  }

  public DashaSystemCode system() {
    return system;
  }

  public String engineVersion() {
    return engineVersion;
  }

  public Instant birthAt() {
    return birthAt;
  }

  public int moonNakshatraIndex() {
    return moonNakshatraIndex;
  }

  public String moonNakshatraName() {
    return moonNakshatraName;
  }

  public Planet birthMahadashaLord() {
    return birthMahadashaLord;
  }

  public double balanceAtBirthYears() {
    return balanceAtBirthYears;
  }

  public double elapsedAtBirthYears() {
    return elapsedAtBirthYears;
  }

  public List<DashaPeriod> mahadashas() {
    return mahadashas;
  }

  public String notes() {
    return notes;
  }
}
