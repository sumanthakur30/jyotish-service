package com.shopmanagement.jyotishservice.service.life;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.shopmanagement.jyotishservice.api.LifeAnalysisApi.CategoryCardFacts;
import com.shopmanagement.jyotishservice.api.LifeAnalysisApi.IndicatorItem;
import com.shopmanagement.jyotishservice.engine.explain.LifeAreaPlainCopy;
import com.shopmanagement.jyotishservice.engine.explain.SimpleLabels;
import com.shopmanagement.jyotishservice.engine.life.LifeCategory;
import com.shopmanagement.jyotishservice.persistence.entity.HousePositionEntity;
import com.shopmanagement.jyotishservice.persistence.entity.PlanetaryPositionEntity;
import com.shopmanagement.jyotishservice.persistence.entity.YogaResultEntity;

/**
 * Builds read-only Kundli indicators for Life Analysis. Never invents predictions — facts only.
 */
@Component
public class LifeAnalysisIndicatorBuilder {

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

  public List<IndicatorItem> build(
      LifeCategory category,
      List<HousePositionEntity> houses,
      List<PlanetaryPositionEntity> planets,
      List<YogaResultEntity> yogas,
      String currentDashaSummary,
      String currentGocharSummary) {
    List<IndicatorItem> items = new ArrayList<>();
    Map<Integer, HousePositionEntity> byHouse = new LinkedHashMap<>();
    for (HousePositionEntity h : houses) {
      byHouse.put((int) h.getHouse(), h);
    }

    for (int house : category.indicatorHouses()) {
      HousePositionEntity h = byHouse.get(house);
      if (h == null) {
        continue;
      }
      String sign = h.getSignName();
      String lord = SIGN_LORDS.getOrDefault(sign, "—");
      items.add(
          new IndicatorItem(
              "H" + house,
              house + "th house",
              sign + " · lord " + lord,
              "D1 WHOLE_SIGN"));
      String occupants =
          planets.stream()
              .filter(p -> p.getHouse() == house)
              .map(p -> p.getPlanetCode() + (p.isRetrograde() ? "ᵣ" : ""))
              .collect(Collectors.joining(", "));
      items.add(
          new IndicatorItem(
              "H" + house + "_OCC",
              "Planets in " + house,
              occupants.isBlank() ? "None" : occupants,
              "D1"));
    }

    for (String code : relevantPlanetCodes(category)) {
      addPlanetFact(planets, items, code);
    }

    if (yogas != null && !yogas.isEmpty()) {
      String yogaLine =
          yogas.stream()
              .filter(YogaResultEntity::isPresent)
              .limit(8)
              .map(
                  y ->
                      y.getYogaCode()
                          + (y.getStrengthCode() != null ? " (" + y.getStrengthCode() + ")" : ""))
              .collect(Collectors.joining(", "));
      if (!yogaLine.isBlank()) {
        items.add(new IndicatorItem("YOGAS", "Yogas (present)", yogaLine, "Yoga engine"));
      }
    }

    if (currentDashaSummary != null && !currentDashaSummary.isBlank()) {
      items.add(new IndicatorItem("DASHA", "Current Dasha", currentDashaSummary, "Vimshottari"));
    }
    if (currentGocharSummary != null && !currentGocharSummary.isBlank()) {
      items.add(new IndicatorItem("GOCHAR", "Current Gochar", currentGocharSummary, "Transit"));
    }

    if (category == LifeCategory.HEALTH) {
      items.add(
          new IndicatorItem(
              "HEALTH_NOTE",
              "Disclaimer",
              "Traditional Jyotish indicators only — not medical advice",
              "Policy"));
    }

    return items;
  }

