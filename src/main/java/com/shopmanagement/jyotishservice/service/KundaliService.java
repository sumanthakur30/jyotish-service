package com.shopmanagement.jyotishservice.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.shopmanagement.jyotishservice.api.KundaliApi.ChartCatalogItem;
import com.shopmanagement.jyotishservice.api.KundaliApi.ChartListResponse;
import com.shopmanagement.jyotishservice.api.KundaliApi.ComingSoonFeature;
import com.shopmanagement.jyotishservice.api.KundaliApi.DashaCatalogItem;
import com.shopmanagement.jyotishservice.api.KundaliApi.DashaCurrentDto;
import com.shopmanagement.jyotishservice.api.KundaliApi.DashaPeriodDto;
import com.shopmanagement.jyotishservice.api.KundaliApi.DashaResponse;
import com.shopmanagement.jyotishservice.api.KundaliApi.GenerateRequest;
import com.shopmanagement.jyotishservice.api.KundaliApi.HouseDto;
import com.shopmanagement.jyotishservice.api.KundaliApi.HouseListResponse;
import com.shopmanagement.jyotishservice.api.KundaliApi.InlineBirthRequest;
import com.shopmanagement.jyotishservice.api.KundaliApi.KundaliResponse;
import com.shopmanagement.jyotishservice.api.KundaliApi.PlanetDto;
import com.shopmanagement.jyotishservice.api.KundaliApi.PlanetListResponse;
import com.shopmanagement.jyotishservice.api.KundaliApi.VargaChartResponse;
import com.shopmanagement.jyotishservice.api.KundaliApi.YogaCatalogItem;
import com.shopmanagement.jyotishservice.api.KundaliApi.YogaDto;
import com.shopmanagement.jyotishservice.api.KundaliApi.YogaListResponse;
import com.shopmanagement.jyotishservice.engine.CalculationEngine;
import com.shopmanagement.jyotishservice.engine.ephemeris.EphemerisProvider;
import com.shopmanagement.jyotishservice.engine.ephemeris.SwissEphemerisProvider;
import com.shopmanagement.jyotishservice.engine.dasha.DashaLevel;
import com.shopmanagement.jyotishservice.engine.dasha.DashaPeriod;
import com.shopmanagement.jyotishservice.engine.dasha.DashaRegistry;
import com.shopmanagement.jyotishservice.engine.dasha.DashaSystemCode;
import com.shopmanagement.jyotishservice.engine.dasha.DashaTimeline;
import com.shopmanagement.jyotishservice.engine.model.AyanamsaMode;
import com.shopmanagement.jyotishservice.engine.model.BirthMoment;
import com.shopmanagement.jyotishservice.engine.model.ChartRequest;
import com.shopmanagement.jyotishservice.engine.model.D1Chart;
import com.shopmanagement.jyotishservice.engine.model.HouseCusp;
import com.shopmanagement.jyotishservice.engine.model.Planet;
import com.shopmanagement.jyotishservice.engine.model.PlanetPosition;
import com.shopmanagement.jyotishservice.engine.model.VargaChart;
import com.shopmanagement.jyotishservice.engine.varga.VargaCode;
import com.shopmanagement.jyotishservice.engine.varga.VargaRegistry;
import com.shopmanagement.jyotishservice.engine.yoga.YogaCategory;
import com.shopmanagement.jyotishservice.engine.yoga.YogaCode;
import com.shopmanagement.jyotishservice.engine.yoga.YogaHit;
import com.shopmanagement.jyotishservice.engine.yoga.YogaRegistry;
import com.shopmanagement.jyotishservice.engine.yoga.YogaReport;
import com.shopmanagement.jyotishservice.filter.TenantContextFilter;
import com.shopmanagement.jyotishservice.persistence.entity.BirthDetailsEntity;
import com.shopmanagement.jyotishservice.persistence.entity.BirthLocationEntity;
import com.shopmanagement.jyotishservice.persistence.entity.BirthProfileEntity;
import com.shopmanagement.jyotishservice.persistence.entity.DashaPeriodEntity;
import com.shopmanagement.jyotishservice.persistence.entity.DivisionalChartEntity;
import com.shopmanagement.jyotishservice.persistence.entity.DivisionalHousePositionEntity;
import com.shopmanagement.jyotishservice.persistence.entity.DivisionalPlanetPositionEntity;
import com.shopmanagement.jyotishservice.persistence.entity.HousePositionEntity;
import com.shopmanagement.jyotishservice.persistence.entity.KundaliSnapshotEntity;
import com.shopmanagement.jyotishservice.persistence.entity.PlanetaryPositionEntity;
import com.shopmanagement.jyotishservice.persistence.entity.YogaResultEntity;
import com.shopmanagement.jyotishservice.persistence.repo.BirthDetailsRepository;
import com.shopmanagement.jyotishservice.persistence.repo.BirthLocationRepository;
import com.shopmanagement.jyotishservice.persistence.repo.BirthProfileRepository;
import com.shopmanagement.jyotishservice.persistence.repo.DashaPeriodRepository;
import com.shopmanagement.jyotishservice.persistence.repo.DivisionalChartRepository;
import com.shopmanagement.jyotishservice.persistence.repo.DivisionalHousePositionRepository;
import com.shopmanagement.jyotishservice.persistence.repo.DivisionalPlanetPositionRepository;
import com.shopmanagement.jyotishservice.persistence.repo.HousePositionRepository;
import com.shopmanagement.jyotishservice.persistence.repo.JyotishWorkspaceRepository;
import com.shopmanagement.jyotishservice.persistence.repo.KundaliSnapshotRepository;
import com.shopmanagement.jyotishservice.persistence.repo.PlanetaryPositionRepository;
import com.shopmanagement.jyotishservice.persistence.repo.YogaResultRepository;

