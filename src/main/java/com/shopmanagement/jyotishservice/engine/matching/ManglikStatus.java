package com.shopmanagement.jyotishservice.engine.matching;

/** Manglik presence from Mars house placement (cancellations not applied in V1.4). */
public enum ManglikStatus {
  PRESENT("PRESENT", "Present"),
  ABSENT("ABSENT", "Absent");

  private final String code;
  private final String label;

  ManglikStatus(String code, String label) {
    this.code = code;
    this.label = label;
  }

  public String code() {
    return code;
  }

  public String label() {
    return label;
  }
}