  /**
   * One-card D1 snapshot for the dashboard. Plain meaning + house facts — never Phaladesh scores.
   */
  public CategoryCardFacts preview(
      LifeCategory category,
      List<HousePositionEntity> houses,
      List<PlanetaryPositionEntity> planets,
      String mahaLordCode,
      String antarLordCode) {
    List<HousePositionEntity> houseRows = houses != null ? houses : List.of();
    Map<Integer, HousePositionEntity> byHouse = new LinkedHashMap<>();
    for (HousePositionEntity h : houseRows) {
      byHouse.put((int) h.getHouse(), h);
    }
    List<PlanetaryPositionEntity> chartPlanets =
        planets == null
            ? List.of()
            : planets.stream().filter(p -> isChartGraha(p.getPlanetCode())).toList();

    List<String> houseEn = new ArrayList<>();
    List<String> houseHi = new ArrayList<>();
    List<Integer> occupied = new ArrayList<>();
    for (int house : category.indicatorHouses()) {
      HousePositionEntity h = byHouse.get(house);
      if (h == null) {
        continue;
      }
      String sign = h.getSignName();
      String lord = SIGN_LORDS.getOrDefault(sign, "—");
      String occEn = occupantsEn(chartPlanets, house);
      String occHi = occupantsHi(chartPlanets, house);
      if (!occEn.isBlank()) {
        occupied.add(house);
      }
      houseEn.add(
          houseOrdinal(house)
              + " "
              + sign
              + " · "
              + lord
              + (occEn.isBlank() ? " · empty" : " · " + occEn));
      houseHi.add(
          house
              + " भाव "
              + SimpleLabels.signHi(sign)
              + " · "
              + planetHi(lord)
              + (occHi.isBlank() ? " · खाली" : " · " + occHi));
    }

    List<String> grahaEn = new ArrayList<>();
    List<String> grahaHi = new ArrayList<>();
    for (String code : relevantPlanetCodes(category)) {
      chartPlanets.stream()
          .filter(p -> code.equalsIgnoreCase(p.getPlanetCode()))
          .findFirst()
          .ifPresent(
              p -> {
                grahaEn.add(
                    planetEn(code)
                        + " "
                        + p.getSignName()
                        + " H"
                        + p.getHouse()
                        + (p.isRetrograde() ? " R" : ""));
                grahaHi.add(
                    planetHi(code)
                        + " "
                        + SimpleLabels.signHi(p.getSignName())
                        + " भाव "
                        + p.getHouse()
                        + (p.isRetrograde() ? " वक्री" : ""));
              });
    }

    Integer mahaHouse = houseOf(chartPlanets, mahaLordCode);
    Integer antarHouse = houseOf(chartPlanets, antarLordCode);
    var copy =
        LifeAreaPlainCopy.of(category, occupied, mahaLordCode, mahaHouse, antarLordCode, antarHouse);

    String gEn = grahaEn.isEmpty() ? null : String.join(" · ", grahaEn);
    String gHi = grahaHi.isEmpty() ? null : String.join(" · ", grahaHi);
    String summaryEn =
        houseEn.isEmpty()
            ? "Chart houses for this topic are not stored yet."
            : category.labelEn() + ": " + String.join("; ", houseEn);
    String summaryHi =
        houseHi.isEmpty()
            ? "इस विषय के भाव अभी संग्रहीत नहीं हैं।"
            : category.labelHi() + ": " + String.join("; ", houseHi);
    return new CategoryCardFacts(
        houseEn,
        houseHi,
        gEn,
        gHi,
        summaryEn,
        summaryHi,
        copy.meaningEn(),
        copy.meaningHi(),
        copy.emphasisLevel(),
        copy.emphasisLabelEn(),
        copy.emphasisLabelHi());
  }

  private static Integer houseOf(List<PlanetaryPositionEntity> planets, String code) {
    if (code == null || code.isBlank()) {
      return null;
    }
    return planets.stream()
        .filter(p -> code.equalsIgnoreCase(p.getPlanetCode()))
        .map(p -> (int) p.getHouse())
        .findFirst()
        .orElse(null);
  }

