package com.shopmanagement.jyotishservice.engine.panchang;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Muhurat periods from sunrise/sunset + weekday (North Indian conventions). Moonrise/moonset stay
 * Coming Soon elsewhere.
 */
public final class MuhuratCalculator {

  private static final String[] CHOGHADIYA_CYCLE = {
    "Udaya", "Amrit", "Shubh", "Labh", "Char", "Rog", "Kaal", "Gulika"
  };

  /** Day Choghadiya start index by Sunday-based weekday (0=Sun … 6=Sat). */
  private static final int[] CHOGHADIYA_DAY_START = {0, 1, 5, 3, 2, 4, 6};

  /** Night Choghadiya start index by Sunday-based weekday. */
  private static final int[] CHOGHADIYA_NIGHT_START = {2, 4, 1, 6, 3, 0, 5};

  /** Planetary hora lords cycling: Sun → Venus → Mercury → Moon → Saturn → Jupiter → Mars. */
  private static final String[] HORA_LORDS = {
    "Sun", "Venus", "Mercury", "Moon", "Saturn", "Jupiter", "Mars"
  };

  /** Weekday lord index into HORA_LORDS (Sunday-based 0–6). */
  private static final int[] WEEKDAY_HORA_START = {0, 3, 6, 2, 5, 1, 4};

  private MuhuratCalculator() {}

  public static PanchangResult.MuhuratBundle compute(
      Instant sunrise,
      Instant sunset,
      ZoneId zone,
      DayOfWeek dayOfWeek,
      boolean solarAvailable) {
    if (!solarAvailable || sunrise == null || sunset == null || !sunset.isAfter(sunrise)) {
      return new PanchangResult.MuhuratBundle(
          List.of(),
          "Muhurat periods unavailable (polar / missing sunrise–sunset).");
    }

    int sundayBased = dayOfWeek.getValue() % 7; // MON=1→1 … SUN=7→0
    List<PanchangResult.MuhuratPeriod> periods = new ArrayList<>();

    Duration dayLen = Duration.between(sunrise, sunset);
    Duration seg = dayLen.dividedBy(8);

    periods.add(segment("RAHU_KAAL", "Rahu Kaal", sunrise, seg, rahuKaalSegment(sundayBased), "Inauspicious"));
    periods.add(
        segment("YAMAGANDA", "Yamaganda", sunrise, seg, yamagandaSegment(sundayBased), "Inauspicious"));
    periods.add(segment("GULIKA", "Gulika Kaal", sunrise, seg, gulikaSegment(sundayBased), "Inauspicious"));

    addChoghadiya(periods, "CHOGHADIYA_DAY", sunrise, sunset, CHOGHADIYA_DAY_START[sundayBased], true);
    Instant nextSunrise = sunrise.plus(Duration.ofDays(1));
    // Night from sunset to next civil sunrise approximation: sunset + dayLen (symmetric night when
    // using equal day/night length for Choghadiya MVP).
    Instant nightEnd = sunset.plus(dayLen);
    if (nightEnd.isAfter(nextSunrise)) {
      nightEnd = nextSunrise;
    }
    addChoghadiya(
        periods, "CHOGHADIYA_NIGHT", sunset, nightEnd, CHOGHADIYA_NIGHT_START[sundayBased], false);

    addHoras(periods, sunrise, sundayBased);
    periods.add(abhijit(sunrise, sunset, zone));

    return new PanchangResult.MuhuratBundle(
        List.copyOf(periods),
        "Rahu Kaal / Yamaganda / Gulika / Choghadiya / Hora / Abhijit from sunrise–sunset. North"
            + " Indian weekday segment tables. Moonrise/moonset Coming Soon.");
  }

  /** Sunday-based 0–6 → 1-based day segment for Rahu Kaal. */
  static int rahuKaalSegment(int sundayBased) {
    return switch (sundayBased) {
      case 0 -> 8;
      case 1 -> 2;
      case 2 -> 7;
      case 3 -> 5;
      case 4 -> 6;
      case 5 -> 4;
      case 6 -> 3;
      default -> 8;
    };
  }

  static int yamagandaSegment(int sundayBased) {
    return switch (sundayBased) {
      case 0 -> 5;
      case 1 -> 4;
      case 2 -> 3;
      case 3 -> 2;
      case 4 -> 1;
      case 5 -> 7;
      case 6 -> 6;
      default -> 5;
    };
  }

  static int gulikaSegment(int sundayBased) {
    return switch (sundayBased) {
      case 0 -> 7;
      case 1 -> 6;
      case 2 -> 5;
      case 3 -> 4;
      case 4 -> 3;
      case 5 -> 2;
      case 6 -> 1;
      default -> 7;
    };
  }

  private static PanchangResult.MuhuratPeriod segment(
      String code, String name, Instant sunrise, Duration seg, int oneBased, String quality) {
    Instant start = sunrise.plus(seg.multipliedBy(oneBased - 1L));
    Instant end = sunrise.plus(seg.multipliedBy(oneBased));
    return new PanchangResult.MuhuratPeriod(code, name, start, end, quality);
  }

  private static void addChoghadiya(
      List<PanchangResult.MuhuratPeriod> out,
      String codePrefix,
      Instant start,
      Instant end,
      int startIdx,
      boolean day) {
    Duration len = Duration.between(start, end);
    if (len.isZero() || len.isNegative()) {
      return;
    }
    Duration seg = len.dividedBy(8);
    for (int i = 0; i < 8; i++) {
      String name = CHOGHADIYA_CYCLE[(startIdx + i) % 8];
      Instant s = start.plus(seg.multipliedBy(i));
      Instant e = i == 7 ? end : start.plus(seg.multipliedBy(i + 1L));
      String quality = choghadiyaQuality(name);
      out.add(
          new PanchangResult.MuhuratPeriod(
              codePrefix + "_" + (i + 1),
              (day ? "Day " : "Night ") + name,
              s,
              e,
              quality));
    }
  }

  private static String choghadiyaQuality(String name) {
    return switch (name) {
      case "Amrit", "Shubh", "Labh" -> "Auspicious";
      case "Udaya", "Char" -> "Neutral";
      default -> "Inauspicious";
    };
  }

  private static void addHoras(
      List<PanchangResult.MuhuratPeriod> out, Instant sunrise, int sundayBased) {
    Duration hora = Duration.ofHours(1);
    int lord = WEEKDAY_HORA_START[sundayBased];
    for (int i = 0; i < 24; i++) {
      Instant s = sunrise.plus(hora.multipliedBy(i));
      Instant e = sunrise.plus(hora.multipliedBy(i + 1L));
      String name = HORA_LORDS[lord % 7];
      out.add(new PanchangResult.MuhuratPeriod("HORA_" + (i + 1), name + " Hora", s, e, name));
      lord++;
    }
  }

  private static PanchangResult.MuhuratPeriod abhijit(
      Instant sunrise, Instant sunset, ZoneId zone) {
    Duration half = Duration.between(sunrise, sunset).dividedBy(2);
    Instant noon = sunrise.plus(half);
    Instant start = noon.minus(Duration.ofMinutes(12));
    Instant end = noon.plus(Duration.ofMinutes(12));
    LocalTime localNoon = noon.atZone(zone).toLocalTime();
    return new PanchangResult.MuhuratPeriod(
        "ABHIJIT",
        "Abhijit Muhurat",
        start,
        end,
        "Auspicious (~24 min centered on local solar noon " + localNoon + ")");
  }
}
