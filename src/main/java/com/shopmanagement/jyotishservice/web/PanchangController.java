package com.shopmanagement.jyotishservice.web;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.shopmanagement.jyotishservice.api.PanchangApi.PanchangRequestBody;
import com.shopmanagement.jyotishservice.api.PanchangApi.PanchangResponse;
import com.shopmanagement.jyotishservice.service.PanchangService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/jyotish")
public class PanchangController {

  private final PanchangService panchangService;

  public PanchangController(PanchangService panchangService) {
    this.panchangService = panchangService;
  }

  /**
   * Compute Panchang for a civil date and place. Query params: {@code date}, {@code lat}, {@code
   * lon}, {@code timezone}; optional {@code placeName}, {@code ayanamsaCode}.
   */
  @GetMapping("/panchang")
  public PanchangResponse getPanchang(
      @RequestParam LocalDate date,
      @RequestParam BigDecimal lat,
      @RequestParam BigDecimal lon,
      @RequestParam String timezone,
      @RequestParam(required = false) String placeName,
      @RequestParam(required = false) String ayanamsaCode) {
    return panchangService.computeGet(date, lat, lon, timezone, placeName, ayanamsaCode);
  }

  @PostMapping("/panchang")
  public PanchangResponse postPanchang(@Valid @RequestBody PanchangRequestBody body) {
    return panchangService.compute(body);
  }
}
