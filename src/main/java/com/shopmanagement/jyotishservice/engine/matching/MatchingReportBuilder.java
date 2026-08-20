package com.shopmanagement.jyotishservice.engine.matching;

import java.util.ArrayList;
import java.util.List;

/** Mutable builder used by {@link MatchingCalculator} contributions. */
public final class MatchingReportBuilder {

  private MatchingPerson personA;
  private MatchingPerson personB;
  private final List<KootaScore> kootas = new ArrayList<>();
  private ManglikAssessment manglikA;
  private ManglikAssessment manglikB;
  private String notes = "";

  public MatchingReportBuilder persons(MatchingPerson a, MatchingPerson b) {
    this.personA = a;
    this.personB = b;
    return this;
  }

  public MatchingReportBuilder addKootas(List<KootaScore> scores) {
    this.kootas.addAll(scores);
    return this;
  }

  public MatchingReportBuilder manglik(ManglikAssessment a, ManglikAssessment b) {
    this.manglikA = a;
    this.manglikB = b;
    return this;
  }

  public MatchingReportBuilder notes(String notes) {
    this.notes = notes == null ? "" : notes;
    return this;
  }

  public MatchingReport build(String engineVersion) {
    int total = AshtaKootaCalculator.totalObtained(kootas);
    int max = KootaCode.totalMax();
    double pct = max == 0 ? 0.0 : (100.0 * total) / max;
    String summary =
        "Traditional compatibility indicators suggest "
            + total
            + " of "
            + max
            + " Ashta Koota points ("
            + String.format(java.util.Locale.ROOT, "%.1f", pct)
            + "%). Manglik status is reported separately as a placement flag.";
    return new MatchingReport(
        engineVersion,
        personA,
        personB,
        List.copyOf(kootas),
        total,
        max,
        pct,
        manglikA,
        manglikB,
        summary,
        notes,
        MatchingReport.DISCLAIMER);
  }
}
