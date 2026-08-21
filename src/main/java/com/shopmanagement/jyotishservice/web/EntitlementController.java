package com.shopmanagement.jyotishservice.web;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopmanagement.jyotishservice.entitlement.JyotishEntitlementGuard;

@RestController
@RequestMapping("/api/v1/jyotish")
public class EntitlementController {

  private final JyotishEntitlementGuard entitlementGuard;

  public EntitlementController(JyotishEntitlementGuard entitlementGuard) {
    this.entitlementGuard = entitlementGuard;
  }

  /**
   * Feature snapshot for jyotish-ui tab gating. Does not require FEATURE_JYOTISH itself so the UI can
   * show an upgrade message when the master flag is off.
   */
  @GetMapping("/entitlements")
  public Map<String, Object> entitlements() {
    return entitlementGuard.entitlementsSnapshot();
  }
}
