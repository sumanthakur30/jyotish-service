package com.shopmanagement.jyotishservice.engine.matching;

import java.util.List;

/**
 * Transparent Manglik assessment from Mars whole-sign house. Cancellation rules are not applied —
 * see {@link #cancellationsComingSoon()}.
 */
public record ManglikAssessment(
    ManglikStatus status,
    int marsHouse,
    int marsSignIndex,
    String marsSignName,
    List<Integer> relevantHouses,
    String reasoning,
    boolean cancellationsComingSoon,
    String cancellationsNote) {

  public boolean present() {
    return status == ManglikStatus.PRESENT;
  }
}
