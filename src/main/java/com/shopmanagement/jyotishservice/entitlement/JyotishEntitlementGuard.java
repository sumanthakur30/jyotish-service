package com.shopmanagement.jyotishservice.entitlement;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.shopmanagement.jyotishservice.config.JyotishEntitlementProperties;
import com.shopmanagement.jyotishservice.support.TenantIds;

@Component
public class JyotishEntitlementGuard {

  private final JyotishEntitlementProperties properties;
  private final SubscriptionEntitlementClient client;

  public JyotishEntitlementGuard(
      JyotishEntitlementProperties properties, SubscriptionEntitlementClient client) {
    this.properties = properties;
    this.client = client;
  }

  /** Master gate — FEATURE_JYOTISH. */
  public void requireJyotishAccess() {
    requireFlag(properties.getFlag(), "assign a Jyotish plan (jyotish-starter or higher)");
  }

  /** Matching APIs — FEATURE_JYOTISH + FEATURE_JYOTISH_MATCHING. */
  public void requireMatchingAccess() {
    requireJyotishAccess();
    requireSubFlag(
        properties.getMatchingFlag(), "assign jyotish-professional or higher for matching");
  }

  /** Report APIs — FEATURE_JYOTISH + FEATURE_JYOTISH_REPORTS. */
  public void requireReportsAccess() {
    requireJyotishAccess();
    requireSubFlag(properties.getReportsFlag(), "assign jyotish-professional or higher for reports");
  }

  /** AI ask — FEATURE_JYOTISH + FEATURE_JYOTISH_AI. */
  public void requireAiAccess() {
    requireJyotishAccess();
    requireSubFlag(properties.getAiFlag(), "assign jyotish-enterprise for Jyotish AI");
  }

  /**
   * Snapshot for jyotish-ui tab gating. When entitlement checks are off (local), all gated features
   * report enabled=true so the pilot UI stays fully usable.
   */
  public Map<String, Object> entitlementsSnapshot() {
    boolean checksOn = properties.isEnabled();
    Map<String, Boolean> features = new LinkedHashMap<>();
    String tenantId = null;
    if (checksOn) {
      tenantId = TenantIds.require();
    }
    putFeature(features, properties.getFlag(), tenantId, checksOn);
    putFeature(features, properties.getMatchingFlag(), tenantId, checksOn);
    putFeature(features, properties.getReportsFlag(), tenantId, checksOn);
    putFeature(features, properties.getAiFlag(), tenantId, checksOn);

    Map<String, Boolean> modules = new LinkedHashMap<>();
    boolean core = Boolean.TRUE.equals(features.get(properties.getFlag()));
    modules.put("kundali", core);
    modules.put("transit", core);
    modules.put("matching", Boolean.TRUE.equals(features.get(properties.getMatchingFlag())));
    modules.put("reports", Boolean.TRUE.equals(features.get(properties.getReportsFlag())));
    modules.put("ai", Boolean.TRUE.equals(features.get(properties.getAiFlag())));
    modules.put("profiles", true);
    modules.put("clients", true);
    modules.put("appointments", true);

    Map<String, Object> out = new LinkedHashMap<>();
    out.put("checksEnabled", checksOn);
    out.put("features", features);
    out.put("modules", modules);
    return out;
  }

  /** Whether the master FEATURE_JYOTISH flag is on for the current tenant (when checks enabled). */
  public boolean hasJyotishFeature() {
    if (!properties.isEnabled()) {
      return true;
    }
    return client.hasFeature(TenantIds.require(), properties.getFlag());
  }

  private void putFeature(
      Map<String, Boolean> features, String flag, String tenantId, boolean checksOn) {
    if (flag == null || flag.isBlank()) {
      return;
    }
    String code = flag.trim();
    if (!checksOn) {
      features.put(code, true);
      return;
    }
    features.put(code, client.hasFeature(tenantId, code));
  }

  private void requireFlag(String flag, String hint) {
    if (!properties.isEnabled()) {
      return;
    }
    String code = flag == null ? "" : flag.trim();
    if (code.isBlank()) {
      return;
    }
    String tenantId = TenantIds.require();
    if (!client.hasFeature(tenantId, code)) {
      throw new JyotishEntitlementException(code + " is not enabled for this tenant — " + hint);
    }
  }

  private void requireSubFlag(String flag, String hint) {
    if (!properties.isEnabled()) {
      return;
    }
    if (flag == null || flag.isBlank()) {
      return;
    }
    requireFlag(flag, hint);
  }
}
