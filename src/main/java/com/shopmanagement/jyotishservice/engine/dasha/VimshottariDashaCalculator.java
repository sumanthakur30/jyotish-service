package com.shopmanagement.jyotishservice.engine.dasha;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.shopmanagement.jyotishservice.engine.astro.AstroMath;
import com.shopmanagement.jyotishservice.engine.astro.ZodiacCatalog;
import com.shopmanagement.jyotishservice.engine.model.Planet;

/**
 * Classical Vimshottari (120-year) dasha from Moon nakshatra: Mahadasha → Antardasha →
 * Pratyantardasha.
 *
 * <p>Date math uses {@link #DAYS_PER_YEAR} days/year (common Jyotish software convention).
 */
public final class VimshottariDashaCalculator implements DashaCalculator {

  public static final VimshottariDashaCalculator INSTANCE = new VimshottariDashaCalculator();

  /** Mean Julian year used for dasha date arithmetic. */
  public static final double DAYS_PER_YEAR = 365.25;

  /** Full cycle length in years. */
  public static final double CYCLE_YEARS = 120.0;

  /** Lord order starting at Ketu (Ashwini). */
  public static final Planet[] LORD_ORDER = {
    Planet.KETU,
    Planet.VENUS,
    Planet.SUN,
    Planet.MOON,
    Planet.MARS,
    Planet.RAHU,
    Planet.JUPITER,
    Planet.SATURN,
    Planet.MERCURY
  };

  private static final Map<Planet, Double> FULL_YEARS = new EnumMap<>(Planet.class);

  static {
    FULL_YEARS.put(Planet.KETU, 7.0);
    FULL_YEARS.put(Planet.VENUS, 20.0);
    FULL_YEARS.put(Planet.SUN, 6.0);
    FULL_YEARS.put(Planet.MOON, 10.0);
    FULL_YEARS.put(Planet.MARS, 7.0);
    FULL_YEARS.put(Planet.RAHU, 18.0);
    FULL_YEARS.put(Planet.JUPITER, 16.0);
    FULL_YEARS.put(Planet.SATURN, 19.0);
    FULL_YEARS.put(Planet.MERCURY, 17.0);
  }

  private VimshottariDashaCalculator() {}

  @Override
  public DashaSystemCode system() {
    return DashaSystemCode.VIMSHOTTARI;
  }

  public static double fullYears(Planet lord) {
    Double y = FULL_YEARS.get(lord);
    if (y == null) {
      throw new IllegalArgumentException("Planet is not a Vimshottari lord: " + lord);
    }
    return y;
  }

  /** Nakshatra lord for index 0–26 (Ashwini → Ketu). */
  public static Planet nakshatraLord(int nakshatraIndex) {
    return LORD_ORDER[Math.floorMod(nakshatraIndex, 27) % 9];
  }

  /**
   * Fraction of the birth nakshatra already traversed by Moon (0 = start, 1 = end). Balance years =
   * (1 − fraction) × full mahadasha years of the nakshatra lord.
   */
  public static double elapsedFractionInNakshatra(double moonLongitudeDeg) {
    double lon = AstroMath.norm360(moonLongitudeDeg);
    double within = lon % ZodiacCatalog.NAKSHATRA_SPAN;
    return within / ZodiacCatalog.NAKSHATRA_SPAN;
  }

  public static Balance balanceAtBirth(double moonLongitudeDeg) {
    int nak = ZodiacCatalog.nakshatraIndex(moonLongitudeDeg);
    Planet lord = nakshatraLord(nak);
    double full = fullYears(lord);
    double elapsedFrac = elapsedFractionInNakshatra(moonLongitudeDeg);
    double elapsedYears = full * elapsedFrac;
    double balanceYears = full - elapsedYears;
    return new Balance(
        nak, ZodiacCatalog.nakshatraName(nak), lord, full, elapsedYears, balanceYears);
  }

  @Override
  public DashaTimeline compute(double moonLongitudeDeg, Instant birthAt, String engineVersion) {
    Balance bal = balanceAtBirth(moonLongitudeDeg);
    List<DashaPeriod> mahas = buildMahadashas(birthAt, bal);

    String notes =
        "Vimshottari from Moon "
            + bal.nakshatraName()
            + " (lord "
            + bal.lord().displayName()
            + "); balance at birth "
            + String.format(Locale.ROOT, "%.4f", bal.balanceYears())
            + " y; "
            + DAYS_PER_YEAR
            + " d/y; no interpretive strengths.";

    return new DashaTimeline(
        DashaSystemCode.VIMSHOTTARI,
        engineVersion,
        birthAt,
        bal.nakshatraIndex(),
        bal.nakshatraName(),
        bal.lord(),
        bal.balanceYears(),
        bal.elapsedYears(),
        mahas,
        notes);
  }

