package com.shopmanagement.jyotishservice.entitlement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.shopmanagement.jyotishservice.config.JyotishEntitlementProperties;
import com.shopmanagement.jyotishservice.filter.TenantContextFilter;

@ExtendWith(MockitoExtension.class)
class JyotishEntitlementGuardTest {

  @Mock private SubscriptionEntitlementClient client;

  private JyotishEntitlementProperties properties;
  private JyotishEntitlementGuard guard;

  @BeforeEach
  void setUp() {
    properties = new JyotishEntitlementProperties();
    properties.setEnabled(true);
    guard = new JyotishEntitlementGuard(properties, client);
    TenantContextFilter.bindTenantForTests("tenant-demo");
  }

  @AfterEach
  void tearDown() {
    TenantContextFilter.clearTenantForTests();
  }

  @Test
  void requireJyotishAccess_noopWhenEntitlementDisabled() {
    properties.setEnabled(false);
    guard.requireJyotishAccess();
    verify(client, never()).hasFeature(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
  }

  @Test
  void requireJyotishAccess_blocksWhenFlagMissing() {
    when(client.hasFeature("tenant-demo", "FEATURE_JYOTISH")).thenReturn(false);

    assertThatThrownBy(() -> guard.requireJyotishAccess())
        .isInstanceOf(JyotishEntitlementException.class)
        .hasMessageContaining("FEATURE_JYOTISH");
  }

  @Test
  void requireMatchingAccess_blocksWhenSubFlagMissing() {
    when(client.hasFeature("tenant-demo", "FEATURE_JYOTISH")).thenReturn(true);
    when(client.hasFeature("tenant-demo", "FEATURE_JYOTISH_MATCHING")).thenReturn(false);

    assertThatThrownBy(() -> guard.requireMatchingAccess())
        .isInstanceOf(JyotishEntitlementException.class)
        .hasMessageContaining("FEATURE_JYOTISH_MATCHING");
  }

  @Test
  void requireAiAccess_blocksWhenSubFlagMissing() {
    when(client.hasFeature("tenant-demo", "FEATURE_JYOTISH")).thenReturn(true);
    when(client.hasFeature("tenant-demo", "FEATURE_JYOTISH_AI")).thenReturn(false);

    assertThatThrownBy(() -> guard.requireAiAccess())
        .isInstanceOf(JyotishEntitlementException.class)
        .hasMessageContaining("FEATURE_JYOTISH_AI");
  }

  @Test
  void requireReportsAccess_blocksWhenSubFlagMissing() {
    when(client.hasFeature("tenant-demo", "FEATURE_JYOTISH")).thenReturn(true);
    when(client.hasFeature("tenant-demo", "FEATURE_JYOTISH_REPORTS")).thenReturn(false);

    assertThatThrownBy(() -> guard.requireReportsAccess())
        .isInstanceOf(JyotishEntitlementException.class)
        .hasMessageContaining("FEATURE_JYOTISH_REPORTS");
  }

  @Test
  void entitlementsSnapshot_whenChecksOff_allEnabled() {
    properties.setEnabled(false);
    Map<String, Object> snap = guard.entitlementsSnapshot();

    assertThat(snap.get("checksEnabled")).isEqualTo(false);
    @SuppressWarnings("unchecked")
    Map<String, Boolean> modules = (Map<String, Boolean>) snap.get("modules");
    assertThat(modules.get("kundali")).isTrue();
    assertThat(modules.get("matching")).isTrue();
    assertThat(modules.get("reports")).isTrue();
    assertThat(modules.get("ai")).isTrue();
  }

  @Test
  void entitlementsSnapshot_whenChecksOn_reflectsClient() {
    when(client.hasFeature("tenant-demo", "FEATURE_JYOTISH")).thenReturn(true);
    when(client.hasFeature("tenant-demo", "FEATURE_JYOTISH_MATCHING")).thenReturn(false);
    when(client.hasFeature("tenant-demo", "FEATURE_JYOTISH_REPORTS")).thenReturn(true);
    when(client.hasFeature("tenant-demo", "FEATURE_JYOTISH_AI")).thenReturn(false);

    Map<String, Object> snap = guard.entitlementsSnapshot();
    @SuppressWarnings("unchecked")
    Map<String, Boolean> modules = (Map<String, Boolean>) snap.get("modules");
    assertThat(modules.get("kundali")).isTrue();
    assertThat(modules.get("matching")).isFalse();
    assertThat(modules.get("reports")).isTrue();
    assertThat(modules.get("ai")).isFalse();
  }
}
