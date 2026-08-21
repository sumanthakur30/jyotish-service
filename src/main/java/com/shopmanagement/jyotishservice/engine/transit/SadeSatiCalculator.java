package com.shopmanagement.jyotishservice.engine.transit;

import java.util.Objects;

import com.shopmanagement.jyotishservice.engine.astro.ZodiacCatalog;
import com.shopmanagement.jyotishservice.engine.model.Planet;
import com.shopmanagement.jyotishservice.engine.model.PlanetPosition;

/**
 * Sade Sati from natal Moon sign vs transit Saturn sign. House distance of Saturn from Moon:
 * 12 = rising, 1 = peak, 2 = setting; otherwise {@link Phase#NOT_IN_SADE_SATI}.
 */
public final class SadeSatiCalculator {

  private SadeSatiCalculator() {}

  public static SadeSatiAnalysis analyze(int natalMoonSignIndex, int transitSaturnSignIndex) {
    int moon = Math.floorMod(natalMoonSignIndex, 12);
    int saturn = Math.floorMod(transitSaturnSignIndex, 12);
    // Signs from Moon to Saturn (0 = same sign = house 1 from Moon).
    int signsFromMoon = Math.floorMod(saturn - moon, 12);
    // Whole-sign house of Saturn counted from Moon (1 = Moon's sign).
    int houseFromMoon = signsFromMoon + 1;

    Phase phase;
    if (houseFromMoon == 12) {
      phase = Phase.RISING;
    } else if (houseFromMoon == 1) {
      phase = Phase.PEAK;
    } else if (houseFromMoon == 2) {
      phase = Phase.SETTING;
    } else {
      phase = Phase.NOT_IN_SADE_SATI;
    }

    return new SadeSatiAnalysis(
        phase,
        phase.code(),
        phase.label(),
        moon,
        ZodiacCatalog.signName(moon),
        saturn,
        ZodiacCatalog.signName(saturn),
        signsFromMoon,
        houseFromMoon,
        phase != Phase.NOT_IN_SADE_SATI,
        "Saturn house from natal Moon: 12=rising, 1=peak, 2=setting. Descriptive only.");
  }

  public static SadeSatiAnalysis fromPositions(
      java.util.List<PlanetPosition> natalPlanets, java.util.List<PlanetPosition> transitPlanets) {
    Objects.requireNonNull(natalPlanets, "natalPlanets");
    Objects.requireNonNull(transitPlanets, "transitPlanets");
    int moonSign =
        natalPlanets.stream()
            .filter(p -> p.planet() == Planet.MOON)
            .map(PlanetPosition::signIndex)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Natal Moon required for Sade Sati"));
    int saturnSign =
        transitPlanets.stream()
            .filter(p -> p.planet() == Planet.SATURN)
            .map(PlanetPosition::signIndex)
            .findFirst()
            .orElseThrow(
                () -> new IllegalArgumentException("Transit Saturn required for Sade Sati"));
    return analyze(moonSign, saturnSign);
  }

  public enum Phase {
    RISING("RISING", "Rising (Saturn in 12th from Moon)"),
    PEAK("PEAK", "Peak (Saturn over Moon)"),
    SETTING("SETTING", "Setting (Saturn in 2nd from Moon)"),
    NOT_IN_SADE_SATI("NOT_IN_SADE_SATI", "Not in Sade Sati");

    private final String code;
    private final String label;

    Phase(String code, String label) {
      this.code = code;
      this.label = label;
    }

    public String code() {
      return code;
    }

    public String label() {
      return label;
    }
  }

  public record SadeSatiAnalysis(
      Phase phase,
      String phaseCode,
      String phaseLabel,
      int natalMoonSignIndex,
      String natalMoonSignName,
      int transitSaturnSignIndex,
      String transitSaturnSignName,
      int signsFromMoon,
      int houseFromMoon,
      boolean inSadeSati,
      String notes) {}
}
