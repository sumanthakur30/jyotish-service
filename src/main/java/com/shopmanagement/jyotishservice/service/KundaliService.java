package com.shopmanagement.jyotishservice.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.shopmanagement.jyotishservice.api.KundaliApi.ComingSoonFeature;
import com.shopmanagement.jyotishservice.api.KundaliApi.GenerateRequest;
import com.shopmanagement.jyotishservice.api.KundaliApi.HouseDto;
import com.shopmanagement.jyotishservice.api.KundaliApi.HouseListResponse;
import com.shopmanagement.jyotishservice.api.KundaliApi.InlineBirthRequest;
import com.shopmanagement.jyotishservice.api.KundaliApi.KundaliResponse;
import com.shopmanagement.jyotishservice.api.KundaliApi.PlanetDto;
import com.shopmanagement.jyotishservice.api.KundaliApi.PlanetListResponse;
import com.shopmanagement.jyotishservice.engine.CalculationEngine;
import com.shopmanagement.jyotishservice.engine.model.AyanamsaMode;
import com.shopmanagement.jyotishservice.engine.model.BirthMoment;
import com.shopmanagement.jyotishservice.engine.model.ChartRequest;
import com.shopmanagement.jyotishservice.engine.model.D1Chart;
import com.shopmanagement.jyotishservice.engine.model.HouseCusp;
import com.shopmanagement.jyotishservice.engine.model.Planet;
import com.shopmanagement.jyotishservice.engine.model.PlanetPosition;
import com.shopmanagement.jyotishservice.filter.TenantContextFilter;
import com.shopmanagement.jyotishservice.persistence.entity.BirthDetailsEntity;
import com.shopmanagement.jyotishservice.persistence.entity.BirthLocationEntity;
import com.shopmanagement.jyotishservice.persistence.entity.BirthProfileEntity;
import com.shopmanagement.jyotishservice.persistence.entity.HousePositionEntity;
import com.shopmanagement.jyotishservice.persistence.entity.KundaliSnapshotEntity;
import com.shopmanagement.jyotishservice.persistence.entity.PlanetaryPositionEntity;
import com.shopmanagement.jyotishservice.persistence.repo.BirthDetailsRepository;
import com.shopmanagement.jyotishservice.persistence.repo.BirthLocationRepository;
import com.shopmanagement.jyotishservice.persistence.repo.BirthProfileRepository;
import com.shopmanagement.jyotishservice.persistence.repo.HousePositionRepository;
import com.shopmanagement.jyotishservice.persistence.repo.KundaliSnapshotRepository;
import com.shopmanagement.jyotishservice.persistence.repo.PlanetaryPositionRepository;
import com.shopmanagement.jyotishservice.persistence.repo.JyotishWorkspaceRepository;

@Service
public class KundaliService {

  private static final List<ComingSoonFeature> COMING_SOON =
      List.of(
          new ComingSoonFeature("COMBUST", "Combust detection with classical orbs"),
          new ComingSoonFeature("SHADBALA", "Shadbala strength scores"),
          new ComingSoonFeature("VARGAS", "D9–D60 divisional charts"),
          new ComingSoonFeature("DASHA", "Vimshottari dasha timeline"),
          new ComingSoonFeature("YOGA", "Yoga detection"));

  private final CalculationEngine calculationEngine;
  private final KundaliSnapshotRepository kundaliRepository;
  private final PlanetaryPositionRepository planetaryRepository;
  private final HousePositionRepository houseRepository;
  private final BirthProfileRepository profileRepository;
  private final BirthDetailsRepository detailsRepository;
  private final BirthLocationRepository locationRepository;
  private final JyotishWorkspaceRepository workspaceRepository;

  public KundaliService(
      CalculationEngine calculationEngine,
      KundaliSnapshotRepository kundaliRepository,
      PlanetaryPositionRepository planetaryRepository,
      HousePositionRepository houseRepository,
      BirthProfileRepository profileRepository,
      BirthDetailsRepository detailsRepository,
      BirthLocationRepository locationRepository,
      JyotishWorkspaceRepository workspaceRepository) {
    this.calculationEngine = calculationEngine;
    this.kundaliRepository = kundaliRepository;
    this.planetaryRepository = planetaryRepository;
    this.houseRepository = houseRepository;
    this.profileRepository = profileRepository;
    this.detailsRepository = detailsRepository;
    this.locationRepository = locationRepository;
    this.workspaceRepository = workspaceRepository;
  }

