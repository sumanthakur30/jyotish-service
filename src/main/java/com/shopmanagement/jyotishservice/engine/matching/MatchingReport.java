package com.shopmanagement.jyotishservice.engine.matching;

import java.util.List;

/** Full matching report: Ashta Koota + per-person Manglik. */
public record MatchingReport(
    String engineVersion,
    MatchingPerson personA,
    MatchingPerson personB,
    List<KootaScore> kootas,
    int totalScore,
    int maxScore,
    double percentage,
    ManglikAssessment manglikA,
    ManglikAssessment manglikB,
    String summary,
    String notes,
    String disclaimer) {

  public static final String DISCLAIMER =
      "Traditional compatibility indicators suggest patterns for reflection with a qualified"
          + " practitioner. They do not determine whether a relationship or marriage will succeed or"
          + " fail.";
}
