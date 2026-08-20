package com.shopmanagement.jyotishservice.engine.yoga;

import java.util.List;
import java.util.Objects;

/** Full yoga evaluation for one kundali (implemented detectors only). */
public final class YogaReport {

  private final String engineVersion;
  private final List<YogaHit> hits;
  private final String notes;

  public YogaReport(String engineVersion, List<YogaHit> hits, String notes) {
    this.engineVersion = Objects.requireNonNull(engineVersion, "engineVersion");
    this.hits = List.copyOf(Objects.requireNonNull(hits, "hits"));
    this.notes = notes == null ? "" : notes;
  }

  public String engineVersion() {
    return engineVersion;
  }

  public List<YogaHit> hits() {
    return hits;
  }

  public String notes() {
    return notes;
  }
}
