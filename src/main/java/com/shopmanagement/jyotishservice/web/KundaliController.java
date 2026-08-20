package com.shopmanagement.jyotishservice.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.shopmanagement.jyotishservice.api.KundaliApi.GenerateRequest;
import com.shopmanagement.jyotishservice.api.KundaliApi.HouseListResponse;
import com.shopmanagement.jyotishservice.api.KundaliApi.KundaliResponse;
import com.shopmanagement.jyotishservice.api.KundaliApi.PlanetListResponse;
import com.shopmanagement.jyotishservice.service.KundaliService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/jyotish/kundali")
public class KundaliController {

  private final KundaliService kundaliService;

  public KundaliController(KundaliService kundaliService) {
    this.kundaliService = kundaliService;
  }

  @PostMapping("/generate")
  @ResponseStatus(HttpStatus.CREATED)
  public KundaliResponse generate(@Valid @RequestBody GenerateRequest body) {
    return kundaliService.generate(body);
  }

  @GetMapping("/{id}")
  public KundaliResponse get(@PathVariable Long id) {
    return kundaliService.get(id);
  }

  @GetMapping("/{id}/planets")
  public PlanetListResponse planets(@PathVariable Long id) {
    return kundaliService.planets(id);
  }

  @GetMapping("/{id}/houses")
  public HouseListResponse houses(@PathVariable Long id) {
    return kundaliService.houses(id);
  }
}
