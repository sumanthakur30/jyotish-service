package com.shopmanagement.jyotishservice.config;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jyotish.website-sync")
public class JyotishWebsiteSyncProperties {

  /** Shared secret from JhaAstro (`JYOTISH_API_KEY` / `JYOTISH_LOCAL_API_KEY`). */
  private String apiKey = "";

  /** Public org code → shop tenant id (e.g. JHAASTRO → 8801). */
  private Map<String, String> orgs = new LinkedHashMap<>();

  public String getApiKey() {
    return apiKey;
  }

  public void setApiKey(String apiKey) {
    this.apiKey = apiKey == null ? "" : apiKey;
  }

  public Map<String, String> getOrgs() {
    return orgs;
  }

  public void setOrgs(Map<String, String> orgs) {
    this.orgs = orgs == null ? new LinkedHashMap<>() : orgs;
  }
}
