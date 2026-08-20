package com.shopmanagement.jyotishservice.engine.matching;

import java.util.List;

import com.shopmanagement.jyotishservice.engine.astro.ZodiacCatalog;

/**
 * Manglik Dosha from Mars whole-sign house. Relevant houses: 1, 2, 4, 7, 8, 12 (common North-Indian
 * set). Cancellation / exception rules are <em>not</em> applied — Coming Soon.
 */
public final class ManglikAnalyzer {

  /** Houses where Mars is traditionally considered for Manglik assessment. */
  public static final List<Integer> RELEVANT_HOUSES = List.of(1, 2, 4, 7, 8, 12);

  private ManglikAnalyzer() {}

  public static ManglikAssessment assess(MatchingPerson person) {
    int house = person.marsHouse();
    boolean present = RELEVANT_HOUSES.contains(house);
    ManglikStatus status = present ? ManglikStatus.PRESENT : ManglikStatus.ABSENT;
    String reasoning =
        present
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

    return new ManglikAssessment(
        status,
        house,
        person.marsSignIndex(),
        ZodiacCatalog.signName(person.marsSignIndex()),
        RELEVANT_HOUSES,
        reasoning,
        true,
        "Manglik cancellation / exception rules (e.g. mutual Manglik, Mars with benefics) are Coming"
            + " Soon and are not applied here.");
  }
}