@Service
public class KundaliService {

  private static final List<ComingSoonFeature> COMING_SOON =
      List.of(
          new ComingSoonFeature("COMBUST", "Combust detection with classical orbs"),
          new ComingSoonFeature("SHADBALA", "Shadbala strength scores"),
          new ComingSoonFeature("YOGA_EXT", "Additional yogas beyond Phase 5 detectors"),
          new ComingSoonFeature("VARGA_EXT", "Additional Vargas beyond D2/D3/D9/D10"),
          new ComingSoonFeature("DASHA_EXT", "Yogini / Chara / Ashtottari dasha systems"),
          new ComingSoonFeature("MANGLIK_CANCEL", "Manglik cancellation rules (Phase 6+)"),
          new ComingSoonFeature("SADE_SATI", "Sade Sati / Saturn transit analysis (Phase 7+)"));

  private final CalculationEngine calculationEngine;
  private final EphemerisProvider ephemerisProvider;
  private final KundaliSnapshotRepository kundaliRepository;
  private final PlanetaryPositionRepository planetaryRepository;
  private final HousePositionRepository houseRepository;
  private final DivisionalChartRepository divisionalChartRepository;
  private final DivisionalPlanetPositionRepository divisionalPlanetRepository;
  private final DivisionalHousePositionRepository divisionalHouseRepository;
  private final DashaPeriodRepository dashaPeriodRepository;
  private final YogaResultRepository yogaResultRepository;
  private final BirthProfileRepository profileRepository;
  private final BirthDetailsRepository detailsRepository;
  private final BirthLocationRepository locationRepository;
  private final JyotishWorkspaceRepository workspaceRepository;

  public KundaliService(
      CalculationEngine calculationEngine,
      EphemerisProvider ephemerisProvider,
      KundaliSnapshotRepository kundaliRepository,
      PlanetaryPositionRepository planetaryRepository,
      HousePositionRepository houseRepository,
      DivisionalChartRepository divisionalChartRepository,
      DivisionalPlanetPositionRepository divisionalPlanetRepository,
      DivisionalHousePositionRepository divisionalHouseRepository,
      DashaPeriodRepository dashaPeriodRepository,
      YogaResultRepository yogaResultRepository,
      BirthProfileRepository profileRepository,
      BirthDetailsRepository detailsRepository,
      BirthLocationRepository locationRepository,
      JyotishWorkspaceRepository workspaceRepository) {
    this.calculationEngine = calculationEngine;
    this.ephemerisProvider = ephemerisProvider;
    this.kundaliRepository = kundaliRepository;
    this.planetaryRepository = planetaryRepository;
    this.houseRepository = houseRepository;
    this.divisionalChartRepository = divisionalChartRepository;
    this.divisionalPlanetRepository = divisionalPlanetRepository;
    this.divisionalHouseRepository = divisionalHouseRepository;
    this.dashaPeriodRepository = dashaPeriodRepository;
    this.yogaResultRepository = yogaResultRepository;
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
    snap.setInputJson(buildSnapshotInputJson(resolved.profileId() != null, ayanamsa));
    snap = kundaliRepository.save(snap);

    List<PlanetDto> planetDtos = new ArrayList<>();
    for (PlanetPosition p : chart.planets()) {
      PlanetaryPositionEntity row = toPlanetEntity(tenantId, snap.getId(), p);
      planetaryRepository.save(row);
      planetDtos.add(toPlanetDto(p));
    }
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

    // Eager D9 on generate (cheap; primary Phase 3 chart). Other Vargas lazy on first GET.
    VargaChart d9 = calculationEngine.computeVarga(chart, VargaCode.D9);
    persistVarga(tenantId, snap.getId(), d9);

    // Eager Vimshottari timeline (Phase 4).
    Instant birthAt = moment.toZonedDateTime().toInstant();
    double moonLon =
        chart.planets().stream()
            .filter(p -> p.planet() == Planet.MOON)
            .mapToDouble(PlanetPosition::longitudeDeg)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Moon missing from D1 chart"));
    DashaTimeline vimshottari =
        calculationEngine.computeDasha(DashaSystemCode.VIMSHOTTARI, moonLon, birthAt);
    persistDasha(tenantId, snap.getId(), vimshottari);

    // Eager yoga evaluation (Phase 5).
    YogaReport yogas = calculationEngine.computeYogas(chart, d9);
    persistYogas(tenantId, snap.getId(), yogas);

    return toResponse(snap, toPlanetDto(chart.ascendant()), planetDtos, houseDtos);
  }

