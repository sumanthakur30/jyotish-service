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

import com.shopmanagement.jyotishservice.api.KundaliApi.AshtakavargaResponse;
import com.shopmanagement.jyotishservice.api.KundaliApi.ChartListResponse;
import com.shopmanagement.jyotishservice.api.KundaliApi.DashaResponse;
import com.shopmanagement.jyotishservice.api.KundaliApi.DoshaListResponse;
import com.shopmanagement.jyotishservice.api.KundaliApi.GenerateRequest;
import com.shopmanagement.jyotishservice.api.KundaliApi.HouseListResponse;
import com.shopmanagement.jyotishservice.api.KundaliApi.KundaliResponse;
import com.shopmanagement.jyotishservice.api.KundaliApi.PlanetListResponse;
import com.shopmanagement.jyotishservice.api.KundaliApi.ShadbalaResponse;
import com.shopmanagement.jyotishservice.api.KundaliApi.VargaChartResponse;
import com.shopmanagement.jyotishservice.api.KundaliApi.YogaListResponse;
import com.shopmanagement.jyotishservice.api.SimpleOverviewApi.SimpleOverviewResponse;
import com.shopmanagement.jyotishservice.api.SimpleOverviewApi.SimplePeriodExplainResponse;
import com.shopmanagement.jyotishservice.entitlement.JyotishEntitlementGuard;
import com.shopmanagement.jyotishservice.service.KundaliService;
import com.shopmanagement.jyotishservice.service.SimpleOverviewService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/jyotish/kundali")
public class KundaliController {

  private final KundaliService kundaliService;
  private final SimpleOverviewService simpleOverviewService;
  private final JyotishEntitlementGuard entitlementGuard;

  public KundaliController(
      KundaliService kundaliService,
      SimpleOverviewService simpleOverviewService,
      JyotishEntitlementGuard entitlementGuard) {
    this.kundaliService = kundaliService;
    this.simpleOverviewService = simpleOverviewService;
    this.entitlementGuard = entitlementGuard;
  }

  @PostMapping("/generate")
  @ResponseStatus(HttpStatus.CREATED)
  public KundaliResponse generate(@Valid @RequestBody GenerateRequest body) {
    entitlementGuard.requireJyotishAccess();
    return kundaliService.generate(body);
  }

  @GetMapping("/{id}")
  public KundaliResponse get(@PathVariable Long id) {
    entitlementGuard.requireJyotishAccess();
    return kundaliService.get(id);
  }

  /** Customer Simple View: templated bilingual overview from stored facts only. */
  @GetMapping("/{id}/simple-overview")
  public SimpleOverviewResponse simpleOverview(@PathVariable Long id) {
    entitlementGuard.requireJyotishAccess();
    return simpleOverviewService.overview(id);
  }

  /**
   * Explain a dasha period only when lords/level match a stored {@code dasha_period} row for this
   * kundali.
   */
  @GetMapping("/{id}/simple-period")
  public SimplePeriodExplainResponse simplePeriod(
      @PathVariable Long id,
      @RequestParam String level,
      @RequestParam(required = false) String mahaLord,
      @RequestParam(required = false) String antarLord) {
    entitlementGuard.requireJyotishAccess();
    return simpleOverviewService.explainStoredPeriod(id, level, mahaLord, antarLord);
  }

  @GetMapping("/{id}/planets")
  public PlanetListResponse planets(@PathVariable Long id) {
    entitlementGuard.requireJyotishAccess();
    return kundaliService.planets(id);
  }

  @GetMapping("/{id}/houses")
  public HouseListResponse houses(@PathVariable Long id) {
    entitlementGuard.requireJyotishAccess();
    return kundaliService.houses(id);
  }

  @GetMapping("/{id}/charts")
  public ChartListResponse charts(@PathVariable Long id) {
    entitlementGuard.requireJyotishAccess();
    return kundaliService.listCharts(id);
  }

  @GetMapping("/{id}/charts/{varga}")
  public VargaChartResponse chart(@PathVariable Long id, @PathVariable String varga) {
    entitlementGuard.requireJyotishAccess();
    return kundaliService.getChart(id, varga);
  }

  @GetMapping("/{id}/dasha")
  public DashaResponse dashaDefault(@PathVariable Long id) {
    entitlementGuard.requireJyotishAccess();
    return kundaliService.getDasha(id, "VIMSHOTTARI");
  }

  @GetMapping("/{id}/dasha/{system}")
  public DashaResponse dasha(@PathVariable Long id, @PathVariable String system) {
    entitlementGuard.requireJyotishAccess();
    return kundaliService.getDasha(id, system);
  }

  @GetMapping("/{id}/yogas")
  public YogaListResponse yogas(
      @PathVariable Long id, @RequestParam(required = false) String category) {
    entitlementGuard.requireJyotishAccess();
    return kundaliService.getYogas(id, category);
  }

  @GetMapping("/{id}/doshas")
  public DoshaListResponse doshas(@PathVariable Long id) {
    entitlementGuard.requireJyotishAccess();
    return kundaliService.getDoshas(id);
  }

  @GetMapping("/{id}/ashtakavarga")
  public AshtakavargaResponse ashtakavarga(@PathVariable Long id) {
    entitlementGuard.requireJyotishAccess();
    return kundaliService.getAshtakavarga(id);
  }

  @GetMapping("/{id}/shadbala")
  public ShadbalaResponse shadbala(@PathVariable Long id) {
    entitlementGuard.requireJyotishAccess();
    return kundaliService.getShadbala(id);
  }
}
