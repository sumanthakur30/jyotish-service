package com.shopmanagement.jyotishservice.web;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.shopmanagement.jyotishservice.api.TransitApi.TransitRequestBody;
import com.shopmanagement.jyotishservice.api.TransitApi.TransitResponse;
import com.shopmanagement.jyotishservice.entitlement.JyotishEntitlementGuard;
import com.shopmanagement.jyotishservice.service.TransitService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/jyotish")
public class TransitController {

  private final TransitService transitService;
  private final JyotishEntitlementGuard entitlementGuard;

  public TransitController(
      TransitService transitService, JyotishEntitlementGuard entitlementGuard) {
    this.transitService = transitService;
    this.entitlementGuard = entitlementGuard;
  }

  @GetMapping("/kundali/{id}/transit")
  public TransitResponse getTransit(
      @PathVariable Long id,
      @RequestParam(required = false) LocalDate date,
      @RequestParam(required = false) LocalTime time) {
    entitlementGuard.requireJyotishAccess();
    return transitService.getForKundali(id, date, time);
  }

  @PostMapping("/transit")
  @ResponseStatus(HttpStatus.CREATED)
  public TransitResponse postTransit(@Valid @RequestBody TransitRequestBody body) {
    entitlementGuard.requireJyotishAccess();
    return transitService.compute(body);
  }
}