  @Transactional(readOnly = true)
  public KundaliResponse get(Long id) {
    String tenantId = requireTenant();
    KundaliSnapshotEntity snap = requireSnapshot(id, tenantId);
    List<PlanetDto> planets =
        loadPlanets(id, tenantId).stream().filter(p -> !"ASCENDANT".equals(p.planetCode())).toList();
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

  @Transactional(readOnly = true)
  public ChartListResponse listCharts(Long kundaliId) {
    String tenantId = requireTenant();
    requireSnapshot(kundaliId, tenantId);
    Set<String> computed = new HashSet<>();
    computed.add(VargaCode.D1.code());
    for (DivisionalChartEntity row :
        divisionalChartRepository.findByKundaliIdAndTenantIdOrderByVargaCodeAsc(
            kundaliId, tenantId)) {
      computed.add(row.getVargaCode().toUpperCase(Locale.ROOT));
    }

    List<ChartCatalogItem> items = new ArrayList<>();
    for (VargaCode code : VargaRegistry.all()) {
      boolean implemented = VargaRegistry.isImplemented(code);
      boolean isComputed = computed.contains(code.code());
      String status;
      if (!implemented) {
        status = "COMING_SOON";
      } else if (code == VargaCode.D1 || isComputed) {
        status = "READY";
      } else {
        status = "LAZY"; // implemented; compute+store on first GET
      }
      items.add(
          new ChartCatalogItem(
              code.code(),
              code.displayName(),
              code.divisions(),
              implemented,
              code == VargaCode.D1 || isComputed,
              status));
    }
    return new ChartListResponse(kundaliId, items);
  }

  /**
   * Returns a varga chart. D1 is served from D1 tables. Implemented Vargas are loaded if stored,
   * otherwise computed from D1 longitudes and persisted (lazy). Unimplemented → 501 Coming Soon.
   */
  @Transactional
  public VargaChartResponse getChart(Long kundaliId, String vargaRaw) {
    String tenantId = requireTenant();
    requireSnapshot(kundaliId, tenantId);
    VargaCode code = VargaCode.parse(vargaRaw);

    if (!VargaRegistry.isImplemented(code)) {
      throw new ResponseStatusException(
          HttpStatus.NOT_IMPLEMENTED,
          code.code() + " (" + code.displayName() + ") is Coming Soon");
    }

    if (code == VargaCode.D1) {
      return d1AsVargaResponse(kundaliId, tenantId);
    }

    return divisionalChartRepository
        .findByKundaliIdAndTenantIdAndVargaCode(kundaliId, tenantId, code.code())
        .map(row -> toVargaResponse(kundaliId, row, tenantId))
        .orElseGet(() -> computeAndPersist(kundaliId, tenantId, code));
  }

  /**
   * Vimshottari (default) or another registered dasha system. Loads persisted rows if present;
   * otherwise computes from Moon longitude and stores (lazy for older snapshots).
   */
  @Transactional
  public DashaResponse getDasha(Long kundaliId, String systemRaw) {
    String tenantId = requireTenant();
    KundaliSnapshotEntity snap = requireSnapshot(kundaliId, tenantId);
    DashaSystemCode system = DashaSystemCode.parse(systemRaw);

    if (!DashaRegistry.isImplemented(system)) {
      throw new ResponseStatusException(
          HttpStatus.NOT_IMPLEMENTED,
          system.code() + " (" + system.displayName() + ") is Coming Soon");
    }

    Instant asOf = Instant.now();
    List<DashaPeriodEntity> rows =
        dashaPeriodRepository.findByKundaliIdAndTenantIdAndSystemCodeOrderByStartAtAscSequenceNoAsc(
            kundaliId, tenantId, system.code());
    DashaTimeline timelineMeta;
    List<DashaPeriod> tree;
    if (rows.isEmpty()) {
      DashaTimeline computed = computeAndPersistDasha(kundaliId, tenantId, snap, system);
      timelineMeta = computed;
      tree = computed.mahadashas();
    } else {
      tree = rebuildTree(rows);
      timelineMeta = metaFromStored(snap, system, rows, tree);
    }
    return toDashaResponse(kundaliId, timelineMeta, tree, asOf);
  }

  /**
   * Yoga evaluation for a kundali. Loads persisted detector results if present; otherwise computes
   * from D1 positions and stores (lazy for older snapshots). Optional {@code category} filters
   * results; catalog always lists Coming Soon entries.
   */
  @Transactional
  public YogaListResponse getYogas(Long kundaliId, String categoryRaw) {
    String tenantId = requireTenant();
    KundaliSnapshotEntity snap = requireSnapshot(kundaliId, tenantId);
    YogaCategory categoryFilter = null;
    if (categoryRaw != null && !categoryRaw.isBlank()) {
      categoryFilter = YogaCategory.parse(categoryRaw);
    }

    List<YogaResultEntity> rows =
        yogaResultRepository.findByKundaliIdAndTenantIdOrderByYogaCodeAsc(kundaliId, tenantId);
    YogaReport report;
    if (rows.isEmpty()) {
      report = computeAndPersistYogas(kundaliId, tenantId, snap);
      rows = yogaResultRepository.findByKundaliIdAndTenantIdOrderByYogaCodeAsc(kundaliId, tenantId);
    } else {
      report =
          new YogaReport(
              rows.get(0).getCalculationEngineVersion(),
              List.of(),
              "Yoga results from store; patterns are descriptive — not predictions.");
    }

    List<YogaDto> yogas = new ArrayList<>();
    for (YogaResultEntity row : rows) {
      if (categoryFilter != null && !categoryFilter.code().equals(row.getCategoryCode())) {
        continue;
      }
      yogas.add(toYogaDto(row));
    }

    List<YogaCatalogItem> catalog = buildYogaCatalog();
    return new YogaListResponse(
        kundaliId,
        report.engineVersion(),
        categoryFilter == null ? null : categoryFilter.code(),
        yogas,
        catalog,
        report.notes(),
        "Yoga presence flags geometric / dignity rules only. Not medical, legal, or life advice;"
            + " no guaranteed outcomes.");
  }

  private YogaReport computeAndPersistYogas(
      Long kundaliId, String tenantId, KundaliSnapshotEntity snap) {
    List<PlanetaryPositionEntity> rows =
        planetaryRepository.findByKundaliIdAndTenantIdOrderByPlanetCodeAsc(kundaliId, tenantId);
    List<PlanetPosition> planets = new ArrayList<>();
    for (PlanetaryPositionEntity r : rows) {
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
    VargaChart d9 = null;
    // Phase 5 detectors are D1-primary; optional D9 reinforcement can plug into YogaContext later.
    YogaReport report =
        calculationEngine.computeYogasFromPositions(snap.getAscendantSignIndex(), planets, d9);
    persistYogas(tenantId, kundaliId, report);
    return report;
  }

  private void persistYogas(String tenantId, Long kundaliId, YogaReport report) {
    yogaResultRepository.deleteByKundaliIdAndTenantId(kundaliId, tenantId);
    List<YogaResultEntity> batch = new ArrayList<>();
    for (YogaHit hit : report.hits()) {
      YogaResultEntity e = new YogaResultEntity();
      e.setTenantId(tenantId);
      e.setKundaliId(kundaliId);
      e.setYogaCode(hit.code().code());
      e.setCategoryCode(hit.code().category().code());
      e.setDisplayName(hit.code().displayName());
      e.setPresent(hit.present());
      e.setStrengthCode(hit.strength() == null ? null : hit.strength().code());
      e.setPlanetCodesJson(toJsonStringArray(hit.planetCodes()));
      e.setHousesJson(toJsonIntArray(hit.houses()));
      e.setExplanation(hit.explanation());
      e.setRuleId(hit.ruleId());
      e.setCalculationEngineVersion(report.engineVersion());
      e.setMetaJson("{\"ruleId\":\"" + hit.ruleId() + "\"}");
      batch.add(e);
    }
    yogaResultRepository.saveAll(batch);
  }

  private static List<YogaCatalogItem> buildYogaCatalog() {
    List<YogaCatalogItem> catalog = new ArrayList<>();
    for (YogaCode code : YogaRegistry.all()) {
      boolean implemented = YogaRegistry.isImplemented(code);
      catalog.add(
          new YogaCatalogItem(
              code.code(),
              code.displayName(),
              code.category().code(),
              code.category().displayName(),
              implemented,
              implemented ? "READY" : "COMING_SOON"));
    }
    return catalog;
  }

  private static YogaDto toYogaDto(YogaResultEntity row) {
    String strengthLabel = null;
    if (row.getStrengthCode() != null && !row.getStrengthCode().isBlank()) {
      strengthLabel = row.getStrengthCode();
    }
    return new YogaDto(
        row.getYogaCode(),
        row.getDisplayName(),
        row.getCategoryCode(),
        categoryDisplay(row.getCategoryCode()),
        row.isPresent(),
        row.getStrengthCode(),
        strengthLabel,
        parseStringArray(row.getPlanetCodesJson()),
        parseIntArray(row.getHousesJson()),
        row.getExplanation(),
        row.getRuleId());
  }

  private static String categoryDisplay(String code) {
    try {
      return YogaCategory.parse(code).displayName();
    } catch (Exception ex) {
      return code;
    }
  }

  private static String toJsonStringArray(List<String> items) {
    StringBuilder sb = new StringBuilder("[");
    for (int i = 0; i < items.size(); i++) {
      if (i > 0) {
        sb.append(',');
      }
      sb.append('"').append(items.get(i).replace("\"", "")).append('"');
    }
    sb.append(']');
    return sb.toString();
  }

  private static String toJsonIntArray(List<Integer> items) {
    StringBuilder sb = new StringBuilder("[");
    for (int i = 0; i < items.size(); i++) {
      if (i > 0) {
        sb.append(',');
      }
      sb.append(items.get(i));
    }
    sb.append(']');
    return sb.toString();
  }

  private static List<String> parseStringArray(String json) {
    if (json == null || json.isBlank() || "[]".equals(json.trim())) {
      return List.of();
    }
    String inner = json.trim();
    if (inner.startsWith("[")) {
      inner = inner.substring(1);
    }
    if (inner.endsWith("]")) {
      inner = inner.substring(0, inner.length() - 1);
    }
    if (inner.isBlank()) {
      return List.of();
    }
    List<String> out = new ArrayList<>();
    for (String part : inner.split(",")) {
      String p = part.trim();
      if (p.startsWith("\"") && p.endsWith("\"") && p.length() >= 2) {
        p = p.substring(1, p.length() - 1);
      }
      if (!p.isBlank()) {
        out.add(p);
      }
    }
    return out;
  }

  private static List<Integer> parseIntArray(String json) {
    if (json == null || json.isBlank() || "[]".equals(json.trim())) {
      return List.of();
    }
    String inner = json.trim();
    if (inner.startsWith("[")) {
      inner = inner.substring(1);
    }
    if (inner.endsWith("]")) {
      inner = inner.substring(0, inner.length() - 1);
    }
    if (inner.isBlank()) {
      return List.of();
    }
    List<Integer> out = new ArrayList<>();
    for (String part : inner.split(",")) {
      String p = part.trim();
      if (!p.isBlank()) {
        out.add(Integer.parseInt(p));
      }
    }
    return out;
  }

  private DashaTimeline computeAndPersistDasha(
      Long kundaliId, String tenantId, KundaliSnapshotEntity snap, DashaSystemCode system) {
    PlanetaryPositionEntity moon =
        planetaryRepository
            .findByKundaliIdAndTenantIdOrderByPlanetCodeAsc(kundaliId, tenantId)
            .stream()
            .filter(r -> "MOON".equals(r.getPlanetCode()))
            .findFirst()
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Moon missing for kundali"));
    Instant birthAt = birthInstant(snap);
    DashaTimeline timeline =
        calculationEngine.computeDasha(system, moon.getLongitudeDeg().doubleValue(), birthAt);
    persistDasha(tenantId, kundaliId, timeline);
    return timeline;
  }

  private void persistDasha(String tenantId, Long kundaliId, DashaTimeline timeline) {
    String system = timeline.system().code();
    dashaPeriodRepository.deleteByKundaliIdAndTenantIdAndSystemCode(kundaliId, tenantId, system);
    List<DashaPeriodEntity> batch = new ArrayList<>();
    flattenPersist(tenantId, kundaliId, timeline, timeline.mahadashas(), batch);
    dashaPeriodRepository.saveAll(batch);
  }

  private void flattenPersist(
      String tenantId,
      Long kundaliId,
      DashaTimeline timeline,
      List<DashaPeriod> periods,
      List<DashaPeriodEntity> batch) {
    for (DashaPeriod p : periods) {
      DashaPeriodEntity e = new DashaPeriodEntity();
      e.setTenantId(tenantId);
      e.setKundaliId(kundaliId);
      e.setSystemCode(timeline.system().code());
      e.setLevelCode(p.level().name());
      e.setLordCode(p.lord().name());
      e.setMahaLordCode(p.mahaLord().name());
      e.setAntarLordCode(p.antarLord() == null ? null : p.antarLord().name());
      e.setPratyantarLordCode(p.pratyantarLord() == null ? null : p.pratyantarLord().name());
      e.setSequenceNo(p.sequenceNo());
      e.setStartAt(p.startAt());
      e.setEndAt(p.endAt());
      e.setCalculationEngineVersion(timeline.engineVersion());
      e.setMetaJson(
          "{\"nakshatra\":\""
              + timeline.moonNakshatraName()
              + "\",\"balanceYears\":"
              + timeline.balanceAtBirthYears()
              + "}");
      batch.add(e);
      if (!p.children().isEmpty()) {
        flattenPersist(tenantId, kundaliId, timeline, p.children(), batch);
      }
    }
  }

  private static List<DashaPeriod> rebuildTree(List<DashaPeriodEntity> rows) {
    List<DashaPeriodEntity> mahas =
        rows.stream().filter(r -> DashaLevel.MAHA.name().equals(r.getLevelCode())).toList();
    List<DashaPeriod> tree = new ArrayList<>();
    for (DashaPeriodEntity m : mahas) {
      List<DashaPeriod> antars = new ArrayList<>();
      for (DashaPeriodEntity a : rows) {
        if (!DashaLevel.ANTAR.name().equals(a.getLevelCode())) {
          continue;
        }
        if (!m.getMahaLordCode().equals(a.getMahaLordCode())) {
          continue;
        }
        if (a.getStartAt().compareTo(m.getStartAt()) < 0
            || a.getEndAt().compareTo(m.getEndAt()) > 0) {
          continue;
        }
        List<DashaPeriod> praty = new ArrayList<>();
        for (DashaPeriodEntity p : rows) {
          if (!DashaLevel.PRATYANTAR.name().equals(p.getLevelCode())) {
            continue;
          }
          if (!a.getMahaLordCode().equals(p.getMahaLordCode())) {
            continue;
          }
          if (p.getAntarLordCode() == null || !p.getAntarLordCode().equals(a.getLordCode())) {
            continue;
          }
          if (p.getStartAt().compareTo(a.getStartAt()) < 0
              || p.getEndAt().compareTo(a.getEndAt()) > 0) {
            continue;
          }
          praty.add(toEnginePeriod(p, List.of()));
        }
        antars.add(toEnginePeriod(a, praty));
      }
      tree.add(toEnginePeriod(m, antars));
    }
    return tree;
  }

  private static DashaPeriod toEnginePeriod(DashaPeriodEntity e, List<DashaPeriod> children) {
    return new DashaPeriod(
        DashaLevel.valueOf(e.getLevelCode()),
        Planet.valueOf(e.getLordCode()),
        Planet.valueOf(e.getMahaLordCode()),
        e.getAntarLordCode() == null ? null : Planet.valueOf(e.getAntarLordCode()),
        e.getPratyantarLordCode() == null ? null : Planet.valueOf(e.getPratyantarLordCode()),
        e.getStartAt(),
        e.getEndAt(),
        e.getSequenceNo(),
        children);
  }

  private DashaTimeline metaFromStored(
      KundaliSnapshotEntity snap, DashaSystemCode system, List<DashaPeriodEntity> rows, List<DashaPeriod> tree) {
    String version =
        rows.isEmpty()
            ? snap.getCalculationEngineVersion()
            : rows.get(0).getCalculationEngineVersion();
    Instant birthAt = birthInstant(snap);
    PlanetaryPositionEntity moon =
        planetaryRepository
            .findByKundaliIdAndTenantIdOrderByPlanetCodeAsc(snap.getId(), snap.getTenantId())
            .stream()
            .filter(r -> "MOON".equals(r.getPlanetCode()))
            .findFirst()
            .orElse(null);
    if (moon != null && system == DashaSystemCode.VIMSHOTTARI) {
      var bal =
          com.shopmanagement.jyotishservice.engine.dasha.VimshottariDashaCalculator.balanceAtBirth(
              moon.getLongitudeDeg().doubleValue());
      return new DashaTimeline(
          system,
          version,
          birthAt,
          bal.nakshatraIndex(),
          bal.nakshatraName(),
          bal.lord(),
          bal.balanceYears(),
          bal.elapsedYears(),
          tree,
          "Vimshottari from stored periods; Moon "
              + bal.nakshatraName()
              + "; balance "
              + String.format(Locale.ROOT, "%.4f", bal.balanceYears())
              + " y.");
    }
    Planet birthLord = tree.isEmpty() ? Planet.KETU : tree.get(0).lord();
    return new DashaTimeline(system, version, birthAt, 0, "", birthLord, 0, 0, tree, snap.getNotes());
  }

  private static Instant birthInstant(KundaliSnapshotEntity snap) {
    ZoneId zone;
    try {
      zone = ZoneId.of(snap.getTimeZone());
    } catch (Exception ex) {
      zone = ZoneId.of("UTC");
    }
    LocalTime time =
        snap.isBirthTimeUnknown()
            ? LocalTime.NOON
            : (snap.getBirthTime() != null ? snap.getBirthTime() : LocalTime.NOON);
    return snap.getBirthDate().atTime(time).atZone(zone).toInstant();
  }

  private DashaResponse toDashaResponse(
      Long kundaliId, DashaTimeline meta, List<DashaPeriod> tree, Instant asOf) {
    List<DashaPeriodDto> timeline = tree.stream().map(p -> toPeriodDto(p, asOf)).toList();
    DashaPeriodDto curMaha = findCurrent(timeline);
    DashaPeriodDto curAntar =
        curMaha == null
            ? null
            : curMaha.children().stream().filter(DashaPeriodDto::current).findFirst().orElse(null);
    DashaPeriodDto curPraty =
        curAntar == null
            ? null
            : curAntar.children().stream()
                .filter(DashaPeriodDto::current)
                .findFirst()
                .orElse(null);

    List<DashaCatalogItem> catalog = new ArrayList<>();
    for (DashaSystemCode code : DashaRegistry.all()) {
      boolean implemented = DashaRegistry.isImplemented(code);
      catalog.add(
          new DashaCatalogItem(
              code.code(),
              code.displayName(),
              implemented,
              implemented ? "READY" : "COMING_SOON"));
    }

    return new DashaResponse(
        kundaliId,
        meta.system().code(),
        meta.system().displayName(),
        meta.engineVersion(),
        meta.moonNakshatraIndex(),
        meta.moonNakshatraName(),
        meta.birthMahadashaLord().name(),
        bd(meta.balanceAtBirthYears(), 6),
        bd(meta.elapsedAtBirthYears(), 6),
        new DashaCurrentDto(stripChildren(curMaha), stripChildren(curAntar), stripChildren(curPraty)),
        timeline,
        catalog,
        meta.notes(),
        "Coming Soon",
        asOf);
  }

  private static DashaPeriodDto stripChildren(DashaPeriodDto dto) {
    if (dto == null) {
      return null;
    }
    return new DashaPeriodDto(
        dto.level(),
        dto.lordCode(),
        dto.lordName(),
        dto.mahaLordCode(),
        dto.antarLordCode(),
        dto.pratyantarLordCode(),
        dto.startAt(),
        dto.endAt(),
        dto.remainingDays(),
        dto.current(),
        List.of());
  }

  private static DashaPeriodDto findCurrent(List<DashaPeriodDto> timeline) {
    return timeline.stream().filter(DashaPeriodDto::current).findFirst().orElse(null);
  }

  private static DashaPeriodDto toPeriodDto(DashaPeriod p, Instant asOf) {
    boolean current = p.contains(asOf);
    Long remaining =
        asOf.isBefore(p.endAt())
            ? Duration.between(asOf.isBefore(p.startAt()) ? p.startAt() : asOf, p.endAt()).toDays()
            : 0L;
    List<DashaPeriodDto> kids =
        p.children().stream().map(c -> toPeriodDto(c, asOf)).toList();
    return new DashaPeriodDto(
        p.level().name(),
        p.lord().name(),
        p.lord().displayName(),
        p.mahaLord() == null ? null : p.mahaLord().name(),
        p.antarLord() == null ? null : p.antarLord().name(),
        p.pratyantarLord() == null ? null : p.pratyantarLord().name(),
        p.startAt(),
        p.endAt(),
        remaining,
        current,
        kids);
  }

  private VargaChartResponse computeAndPersist(Long kundaliId, String tenantId, VargaCode code) {
    List<PlanetaryPositionEntity> rows =
        planetaryRepository.findByKundaliIdAndTenantIdOrderByPlanetCodeAsc(kundaliId, tenantId);
    PlanetaryPositionEntity ascRow =
        rows.stream()
            .filter(r -> "ASCENDANT".equals(r.getPlanetCode()))
            .findFirst()
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "D1 ascendant missing for kundali"));

    List<CalculationEngine.PlanetLongitude> longs = new ArrayList<>();
    for (PlanetaryPositionEntity r : rows) {
      if ("ASCENDANT".equals(r.getPlanetCode())) {
        continue;
      }
      Planet planet;
      try {
        planet = Planet.valueOf(r.getPlanetCode());
      } catch (Exception ex) {
        continue;
      }
      longs.add(
          new CalculationEngine.PlanetLongitude(
              planet,
              r.getLongitudeDeg().doubleValue(),
              r.isRetrograde(),
              r.getSpeedDegPerDay() == null ? null : r.getSpeedDegPerDay().doubleValue()));
    }

    VargaChart chart =
        calculationEngine.computeVargaFromLongitudes(
            code, ascRow.getLongitudeDeg().doubleValue(), longs);
    DivisionalChartEntity saved = persistVarga(tenantId, kundaliId, chart);
    return toVargaResponse(kundaliId, saved, tenantId);
  }

