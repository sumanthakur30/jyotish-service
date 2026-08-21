package com.shopmanagement.jyotishservice.support;

import com.shopmanagement.jyotishservice.filter.TenantContextFilter;

public final class TenantIds {

  private TenantIds() {}

  public static String require() {
    String tenantId = TenantContextFilter.getCurrentTenantId();
    if (tenantId == null || tenantId.isBlank()) {
      throw new IllegalStateException("Missing tenant context");
    }
    return tenantId;
  }
}
