package com.shopmanagement.jyotishservice.engine.explain;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import com.shopmanagement.jyotishservice.api.KundaliApi.HouseDto;
import com.shopmanagement.jyotishservice.api.KundaliApi.PlanetDto;
import com.shopmanagement.jyotishservice.api.SimpleOverviewApi.FactBullet;
import com.shopmanagement.jyotishservice.api.SimpleOverviewApi.LifeAreaCard;
import com.shopmanagement.jyotishservice.engine.explain.DashaLordThemes.Theme;
import com.shopmanagement.jyotishservice.engine.explain.SimpleExplanationComposer.LordPlacement;
import com.shopmanagement.jyotishservice.engine.life.LifeCategory;

/**
 * Topic-specific Simple View life-area cards from stored D1 houses/planets only. Never invents
 * placements, scores, or Phaladesh outcomes.
 */
public final class SimpleLifeAreaComposer {

  private static final Map<String, String> SIGN_LORDS =
      Map.ofEntries(
          Map.entry("Aries", "Mars"),
          Map.entry("Taurus", "Venus"),
          Map.entry("Gemini", "Mercury"),
          Map.entry("Cancer", "Moon"),
          Map.entry("Leo", "Sun"),
          Map.entry("Virgo", "Mercury"),
          Map.entry("Libra", "Venus"),
          Map.entry("Scorpio", "Mars"),
          Map.entry("Sagittarius", "Jupiter"),
          Map.entry("Capricorn", "Saturn"),
          Map.entry("Aquarius", "Saturn"),
          Map.entry("Pisces", "Jupiter"));

  private SimpleLifeAreaComposer() {}

  public static LifeAreaCard compose(
      LifeCategory category,
      String status,
      List<HouseDto> houses,
      List<PlanetDto> planets,
      LordPlacement mahaPlacement,
      LordPlacement antarPlacement,
      String mahaLordCode,
      String antarLordCode) {
    if (category == null || category == LifeCategory.GENERAL) {
      return null;
    }

    Map<Integer, HouseDto> byHouse = indexHouses(houses);
    List<PlanetDto> planetList = planets != null ? planets : List.of();

    List<String> focusPartsEn = new ArrayList<>();
    List<String> focusPartsHi = new ArrayList<>();
    List<FactBullet> bullets = new ArrayList<>();

    for (int house : category.indicatorHouses()) {
      HouseDto h = byHouse.get(house);
      if (h == null || h.signName() == null || h.signName().isBlank()) {
        continue;
      }
      String sign = h.signName();
      String lord = SIGN_LORDS.getOrDefault(sign, "—");
      String lordHi = planetNameHi(lord);
      String ordinal = houseOrdinal(house);
      String occupants = occupantsLine(planetList, house);
      String occupantsHi = occupantsLineHi(planetList, house);

      focusPartsEn.add(
          ordinal
              + " "
              + sign
              + " · lord "
              + lord
              + (occupants.isBlank() ? "" : " · " + occupants));
      focusPartsHi.add(
          house
              + "वाँ "
              + SimpleLabels.signHi(sign)
              + " · स्वामी "
              + lordHi
              + (occupantsHi.isBlank() ? "" : " · " + occupantsHi));

      bullets.add(
          new FactBullet(
              "H" + house,
              ordinal + " house",
              house + "वाँ भाव",
              sign + " · lord " + lord + " / " + SimpleLabels.signHi(sign) + " · स्वामी " + lordHi));
      bullets.add(
          new FactBullet(
              "H" + house + "_OCC",
              "Planets in " + house,
              house + " भाव में ग्रह",
              occupants.isBlank() ? "None / कोई नहीं" : occupants + " / " + occupantsHi));
    }

    List<String> planetLinesEn = new ArrayList<>();
    List<String> planetLinesHi = new ArrayList<>();
    for (String code : relevantPlanetCodes(category)) {
      PlanetDto p = findPlanet(planetList, code);
      if (p == null || p.signName() == null) {
        continue;
      }
      Theme theme = DashaLordThemes.themeOrNull(code);
      String nameEn = theme != null ? theme.nameEn() : code;
      String nameHi = theme != null ? theme.nameHi() : code;
      String lineEn =
          nameEn
              + " in "
              + p.signName()
              + " · house "
              + p.house()
              + (p.retrograde() ? " R" : "");
      String lineHi =
          nameHi
              + " "
              + SimpleLabels.signHi(p.signName())
              + " · भाव "
              + p.house()
              + (p.retrograde() ? " वक्री" : "");
      planetLinesEn.add(lineEn);
      planetLinesHi.add(lineHi);
      bullets.add(new FactBullet(code, nameEn, nameHi, lineEn + " / " + lineHi));
    }

    addDashaLordHouseBullet(bullets, "MAHA_LORD_HOUSE", "Major period lord in chart", "मुख्य अवधि स्वामी कुंडली में", mahaLordCode, mahaPlacement);
    addDashaLordHouseBullet(bullets, "ANTAR_LORD_HOUSE", "Chapter lord in chart", "अध्याय स्वामी कुंडली में", antarLordCode, antarPlacement);

    if (category == LifeCategory.HEALTH) {
      bullets.add(
          new FactBullet(
              "HEALTH_NOTE",
              "Disclaimer",
              "अस्वीकरण",
              "Traditional Jyotish indicators only — not medical advice / केवल पारंपरिक ज्योतिष संकेत — चिकित्सा सलाह नहीं"));
    }

    String focusEn =
        focusPartsEn.isEmpty()
            ? "Focus houses not available from stored chart yet."
            : String.join("; ", focusPartsEn);
    String focusHi =
        focusPartsHi.isEmpty()
            ? "संग्रहीत कुंडली से केंद्र भाव अभी उपलब्ध नहीं।"
            : String.join("; ", focusPartsHi);

    List<String> parasEn = buildParagraphsEn(category, focusPartsEn, planetLinesEn);
    List<String> parasHi = buildParagraphsHi(category, focusPartsHi, planetLinesHi);

    return new LifeAreaCard(
        category.code(),
        category.labelEn(),
        category.labelHi(),
        status,
        SimpleLabels.statusLineEn(status),
        SimpleLabels.statusLineHi(status),
        null,
        null,
        null,
        null,
        null,
        focusEn,
        focusHi,
        List.copyOf(parasEn),
        List.copyOf(parasHi),
        List.copyOf(bullets),
        List.copyOf(planetLinesEn),
        List.copyOf(planetLinesHi),
        List.copyOf(category.indicatorHouses()));
  }

