package com.shopmanagement.jyotishservice.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.shopmanagement.jyotishservice.api.TransitApi.ComingSoonFeature;
import com.shopmanagement.jyotishservice.api.TransitApi.SadeSatiDto;
import com.shopmanagement.jyotishservice.api.TransitApi.TransitCatalogItem;
import com.shopmanagement.jyotishservice.api.TransitApi.TransitPlanetDto;
import com.shopmanagement.jyotishservice.api.TransitApi.TransitRequestBody;
import com.shopmanagement.jyotishservice.api.TransitApi.TransitResponse;
import com.shopmanagement.jyotishservice.engine.CalculationEngine;
import com.shopmanagement.jyotishservice.engine.model.AyanamsaMode;
import com.shopmanagement.jyotishservice.engine.model.Planet;
import com.shopmanagement.jyotishservice.engine.model.PlanetPosition;
import com.shopmanagement.jyotishservice.engine.transit.NatalTransitRow;
import com.shopmanagement.jyotishservice.engine.transit.SadeSatiCalculator;
import com.shopmanagement.jyotishservice.engine.transit.TransitChart;
import com.shopmanagement.jyotishservice.engine.transit.TransitPlanetPosition;
import com.shopmanagement.jyotishservice.engine.transit.TransitRegistry;
import com.shopmanagement.jyotishservice.engine.transit.TransitRequest;
import com.shopmanagement.jyotishservice.engine.transit.TransitSystemCode;
import com.shopmanagement.jyotishservice.filter.TenantContextFilter;
import com.shopmanagement.jyotishservice.persistence.entity.KundaliSnapshotEntity;
import com.shopmanagement.jyotishservice.persistence.entity.PlanetaryPositionEntity;
import com.shopmanagement.jyotishservice.persistence.entity.TransitPlanetPositionEntity;
import com.shopmanagement.jyotishservice.persistence.entity.TransitSnapshotEntity;
import com.shopmanagement.jyotishservice.persistence.repo.KundaliSnapshotRepository;
import com.shopmanagement.jyotishservice.persistence.repo.PlanetaryPositionRepository;
import com.shopmanagement.jyotishservice.persistence.repo.TransitPlanetPositionRepository;
import com.shopmanagement.jyotishservice.persistence.repo.TransitSnapshotRepository;

@Service
public class TransitService {

  private static final List<ComingSoonFeature> COMING_SOON =
      List.of(
          new ComingSoonFeature("ASHTAKAVARGA_TRANSIT", "Ashtakavarga-weighted Gochar"),
          new ComingSoonFeature("TRANSIT_ASPECTS", "Transit-to-natal aspects detail"));

  private final CalculationEngine calculationEngine;
  private final KundaliSnapshotRepository kundaliRepository;
  private final PlanetaryPositionRepository planetaryRepository;
  private final TransitSnapshotRepository transitSnapshotRepository;
  private final TransitPlanetPositionRepository transitPlanetRepository;

  public TransitService(
      CalculationEngine calculationEngine,
      KundaliSnapshotRepository kundaliRepository,
      PlanetaryPositionRepository planetaryRepository,
      TransitSnapshotRepository transitSnapshotRepository,
      TransitPlanetPositionRepository transitPlanetRepository) {
    this.calculationEngine = calculationEngine;
    this.kundaliRepository = kundaliRepository;
    this.planetaryRepository = planetaryRepository;
    this.transitSnapshotRepository = transitSnapshotRepository;
    this.transitPlanetRepository = transitPlanetRepository;
  }

  /** GET .../kundali/{id}/transit?date= — default date = today in kundali timezone. */
  @Transactional
  public TransitResponse getForKundali(Long kundaliId, LocalDate date, LocalTime time) {
    return computeOrLoad(kundaliId, date, time);
  }

  /** POST /transit with kundaliId + optional date/time. */
  @Transactional
  public TransitResponse compute(TransitRequestBody body) {
    if (body == null || body.kundaliId() == null) {
      throw new IllegalArgumentException("kundaliId is required");
    }
    return computeOrLoad(body.kundaliId(), body.date(), body.time());
  }

