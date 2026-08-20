package com.shopmanagement.jyotishservice.engine.ephemeris;

/**
 * Tropical geocentric ecliptic longitude (+ optional daily speed) for a body.
 *
 * <p>Ephemeris choice (Phase 2 V1.0): pure Java Jean Meeus / VSOP-style truncated series — see
 * {@link MeeusEphemeris}. No JNI Swiss Ephemeris packaging in this slice (Windows/Docker-friendly;
 * avoids AGPL native SE dependency). Positions are real astronomy, not demo constants.
 */
public final class TropicalBody {

  private final double longitudeDeg;
  private final double speedDegPerDay;
  private final boolean retrograde;

  public TropicalBody(double longitudeDeg, double speedDegPerDay) {
    this.longitudeDeg = longitudeDeg;
    this.speedDegPerDay = speedDegPerDay;
    this.retrograde = speedDegPerDay < 0;
  }

  public double longitudeDeg() {
    return longitudeDeg;
  }

  public double speedDegPerDay() {
    return speedDegPerDay;
  }

  public boolean retrograde() {
    return retrograde;
  }
}
