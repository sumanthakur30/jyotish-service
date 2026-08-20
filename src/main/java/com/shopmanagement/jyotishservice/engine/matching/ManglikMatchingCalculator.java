package com.shopmanagement.jyotishservice.engine.matching;

/** Manglik contribution for {@link MatchingRegistry}. */
public final class ManglikMatchingCalculator implements MatchingCalculator {

  public static final ManglikMatchingCalculator INSTANCE = new ManglikMatchingCalculator();

  private ManglikMatchingCalculator() {}

  @Override
  public MatchingSystemCode system() {
    return MatchingSystemCode.MANGLIK;
  }

  @Override
  public void contribute(MatchingPerson personA, MatchingPerson personB, MatchingReportBuilder builder) {
    builder.manglik(ManglikAnalyzer.assess(personA), ManglikAnalyzer.assess(personB));
  }
}
