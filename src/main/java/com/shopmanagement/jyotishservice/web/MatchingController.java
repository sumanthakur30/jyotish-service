package com.shopmanagement.jyotishservice.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.shopmanagement.jyotishservice.api.MatchingApi.MatchRequest;
import com.shopmanagement.jyotishservice.api.MatchingApi.MatchingResponse;
import com.shopmanagement.jyotishservice.entitlement.JyotishEntitlementGuard;
import com.shopmanagement.jyotishservice.service.MatchingService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/jyotish/matching")
public class MatchingController {

  private final MatchingService matchingService;
  private final JyotishEntitlementGuard entitlementGuard;

  public MatchingController(
      MatchingService matchingService, JyotishEntitlementGuard entitlementGuard) {
    this.matchingService = matchingService;
    this.entitlementGuard = entitlementGuard;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public MatchingResponse match(@Valid @RequestBody MatchRequest body) {
    entitlementGuard.requireMatchingAccess();
    return matchingService.match(body);
  }

  @GetMapping("/{id}")
  public MatchingResponse get(@PathVariable Long id) {
    entitlementGuard.requireMatchingAccess();
    return matchingService.get(id);
  }
}
