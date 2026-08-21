package com.shopmanagement.jyotishservice.web;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopmanagement.jyotishservice.api.AiApi.AskRequest;
import com.shopmanagement.jyotishservice.api.AiApi.AskResponse;
import com.shopmanagement.jyotishservice.entitlement.JyotishEntitlementGuard;
import com.shopmanagement.jyotishservice.service.AiAskService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/jyotish/ai")
public class AiController {

  private final AiAskService aiAskService;
  private final JyotishEntitlementGuard entitlementGuard;

  public AiController(AiAskService aiAskService, JyotishEntitlementGuard entitlementGuard) {
    this.aiAskService = aiAskService;
    this.entitlementGuard = entitlementGuard;
  }

  @PostMapping("/ask")
  public AskResponse ask(@Valid @RequestBody AskRequest request) {
    entitlementGuard.requireAiAccess();
    return aiAskService.ask(request);
  }
}