  @Transactional
  public KundaliResponse generate(GenerateRequest request) {
    String tenantId = requireTenant();
    if (request == null || (request.birthProfileId() == null && request.birth() == null)) {
      throw new IllegalArgumentException("Provide birthProfileId or inline birth details.");
    }

    ResolvedBirth resolved = resolveBirth(tenantId, request);
    AyanamsaMode ayanamsa = resolveAyanamsa(tenantId, request.ayanamsaCode());

    ZoneId zone;
    try {
      zone = ZoneId.of(resolved.timeZone());
    } catch (Exception ex) {
      throw new IllegalArgumentException("Invalid time zone: " + resolved.timeZone());
    }

    LocalTime time =
        resolved.birthTimeUnknown()
            ? LocalTime.NOON
            : (resolved.birthTime() != null ? resolved.birthTime() : LocalTime.NOON);

    BirthMoment moment =
        new BirthMoment(
            resolved.birthDate(),
            time,
            zone,
            resolved.latitude().doubleValue(),
            resolved.longitude().doubleValue(),
            resolved.placeName());

    D1Chart chart =
        calculationEngine.computeD1(new ChartRequest(moment, ayanamsa, resolved.birthTimeUnknown()));

    KundaliSnapshotEntity snap = new KundaliSnapshotEntity();
    snap.setTenantId(tenantId);
    snap.setBirthProfileId(resolved.profileId());
    snap.setDisplayName(resolved.displayName());
    snap.setBirthDate(resolved.birthDate());
    snap.setBirthTime(resolved.birthTimeUnknown() ? null : resolved.birthTime());
    snap.setBirthTimeUnknown(resolved.birthTimeUnknown());
    snap.setTimeZone(resolved.timeZone());
    snap.setPlaceName(resolved.placeName());
    snap.setLatitude(resolved.latitude());
    snap.setLongitude(resolved.longitude());
    snap.setAyanamsaCode(ayanamsa.name());
    snap.setAyanamsaDeg(bd(chart.ayanamsaDeg(), 6));
    snap.setZodiacSystem("SIDEREAL");
    snap.setHouseSystem(chart.houseSystem());
    snap.setChartStyle("NORTH_INDIAN");
    snap.setCalculationEngineVersion(chart.engineVersion());
    snap.setJulianDayUt(bd(chart.julianDayUt(), 8));
    snap.setAscendantLongitude(bd(chart.ascendant().longitudeDeg(), 6));
    snap.setAscendantSignIndex((short) chart.ascendant().signIndex());
    snap.setNotes(chart.notes());
    snap.setInputJson(
        "{\"source\":\""
            + (resolved.profileId() != null ? "profile" : "inline")
            + "\",\"ayanamsa\":\""
            + ayanamsa.name()
            + "\"}");
    snap = kundaliRepository.save(snap);

    List<PlanetDto> planetDtos = new ArrayList<>();
    for (PlanetPosition p : chart.planets()) {
      PlanetaryPositionEntity row = toPlanetEntity(tenantId, snap.getId(), p);
      planetaryRepository.save(row);
      planetDtos.add(toPlanetDto(p));
    }
    // Persist ascendant as well for consistent GET/planets views
    PlanetaryPositionEntity ascRow = toPlanetEntity(tenantId, snap.getId(), chart.ascendant());
    planetaryRepository.save(ascRow);

    List<HouseDto> houseDtos = new ArrayList<>();
    for (HouseCusp h : chart.houses()) {
      HousePositionEntity row = new HousePositionEntity();
      row.setTenantId(tenantId);
      row.setKundaliId(snap.getId());
      row.setHouse((short) h.house());
      row.setSignIndex((short) h.signIndex());
      row.setSignName(h.signName());
      row.setCuspLongitudeDeg(bd(h.cuspLongitudeDeg(), 6));
      houseRepository.save(row);
      houseDtos.add(toHouseDto(h));
    }

    return toResponse(snap, toPlanetDto(chart.ascendant()), planetDtos, houseDtos);
  }

