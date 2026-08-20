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
import com.shopmanagement.jyotishservice.service.MatchingService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/jyotish/matching")
public class MatchingController {

  private final MatchingService matchingService;

  public MatchingController(MatchingService matchingService) {
    this.matchingService = matchingService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public MatchingResponse match(@Valid @RequestBody MatchRequest body) {
    return matchingService.match(body);
  }

  @GetMapping("/{id}")
  public MatchingResponse get(@PathVariable Long id) {
    return matchingService.get(id);
  }
}
