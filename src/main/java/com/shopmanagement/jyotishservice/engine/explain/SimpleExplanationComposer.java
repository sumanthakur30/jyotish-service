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

  /**
   * Builds 3–5 simple paragraphs for a maha/antar pair using only provided lord codes and dates.
   */
  public static ExplainedBlock explainDashaPeriod(
      String mahaLordCode,
      String mahaLordName,
      String antarLordCode,
      String antarLordName,
      Instant startAt,
      Instant endAt,
      String systemCode) {
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
          "You are currently in "
              + mahaLabel
              + " Mahadasha with "
              + antarLabel
              + " Antardasha"
              + (range != null ? " (" + range + ")" : "")
              + ". These names come from your stored Vimshottari timeline.");
      hi.add(
          "आप वर्तमान में "
              + (maha != null ? maha.nameHi() : mahaLabel)
              + " महादशा और "
              + (antar != null ? antar.nameHi() : antarLabel)
              + " अंतर्दशा में हैं"
              + (range != null ? " (" + range + ")" : "")
              + "। ये नाम संग्रहीत विंशोत्तरी कालखंड से लिए गए हैं।");
    } else if (mahaLabel != null) {
      en.add(
          "Your current major period is "
              + mahaLabel
              + " Mahadasha"
              + (range != null ? " (" + range + ")" : "")
              + ", taken from the stored dasha calculation.");
      hi.add(
          "आपकी वर्तमान मुख्य अवधि "
              + (maha != null ? maha.nameHi() : mahaLabel)
              + " महादशा है"
              + (range != null ? " (" + range + ")" : "")
              + " — यह संग्रहीत दशा गणना से है।");
    } else {
      en.add(
          "Your current sub-period is "
              + antarLabel
              + " Antardasha"
              + (range != null ? " (" + range + ")" : "")
              + ", taken from the stored dasha calculation.");
      hi.add(
          "आपकी वर्तमान उप-अवधि "
              + (antar != null ? antar.nameHi() : antarLabel)
              + " अंतर्दशा है"
              + (range != null ? " (" + range + ")" : "")
              + " — यह संग्रहीत दशा गणना से है।");
    }

    if (maha != null && !maha.meaningEn().isEmpty()) {
      en.add(maha.meaningEn().get(0));
      hi.add(maha.meaningHi().get(0));
    }
    if (antar != null && !antar.meaningEn().isEmpty()) {
      String antarLineEn =
          "Within this, the "
              + antar.nameEn()
              + " Antardasha "
              + soften(antar.meaningEn().get(0));
      String antarLineHi =
          "इसके भीतर "
              + antar.nameHi()
              + " अंतर्दशा "
              + softenHi(antar.meaningHi().get(0));
      en.add(antarLineEn);
      hi.add(antarLineHi);
    } else if (maha != null && maha.meaningEn().size() > 1) {
      en.add(maha.meaningEn().get(1));
      hi.add(maha.meaningHi().get(1));
    }

    en.add(
        "These lines are general traditional themes only. A Jyotish can relate them to your full"
            + " chart and life questions.");
    hi.add(
        "ये पंक्तियाँ केवल सामान्य पारंपरिक संकेत हैं। योग्य ज्योतिषी इन्हें पूरी कुंडली और आपके"
            + " प्रश्नों से जोड़कर समझा सकते हैं।");

    List<FactBullet> why = new ArrayList<>();
    if (systemCode != null && !systemCode.isBlank()) {
      why.add(new FactBullet("SYSTEM", "Dasha system", "दशा प्रणाली", systemCode));
    }
    if (mahaLordCode != null && !mahaLordCode.isBlank()) {
      why.add(
          new FactBullet(
              "MAHA",
              "Mahadasha lord",
              "महादशा स्वामी",
              mahaLabel != null ? mahaLabel : mahaLordCode));
    }
    if (antarLordCode != null && !antarLordCode.isBlank()) {
      why.add(
          new FactBullet(
              "ANTAR",
              "Antardasha lord",
              "अंतर्दशा स्वामी",
              antarLabel != null ? antarLabel : antarLordCode));
    }
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
            "Stored dasha_period rows (not invented)"));

    // Cap at 5 paragraphs
    if (en.size() > 5) {
      en = new ArrayList<>(en.subList(0, 5));
      hi = new ArrayList<>(hi.subList(0, 5));
    }

    return new ExplainedBlock(false, List.copyOf(en), List.copyOf(hi), List.copyOf(why));
  }

  private static String soften(String sentence) {
    String s = sentence.trim();
    if (s.toLowerCase(Locale.ROOT).startsWith("may be considered")) {
      return s;
    }
    // Re-anchor antar theme as continuation
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
