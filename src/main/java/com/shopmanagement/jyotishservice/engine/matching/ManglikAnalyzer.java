package com.shopmanagement.jyotishservice.engine.matching;

import java.util.ArrayList;
import java.util.List;

import com.shopmanagement.jyotishservice.engine.astro.ZodiacCatalog;

/**
 * Manglik Dosha from Mars whole-sign house. Relevant houses: 1, 2, 4, 7, 8, 12 (common North-Indian
 * set). Single-chart cancellation rules are applied (see {@code docs/MANGLIK-CANCELLATIONS.md}).
 * Mutual Manglik is applied in {@link ManglikMatchingCalculator}.
 */
public final class ManglikAnalyzer {

  /** Houses where Mars is traditionally considered for Manglik assessment. */
  public static final List<Integer> RELEVANT_HOUSES = List.of(1, 2, 4, 7, 8, 12);

  private ManglikAnalyzer() {}

  public static ManglikAssessment assess(MatchingPerson person) {
    int house = person.marsHouse();
    boolean placement = RELEVANT_HOUSES.contains(house);
    List<CancellationRule> applied = new ArrayList<>();

    if (placement) {
      int marsSign = person.marsSignIndex();
      if (marsSign == 0 || marsSign == 7) {
        applied.add(
            new CancellationRule(
                "MARS_OWN_SIGN",
                "Mars in own sign",
                "Mars in Aries or Scorpio cancels Manglik under this rule set."));
      }
      if (marsSign == 9) {
        applied.add(
            new CancellationRule(
                "MARS_EXALTED",
                "Mars exalted",
                "Mars in Capricorn (exaltation) cancels Manglik under this rule set."));
      }
      if (marsSign == 4) {
        applied.add(
            new CancellationRule(
                "MARS_IN_LEO",
                "Mars in Leo",
                "Common optional cancellation when Mars is in Leo."));
      }
      if (person.jupiterSignIndex() == marsSign) {
        applied.add(
            new CancellationRule(
                "JUPITER_WITH_MARS",
                "Jupiter with Mars",
                "Jupiter in the same sign as Mars cancels Manglik under this rule set."));
      }
    }

    boolean cancelled = !applied.isEmpty();
    ManglikStatus status;
    if (!placement) {
      status = ManglikStatus.ABSENT;
    } else if (cancelled) {
      status = ManglikStatus.CANCELLED;
    } else {
      status = ManglikStatus.PRESENT;
    }

    String reasoning =
        placement
            ? "Mars is in whole-sign house "
                + house
                + " ("
                + ZodiacCatalog.signName(person.marsSignIndex())
                + "). Traditional Manglik indicators treat Mars in houses "
                + RELEVANT_HOUSES
                + " as relevant. This is a placement flag only — not a prediction about marriage"
                + " outcomes."
            : "Mars is in whole-sign house "
                + house
                + " ("
                + ZodiacCatalog.signName(person.marsSignIndex())
                + "), outside the classical Manglik set "
                + RELEVANT_HOUSES
                + ". Status recorded as Absent under this rule set.";

    String cancelNote =
        cancelled
            ? "Applied cancellations: "
                + applied.stream().map(CancellationRule::code).toList()
                + ". See docs/MANGLIK-CANCELLATIONS.md."
            : placement
                ? "No single-chart cancellation rules matched. Mutual Manglik may still apply in"
                    + " matching."
                : "Cancellation rules not evaluated (Mars not in Manglik houses).";

    return new ManglikAssessment(
        status,
        house,
        person.marsSignIndex(),
        ZodiacCatalog.signName(person.marsSignIndex()),
        RELEVANT_HOUSES,
        reasoning,
        cancelled,
        applied,
        false,
        cancelNote);
  }
}
