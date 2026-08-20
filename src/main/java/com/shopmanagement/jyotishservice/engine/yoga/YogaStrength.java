package com.shopmanagement.jyotishservice.engine.yoga;

/**
 * Optional strength labels — only set when a detector defines an explicit rule. Detectors that do
 * not grade strength leave this null (API omits / UI shows Coming Soon for strength).
 */
public enum YogaStrength {
  FULL("FULL", "Full"),
  PARTIAL("PARTIAL", "Partial"),
  MODERATE("MODERATE", "Moderate");

  private final String code;
  private final String displayName;

  YogaStrength(String code, String displayName) {
    this.code = code;
    this.displayName = displayName;
  }

  public String code() {
    return code;
  }

  public String displayName() {
    return displayName;
  }
}