  private DivisionalChartEntity persistVarga(String tenantId, Long kundaliId, VargaChart chart) {
    DivisionalChartEntity entity = new DivisionalChartEntity();
    entity.setTenantId(tenantId);
    entity.setKundaliId(kundaliId);
    entity.setVargaCode(chart.varga().code());
    entity.setCalculationEngineVersion(chart.engineVersion());
    entity.setHouseSystem(chart.houseSystem());
    entity.setAscendantLongitude(bd(chart.ascendant().longitudeDeg(), 6));
    entity.setAscendantSignIndex((short) chart.ascendant().signIndex());
    entity.setNotes(chart.notes());
    entity.setMetaJson(
        "{\"varga\":\""
            + chart.varga().code()
            + "\",\"source\":\"D1_LONGITUDES\",\"engine\":\""
            + chart.engineVersion()
            + "\"}");
    entity = divisionalChartRepository.save(entity);

    DivisionalPlanetPositionEntity asc = toDivPlanetEntity(tenantId, entity.getId(), chart.ascendant());
    divisionalPlanetRepository.save(asc);
    for (PlanetPosition p : chart.planets()) {
      divisionalPlanetRepository.save(toDivPlanetEntity(tenantId, entity.getId(), p));
    }
    for (HouseCusp h : chart.houses()) {
      DivisionalHousePositionEntity row = new DivisionalHousePositionEntity();
      row.setTenantId(tenantId);
      row.setDivisionalChartId(entity.getId());
      row.setHouse((short) h.house());
      row.setSignIndex((short) h.signIndex());
      row.setSignName(h.signName());
      row.setCuspLongitudeDeg(bd(h.cuspLongitudeDeg(), 6));
      divisionalHouseRepository.save(row);
    }
    return entity;
  }

