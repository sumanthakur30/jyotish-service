package com.shopmanagement.jyotishservice.engine.explain;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.shopmanagement.jyotishservice.api.SimpleOverviewApi.ExplainedBlock;
import com.shopmanagement.jyotishservice.api.SimpleOverviewApi.FactBullet;
import com.shopmanagement.jyotishservice.engine.explain.DashaLordThemes.Theme;

/**
 * Pure templating over structured facts. Returns {@code calculationNotAvailable} when required
 * inputs are missing — never invents lords, dates, or yogas.
 */
public final class SimpleExplanationComposer {

  public static final String GENERAL_DISCLAIMER_EN =
      "Sugam Jyotish Simple View uses calculated chart facts with gentle traditional wording. It is"
          + " not a destiny guarantee and does not replace a personal consultation.";
  public static final String GENERAL_DISCLAIMER_HI =
      "सुगम ज्योतिष सिंपल व्यू गणना तथ्यों पर आधारित सौम्य पारंपरिक भाषा उपयोग करता है। यह भाग्य की"
          + " गारंटी नहीं है और व्यक्तिगत परामर्श का विकल्प नहीं है।";

  private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

  /** Optional birth-chart placement for a dasha lord (from stored D1 only). */
  public record LordPlacement(String signName, int house, String nakshatraName) {}

  private SimpleExplanationComposer() {}

  public static ExplainedBlock emptyUnavailable() {
    return new ExplainedBlock(
        true,
        List.of(
            "This explanation is not available yet because the required calculation facts are"
                + " missing for this Kundli."),
        List.of(
            "यह व्याख्या अभी उपलब्ध नहीं है क्योंकि इस कुंडली के लिए आवश्यक गणना तथ्य उपलब्ध नहीं"
                + " हैं।"),
        List.of());
  }

  public static ExplainedBlock explainDashaPeriod(
      String mahaLordCode,
      String mahaLordName,
      String antarLordCode,
      String antarLordName,
      Instant startAt,
      Instant endAt,
      String systemCode) {
    return explainDashaPeriod(
        mahaLordCode,
        mahaLordName,
        antarLordCode,
        antarLordName,
        startAt,
        endAt,
        systemCode,
        null,
        null);
  }

