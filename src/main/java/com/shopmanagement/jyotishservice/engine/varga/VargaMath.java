package com.shopmanagement.jyotishservice.engine.varga;

import com.shopmanagement.jyotishservice.engine.astro.AstroMath;

/** Shared helpers for equal-division varga mappers (Parashara style). */
public final class VargaMath {

  private VargaMath() {}

  /**
   * Equal N-fold division of a sign with a start-sign offset.
   *
   * @param d1LongitudeDeg D1 longitude
   * @param divisions parts per sign (e.g. 9 for Navamsha)
   * @param startSignIndex 0–11 starting rashi for part 0
   */
  public static double mapEqualDivision(double d1LongitudeDeg, int divisions, int startSignIndex) {
    double lon = AstroMath.norm360(d1LongitudeDeg);
    double degInSign = lon % 30.0;
    double partSize = 30.0 / divisions;
    int part = Math.min(divisions - 1, (int) Math.floor(degInSign / partSize));
    double within = degInSign - part * partSize;
    double fraction = within / partSize;
    int dSign = Math.floorMod(startSignIndex + part, 12);
    return AstroMath.norm360(dSign * 30.0 + fraction * 30.0);
  }

  /** Movable / fixed / dual start offsets used by Navamsha and related Vargas. */
  public static int movableFixedDualStart(int signIndex) {
    int mod = Math.floorMod(signIndex, 3);
    if (mod == 0) {
      return signIndex; // movable — same sign
    }
    if (mod == 1) {
      return Math.floorMod(signIndex + 8, 12); // fixed — 9th
    }
    return Math.floorMod(signIndex + 4, 12); // dual — 5th
  }

  /** Odd (1-based) signs: Aries, Gemini, … → 0-based even indices. */
  public static boolean oddSign(int signIndex) {
    return Math.floorMod(signIndex, 2) == 0;
  }
}
