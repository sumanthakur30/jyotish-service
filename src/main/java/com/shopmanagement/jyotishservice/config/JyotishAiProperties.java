package com.shopmanagement.jyotishservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Phase 10 AI assistant. Default provider is HEURISTIC (no remote call). Optional HTTP LLM when
 * {@code jyotish.ai.provider=HTTP}. Entitlement stub flag {@code FEATURE_JYOTISH_AI} is documented
 * for future catalog gating; remote HTTP stays off by default.
 */
@ConfigurationProperties(prefix = "jyotish.ai")
public class JyotishAiProperties {

  /** HEURISTIC (default) or HTTP. */
  private String provider = "HEURISTIC";

  private String httpUrl = "";

  private String modelCode = "HEURISTIC_V1";

  /**
   * Future platform entitlement flag (catalog TBD). Not enforced in Phase 10 MVP — comment/property
   * only so SaaS can wire FEATURE_JYOTISH_AI later.
   */
  private String entitlementFlag = "FEATURE_JYOTISH_AI";

  public String getProvider() {
    return provider;
  }

  public void setProvider(String provider) {
    this.provider = provider;
  }

  public String getHttpUrl() {
    return httpUrl;
  }

  public void setHttpUrl(String httpUrl) {
    this.httpUrl = httpUrl;
  }

  public String getModelCode() {
    return modelCode;
  }

  public void setModelCode(String modelCode) {
    this.modelCode = modelCode;
  }

  public String getEntitlementFlag() {
    return entitlementFlag;
  }

  public void setEntitlementFlag(String entitlementFlag) {
    this.entitlementFlag = entitlementFlag;
  }
}
