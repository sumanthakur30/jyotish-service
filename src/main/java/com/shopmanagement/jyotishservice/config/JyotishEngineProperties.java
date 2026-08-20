package com.shopmanagement.jyotishservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jyotish.engine")
public class JyotishEngineProperties {

  /** Stamped on kundali snapshots when calculations run (Phase 2+). */
  private String version = "V1.0";

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }
}
