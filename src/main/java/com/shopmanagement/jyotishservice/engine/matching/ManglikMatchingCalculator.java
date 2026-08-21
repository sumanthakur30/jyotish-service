package com.shopmanagement.jyotishservice.engine.matching;

/** Manglik contribution for {@link MatchingRegistry}. Applies mutual cancellation when both placed. */
public final class ManglikMatchingCalculator implements MatchingCalculator {

  public static final ManglikMatchingCalculator INSTANCE = new ManglikMatchingCalculator();

  private static final CancellationRule MUTUAL =
      new CancellationRule(
          "MUTUAL_MANGLIK",
          "Mutual Manglik",
          "Both charts have Manglik placement — cancels for matching purpose.");

  private ManglikMatchingCalculator() {}

  @Override
  public MatchingSystemCode system() {
    return MatchingSystemCode.MANGLIK;
  }

  @Override
  public void contribute(MatchingPerson personA, MatchingPerson personB, MatchingReportBuilder builder) {
    ManglikAssessment a = ManglikAnalyzer.assess(personA);
    ManglikAssessment b = ManglikAnalyzer.assess(personB);
    if (a.placementManglik() && b.placementManglik()) {
      a = a.withAdditionalCancellation(MUTUAL);
      b = b.withAdditionalCancellation(MUTUAL);
    }
    builder.manglik(a, b);
  }
}
