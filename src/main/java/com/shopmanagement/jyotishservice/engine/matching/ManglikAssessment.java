package com.shopmanagement.jyotishservice.engine.matching;

import java.util.ArrayList;
import java.util.List;

/**
 * Transparent Manglik assessment from Mars whole-sign house plus documented cancellation rules
 * (see {@code docs/MANGLIK-CANCELLATIONS.md}).
 */
public record ManglikAssessment(
    ManglikStatus status,
    int marsHouse,
    int marsSignIndex,
    String marsSignName,
    List<Integer> relevantHouses,
    String reasoning,
    boolean cancelled,
    List<CancellationRule> appliedCancellations,
    boolean cancellationsComingSoon,
    String cancellationsNote) {

  public ManglikAssessment {
    relevantHouses = List.copyOf(relevantHouses);
    appliedCancellations =
        appliedCancellations == null ? List.of() : List.copyOf(appliedCancellations);
  }

  /** Effective dosha flag (false when ABSENT or CANCELLED). */
  public boolean present() {
    return status == ManglikStatus.PRESENT;
  }

  /** True when Mars sits in a classical Manglik house (before / regardless of cancel). */
  public boolean placementManglik() {
    return ManglikAnalyzer.RELEVANT_HOUSES.contains(marsHouse);
  }

  public ManglikAssessment withAdditionalCancellation(CancellationRule rule) {
    List<CancellationRule> next = new ArrayList<>(appliedCancellations);
    next.add(rule);
    String note =
        cancellationsNote
            + (cancellationsNote.isBlank() ? "" : " ")
            + rule.code()
            + " applied.";
    return new ManglikAssessment(
        ManglikStatus.CANCELLED,
        marsHouse,
        marsSignIndex,
        marsSignName,
        relevantHouses,
        reasoning,
        true,
        next,
        false,
        note);
  }
}