  private VargaChartResponse d1AsVargaResponse(Long kundaliId, String tenantId) {
    KundaliSnapshotEntity snap = requireSnapshot(kundaliId, tenantId);
    List<PlanetDto> all = loadPlanets(kundaliId, tenantId);
    PlanetDto asc =
        all.stream().filter(p -> "ASCENDANT".equals(p.planetCode())).findFirst().orElse(null);
    List<PlanetDto> planets =
        all.stream().filter(p -> !"ASCENDANT".equals(p.planetCode())).toList();
    return new VargaChartResponse(
        kundaliId,
        null,
        VargaCode.D1.code(),
        VargaCode.D1.displayName(),
        snap.getCalculationEngineVersion(),
        snap.getHouseSystem(),
        asc,
        planets,
        loadHouses(kundaliId, tenantId),
        snap.getNotes(),
        false,
        snap.getCreatedAt());
  }

  private VargaChartResponse toVargaResponse(
      Long kundaliId, DivisionalChartEntity row, String tenantId) {
    List<PlanetDto> all = loadDivPlanets(row.getId(), tenantId);
    PlanetDto asc =
        all.stream().filter(p -> "ASCENDANT".equals(p.planetCode())).findFirst().orElse(null);
    List<PlanetDto> planets =
        all.stream().filter(p -> !"ASCENDANT".equals(p.planetCode())).toList();
    List<HouseDto> houses = loadDivHouses(row.getId(), tenantId);
    VargaCode code = VargaCode.parse(row.getVargaCode());
    return new VargaChartResponse(
        kundaliId,
        row.getId(),
        code.code(),
        code.displayName(),
        row.getCalculationEngineVersion(),
        row.getHouseSystem(),
        asc,
        planets,
        houses,
        row.getNotes(),
        false,
        row.getCreatedAt());
  }