  @Transactional(readOnly = true)
  public KundaliResponse get(Long id) {
    String tenantId = requireTenant();
    KundaliSnapshotEntity snap = requireSnapshot(id, tenantId);
    List<PlanetDto> planets = loadPlanets(id, tenantId).stream()
        .filter(p -> !"ASCENDANT".equals(p.planetCode()))
        .toList();
    PlanetDto asc =
        loadPlanets(id, tenantId).stream()
            .filter(p -> "ASCENDANT".equals(p.planetCode()))
            .findFirst()
            .orElse(null);
    List<HouseDto> houses = loadHouses(id, tenantId);
    return toResponse(snap, asc, planets, houses);
  }

  @Transactional(readOnly = true)
  public PlanetListResponse planets(Long id) {
    String tenantId = requireTenant();
    requireSnapshot(id, tenantId);
    return new PlanetListResponse(id, loadPlanets(id, tenantId));
  }

  @Transactional(readOnly = true)
  public HouseListResponse houses(Long id) {
    String tenantId = requireTenant();
    requireSnapshot(id, tenantId);
    return new HouseListResponse(id, loadHouses(id, tenantId));
  }

  private List<PlanetDto> loadPlanets(Long kundaliId, String tenantId) {
    List<PlanetDto> out = new ArrayList<>();
    for (PlanetaryPositionEntity e :
        planetaryRepository.findByKundaliIdAndTenantIdOrderByPlanetCodeAsc(kundaliId, tenantId)) {
      out.add(
          new PlanetDto(
              e.getPlanetCode(),
              displayName(e.getPlanetCode()),
              e.getLongitudeDeg(),
              e.getSignIndex(),
              e.getSignName(),
              e.getDegreeInSign(),
              e.getHouse(),
              e.getNakshatraIndex(),
              e.getNakshatraName(),
              e.getPada(),
              e.isRetrograde(),
              e.isCombust(),
              e.getSpeedDegPerDay()));
    }
    return out;
  }

  private List<HouseDto> loadHouses(Long kundaliId, String tenantId) {
    List<HouseDto> out = new ArrayList<>();
    for (HousePositionEntity e :
        houseRepository.findByKundaliIdAndTenantIdOrderByHouseAsc(kundaliId, tenantId)) {
      out.add(
          new HouseDto(e.getHouse(), e.getSignIndex(), e.getSignName(), e.getCuspLongitudeDeg()));
    }
    return out;
  }

  private KundaliSnapshotEntity requireSnapshot(Long id, String tenantId) {
    return kundaliRepository
        .findByIdAndTenantId(id, tenantId)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kundali snapshot not found"));
  }

