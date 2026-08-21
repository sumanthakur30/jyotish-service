package com.shopmanagement.jyotishservice.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopmanagement.jyotishservice.config.JyotishEngineProperties;
import com.shopmanagement.jyotishservice.config.JyotishEntitlementProperties;
import com.shopmanagement.jyotishservice.config.JyotishEphemerisProperties;
import com.shopmanagement.jyotishservice.engine.ephemeris.EphemerisProvider;
import com.shopmanagement.jyotishservice.engine.ephemeris.EphemerisProviders;

@RestController
@RequestMapping("/api/v1/jyotish")
public class JyotishStatusController {

  private final JyotishEntitlementProperties entitlementProperties;
  private final JyotishEngineProperties engineProperties;
  private final JyotishEphemerisProperties ephemerisProperties;
  private final EphemerisProvider ephemerisProvider;

  public JyotishStatusController(
      JyotishEntitlementProperties entitlementProperties,
      JyotishEngineProperties engineProperties,
      JyotishEphemerisProperties ephemerisProperties,
      EphemerisProvider ephemerisProvider) {
    this.entitlementProperties = entitlementProperties;
    this.engineProperties = engineProperties;
    this.ephemerisProperties = ephemerisProperties;
    this.ephemerisProvider = ephemerisProvider;
  }

  @GetMapping("/status")
  public StatusResponse status() {
    String active = ephemerisProvider.code();
    return new StatusResponse(
        "jyotish-service",
        "panchang+swiss-ephemeris",
        engineProperties.getVersion(),
        entitlementProperties.isEnabled(),
        entitlementProperties.getFlag(),
        active,
        EphemerisProviders.normalize(ephemerisProperties.getProvider()),
        EphemerisProviders.swissJarPresent(ephemerisProperties.getSwissJarPath()));
  }

  public record StatusResponse(
      String service,
      String phase,
      String engineVersion,
      boolean entitlementEnabled,
      String entitlementFlag,
      String ephemerisProvider,
      String ephemerisProviderConfigured,
      boolean swissJarConfigured) {}
}