  /**
   * Builds 3–5 simple paragraphs for a maha/antar pair using only provided lord codes, dates, and
   * optional D1 placements.
   */
  public static ExplainedBlock explainDashaPeriod(
      String mahaLordCode,
      String mahaLordName,
      String antarLordCode,
      String antarLordName,
      Instant startAt,
      Instant endAt,
      String systemCode,
      LordPlacement mahaPlacement,
      LordPlacement antarPlacement) {
    if ((mahaLordCode == null || mahaLordCode.isBlank())
        && (antarLordCode == null || antarLordCode.isBlank())) {
      return emptyUnavailable();
    }

    Theme maha = DashaLordThemes.themeOrNull(mahaLordCode);
    Theme antar = DashaLordThemes.themeOrNull(antarLordCode);

    List<String> en = new ArrayList<>();
    List<String> hi = new ArrayList<>();

    String mahaLabel = display(mahaLordName, mahaLordCode, maha);
    String antarLabel = display(antarLordName, antarLordCode, antar);
    String range = formatRange(startAt, endAt);

    if (mahaLabel != null && antarLabel != null) {
      en.add(
          "You are in the "
              + mahaLabel
              + " major life period, with a "
              + antarLabel
              + " chapter inside it"
              + (range != null ? " (" + range + ")" : "")
              + ". These names come from your stored life-period timeline.");
      hi.add(
          "आप "
              + (maha != null ? maha.nameHi() : mahaLabel)
              + " की मुख्य जीवन अवधि में हैं, और उसके भीतर "
              + (antar != null ? antar.nameHi() : antarLabel)
              + " का अध्याय चल रहा है"
              + (range != null ? " (" + range + ")" : "")
              + "। ये नाम संग्रहीत जीवन-अवधि समयरेखा से लिए गए हैं।");
    } else if (mahaLabel != null) {
      en.add(
          "Your current major life period is "
              + mahaLabel
              + (range != null ? " (" + range + ")" : "")
              + ", taken from the stored period calculation.");
      hi.add(
          "आपकी वर्तमान मुख्य जीवन अवधि "
              + (maha != null ? maha.nameHi() : mahaLabel)
              + " है"
              + (range != null ? " (" + range + ")" : "")
              + " — यह संग्रहीत अवधि गणना से है।");
    } else {
      en.add(
          "Your current chapter is "
              + antarLabel
              + (range != null ? " (" + range + ")" : "")
              + ", taken from the stored period calculation.");
      hi.add(
          "आपका वर्तमान अध्याय "
              + (antar != null ? antar.nameHi() : antarLabel)
              + " है"
              + (range != null ? " (" + range + ")" : "")
              + " — यह संग्रहीत अवधि गणना से है।");
    }

    if (maha != null && !maha.meaningEn().isEmpty()) {
      en.add(maha.meaningEn().get(0));
      hi.add(maha.meaningHi().get(0));
    }
    if (antar != null && !antar.meaningEn().isEmpty()) {
      String antarLineEn =
          "Within this, the "
              + antar.nameEn()
              + " chapter "
              + soften(antar.meaningEn().get(0));
      String antarLineHi =
          "इसके भीतर "
              + antar.nameHi()
              + " अध्याय "
              + softenHi(antar.meaningHi().get(0));
      en.add(antarLineEn);
      hi.add(antarLineHi);
    } else if (maha != null && maha.meaningEn().size() > 1) {
      en.add(maha.meaningEn().get(1));
      hi.add(maha.meaningHi().get(1));
    }

    String placeEn = placementSentenceEn(mahaLabel, mahaPlacement, antarLabel, antarPlacement);
    String placeHi = placementSentenceHi(maha, mahaLabel, mahaPlacement, antar, antarLabel, antarPlacement);
    if (placeEn != null) {
      en.add(placeEn);
      hi.add(placeHi);
    }

    en.add(
        "These lines are general traditional themes only. A Jyotish can relate them to your full"
            + " chart and life questions.");
    hi.add(
        "ये पंक्तियाँ केवल सामान्य पारंपरिक संकेत हैं। योग्य ज्योतिषी इन्हें पूरी कुंडली और आपके"
            + " प्रश्नों से जोड़कर समझा सकते हैं।");

    List<FactBullet> why = new ArrayList<>();
    if (systemCode != null && !systemCode.isBlank()) {
      why.add(new FactBullet("SYSTEM", "Period system", "अवधि प्रणाली", systemCode));
    }
    if (mahaLordCode != null && !mahaLordCode.isBlank()) {
      why.add(
          new FactBullet(
              "MAHA",
              "Major period lord",
              "मुख्य अवधि स्वामी",
              mahaLabel != null ? mahaLabel : mahaLordCode));
    }
    if (antarLordCode != null && !antarLordCode.isBlank()) {
      why.add(
          new FactBullet(
              "ANTAR",
              "Chapter lord",
              "अध्याय स्वामी",
              antarLabel != null ? antarLabel : antarLordCode));
    }
    addPlacementFacts(why, "MAHA_PLACE", "Major lord in chart", "मुख्य स्वामी कुंडली में", mahaPlacement);
    addPlacementFacts(why, "ANTAR_PLACE", "Chapter lord in chart", "अध्याय स्वामी कुंडली में", antarPlacement);
    if (startAt != null) {
      why.add(new FactBullet("START", "Period starts", "अवधि आरंभ", ISO_DATE.format(toDate(startAt))));
    }
    if (endAt != null) {
      why.add(new FactBullet("END", "Period ends", "अवधि समाप्ति", ISO_DATE.format(toDate(endAt))));
    }
    why.add(
        new FactBullet(
            "SOURCE",
            "Source",
            "स्रोत",
            "Stored period rows (not invented) / संग्रहीत अवधि पंक्तियाँ (कल्पित नहीं)"));

    if (en.size() > 5) {
      en = new ArrayList<>(en.subList(0, 5));
      hi = new ArrayList<>(hi.subList(0, 5));
    }

    return new ExplainedBlock(false, List.copyOf(en), List.copyOf(hi), List.copyOf(why));
  }

