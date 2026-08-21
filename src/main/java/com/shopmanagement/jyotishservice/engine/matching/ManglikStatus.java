package com.shopmanagement.jyotishservice.engine.matching;

/** Manglik presence from Mars house placement; CANCELLED when exception rules apply. */
public enum ManglikStatus {
  PRESENT("PRESENT", "Present"),
  ABSENT("ABSENT", "Absent"),
  CANCELLED("CANCELLED", "Cancelled");

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
