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
   * Compute Panchang for a civil date and place.
   *
   * <p>Query params (canonical): {@code date}, {@code lat}, {@code lon}, {@code timezone}. Aliases
   * accepted: {@code latitude}/{@code longitude}/{@code timeZone}. Optional: {@code placeName},
   * {@code ayanamsaCode}.
   */
  @GetMapping("/panchang")
  public PanchangResponse getPanchang(
      @RequestParam LocalDate date,
      @RequestParam(required = false) BigDecimal lat,
      @RequestParam(required = false) BigDecimal latitude,
      @RequestParam(required = false) BigDecimal lon,
      @RequestParam(required = false) BigDecimal longitude,
      @RequestParam(required = false) String timezone,
      @RequestParam(required = false) String timeZone,
      @RequestParam(required = false) String placeName,
      @RequestParam(required = false) String ayanamsaCode) {
    BigDecimal resolvedLat = firstNonNull(lat, latitude);
    BigDecimal resolvedLon = firstNonNull(lon, longitude);
    String resolvedTz = firstNonBlank(timezone, timeZone);
    return panchangService.computeGet(
        date, resolvedLat, resolvedLon, resolvedTz, placeName, ayanamsaCode);
  }

  @PostMapping("/panchang")
  public PanchangResponse postPanchang(@Valid @RequestBody PanchangRequestBody body) {
    return panchangService.compute(body);
  }

  @SafeVarargs
  private static <T> T firstNonNull(T... values) {
    if (values == null) {
      return null;
    }
    for (T v : values) {
      if (v != null) {
        return v;
      }
    }
    return null;
  }

  private static String firstNonBlank(String... values) {
    if (values == null) {
      return null;
    }
    for (String v : values) {
      if (v != null && !v.isBlank()) {
        return v;
      }
    }
    return null;
  }
}
