package com.shopmanagement.jyotishservice.engine.houses;

import java.util.ArrayList;
import java.util.List;

import com.shopmanagement.jyotishservice.engine.astro.AstroMath;
import com.shopmanagement.jyotishservice.engine.astro.ZodiacCatalog;
import com.shopmanagement.jyotishservice.engine.model.HouseCusp;

/**
 * Nirayana <em>Bhava Chalit</em> via classical <strong>Sripati / Porphyry</strong> cusps — the
 * unequal-bhava system used by most North-Indian Kundli print software for “Bhava Chalit
 * (Nirayana)”.
 *
 * <p><b>Method (documented; do not substitute whole-sign):</b>
 *
 * <ol>
 *   <li>Angles (sidereal): ASC, MC, DSC = ASC+180°, IC = MC+180°.
 *   <li>House <em>cusps</em> (starts): C1=ASC, C4=IC, C7=DSC, C10=MC; trisect each quadrant for
 *       C2–C3, C5–C6, C8–C9, C11–C12 (equal arc within the quadrant — Sripati / Porphyry).
 *   <li>A planet belongs to house h when its longitude lies in [C[h], C[h+1]) going zodiacally —
 *       not by rashi alone.
 * </ol>
 *
 * <p>This is <em>not</em> Placidus and must not be faked by copying Rashi (whole-sign) houses.
 * Swiss Ephemeris house letter {@code 'O'} (Porphyry) matches these cusps when fed the same
 * angles.
 */
public final class SripatiBhavaChalit {

  private SripatiBhavaChalit() {}

  /**
   * Twelve sidereal house starts (cusps) for Sripati / Porphyry Chalit.
   *
   * @param siderealAscDeg sidereal Ascendant (= cusp of bhava 1)
   * @param siderealMcDeg sidereal Midheaven (= cusp of bhava 10)
   * @return cusps indexed 0 unused; cusp[1]…cusp[12]
   */
  public static double[] sandhiCusps(double siderealAscDeg, double siderealMcDeg) {
    double asc = AstroMath.norm360(siderealAscDeg);
    double mc = AstroMath.norm360(siderealMcDeg);
    double dsc = AstroMath.norm360(asc + 180.0);
    double ic = AstroMath.norm360(mc + 180.0);

    double[] cusp = new double[13];
    cusp[1] = asc;
    cusp[4] = ic;
    cusp[7] = dsc;
    cusp[10] = mc;
    fillTrisect(cusp, 1, 4, 2, 3); // ASC → IC
    fillTrisect(cusp, 4, 7, 5, 6); // IC → DSC
    fillTrisect(cusp, 7, 10, 8, 9); // DSC → MC
    fillTrisect(cusp, 10, 1, 11, 12); // MC → ASC
    return cusp;
  }

  /** House cusps for persistence / API (sign of each cusp). */
  public static List<HouseCusp> houseCusps(double siderealAscDeg, double siderealMcDeg) {
    double[] cusp = sandhiCusps(siderealAscDeg, siderealMcDeg);
    List<HouseCusp> houses = new ArrayList<>(12);
    for (int h = 1; h <= 12; h++) {
      int signIndex = ZodiacCatalog.signIndex(cusp[h]);
      houses.add(new HouseCusp(h, signIndex, ZodiacCatalog.signName(signIndex), cusp[h]));
    }
    return houses;
  }

  /**
   * Bhava number 1–12 for a sidereal longitude given Sripati cusps ({@link #sandhiCusps}).
   */
  public static int houseOf(double siderealLongitudeDeg, double[] cusps) {
    double lon = AstroMath.norm360(siderealLongitudeDeg);
    for (int h = 1; h <= 12; h++) {
      double start = cusps[h];
      double end = cusps[h == 12 ? 1 : h + 1];
      if (inArc(lon, start, end)) {
        return h;
      }
    }
    return 1;
  }

  private static void fillTrisect(double[] cusp, int from, int to, int a, int b) {
    double start = cusp[from];
    double arc = forwardArc(start, cusp[to]);
    cusp[a] = AstroMath.norm360(start + arc / 3.0);
    cusp[b] = AstroMath.norm360(start + 2.0 * arc / 3.0);
  }

  private static double forwardArc(double from, double to) {
    return AstroMath.norm360(to - from);
  }

  /** True if {@code lon} lies in [start, end) along the zodiac (handles wrap). */
  static boolean inArc(double lon, double start, double end) {
    double x = AstroMath.norm360(lon - start);
    double span = forwardArc(start, end);
    if (span < 1e-9) {
      return true;
    }
    return x < span - 1e-12 || Math.abs(x) < 1e-12;
  }
}