  /** Print-style placement + short gloss for an upcoming period lord (D1 only). */
  public static PeriodChapterExtras chapterExtras(String lordCode, LordPlacement placement) {
    if (placement == null || placement.signName() == null || placement.house() <= 0) {
      return new PeriodChapterExtras(null, 0, null, null, null, null);
    }
    Theme theme = DashaLordThemes.themeOrNull(lordCode);
    String nameEn = theme != null ? theme.nameEn() : (lordCode != null ? lordCode : "Lord");
    String nameHi = theme != null ? theme.nameHi() : nameEn;
    String placeEn =
        nameEn + " in " + placement.signName() + " · house " + placement.house();
    String placeHi =
        nameHi
            + " "
            + SimpleLabels.signHi(placement.signName())
            + " · भाव "
            + placement.house();
    String glossEn =
        "In this birth chart, "
            + nameEn
            + " sits in "
            + placement.signName()
            + " (house "
            + placement.house()
            + "). This is a stored placement fact — not a prediction of outcomes.";
    String glossHi =
        "इस जन्म कुंडली में "
            + nameHi
            + " "
            + SimpleLabels.signHi(placement.signName())
            + " (भाव "
            + placement.house()
            + ") में हैं। यह संग्रहीत स्थिति तथ्य है — परिणाम की भविष्यवाणी नहीं।";
    return new PeriodChapterExtras(
        placement.signName(), placement.house(), placeEn, placeHi, glossEn, glossHi);
  }

  public record PeriodChapterExtras(
      String lordSignName,
      int lordHouse,
      String placementLineEn,
      String placementLineHi,
      String glossEn,
      String glossHi) {}

  private static List<String> buildParagraphsEn(
      LifeCategory category, List<String> focusParts, List<String> planetLines) {
    List<String> out = new ArrayList<>();
    String topic = category.labelEn();
    if (!focusParts.isEmpty()) {
      out.add(
          "For "
              + topic
              + ", traditional focus may be considered on: "
              + String.join("; ", focusParts)
              + ". These are birth-chart house facts from the stored D1.");
    } else {
      out.add(
          "For "
              + topic
              + ", focus-house facts are not available yet from the stored chart.");
    }
    if (!planetLines.isEmpty()) {
      out.add(
          "Key graha often related to "
              + topic.toLowerCase(Locale.ROOT)
              + " in this chart: "
              + String.join("; ", planetLines)
              + ". A Jyotish can interpret these with your notes.");
    } else if (category == LifeCategory.HEALTH) {
      out.add(
          "Health indicators here are traditional chart facts only — not medical advice. Open notes"
              + " for Jyotish interpretation.");
    } else {
      out.add(
          "Open Life Analysis notes for this topic so a Jyotish can relate these houses to your"
              + " questions.");
    }
    return out;
  }

