package com.shopmanagement.jyotishservice.service.life;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.shopmanagement.jyotishservice.api.LifeAnalysisApi.IndicatorItem;
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

    addPlanetIfRelevant(category, planets, items, "VENUS", "Venus");
    addPlanetIfRelevant(category, planets, items, "JUPITER", "Jupiter");
    addPlanetIfRelevant(category, planets, items, "MERCURY", "Mercury");
    addPlanetIfRelevant(category, planets, items, "MARS", "Mars");
    addPlanetIfRelevant(category, planets, items, "SATURN", "Saturn");
    addPlanetIfRelevant(category, planets, items, "KETU", "Ketu");

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

  private static void addPlanetIfRelevant(
      LifeCategory category,
      List<PlanetaryPositionEntity> planets,
      List<IndicatorItem> items,
      String code,
      String label) {
    boolean want =
        switch (category) {
          case MARRIAGE -> code.equals("VENUS") || code.equals("JUPITER");
          case EDUCATION -> code.equals("MERCURY") || code.equals("JUPITER");
          case PROPERTY -> code.equals("MARS") || code.equals("VENUS");
          case SPIRITUALITY -> code.equals("JUPITER") || code.equals("KETU");
          case HEALTH -> code.equals("MARS") || code.equals("SATURN");
          default -> false;
        };
    if (!want) {
      return;
    }
    planets.stream()
        .filter(p -> code.equalsIgnoreCase(p.getPlanetCode()))
        .findFirst()
        .ifPresent(
            p ->
                items.add(
                    new IndicatorItem(
                        code,
                        label,
                        p.getSignName()
                            + " "
                            + fmt(p.getDegreeInSign())
                            + " · H"
                            + p.getHouse()
                            + (p.isRetrograde() ? " R" : ""),
                        "D1")));
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
