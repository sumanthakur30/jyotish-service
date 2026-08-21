package com.shopmanagement.jyotishservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jyotish.entitlement")
public class JyotishEntitlementProperties {

  /** When false (local default), feature flags are not checked. */
  private boolean enabled = false;

  private String baseUrl = "http://localhost:8182";

  private String flag = "FEATURE_JYOTISH";

  private String matchingFlag = "FEATURE_JYOTISH_MATCHING";

  private String reportsFlag = "FEATURE_JYOTISH_REPORTS";

  private String aiFlag = "FEATURE_JYOTISH_AI";

  /** If subscription-service is unreachable, allow request when true. */
  private boolean failOpen = false;

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public String getFlag() {
    return flag;
  }

  public void setFlag(String flag) {
    this.flag = flag;
  }

  public String getMatchingFlag() {
    return matchingFlag;
  }

  public void setMatchingFlag(String matchingFlag) {
    this.matchingFlag = matchingFlag;
  }

  public String getReportsFlag() {
    return reportsFlag;
  }

  public void setReportsFlag(String reportsFlag) {
    this.reportsFlag = reportsFlag;
  }

  public String getAiFlag() {
    return aiFlag;
  }

  public void setAiFlag(String aiFlag) {
    this.aiFlag = aiFlag;
  }

  public boolean isFailOpen() {
    return failOpen;
  }

  public void setFailOpen(boolean failOpen) {
    this.failOpen = failOpen;
  }
}
