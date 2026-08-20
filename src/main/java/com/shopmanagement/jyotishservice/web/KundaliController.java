package com.shopmanagement.jyotishservice.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.shopmanagement.jyotishservice.api.KundaliApi.ChartListResponse;
import com.shopmanagement.jyotishservice.api.KundaliApi.DashaResponse;
import com.shopmanagement.jyotishservice.api.KundaliApi.GenerateRequest;
import com.shopmanagement.jyotishservice.api.KundaliApi.HouseListResponse;
import com.shopmanagement.jyotishservice.api.KundaliApi.KundaliResponse;
import com.shopmanagement.jyotishservice.api.KundaliApi.PlanetListResponse;
import com.shopmanagement.jyotishservice.api.KundaliApi.VargaChartResponse;
import com.shopmanagement.jyotishservice.api.KundaliApi.YogaListResponse;
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

  @GetMapping("/{id}/charts")
  public ChartListResponse charts(@PathVariable Long id) {
    return kundaliService.listCharts(id);
  }

  @GetMapping("/{id}/charts/{varga}")
  public VargaChartResponse chart(@PathVariable Long id, @PathVariable String varga) {
    return kundaliService.getChart(id, varga);
  }

  @GetMapping("/{id}/dasha")
  public DashaResponse dashaDefault(@PathVariable Long id) {
    return kundaliService.getDasha(id, "VIMSHOTTARI");
  }

  @GetMapping("/{id}/dasha/{system}")
  public DashaResponse dasha(@PathVariable Long id, @PathVariable String system) {
    return kundaliService.getDasha(id, system);
  }

  @GetMapping("/{id}/yogas")
  public YogaListResponse yogas(
      @PathVariable Long id, @RequestParam(required = false) String category) {
    return kundaliService.getYogas(id, category);
  }
}
