package com.shopmanagement.jyotishservice.engine.matching;

/** One Ashta Koota factor result. */
public record KootaScore(
    KootaCode koota,
    int obtained,
    int maxPoints,
    String explanation,
    String ruleId) {

  public KootaScore {
    if (obtained < 0 || obtained > maxPoints) {
      throw new IllegalArgumentException(
          "Score " + obtained + " out of range for " + koota + " (max " + maxPoints + ")");
    }
  }

  public static KootaScore of(KootaCode koota, int obtained, String explanation, String ruleId) {
    return new KootaScore(koota, obtained, koota.maxPoints(), explanation, ruleId);
  }
}