  private TransitResponse computeOrLoad(Long kundaliId, LocalDate date, LocalTime time) {
    String tenantId = requireTenant();
    KundaliSnapshotEntity snap = requireSnapshot(kundaliId, tenantId);

    ZoneId zone = resolveZone(snap.getTimeZone());
    LocalDate transitDate = date != null ? date : LocalDate.now(zone);
    LocalTime transitTime =
        time != null ? time : (date == null ? LocalTime.now(zone).withNano(0) : LocalTime.NOON);

    // Reuse stored snapshot for the civil date (reproducibility). Recompute if engine version
    // advanced or rows missing.
    return transitSnapshotRepository
        .findByKundaliIdAndTenantIdAndTransitDate(kundaliId, tenantId, transitDate)
        .filter(row -> CalculationEngine.VERSION.equals(row.getCalculationEngineVersion()))
        .map(row -> toResponse(row, tenantId))
        .orElseGet(() -> computeAndPersist(snap, tenantId, transitDate, transitTime, zone));
  }

  private TransitResponse computeAndPersist(
      KundaliSnapshotEntity snap,
      String tenantId,
      LocalDate transitDate,
      LocalTime transitTime,
      ZoneId zone) {
    Long kundaliId = snap.getId();
    List<PlanetPosition> natal = loadNatalPlanets(kundaliId, tenantId);
    AyanamsaMode ayanamsa = AyanamsaMode.fromCode(snap.getAyanamsaCode());

    TransitRequest request =
        new TransitRequest(
            transitDate,
            transitTime,
            zone,
            snap.getLatitude().doubleValue(),
            snap.getLongitude().doubleValue(),
            ayanamsa,
            snap.getAscendantSignIndex());

    TransitChart chart =
        calculationEngine.computeTransit(TransitSystemCode.GOCHAR, request, natal);

    transitSnapshotRepository
        .findByKundaliIdAndTenantIdAndTransitDate(kundaliId, tenantId, transitDate)
        .ifPresent(
            existing -> {
              transitPlanetRepository.deleteByTransitIdAndTenantId(existing.getId(), tenantId);
              transitSnapshotRepository.delete(existing);
              transitSnapshotRepository.flush();
            });

    TransitSnapshotEntity entity = new TransitSnapshotEntity();
    entity.setTenantId(tenantId);
    entity.setKundaliId(kundaliId);
    entity.setTransitDate(chart.transitDate());
    entity.setTransitTime(chart.transitTime());
    entity.setTimeZone(zone.getId());
    entity.setJulianDayUt(bd(chart.julianDayUt(), 8));
    entity.setAyanamsaCode(chart.ayanamsa().name());
    entity.setAyanamsaDeg(bd(chart.ayanamsaDeg(), 6));
    entity.setNatalLagnaSignIndex((short) chart.natalLagnaSignIndex());
    entity.setSystemCode(chart.system().code());
    entity.setCalculationEngineVersion(chart.engineVersion());
    entity.setNotes(chart.notes());
    entity.setMetaJson(
        "{\"system\":\""
            + chart.system().code()
            + "\",\"source\":\"LIVE_EPHEMERIS\",\"engine\":\""
            + chart.engineVersion()
            + "\"}");
    entity = transitSnapshotRepository.save(entity);

    List<TransitPlanetPositionEntity> batch = new ArrayList<>();
    for (NatalTransitRow row : chart.rows()) {
      batch.add(toPlanetEntity(tenantId, entity.getId(), row));
    }
    transitPlanetRepository.saveAll(batch);

    return toResponse(entity, tenantId);
  }