  private List<PlanetDto> loadDivPlanets(Long chartId, String tenantId) {
    List<PlanetDto> out = new ArrayList<>();
    for (DivisionalPlanetPositionEntity e :
        divisionalPlanetRepository.findByDivisionalChartIdAndTenantIdOrderByPlanetCodeAsc(
            chartId, tenantId)) {
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

  private List<HouseDto> loadDivHouses(Long chartId, String tenantId) {
    List<HouseDto> out = new ArrayList<>();
    for (DivisionalHousePositionEntity e :
        divisionalHouseRepository.findByDivisionalChartIdAndTenantIdOrderByHouseAsc(
            chartId, tenantId)) {
      out.add(
          new HouseDto(e.getHouse(), e.getSignIndex(), e.getSignName(), e.getCuspLongitudeDeg()));
    }
    return out;
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

  private static DivisionalPlanetPositionEntity toDivPlanetEntity(
      String tenantId, Long chartId, PlanetPosition p) {
    DivisionalPlanetPositionEntity e = new DivisionalPlanetPositionEntity();
    e.setTenantId(tenantId);
    e.setDivisionalChartId(chartId);
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

  private String buildSnapshotInputJson(boolean fromProfile, AyanamsaMode ayanamsa) {
    boolean swissFiles =
        ephemerisProvider instanceof SwissEphemerisProvider swiss && swiss.usingFiles();
    return "{\"source\":\""
        + (fromProfile ? "profile" : "inline")
        + "\",\"ayanamsa\":\""
        + ayanamsa.name()
        + "\",\"ephemerisProvider\":\""
        + ephemerisProvider.code()
        + "\",\"swissUsingFiles\":"
        + swissFiles
        + "}";
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
