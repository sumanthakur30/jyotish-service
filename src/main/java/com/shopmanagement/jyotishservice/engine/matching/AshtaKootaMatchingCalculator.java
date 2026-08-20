package com.shopmanagement.jyotishservice.engine.matching;

/** Ashta Koota contribution for {@link MatchingRegistry}. */
public final class AshtaKootaMatchingCalculator implements MatchingCalculator {

  public static final AshtaKootaMatchingCalculator INSTANCE = new AshtaKootaMatchingCalculator();

  private AshtaKootaMatchingCalculator() {}

  @Override
  public MatchingSystemCode system() {
    return MatchingSystemCode.ASHTA_KOOTA;
  }

  @Override
  public void contribute(MatchingPerson personA, MatchingPerson personB, MatchingReportBuilder builder) {
    builder.addKootas(AshtaKootaCalculator.score(personA, personB));
  }
}