  private static List<DashaPeriod> buildMahadashas(Instant birthAt, Balance bal) {
    List<DashaPeriod> mahas = new ArrayList<>();
    Instant cursor = birthAt;
    int lordIdx = indexOf(bal.lord());

    // First (partial) MD — remaining balance; nested levels skip elapsed portion of full MD.
    Instant firstEnd = plusYears(cursor, bal.balanceYears());
    List<DashaPeriod> firstAntars =
        subdivide(
            DashaLevel.ANTAR,
            cursor,
            firstEnd,
            bal.fullYears(),
            bal.elapsedYears(),
            lordIdx,
            bal.lord(),
            null);
    mahas.add(
        new DashaPeriod(
            DashaLevel.MAHA,
            bal.lord(),
            bal.lord(),
            null,
            null,
            cursor,
            firstEnd,
            0,
            firstAntars));
    cursor = firstEnd;

    double remainingCycle = CYCLE_YEARS - bal.balanceYears();
    int seq = 1;
    int idx = (lordIdx + 1) % 9;
    while (remainingCycle > 1e-9) {
      Planet lord = LORD_ORDER[idx];
      double full = fullYears(lord);
      double dur = Math.min(full, remainingCycle);
      Instant end = plusYears(cursor, dur);
      // Subsequent MDs always begin at the start of that lord's full MD (may truncate at cycle end).
      List<DashaPeriod> antars =
          subdivide(DashaLevel.ANTAR, cursor, end, full, 0.0, idx, lord, null);
      mahas.add(
          new DashaPeriod(DashaLevel.MAHA, lord, lord, null, null, cursor, end, seq, antars));
      cursor = end;
      remainingCycle -= dur;
      idx = (idx + 1) % 9;
      seq++;
    }
    return mahas;
  }

  /**
   * Subdivide a parent wall-clock window that maps onto a slice of a theoretical full parent of
   * {@code fullParentYears}, where {@code elapsedIntoFull} years of that full parent occur before
   * {@code parentStart}.
   */
  private static List<DashaPeriod> subdivide(
      DashaLevel childLevel,
      Instant parentStart,
      Instant parentEnd,
      double fullParentYears,
      double elapsedIntoFull,
      int startLordIndex,
      Planet mahaLord,
      Planet antarLord) {
    double parentYears = yearsBetween(parentStart, parentEnd);
    double windowStart = elapsedIntoFull;
    double windowEnd = elapsedIntoFull + parentYears;

    List<DashaPeriod> children = new ArrayList<>();
    Instant cursor = parentStart;
    double covered = 0.0;
    double theorCursor = 0.0;
    int seq = 0;

    for (int i = 0; i < 9; i++) {
      Planet lord = LORD_ORDER[(startLordIndex + i) % 9];
      double childFull = fullParentYears * (fullYears(lord) / CYCLE_YEARS);
      double theorStart = theorCursor;
      double theorEnd = theorCursor + childFull;
      theorCursor = theorEnd;

      double overlapStart = Math.max(theorStart, windowStart);
      double overlapEnd = Math.min(theorEnd, windowEnd);
      double overlap = overlapEnd - overlapStart;
      if (overlap <= 1e-12) {
        continue;
      }

      double remaining = parentYears - covered;
      if (remaining <= 1e-12) {
        break;
      }
      double use = Math.min(overlap, remaining);
      boolean last = (covered + use + 1e-9 >= parentYears) || i == 8;
      Instant end = last ? parentEnd : plusYears(cursor, use);

      // Elapsed into this child's theoretical full span before the emitted window.
      double elapsedInChild = Math.max(0.0, overlapStart - theorStart);
      List<DashaPeriod> nested = List.of();
      if (childLevel == DashaLevel.ANTAR) {
        nested =
            subdivide(
                DashaLevel.PRATYANTAR,
                cursor,
                end,
                childFull,
                elapsedInChild,
                indexOf(lord),
                mahaLord,
                lord);
      }

      Planet rowAntar = childLevel == DashaLevel.ANTAR ? lord : antarLord;
      Planet rowPraty = childLevel == DashaLevel.PRATYANTAR ? lord : null;

      children.add(
          new DashaPeriod(
              childLevel,
              lord,
              mahaLord,
              rowAntar,
              rowPraty,
              cursor,
              end,
              seq,
              nested));
      cursor = end;
      covered += use;
      seq++;
      if (covered + 1e-9 >= parentYears) {
        break;
      }
    }
    return children;
  }

  static Instant plusYears(Instant start, double years) {
    long millis = Math.round(years * DAYS_PER_YEAR * 24.0 * 3600.0 * 1000.0);
    return start.plusMillis(millis);
  }

  static double yearsBetween(Instant start, Instant end) {
    double days = Duration.between(start, end).toMillis() / (24.0 * 3600.0 * 1000.0);
    return days / DAYS_PER_YEAR;
  }

  private static int indexOf(Planet lord) {
    for (int i = 0; i < LORD_ORDER.length; i++) {
      if (LORD_ORDER[i] == lord) {
        return i;
      }
    }
    throw new IllegalArgumentException("Not a Vimshottari lord: " + lord);
  }

  /** Birth balance inputs derived from Moon longitude. */
  public record Balance(
      int nakshatraIndex,
      String nakshatraName,
      Planet lord,
      double fullYears,
      double elapsedYears,
      double balanceYears) {}
}
