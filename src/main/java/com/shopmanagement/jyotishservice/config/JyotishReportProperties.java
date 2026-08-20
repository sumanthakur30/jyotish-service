package com.shopmanagement.jyotishservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jyotish.reports")
public class JyotishReportProperties {

  /** Local directory for PDF bytes (metadata stored in kundali_report). */
  private String storageDir = "./data/reports";

  public String getStorageDir() {
    return storageDir;
  }

  public void setStorageDir(String storageDir) {
    this.storageDir = storageDir;
  }
}