  private static void addPlacementFacts(
      List<FactBullet> why, String code, String labelEn, String labelHi, LordPlacement p) {
    if (p == null || (p.signName() == null && p.house() <= 0)) {
      return;
    }
    StringBuilder v = new StringBuilder();
    if (p.signName() != null && !p.signName().isBlank()) {
      v.append(p.signName());
    }
    if (p.house() > 0) {
      if (v.length() > 0) {
        v.append(" · ");
      }
      v.append("House ").append(p.house());
    }
    if (p.nakshatraName() != null && !p.nakshatraName().isBlank()) {
      if (v.length() > 0) {
        v.append(" · ");
      }
      v.append(p.nakshatraName());
    }
    why.add(new FactBullet(code, labelEn, labelHi, v.toString()));
  }

  private static String placementSentenceEn(
      String mahaLabel, LordPlacement maha, String antarLabel, LordPlacement antar) {
    List<String> parts = new ArrayList<>();
    if (maha != null && maha.signName() != null && maha.house() > 0 && mahaLabel != null) {
      parts.add(
          mahaLabel
              + " sits in "
              + maha.signName()
              + " (house "
              + maha.house()
              + ") in this birth chart");
    }
    if (antar != null
        && antar.signName() != null
        && antar.house() > 0
        && antarLabel != null
        && (maha == null
            || !samePlacement(maha, antar)
            || (mahaLabel != null && !mahaLabel.equalsIgnoreCase(antarLabel)))) {
      parts.add(
          antarLabel
              + " sits in "
              + antar.signName()
              + " (house "
              + antar.house()
              + ")");
    }
    if (parts.isEmpty()) {
      return null;
    }
    return String.join("; ", parts) + " — facts from the stored chart, not predictions.";
  }

  private static String placementSentenceHi(
      Theme mahaTheme,
      String mahaLabel,
      LordPlacement maha,
      Theme antarTheme,
      String antarLabel,
      LordPlacement antar) {
    List<String> parts = new ArrayList<>();
    String mahaName = mahaTheme != null ? mahaTheme.nameHi() : mahaLabel;
    String antarName = antarTheme != null ? antarTheme.nameHi() : antarLabel;
    if (maha != null && maha.signName() != null && maha.house() > 0 && mahaName != null) {
      parts.add(
          mahaName
              + " इस जन्म कुंडली में "
              + SimpleLabels.signHi(maha.signName())
              + " (भाव "
              + maha.house()
              + ") में हैं");
    }
    if (antar != null
        && antar.signName() != null
        && antar.house() > 0
        && antarName != null
        && (maha == null
            || !samePlacement(maha, antar)
            || (mahaLabel != null && antarLabel != null && !mahaLabel.equalsIgnoreCase(antarLabel)))) {
      parts.add(
          antarName
              + " "
              + SimpleLabels.signHi(antar.signName())
              + " (भाव "
              + antar.house()
              + ") में हैं");
    }
    if (parts.isEmpty()) {
      return null;
    }
    return String.join("; ", parts) + " — ये संग्रहीत कुंडली तथ्य हैं, भविष्यवाणी नहीं।";
  }

  private static boolean samePlacement(LordPlacement a, LordPlacement b) {
    if (a == null || b == null) {
      return false;
    }
    return a.house() == b.house()
        && ((a.signName() == null && b.signName() == null)
            || (a.signName() != null && a.signName().equalsIgnoreCase(b.signName())));
  }

  private static String soften(String sentence) {
    String s = sentence.trim();
    if (s.toLowerCase(Locale.ROOT).startsWith("may be considered")) {
      return s;
    }
    if (s.endsWith(".")) {
      s = s.substring(0, s.length() - 1);
    }
    return "may colour this chapter: " + Character.toLowerCase(s.charAt(0)) + s.substring(1) + ".";
  }

  private static String softenHi(String sentence) {
    String s = sentence.trim();
    if (s.endsWith("।")) {
      s = s.substring(0, s.length() - 1);
    }
    return "इस अध्याय को रंग दे सकती है: " + s + "।";
  }

  private static String display(String name, String code, Theme theme) {
    if (name != null && !name.isBlank()) {
      return name;
    }
    if (theme != null) {
      return theme.nameEn();
    }
    return code != null && !code.isBlank() ? code : null;
  }

  private static String formatRange(Instant start, Instant end) {
    if (start == null && end == null) {
      return null;
    }
    String a = start != null ? ISO_DATE.format(toDate(start)) : "…";
    String b = end != null ? ISO_DATE.format(toDate(end)) : "…";
    return a + " – " + b;
  }

  private static java.time.LocalDate toDate(Instant instant) {
    return instant.atZone(ZoneOffset.UTC).toLocalDate();
  }
}
