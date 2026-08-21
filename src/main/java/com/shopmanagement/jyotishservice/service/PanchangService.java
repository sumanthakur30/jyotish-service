package com.shopmanagement.jyotishservice.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

import com.shopmanagement.jyotishservice.api.PanchangApi.CatalogItem;
import com.shopmanagement.jyotishservice.api.PanchangApi.ComingSoonFeature;
import com.shopmanagement.jyotishservice.api.PanchangApi.LimbDto;
import com.shopmanagement.jyotishservice.api.PanchangApi.LunarEventDto;
import com.shopmanagement.jyotishservice.api.PanchangApi.PanchangRequestBody;
import com.shopmanagement.jyotishservice.api.PanchangApi.PanchangResponse;
import com.shopmanagement.jyotishservice.api.PanchangApi.SolarEventDto;
import com.shopmanagement.jyotishservice.engine.CalculationEngine;
import com.shopmanagement.jyotishservice.engine.model.AyanamsaMode;
import com.shopmanagement.jyotishservice.engine.panchang.PanchangRequest;
import com.shopmanagement.jyotishservice.engine.panchang.PanchangResult;

@Service
public class PanchangService {

  private final CalculationEngine calculationEngine;

  public PanchangService(CalculationEngine calculationEngine) {
    this.calculationEngine = calculationEngine;
  }

  public PanchangResponse computeGet(
      LocalDate date,
      BigDecimal lat,
      BigDecimal lon,
      String timezone,
      String placeName,
      String ayanamsaCode) {
    if (date == null) {
      throw new IllegalArgumentException("date is required (yyyy-MM-dd)");
    }
    if (lat == null || lon == null) {
      throw new IllegalArgumentException("lat and lon are required");
    }
    if (timezone == null || timezone.isBlank()) {
      throw new IllegalArgumentException("timezone is required");
    }
    return compute(
        new PanchangRequestBody(date, lat, lon, timezone.trim(), placeName, ayanamsaCode));
  }

  public PanchangResponse compute(PanchangRequestBody body) {
    PanchangRequest request =
        new PanchangRequest(
            body.date(),
            body.lat().doubleValue(),
            body.lon().doubleValue(),
            body.timezone().trim(),
            body.placeName(),
            AyanamsaMode.fromCode(body.ayanamsaCode()));
    return toResponse(calculationEngine.computePanchang(request));
  }

  private static PanchangResponse toResponse(PanchangResult r) {
    return new PanchangResponse(
        r.date(),
        r.timeZone(),
        r.placeName(),
        bd(r.latitudeDeg(), 7),
        bd(r.longitudeDeg(), 7),
        r.ayanamsa().name(),
        bd(r.ayanamsaDeg(), 6),
        bd(r.julianDayUt(), 6),
        r.asOf(),
        r.engineVersion(),
        limb(r.tithi()),
        limb(r.vara()),
        limb(r.nakshatra()),
        limb(r.yoga()),
        limb(r.karana()),
        solar(r.sunrise()),
        solar(r.sunset()),
        lunar(r.moonrise()),
        lunar(r.moonset()),
        r.catalog().stream()
            .map(c -> new CatalogItem(c.code(), c.displayName(), c.implemented(), c.status()))
            .toList(),
        r.comingSoon().stream()
            .map(c -> new ComingSoonFeature(c.code(), c.displayName()))
            .toList(),
        r.notes(),
        r.disclaimer());
  }

  private static LimbDto limb(PanchangResult.Limb limb) {
    Integer pada = limb.pada() > 0 ? limb.pada() : null;
    return new LimbDto(
        limb.index(),
        limb.name(),
        limb.paksha(),
        pada,
        bd(limb.progress(), 4),
        limb.detail());
  }

  private static SolarEventDto solar(PanchangResult.SolarEvent e) {
    return new SolarEventDto(e.available(), e.localTime(), e.instant(), e.note());
  }

  private static LunarEventDto lunar(PanchangResult.LunarEvent e) {
    return new LunarEventDto(e.available(), e.localTime(), e.instant(), e.note());
  }

  private static BigDecimal bd(double v, int scale) {
    return BigDecimal.valueOf(v).setScale(scale, RoundingMode.HALF_UP);
  }
}
