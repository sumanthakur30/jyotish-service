package com.shopmanagement.jyotishservice.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.shopmanagement.jyotishservice.api.LifeAnalysisApi.AnalysisDetailResponse;
import com.shopmanagement.jyotishservice.api.LifeAnalysisApi.ConsultationListResponse;
import com.shopmanagement.jyotishservice.api.LifeAnalysisApi.DashboardResponse;
import com.shopmanagement.jyotishservice.api.LifeAnalysisApi.HistoryListResponse;
import com.shopmanagement.jyotishservice.api.LifeAnalysisApi.PeriodDto;
import com.shopmanagement.jyotishservice.api.LifeAnalysisApi.PeriodListResponse;
import com.shopmanagement.jyotishservice.api.LifeAnalysisApi.SearchResponse;
import com.shopmanagement.jyotishservice.api.LifeAnalysisApi.UpsertAnalysisRequest;
import com.shopmanagement.jyotishservice.api.LifeAnalysisApi.UpsertPeriodRequest;
import com.shopmanagement.jyotishservice.service.LifeAnalysisService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/jyotish/kundali/{kundaliId}/life-analysis")
public class LifeAnalysisController {

  private final LifeAnalysisService lifeAnalysisService;

  public LifeAnalysisController(LifeAnalysisService lifeAnalysisService) {
    this.lifeAnalysisService = lifeAnalysisService;
  }

  @GetMapping
  public DashboardResponse dashboard(@PathVariable Long kundaliId) {
    return lifeAnalysisService.dashboard(kundaliId);
  }

  @GetMapping("/search")
  public SearchResponse search(@PathVariable Long kundaliId, @RequestParam String q) {
    return lifeAnalysisService.search(kundaliId, q);
  }

  @GetMapping("/consultations")
  public ConsultationListResponse consultations(@PathVariable Long kundaliId) {
    return lifeAnalysisService.consultations(kundaliId);
  }

  @GetMapping("/periods")
  public PeriodListResponse periods(
      @PathVariable Long kundaliId, @RequestParam(required = false) String category) {
    return lifeAnalysisService.listPeriods(kundaliId, category);
  }

  @PostMapping("/periods")
  @ResponseStatus(HttpStatus.CREATED)
  public PeriodDto createPeriod(
      @PathVariable Long kundaliId, @Valid @RequestBody UpsertPeriodRequest body) {
    return lifeAnalysisService.createPeriod(kundaliId, body);
  }

  @PutMapping("/periods/{periodId}")
  public PeriodDto updatePeriod(
      @PathVariable Long kundaliId,
      @PathVariable Long periodId,
      @Valid @RequestBody UpsertPeriodRequest body) {
    return lifeAnalysisService.updatePeriod(kundaliId, periodId, body);
  }

  @DeleteMapping("/periods/{periodId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deletePeriod(@PathVariable Long kundaliId, @PathVariable Long periodId) {
    lifeAnalysisService.deletePeriod(kundaliId, periodId);
  }

  @GetMapping("/{category}")
  public AnalysisDetailResponse get(
      @PathVariable Long kundaliId, @PathVariable String category) {
    return lifeAnalysisService.get(kundaliId, category);
  }

  @PutMapping("/{category}")
  public AnalysisDetailResponse upsert(
      @PathVariable Long kundaliId,
      @PathVariable String category,
      @RequestBody UpsertAnalysisRequest body) {
    return lifeAnalysisService.upsert(kundaliId, category, body);
  }

  @GetMapping("/{category}/history")
  public HistoryListResponse history(
      @PathVariable Long kundaliId, @PathVariable String category) {
    return lifeAnalysisService.history(kundaliId, category);
  }
}