  private TransitResponse toResponse(TransitSnapshotEntity snap, String tenantId) {
    List<TransitPlanetDto> planets = new ArrayList<>();
    for (TransitPlanetPositionEntity e :
        transitPlanetRepository.findByTransitIdAndTenantIdOrderByPlanetCodeAsc(
            snap.getId(), tenantId)) {
      planets.add(toPlanetDto(e));
    }
    SadeSatiDto sadeSati = computeSadeSatiDto(snap.getKundaliId(), tenantId, planets);
    return new TransitResponse(
        snap.getId(),
        snap.getKundaliId(),
        snap.getTransitDate(),
        snap.getTransitTime(),
        snap.getTimeZone(),
        snap.getSystemCode(),
        displaySystem(snap.getSystemCode()),
        snap.getCalculationEngineVersion(),
        snap.getAyanamsaCode(),
        snap.getAyanamsaDeg(),
        snap.getJulianDayUt(),
        snap.getNatalLagnaSignIndex(),
        planets,
        sadeSati,
        buildCatalog(),
        COMING_SOON,
        snap.getNotes(),
        "Transit positions are astronomical overlays on the natal chart. Not predictions or timing"
            + " advice. Sade Sati phase is descriptive (Saturn house from natal Moon).",
        snap.getCreatedAt());
  }

  private SadeSatiDto computeSadeSatiDto(
      Long kundaliId, String tenantId, List<TransitPlanetDto> transitPlanets) {
    Integer moonSign = null;
    for (PlanetaryPositionEntity r :
        planetaryRepository.findByKundaliIdAndTenantIdOrderByPlanetCodeAsc(kundaliId, tenantId)) {
      if ("MOON".equalsIgnoreCase(r.getPlanetCode())) {
        moonSign = (int) r.getSignIndex();
        break;
      }
    }
    Integer saturnSign = null;
    for (TransitPlanetDto p : transitPlanets) {
      if ("SATURN".equalsIgnoreCase(p.planetCode())) {
        saturnSign = p.signIndex();
        break;
      }
    }
    if (moonSign == null || saturnSign == null) {
      return new SadeSatiDto(
          SadeSatiCalculator.Phase.NOT_IN_SADE_SATI.code(),
          SadeSatiCalculator.Phase.NOT_IN_SADE_SATI.label(),
          moonSign == null ? -1 : moonSign,
          "",
          saturnSign == null ? -1 : saturnSign,
          "",
          -1,
          -1,
          false,
          "Moon or Saturn missing — Sade Sati not computed.");
    }
    var a = SadeSatiCalculator.analyze(moonSign, saturnSign);
    return new SadeSatiDto(
        a.phaseCode(),
        a.phaseLabel(),
        a.natalMoonSignIndex(),
        a.natalMoonSignName(),
        a.transitSaturnSignIndex(),
        a.transitSaturnSignName(),
        a.signsFromMoon(),
        a.houseFromMoon(),
        a.inSadeSati(),
        a.notes());
  }

  private List<PlanetPosition> loadNatalPlanets(Long kundaliId, String tenantId) {
    List<PlanetPosition> planets = new ArrayList<>();
    for (PlanetaryPositionEntity r :
        planetaryRepository.findByKundaliIdAndTenantIdOrderByPlanetCodeAsc(kundaliId, tenantId)) {
      if ("ASCENDANT".equals(r.getPlanetCode())) {
        continue;
      }
      Planet planet;
      try {
        planet = Planet.valueOf(r.getPlanetCode());
      } catch (Exception ex) {
        continue;
      }
      planets.add(
          new PlanetPosition(
              planet,
              r.getLongitudeDeg().doubleValue(),
              r.getSignIndex(),
              r.getSignName(),
              r.getDegreeInSign().doubleValue(),
              r.getHouse(),
              r.getNakshatraIndex(),
              r.getNakshatraName(),
              r.getPada(),
              r.isRetrograde(),
              r.isCombust(),
              r.getSpeedDegPerDay() == null ? null : r.getSpeedDegPerDay().doubleValue()));
    }
    return planets;
  }

