package com.shopmanagement.jyotishservice.engine.matching;

/**
 * Pluggable matching calculator. Register in {@link MatchingRegistry}; no Spring / UI imports.
 *
 * <p>Primary path today: {@link MatchingRegistry#compute(MatchingPerson, MatchingPerson, String)}
 * runs Ashta Koota + Manglik together. Individual system calculators remain for catalog / future
 * split APIs.
 */
public interface MatchingCalculator {

  MatchingSystemCode system();

  /** Contribute to a full report; implementations may only fill their slice. */
  void contribute(MatchingPerson personA, MatchingPerson personB, MatchingReportBuilder builder);
}