  private static List<String> buildParagraphsHi(
      LifeCategory category, List<String> focusParts, List<String> planetLines) {
    List<String> out = new ArrayList<>();
    String topic = category.labelHi();
    if (!focusParts.isEmpty()) {
      out.add(
          topic
              + " के लिए पारंपरिक रूप से इन केंद्र भावों पर विचार किया जा सकता है: "
              + String.join("; ", focusParts)
              + "। ये संग्रहीत D1 जन्म-कुंडली के भाव तथ्य हैं।");
    } else {
      out.add(topic + " के लिए केंद्र भाव तथ्य संग्रहीत कुंडली से अभी उपलब्ध नहीं हैं।");
    }
    if (!planetLines.isEmpty()) {
      out.add(
          topic
              + " से जुड़े मुख्य ग्रह (इस कुंडली में): "
              + String.join("; ", planetLines)
              + "। योग्य ज्योतिषी इन्हें आपकी टिप्पणियों से जोड़ सकते हैं।");
    } else if (category == LifeCategory.HEALTH) {
      out.add(
          "स्वास्थ्य संकेत यहाँ केवल पारंपरिक कुंडली तथ्य हैं — चिकित्सा सलाह नहीं। व्याख्या के लिए"
              + " टिप्पणियाँ खोलें।");
    } else {
      out.add(
          "इस विषय की जीवन-विश्लेषण टिप्पणियाँ खोलें ताकि ज्योतिषी इन भावों को आपके प्रश्नों से जोड़"
              + " सकें।");
    }
    return out;
  }

  private static List<String> relevantPlanetCodes(LifeCategory category) {
    return switch (category) {
      case CAREER -> List.of("SUN", "SATURN", "MERCURY");
      case JOB -> List.of("SUN", "SATURN");
      case BUSINESS -> List.of("MERCURY", "JUPITER");
      case FINANCE -> List.of("JUPITER", "VENUS");
      case MARRIAGE -> List.of("VENUS", "JUPITER");
      case FAMILY -> List.of("MOON", "VENUS");
      case CHILDREN -> List.of("JUPITER");
      case EDUCATION -> List.of("MERCURY", "JUPITER");
      case PROPERTY -> List.of("MARS", "VENUS");
      case FOREIGN -> List.of("RAHU", "JUPITER");
      case SPIRITUALITY -> List.of("JUPITER", "KETU");
      case HEALTH -> List.of("MARS", "SATURN");
      default -> List.of();
    };
  }

  private static void addDashaLordHouseBullet(
      List<FactBullet> bullets,
      String code,
      String labelEn,
      String labelHi,
      String lordCode,
      LordPlacement placement) {
    if (placement == null || placement.signName() == null || placement.house() <= 0) {
      return;
    }
    Theme theme = DashaLordThemes.themeOrNull(lordCode);
    String name = theme != null ? theme.nameEn() : (lordCode != null ? lordCode : "Lord");
    bullets.add(
        new FactBullet(
            code,
            labelEn,
            labelHi,
            name
                + " · "
                + placement.signName()
                + " · house "
                + placement.house()
                + " / "
                + (theme != null ? theme.nameHi() : name)
                + " · "
                + SimpleLabels.signHi(placement.signName())
                + " · भाव "
                + placement.house()));
  }

  private static Map<Integer, HouseDto> indexHouses(List<HouseDto> houses) {
    Map<Integer, HouseDto> byHouse = new LinkedHashMap<>();
    if (houses == null) {
      return byHouse;
    }
    for (HouseDto h : houses) {
      if (h != null) {
        byHouse.put(h.house(), h);
      }
    }
    return byHouse;
  }

  private static String occupantsLine(List<PlanetDto> planets, int house) {
    return planets.stream()
        .filter(p -> p != null && p.house() == house && p.planetCode() != null)
        .map(p -> p.planetCode() + (p.retrograde() ? "ᵣ" : ""))
        .collect(Collectors.joining(", "));
  }

  private static String occupantsLineHi(List<PlanetDto> planets, int house) {
    return planets.stream()
        .filter(p -> p != null && p.house() == house && p.planetCode() != null)
        .map(
            p -> {
              Theme t = DashaLordThemes.themeOrNull(p.planetCode());
              String n = t != null ? t.nameHi() : p.planetCode();
              return n + (p.retrograde() ? "ᵣ" : "");
            })
        .collect(Collectors.joining(", "));
  }

  private static PlanetDto findPlanet(List<PlanetDto> planets, String code) {
    for (PlanetDto p : planets) {
      if (p != null && code.equalsIgnoreCase(p.planetCode())) {
        return p;
      }
    }
    return null;
  }

  private static String houseOrdinal(int house) {
    return switch (house) {
      case 1 -> "1st";
      case 2 -> "2nd";
      case 3 -> "3rd";
      default -> house + "th";
    };
  }

  private static String planetNameHi(String englishLord) {
    if (englishLord == null) {
      return "—";
    }
    return switch (englishLord.toLowerCase(Locale.ROOT)) {
      case "sun" -> "सूर्य";
      case "moon" -> "चंद्र";
      case "mars" -> "मंगल";
      case "mercury" -> "बुध";
      case "jupiter" -> "गुरु";
      case "venus" -> "शुक्र";
      case "saturn" -> "शनि";
      default -> englishLord;
    };
  }
}