  private static TransitPlanetPositionEntity toPlanetEntity(
      String tenantId, Long transitId, NatalTransitRow row) {
    TransitPlanetPosition t = row.transit();
    TransitPlanetPositionEntity e = new TransitPlanetPositionEntity();
    e.setTenantId(tenantId);
    e.setTransitId(transitId);
    e.setPlanetCode(t.planet().name());
    e.setLongitudeDeg(bd(t.longitudeDeg(), 6));
    e.setSignIndex((short) t.signIndex());
    e.setSignName(t.signName());
    e.setDegreeInSign(bd(t.degreeInSign(), 6));
    e.setHouse((short) t.house());
    e.setNakshatraIndex((short) t.nakshatraIndex());
    e.setNakshatraName(t.nakshatraName());
    e.setPada((short) t.pada());
    e.setRetrograde(t.retrograde());
    if (t.speedDegPerDay() != null) {
      e.setSpeedDegPerDay(bd(t.speedDegPerDay(), 6));
    }
    if (row.natalLongitudeDeg() != null) {
      e.setNatalLongitudeDeg(bd(row.natalLongitudeDeg(), 6));
    }
    if (row.natalSignIndex() != null) {
      e.setNatalSignIndex(row.natalSignIndex().shortValue());
    }
    e.setNatalSignName(row.natalSignName());
    if (row.natalHouse() != null) {
      e.setNatalHouse(row.natalHouse().shortValue());
    }
    e.setSignChanged(row.signChanged());
    e.setHouseChanged(row.houseChanged());
    return e;
  }

  private static TransitPlanetDto toPlanetDto(TransitPlanetPositionEntity e) {
    return new TransitPlanetDto(
        e.getPlanetCode(),
        displayPlanet(e.getPlanetCode()),
        e.getLongitudeDeg(),
        e.getSignIndex(),
        e.getSignName(),
        e.getDegreeInSign(),
        e.getHouse(),
        e.getNakshatraIndex(),
        e.getNakshatraName(),
        e.getPada(),
        e.isRetrograde(),
        e.getSpeedDegPerDay(),
        e.getNatalLongitudeDeg(),
        e.getNatalSignIndex() == null ? null : e.getNatalSignIndex().intValue(),
        e.getNatalSignName(),
        e.getNatalHouse() == null ? null : e.getNatalHouse().intValue(),
        e.isSignChanged(),
        e.isHouseChanged());
  }

  private static List<TransitCatalogItem> buildCatalog() {
    List<TransitCatalogItem> catalog = new ArrayList<>();
    for (TransitSystemCode code : TransitRegistry.all()) {
      boolean implemented =
          TransitRegistry.isImplemented(code) || code == TransitSystemCode.SADE_SATI;
      catalog.add(
          new TransitCatalogItem(
              code.code(),
              code.displayName(),
              implemented,
              implemented ? "READY" : "COMING_SOON"));
    }
    return catalog;
  }

  private static String displaySystem(String code) {
    try {
      return TransitSystemCode.parse(code).displayName();
    } catch (Exception ex) {
      return code;
    }
  }

  private static String displayPlanet(String code) {
    try {
      return Planet.valueOf(code).displayName();
    } catch (Exception ex) {
      return code;
    }
  }

  private KundaliSnapshotEntity requireSnapshot(Long id, String tenantId) {
    return kundaliRepository
        .findByIdAndTenantId(id, tenantId)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kundali snapshot not found"));
  }

  private static ZoneId resolveZone(String raw) {
    try {
      return ZoneId.of(raw);
    } catch (Exception ex) {
      return ZoneId.of("UTC");
    }
  }

  private static BigDecimal bd(double v, int scale) {
    return BigDecimal.valueOf(v).setScale(scale, RoundingMode.HALF_UP);
  }

  private static String requireTenant() {
    String tenantId = TenantContextFilter.getCurrentTenantId();
    if (tenantId == null || tenantId.isBlank()) {
      throw new IllegalArgumentException("Missing tenant context header: X-Tenant-Id");
    }
    return tenantId;
  }
}
