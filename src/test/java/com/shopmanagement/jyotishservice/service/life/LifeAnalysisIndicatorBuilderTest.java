package com.shopmanagement.jyotishservice.service.life;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.shopmanagement.jyotishservice.api.LifeAnalysisApi.CategoryCardFacts;
import com.shopmanagement.jyotishservice.engine.life.LifeCategory;
import com.shopmanagement.jyotishservice.persistence.entity.HousePositionEntity;
import com.shopmanagement.jyotishservice.persistence.entity.PlanetaryPositionEntity;

class LifeAnalysisIndicatorBuilderTest {

  private final LifeAnalysisIndicatorBuilder builder = new LifeAnalysisIndicatorBuilder();

  @Test
  void marriageCard_usesSeventhHouseAndVenus_notCareerHouses() {
    List<HousePositionEntity> houses =
        List.of(house(7, "Libra"), house(10, "Virgo"), house(4, "Sagittarius"));
    List<PlanetaryPositionEntity> planets =
        List.of(planet("VENUS", "Leo", 12), planet("SUN", "Cancer", 11), planet("JUPITER", "Aries", 8));

    CategoryCardFacts marriage = builder.preview(LifeCategory.MARRIAGE, houses, planets, "VENUS", "MOON");
    CategoryCardFacts career = builder.preview(LifeCategory.CAREER, houses, planets, "VENUS", "MOON");

    assertTrue(marriage.meaningHi().contains("विवाह"));
    assertTrue(career.meaningHi().contains("करियर"));
    assertNotEquals(marriage.meaningEn(), career.meaningEn());
    assertTrue(marriage.houseLinesHi().stream().anyMatch(l -> l.contains("7 भाव")));
    assertTrue(marriage.grahasLineHi().contains("शुक्र"));
    assertFalse(marriage.summaryHi().contains("10 भाव"));

    assertTrue(career.houseLinesHi().stream().anyMatch(l -> l.contains("10 भाव")));
    assertTrue(career.grahasLineHi().contains("सूर्य"));
  }

  @Test
  void preview_skipsOuterPlanetsInHouseOccupants() {
    List<HousePositionEntity> houses = List.of(house(7, "Libra"));
    List<PlanetaryPositionEntity> planets =
        List.of(planet("VENUS", "Leo", 12), planet("URANUS", "Libra", 7), planet("PLUTO", "Virgo", 1));

    CategoryCardFacts marriage = builder.preview(LifeCategory.MARRIAGE, houses, planets, null, null);
    String joined = String.join(" ", marriage.houseLinesEn());
    assertFalse(joined.toUpperCase().contains("URANUS"));
    assertFalse(joined.toUpperCase().contains("PLUTO"));
  }

  private static HousePositionEntity house(int n, String sign) {
    HousePositionEntity h = new HousePositionEntity();
    h.setHouse((short) n);
    h.setSignName(sign);
    return h;
  }

  private static PlanetaryPositionEntity planet(String code, String sign, int house) {
    PlanetaryPositionEntity p = new PlanetaryPositionEntity();
    p.setPlanetCode(code);
    p.setSignName(sign);
    p.setHouse((short) house);
    return p;
  }
}
