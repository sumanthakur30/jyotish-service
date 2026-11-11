package com.shopmanagement.jyotishservice.engine.explain;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.shopmanagement.jyotishservice.api.LifeAnalysisApi.LifeStoryChapter;
import com.shopmanagement.jyotishservice.engine.explain.DashaLordThemes.Theme;
import com.shopmanagement.jyotishservice.engine.life.LifeCategory;
import com.shopmanagement.jyotishservice.persistence.entity.DashaPeriodEntity;

/**
 * Turns stored Vimshottari MAHA rows into a year-wise life story. Themes are traditional teaching
 * aids mapped to life areas — not year-by-year event predictions.
 */
public final class LifeStoryComposer {

  private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

  private LifeStoryComposer() {}

  public static List<LifeStoryChapter> fromMaha(List<DashaPeriodEntity> rows, Instant asOf) {
    Instant now = asOf != null ? asOf : Instant.now();
    List<DashaPeriodEntity> mahas = new ArrayList<>();
    if (rows != null) {
      for (DashaPeriodEntity r : rows) {
        if (r != null && r.getStartAt() != null && r.getEndAt() != null && isMaha(r)) {
          mahas.add(r);
        }
      }
    }
    mahas.sort((a, b) -> a.getStartAt().compareTo(b.getStartAt()));
    List<LifeStoryChapter> out = new ArrayList<>();
    for (DashaPeriodEntity r : mahas) {
      out.add(chapter(r, now));
    }
    return List.copyOf(out);
  }

  private static LifeStoryChapter chapter(DashaPeriodEntity r, Instant now) {
    String code = r.getLordCode() == null ? "" : r.getLordCode().trim().toUpperCase(Locale.ROOT);
    Theme theme = DashaLordThemes.themeOrNull(code);
    String nameEn = theme != null ? theme.nameEn() : code;
    String nameHi = theme != null ? theme.nameHi() : code;
    int fromYear = year(r.getStartAt());
    int toYear = year(r.getEndAt());
    boolean current = !now.isBefore(r.getStartAt()) && now.isBefore(r.getEndAt());
    boolean past = !current && r.getEndAt().isBefore(now);
    List<LifeCategory> focus = focusAreas(code);
    List<String> focusCodes = focus.stream().map(LifeCategory::code).toList();
    String focusEn =
        focus.stream().map(LifeCategory::labelEn).reduce((a, b) -> a + " · " + b).orElse("");
    String focusHi =
        focus.stream().map(LifeCategory::labelHi).reduce((a, b) -> a + " · " + b).orElse("");
    String themeEn =
        theme != null && !theme.meaningEn().isEmpty() ? theme.meaningEn().get(0) : "";
    String themeHi =
        theme != null && !theme.meaningHi().isEmpty() ? theme.meaningHi().get(0) : "";
    String meaningEn =
        fromYear
            + "–"
            + toYear
            + " is the "
            + nameEn
            + " chapter. "
            + themeEn
            + (focusEn.isBlank() ? "" : " Areas that often come forward: " + focusEn + ".")
            + (current ? " You are in this chapter now." : "");
    String meaningHi =
        fromYear
            + "–"
            + toYear
            + " "
            + nameHi
            + " काल है। "
            + themeHi
            + (focusHi.isBlank() ? "" : " अक्सर आगे आने वाले क्षेत्र: " + focusHi + "।")
            + (current ? " आप अभी इसी अध्याय में हैं।" : "");
    return new LifeStoryChapter(
        code,
        nameEn,
        nameHi,
        r.getStartAt(),
        r.getEndAt(),
        fromYear,
        toYear,
        current,
        past,
        themeEn,
        themeHi,
        focusCodes,
        focusEn,
        focusHi,
        meaningEn,
        meaningHi);
  }

  /** Life areas traditionally linked with that mahadasha lord (karaka teaching, not fate). */
  static List<LifeCategory> focusAreas(String lordCode) {
    String c = lordCode == null ? "" : lordCode.trim().toUpperCase(Locale.ROOT);
    return switch (c) {
      case "SUN" -> List.of(LifeCategory.CAREER, LifeCategory.JOB, LifeCategory.HEALTH);
      case "MOON" -> List.of(LifeCategory.FAMILY, LifeCategory.HEALTH, LifeCategory.PROPERTY);
      case "MARS" -> List.of(LifeCategory.PROPERTY, LifeCategory.JOB, LifeCategory.HEALTH);
      case "MERCURY" -> List.of(LifeCategory.EDUCATION, LifeCategory.BUSINESS, LifeCategory.CAREER);
      case "JUPITER" -> List.of(LifeCategory.EDUCATION, LifeCategory.CHILDREN, LifeCategory.SPIRITUALITY);
      case "VENUS" -> List.of(LifeCategory.MARRIAGE, LifeCategory.FINANCE, LifeCategory.FAMILY);
      case "SATURN" -> List.of(LifeCategory.CAREER, LifeCategory.JOB, LifeCategory.HEALTH);
      case "RAHU" -> List.of(LifeCategory.FOREIGN, LifeCategory.CAREER, LifeCategory.BUSINESS);
      case "KETU" -> List.of(LifeCategory.SPIRITUALITY, LifeCategory.FOREIGN, LifeCategory.EDUCATION);
      default -> List.of();
    };
  }

  private static boolean isMaha(DashaPeriodEntity r) {
    return r.getLevelCode() != null && "MAHA".equalsIgnoreCase(r.getLevelCode());
  }

  private static int year(Instant instant) {
    return instant.atZone(IST).getYear();
  }
}
