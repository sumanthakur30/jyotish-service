package com.shopmanagement.jyotishservice.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopmanagement.jyotishservice.config.JyotishEngineProperties;
import com.shopmanagement.jyotishservice.config.JyotishEntitlementProperties;

@RestController
@RequestMapping("/api/v1/jyotish")
public class JyotishStatusController {

  private final JyotishEntitlementProperties entitlementProperties;
  private final JyotishEngineProperties engineProperties;

  public JyotishStatusController(
      JyotishEntitlementProperties entitlementProperties, JyotishEngineProperties engineProperties) {
    this.entitlementProperties = entitlementProperties;
    this.engineProperties = engineProperties;
  }

  @GetMapping("/status")
  public StatusResponse status() {
    return new StatusResponse(
        "jyotish-service",
        "4-dasha",
        engineProperties.getVersion(),
        entitlementProperties.isEnabled(),
        entitlementProperties.getFlag());
  }

  public record StatusResponse(
      String service,
      String phase,
      String engineVersion,
      boolean entitlementEnabled,
      String entitlementFlag) {}
}
