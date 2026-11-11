package com.shopmanagement.jyotishservice.web;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.shopmanagement.jyotishservice.config.JyotishWebsiteSyncProperties;
import com.shopmanagement.jyotishservice.filter.TenantContextFilter;
import com.shopmanagement.jyotishservice.service.WebsiteSyncService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/jyotish/public")
@Tag(
    name = "Public website sync",
    description = "Server-to-server from JhaAstro. Never call from the browser.")
public class PublicWebsiteSyncController {

  private final WebsiteSyncService sync;
  private final JyotishWebsiteSyncProperties properties;

  public PublicWebsiteSyncController(
      WebsiteSyncService sync, JyotishWebsiteSyncProperties properties) {
    this.sync = sync;
    this.properties = properties;
  }

  @PostMapping("/customers")
  @Operation(summary = "Upsert CRM client (tenant + mobile / email / external id)")
  public Map<String, Object> customer(
      @RequestHeader("X-Jyotish-Org") String orgCode,
      @RequestHeader("X-Jyotish-Api-Key") String apiKey,
      @RequestHeader(value = "Idempotency-Key", required = false) String idem,
      @RequestBody Map<String, Object> body) {
    return withOrg(orgCode, apiKey, () -> sync.upsertCustomer(body, idem));
  }

  @PostMapping("/kundlis")
  public Map<String, Object> kundli(
      @RequestHeader("X-Jyotish-Org") String orgCode,
      @RequestHeader("X-Jyotish-Api-Key") String apiKey,
      @RequestHeader(value = "Idempotency-Key", required = false) String idem,
      @RequestBody Map<String, Object> body) {
    return withOrg(orgCode, apiKey, () -> sync.upsertKundli(body, idem));
  }

  @PostMapping("/bookings")
  public Map<String, Object> booking(
      @RequestHeader("X-Jyotish-Org") String orgCode,
      @RequestHeader("X-Jyotish-Api-Key") String apiKey,
      @RequestHeader(value = "Idempotency-Key", required = false) String idem,
      @RequestBody Map<String, Object> body) {
    return withOrg(orgCode, apiKey, () -> sync.upsertBooking(body, idem));
  }

  private Map<String, Object> withOrg(
      String orgCode, String apiKey, SyncAction action) {
    String tenantId = authenticate(orgCode, apiKey);
    TenantContextFilter.bindTenant(tenantId);
    try {
      return action.run();
    } finally {
      TenantContextFilter.clearTenantForTests();
    }
  }

  private String authenticate(String orgCode, String apiKey) {
    if (orgCode == null || orgCode.isBlank() || apiKey == null || apiKey.isBlank()) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing integration credentials");
    }
    String expected = properties.getApiKey();
    if (expected == null || expected.isBlank() || !keysEqual(expected, apiKey)) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid API credentials");
    }
    String tenantId = properties.getOrgs().get(orgCode.trim());
    if (tenantId == null || tenantId.isBlank()) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Organization not found");
    }
    return tenantId.trim();
  }

  private static boolean keysEqual(String expected, String actual) {
    return MessageDigest.isEqual(
        expected.getBytes(StandardCharsets.UTF_8), actual.getBytes(StandardCharsets.UTF_8));
  }

  @FunctionalInterface
  private interface SyncAction {
    Map<String, Object> run();
  }
}