  private ResolvedBirth resolveBirth(String tenantId, GenerateRequest request) {
    if (request.birthProfileId() != null) {
      BirthProfileEntity profile =
          profileRepository
              .findByIdAndTenantIdAndDeletedAtIsNull(request.birthProfileId(), tenantId)
              .orElseThrow(
                  () ->
                      new ResponseStatusException(
                          HttpStatus.NOT_FOUND, "Birth profile not found for this tenant"));
      BirthDetailsEntity details =
          detailsRepository
              .findByProfileIdAndTenantId(profile.getId(), tenantId)
              .orElseThrow(
                  () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Birth details missing"));
      BirthLocationEntity location =
          locationRepository
              .findByProfileIdAndTenantId(profile.getId(), tenantId)
              .orElseThrow(
                  () ->
                      new ResponseStatusException(HttpStatus.NOT_FOUND, "Birth location missing"));
      return new ResolvedBirth(
          profile.getId(),
          profile.getDisplayName(),
          details.getBirthDate(),
          details.getBirthTime(),
          details.isBirthTimeUnknown(),
          details.getTimeZone(),
          location.getPlaceName(),
          location.getLatitude(),
          location.getLongitude());
    }
    InlineBirthRequest b = request.birth();
    boolean timeUnknown = b.birthTimeUnknown() != null && b.birthTimeUnknown();
    if (!timeUnknown && b.birthTime() == null) {
      throw new IllegalArgumentException(
          "Birth time is required for accurate Lagna and house calculations.");
    }
    return new ResolvedBirth(
        null,
        b.displayName().trim(),
        b.birthDate(),
        timeUnknown ? null : b.birthTime(),
        timeUnknown,
        b.timeZone().trim(),
        b.placeName().trim(),
        b.latitude(),
        b.longitude());
  }

  private AyanamsaMode resolveAyanamsa(String tenantId, String override) {
    if (override != null && !override.isBlank()) {
      return AyanamsaMode.fromCode(override);
    }
    return workspaceRepository
        .findByTenantIdAndDeletedAtIsNull(tenantId)
        .map(ws -> AyanamsaMode.fromCode(ws.getAyanamsaCode()))
        .orElse(AyanamsaMode.LAHIRI);
  }

  private static PlanetaryPositionEntity toPlanetEntity(
      String tenantId, Long kundaliId, PlanetPosition p) {
    PlanetaryPositionEntity e = new PlanetaryPositionEntity();
    e.setTenantId(tenantId);
    e.setKundaliId(kundaliId);
    e.setPlanetCode(p.planet().name());
    e.setLongitudeDeg(bd(p.longitudeDeg(), 6));
    e.setSignIndex((short) p.signIndex());
    e.setSignName(p.signName());
    e.setDegreeInSign(bd(p.degreeInSign(), 6));
    e.setHouse((short) p.house());
    e.setNakshatraIndex((short) p.nakshatraIndex());
    e.setNakshatraName(p.nakshatraName());
    e.setPada((short) p.pada());
    e.setRetrograde(p.retrograde());
    e.setCombust(p.combust());
    if (p.speedDegPerDay() != null) {
      e.setSpeedDegPerDay(bd(p.speedDegPerDay(), 6));
    }
    return e;
  }

  private static PlanetDto toPlanetDto(PlanetPosition p) {
    return new PlanetDto(
        p.planet().name(),
        p.planet().displayName(),
        bd(p.longitudeDeg(), 6),
        p.signIndex(),
        p.signName(),
        bd(p.degreeInSign(), 6),
        p.house(),
        p.nakshatraIndex(),
        p.nakshatraName(),
        p.pada(),
        p.retrograde(),
        p.combust(),
        p.speedDegPerDay() == null ? null : bd(p.speedDegPerDay(), 6));
  }

  private static HouseDto toHouseDto(HouseCusp h) {
    return new HouseDto(h.house(), h.signIndex(), h.signName(), bd(h.cuspLongitudeDeg(), 6));
  }

  private static KundaliResponse toResponse(
      KundaliSnapshotEntity snap,
      PlanetDto ascendant,
      List<PlanetDto> planets,
      List<HouseDto> houses) {
    return new KundaliResponse(
        snap.getId(),
        snap.getBirthProfileId(),
        snap.getDisplayName(),
        snap.getBirthDate(),
        snap.getBirthTime(),
        snap.isBirthTimeUnknown(),
        snap.getTimeZone(),
        snap.getPlaceName(),
        snap.getLatitude(),
        snap.getLongitude(),
        snap.getAyanamsaCode(),
        snap.getAyanamsaDeg(),
        snap.getZodiacSystem(),
        snap.getHouseSystem(),
        snap.getChartStyle(),
        snap.getCalculationEngineVersion(),
        snap.getJulianDayUt(),
        ascendant,
        planets,
        houses,
        snap.getNotes(),
        COMING_SOON,
        snap.getCreatedAt());
  }

  private static String displayName(String code) {
    try {
      return Planet.valueOf(code).displayName();
    } catch (Exception ex) {
      return code;
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

  private record ResolvedBirth(
      Long profileId,
      String displayName,
      java.time.LocalDate birthDate,
      LocalTime birthTime,
      boolean birthTimeUnknown,
      String timeZone,
      String placeName,
      BigDecimal latitude,
      BigDecimal longitude) {}
}