  private static void addPlanetFact(
      List<PlanetaryPositionEntity> planets, List<IndicatorItem> items, String code) {
    planets.stream()
        .filter(p -> code.equalsIgnoreCase(p.getPlanetCode()))
        .findFirst()
        .ifPresent(
            p ->
                items.add(
                    new IndicatorItem(
                        code,
                        planetEn(code),
                        p.getSignName()
                            + " "
                            + fmt(p.getDegreeInSign())
                            + " · H"
                            + p.getHouse()
                            + (p.isRetrograde() ? " R" : ""),
                        "D1")));
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

  private static boolean isChartGraha(String code) {
    if (code == null) {
      return false;
    }
    return switch (code.toUpperCase(Locale.ROOT)) {
      case "URANUS", "NEPTUNE", "PLUTO" -> false;
      default -> true;
    };
  }

  private static String occupantsEn(List<PlanetaryPositionEntity> planets, int house) {
    return planets.stream()
        .filter(p -> p.getHouse() == house)
        .map(p -> planetEn(p.getPlanetCode()) + (p.isRetrograde() ? "ᵣ" : ""))
        .collect(Collectors.joining(", "));
  }

  private static String occupantsHi(List<PlanetaryPositionEntity> planets, int house) {
    return planets.stream()
        .filter(p -> p.getHouse() == house)
        .map(p -> planetHi(p.getPlanetCode()) + (p.isRetrograde() ? "ᵣ" : ""))
        .collect(Collectors.joining(", "));
  }

  private static String houseOrdinal(int house) {
    return switch (house) {
      case 1 -> "1st";
      case 2 -> "2nd";
      case 3 -> "3rd";
      default -> house + "th";
    };
  }

  private static String planetEn(String codeOrName) {
    if (codeOrName == null) {
      return "—";
    }
    return switch (codeOrName.trim().toUpperCase(Locale.ROOT)) {
      case "SUN", "SURYA" -> "Sun";
      case "MOON", "CHANDRA" -> "Moon";
      case "MARS", "MANGAL" -> "Mars";
      case "MERCURY", "BUDHA", "BUDH" -> "Mercury";
      case "JUPITER", "GURU" -> "Jupiter";
      case "VENUS", "SHUKRA" -> "Venus";
      case "SATURN", "SHANI" -> "Saturn";
      case "RAHU" -> "Rahu";
      case "KETU" -> "Ketu";
      case "ASCENDANT", "LAGNA" -> "Lagna";
      default -> codeOrName;
    };
  }

  private static String planetHi(String codeOrName) {
    if (codeOrName == null) {
      return "—";
    }
    return switch (codeOrName.trim().toUpperCase(Locale.ROOT)) {
      case "SUN", "SURYA" -> "सूर्य";
      case "MOON", "CHANDRA" -> "चंद्र";
      case "MARS", "MANGAL" -> "मंगल";
      case "MERCURY", "BUDHA", "BUDH" -> "बुध";
      case "JUPITER", "GURU" -> "गुरु";
      case "VENUS", "SHUKRA" -> "शुक्र";
      case "SATURN", "SHANI" -> "शनि";
      case "RAHU" -> "राहु";
      case "KETU" -> "केतु";
      case "ASCENDANT", "LAGNA" -> "लग्न";
      default -> planetEn(codeOrName);
    };
  }

  private static String fmt(java.math.BigDecimal v) {
    if (v == null) {
      return "—";
    }
    return v.setScale(1, java.math.RoundingMode.HALF_UP).toPlainString() + "°";
  }

  public static String normalizeStatus(String raw) {
    if (raw == null || raw.isBlank()) {
      return "NOT_STARTED";
    }
    String s = raw.trim().toUpperCase(Locale.ROOT).replace(' ', '_');
    return switch (s) {
      case "NOT_STARTED", "IN_PROGRESS", "COMPLETED" -> s;
      default -> "NOT_STARTED";
    };
  }
}
