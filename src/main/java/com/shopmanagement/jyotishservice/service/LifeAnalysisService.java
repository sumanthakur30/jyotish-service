package com.shopmanagement.jyotishservice.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopmanagement.jyotishservice.api.LifeAnalysisApi.AnalysisDetailResponse;
import com.shopmanagement.jyotishservice.api.LifeAnalysisApi.CategorySummary;
import com.shopmanagement.jyotishservice.api.LifeAnalysisApi.ConsultationItem;
import com.shopmanagement.jyotishservice.api.LifeAnalysisApi.ConsultationListResponse;
import com.shopmanagement.jyotishservice.api.LifeAnalysisApi.DashboardResponse;
import com.shopmanagement.jyotishservice.api.LifeAnalysisApi.HistoryItem;
import com.shopmanagement.jyotishservice.api.LifeAnalysisApi.HistoryListResponse;
import com.shopmanagement.jyotishservice.api.LifeAnalysisApi.PeriodDto;
import com.shopmanagement.jyotishservice.api.LifeAnalysisApi.PeriodListResponse;
import com.shopmanagement.jyotishservice.api.LifeAnalysisApi.SearchHit;
import com.shopmanagement.jyotishservice.api.LifeAnalysisApi.SearchResponse;
import com.shopmanagement.jyotishservice.api.LifeAnalysisApi.UpsertAnalysisRequest;
import com.shopmanagement.jyotishservice.api.LifeAnalysisApi.UpsertPeriodRequest;
import com.shopmanagement.jyotishservice.engine.life.LifeCategory;
import com.shopmanagement.jyotishservice.filter.TenantContextFilter;
import com.shopmanagement.jyotishservice.persistence.entity.DashaPeriodEntity;
import com.shopmanagement.jyotishservice.persistence.entity.LifeAnalysisConsultationEntity;
import com.shopmanagement.jyotishservice.persistence.entity.LifeAnalysisEntity;
import com.shopmanagement.jyotishservice.persistence.entity.LifeAnalysisHistoryEntity;
import com.shopmanagement.jyotishservice.persistence.entity.LifeAnalysisPeriodEntity;
import com.shopmanagement.jyotishservice.persistence.entity.TransitPlanetPositionEntity;
import com.shopmanagement.jyotishservice.persistence.entity.TransitSnapshotEntity;
import com.shopmanagement.jyotishservice.persistence.repo.DashaPeriodRepository;
import com.shopmanagement.jyotishservice.persistence.repo.HousePositionRepository;
import com.shopmanagement.jyotishservice.persistence.repo.KundaliSnapshotRepository;
import com.shopmanagement.jyotishservice.persistence.repo.LifeAnalysisConsultationRepository;
import com.shopmanagement.jyotishservice.persistence.repo.LifeAnalysisHistoryRepository;
import com.shopmanagement.jyotishservice.persistence.repo.LifeAnalysisPeriodRepository;
import com.shopmanagement.jyotishservice.persistence.repo.LifeAnalysisRepository;
import com.shopmanagement.jyotishservice.persistence.repo.PlanetaryPositionRepository;
import com.shopmanagement.jyotishservice.persistence.repo.TransitPlanetPositionRepository;
import com.shopmanagement.jyotishservice.persistence.repo.TransitSnapshotRepository;
import com.shopmanagement.jyotishservice.persistence.repo.YogaResultRepository;
import com.shopmanagement.jyotishservice.service.life.LifeAnalysisIndicatorBuilder;

@Service
public class LifeAnalysisService {

  public static final String HEALTH_DISCLAIMER_EN =
      "This section provides traditional Jyotish observations and is not medical advice or a medical diagnosis.";
  public static final String HEALTH_DISCLAIMER_HI =
      "यह अनुभाग पारंपरिक ज्योतिषीय अवलोकन प्रदान करता है। यह चिकित्सकीय सलाह या चिकित्सा निदान नहीं है।";

  private final KundaliSnapshotRepository kundaliRepository;
  private final LifeAnalysisRepository analysisRepository;
  private final LifeAnalysisPeriodRepository periodRepository;
  private final LifeAnalysisHistoryRepository historyRepository;
  private final LifeAnalysisConsultationRepository consultationRepository;
  private final HousePositionRepository houseRepository;
  private final PlanetaryPositionRepository planetaryRepository;
  private final YogaResultRepository yogaRepository;
  private final DashaPeriodRepository dashaRepository;
  private final TransitSnapshotRepository transitSnapshotRepository;
  private final TransitPlanetPositionRepository transitPlanetRepository;
  private final LifeAnalysisIndicatorBuilder indicatorBuilder;
  private final ObjectMapper objectMapper;

  public LifeAnalysisService(
      KundaliSnapshotRepository kundaliRepository,
      LifeAnalysisRepository analysisRepository,
      LifeAnalysisPeriodRepository periodRepository,
      LifeAnalysisHistoryRepository historyRepository,
      LifeAnalysisConsultationRepository consultationRepository,
      HousePositionRepository houseRepository,
      PlanetaryPositionRepository planetaryRepository,
      YogaResultRepository yogaRepository,
      DashaPeriodRepository dashaRepository,
      TransitSnapshotRepository transitSnapshotRepository,
      TransitPlanetPositionRepository transitPlanetRepository,
      LifeAnalysisIndicatorBuilder indicatorBuilder,
      ObjectMapper objectMapper) {
    this.kundaliRepository = kundaliRepository;
    this.analysisRepository = analysisRepository;
    this.periodRepository = periodRepository;
    this.historyRepository = historyRepository;
    this.consultationRepository = consultationRepository;
    this.houseRepository = houseRepository;
    this.planetaryRepository = planetaryRepository;
    this.yogaRepository = yogaRepository;
    this.dashaRepository = dashaRepository;
    this.transitSnapshotRepository = transitSnapshotRepository;
    this.transitPlanetRepository = transitPlanetRepository;
    this.indicatorBuilder = indicatorBuilder;
    this.objectMapper = objectMapper;
  }

  @Transactional(readOnly = true)
  public DashboardResponse dashboard(Long kundaliId) {
    String tenantId = requireTenant();
    requireKundali(kundaliId, tenantId);
    Map<String, LifeAnalysisEntity> existing = new LinkedHashMap<>();
    for (LifeAnalysisEntity row :
        analysisRepository.findByKundaliIdAndTenantIdOrderByCategoryAsc(kundaliId, tenantId)) {
      if (row.getSubCategory() == null || row.getSubCategory().isBlank()) {
        existing.put(row.getCategory(), row);
      }
    }
    List<CategorySummary> cards = new ArrayList<>();
    for (LifeCategory cat : LifeCategory.values()) {
      if (cat == LifeCategory.GENERAL) {
        continue;
      }
      LifeAnalysisEntity row = existing.get(cat.code());
      cards.add(
          new CategorySummary(
              cat.code(),
              cat.labelEn(),
              cat.labelHi(),
              row != null ? row.getStatus() : "NOT_STARTED",
              row != null ? row.getUpdatedAt() : null,
              row == null || row.isIncludeInReport()));
    }
    return new DashboardResponse(kundaliId, cards);
  }

  @Transactional(readOnly = true)
  public AnalysisDetailResponse get(Long kundaliId, String categoryRaw) {
    String tenantId = requireTenant();
    requireKundali(kundaliId, tenantId);
    LifeCategory category = LifeCategory.parse(categoryRaw);
    LifeAnalysisEntity row =
        analysisRepository
            .findByKundaliIdAndTenantIdAndCategoryAndSubCategory(
                kundaliId, tenantId, category.code(), "")
            .orElse(null);
    return toDetail(kundaliId, tenantId, category, row);
  }

  @Transactional
  public AnalysisDetailResponse upsert(Long kundaliId, String categoryRaw, UpsertAnalysisRequest req) {
    String tenantId = requireTenant();
    requireKundali(kundaliId, tenantId);
    LifeCategory category = LifeCategory.parse(categoryRaw);
    if (req == null) {
      throw new IllegalArgumentException("body is required");
    }
    LifeAnalysisEntity row =
        analysisRepository
            .findByKundaliIdAndTenantIdAndCategoryAndSubCategory(
                kundaliId, tenantId, category.code(), "")
            .orElseGet(
                () -> {
                  LifeAnalysisEntity e = new LifeAnalysisEntity();
                  e.setTenantId(tenantId);
                  e.setKundaliId(kundaliId);
                  e.setCategory(category.code());
                  e.setSubCategory("");
                  e.setCreatedBy(req.updatedBy());
                  return e;
                });

    String actor = req.updatedBy() != null ? req.updatedBy() : TenantContextFilter.getCurrentUserId();
    track(row, "status", row.getStatus(), LifeAnalysisIndicatorBuilder.normalizeStatus(req.status()), actor);
    track(row, "pastNotes", row.getPastNotes(), req.pastNotes(), actor);
    track(row, "presentNotes", row.getPresentNotes(), req.presentNotes(), actor);
    track(row, "futureNotes", row.getFutureNotes(), req.futureNotes(), actor);
    track(row, "importantPeriodsNotes", row.getImportantPeriodsNotes(), req.importantPeriodsNotes(), actor);
    track(row, "advice", row.getAdvice(), req.advice(), actor);
    track(row, "jyotishNotes", row.getJyotishNotes(), req.jyotishNotes(), actor);

    String newSections = writeSections(req.sections());
    track(row, "sectionsJson", row.getSectionsJson(), newSections, actor);

    row.setStatus(LifeAnalysisIndicatorBuilder.normalizeStatus(req.status()));
    row.setPastNotes(req.pastNotes());
    row.setPresentNotes(req.presentNotes());
    row.setFutureNotes(req.futureNotes());
    row.setImportantPeriodsNotes(req.importantPeriodsNotes());
    row.setAdvice(req.advice());
    row.setJyotishNotes(req.jyotishNotes());
    row.setSectionsJson(newSections);
    if (req.includeInReport() != null) {
      row.setIncludeInReport(req.includeInReport());
    }
    row.setUpdatedBy(actor);
    if (row.getStatus() != null
        && !"NOT_STARTED".equals(row.getStatus())
        && isEffectivelyEmpty(row)
        && "NOT_STARTED".equals(LifeAnalysisIndicatorBuilder.normalizeStatus(req.status()))) {
      // keep
    }
    if (!"NOT_STARTED".equals(row.getStatus()) && isBlankAll(req) && row.getId() == null) {
      row.setStatus("NOT_STARTED");
    }
    if (row.getId() == null && hasContent(req)) {
      if ("NOT_STARTED".equals(row.getStatus())) {
        row.setStatus("IN_PROGRESS");
      }
    }

    LifeAnalysisEntity saved = analysisRepository.save(row);

    if (Boolean.TRUE.equals(req.recordConsultation())) {
      LifeAnalysisConsultationEntity c = new LifeAnalysisConsultationEntity();
      c.setTenantId(tenantId);
      c.setKundaliId(kundaliId);
      c.setCategory(category.code());
      c.setObservation(
          req.consultationObservation() != null
              ? req.consultationObservation()
              : trimJoin(req.presentNotes(), req.jyotishNotes()));
      c.setDashaSnapshot(req.consultationDashaSnapshot());
      c.setGocharSnapshot(req.consultationGocharSnapshot());
      c.setAdvice(req.consultationAdvice() != null ? req.consultationAdvice() : req.advice());
      c.setFollowUpDate(req.followUpDate());
      c.setCreatedBy(actor);
      consultationRepository.save(c);
    }

    return toDetail(kundaliId, tenantId, category, saved);
  }

  @Transactional(readOnly = true)
  public PeriodListResponse listPeriods(Long kundaliId, String category) {
    String tenantId = requireTenant();
    requireKundali(kundaliId, tenantId);
    List<LifeAnalysisPeriodEntity> rows =
        category == null || category.isBlank()
            ? periodRepository.findByKundaliIdAndTenantIdOrderBySortOrderAscFromDateAsc(
                kundaliId, tenantId)
            : periodRepository.findByKundaliIdAndTenantIdAndCategoryOrderBySortOrderAscFromDateAsc(
                kundaliId, tenantId, LifeCategory.parse(category).code());
    return new PeriodListResponse(kundaliId, rows.stream().map(this::toPeriod).toList());
  }

  @Transactional
  public PeriodDto createPeriod(Long kundaliId, UpsertPeriodRequest req) {
    String tenantId = requireTenant();
    requireKundali(kundaliId, tenantId);
    LifeCategory.parse(req.category());
    LifeAnalysisPeriodEntity e = new LifeAnalysisPeriodEntity();
    e.setTenantId(tenantId);
    e.setKundaliId(kundaliId);
    applyPeriod(e, req);
    return toPeriod(periodRepository.save(e));
  }

  @Transactional
  public PeriodDto updatePeriod(Long kundaliId, Long periodId, UpsertPeriodRequest req) {
    String tenantId = requireTenant();
    requireKundali(kundaliId, tenantId);
    LifeAnalysisPeriodEntity e =
        periodRepository
            .findByIdAndTenantId(periodId, tenantId)
            .filter(p -> p.getKundaliId().equals(kundaliId))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Period not found"));
    applyPeriod(e, req);
    return toPeriod(periodRepository.save(e));
  }

  @Transactional
  public void deletePeriod(Long kundaliId, Long periodId) {
    String tenantId = requireTenant();
    LifeAnalysisPeriodEntity e =
        periodRepository
            .findByIdAndTenantId(periodId, tenantId)
            .filter(p -> p.getKundaliId().equals(kundaliId))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Period not found"));
    periodRepository.delete(e);
  }

  @Transactional(readOnly = true)
  public HistoryListResponse history(Long kundaliId, String categoryRaw) {
    String tenantId = requireTenant();
    requireKundali(kundaliId, tenantId);
    LifeCategory category = LifeCategory.parse(categoryRaw);
    Optional<LifeAnalysisEntity> row =
        analysisRepository.findByKundaliIdAndTenantIdAndCategoryAndSubCategory(
            kundaliId, tenantId, category.code(), "");
    if (row.isEmpty()) {
      return new HistoryListResponse(null, List.of());
    }
    List<HistoryItem> items =
        historyRepository
            .findByLifeAnalysisIdAndTenantIdOrderByCreatedAtDesc(row.get().getId(), tenantId)
            .stream()
            .map(
                h ->
                    new HistoryItem(
                        h.getId(),
                        h.getFieldName(),
                        h.getOldValue(),
                        h.getNewValue(),
                        h.getUpdatedBy(),
                        h.getCreatedAt()))
            .toList();
    return new HistoryListResponse(row.get().getId(), items);
  }

  @Transactional(readOnly = true)
  public ConsultationListResponse consultations(Long kundaliId) {
    String tenantId = requireTenant();
    requireKundali(kundaliId, tenantId);
    List<ConsultationItem> items =
        consultationRepository
            .findByKundaliIdAndTenantIdOrderByCreatedAtDesc(kundaliId, tenantId)
            .stream()
            .map(
                c ->
                    new ConsultationItem(
                        c.getId(),
                        c.getCategory(),
                        c.getObservation(),
                        c.getDashaSnapshot(),
                        c.getGocharSnapshot(),
                        c.getAdvice(),
                        c.getFollowUpDate(),
                        c.getCreatedBy(),
                        c.getCreatedAt()))
            .toList();
    return new ConsultationListResponse(kundaliId, items);
  }

  @Transactional(readOnly = true)
  public SearchResponse search(Long kundaliId, String query) {
    String tenantId = requireTenant();
    requireKundali(kundaliId, tenantId);
    String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
    if (q.isBlank()) {
      return new SearchResponse(kundaliId, query, List.of());
    }
    List<SearchHit> hits = new ArrayList<>();
    for (LifeAnalysisEntity row :
        analysisRepository.findByKundaliIdAndTenantIdOrderByCategoryAsc(kundaliId, tenantId)) {
      matchField(hits, row, "pastNotes", row.getPastNotes(), q);
      matchField(hits, row, "presentNotes", row.getPresentNotes(), q);
      matchField(hits, row, "futureNotes", row.getFutureNotes(), q);
      matchField(hits, row, "advice", row.getAdvice(), q);
      matchField(hits, row, "jyotishNotes", row.getJyotishNotes(), q);
      matchField(hits, row, "sections", row.getSectionsJson(), q);
    }
    return new SearchResponse(kundaliId, query, hits);
  }

  private void matchField(
      List<SearchHit> hits, LifeAnalysisEntity row, String field, String value, String q) {
    if (value == null) {
      return;
    }
    int idx = value.toLowerCase(Locale.ROOT).indexOf(q);
    if (idx < 0) {
      return;
    }
    int start = Math.max(0, idx - 40);
    int end = Math.min(value.length(), idx + q.length() + 40);
    hits.add(new SearchHit(row.getCategory(), field, value.substring(start, end), row.getId()));
  }

  private AnalysisDetailResponse toDetail(
      Long kundaliId, String tenantId, LifeCategory category, LifeAnalysisEntity row) {
    var houses = houseRepository.findByKundaliIdAndTenantIdOrderByHouseAsc(kundaliId, tenantId);
    var planets =
        planetaryRepository.findByKundaliIdAndTenantIdOrderByPlanetCodeAsc(kundaliId, tenantId);
    var yogas = yogaRepository.findByKundaliIdAndTenantIdOrderByYogaCodeAsc(kundaliId, tenantId);
    String dasha = currentDashaSummary(kundaliId, tenantId);
    String gochar = currentGocharSummary(kundaliId, tenantId);
    var indicators =
        indicatorBuilder.build(category, houses, planets, yogas, dasha, gochar);

    return new AnalysisDetailResponse(
        row != null ? row.getId() : null,
        kundaliId,
        category.code(),
        row != null ? row.getSubCategory() : "",
        row != null ? row.getStatus() : "NOT_STARTED",
        row != null ? row.getPastNotes() : null,
        row != null ? row.getPresentNotes() : null,
        row != null ? row.getFutureNotes() : null,
        row != null ? row.getImportantPeriodsNotes() : null,
        row != null ? row.getAdvice() : null,
        row != null ? row.getJyotishNotes() : null,
        row != null ? readSections(row.getSectionsJson()) : Map.of(),
        row == null || row.isIncludeInReport(),
        indicators,
        category == LifeCategory.HEALTH ? HEALTH_DISCLAIMER_EN : null,
        category == LifeCategory.HEALTH ? HEALTH_DISCLAIMER_HI : null,
        row != null ? row.getCreatedAt() : null,
        row != null ? row.getUpdatedAt() : null,
        row != null ? row.getUpdatedBy() : null);
  }

  private String currentDashaSummary(Long kundaliId, String tenantId) {
    Instant now = Instant.now();
    List<DashaPeriodEntity> rows =
        dashaRepository.findByKundaliIdAndTenantIdAndSystemCodeOrderByStartAtAscSequenceNoAsc(
            kundaliId, tenantId, "VIMSHOTTARI");
    String maha = null;
    String antar = null;
    for (DashaPeriodEntity r : rows) {
      if (r.getStartAt() == null || r.getEndAt() == null) {
        continue;
      }
      if (now.isBefore(r.getStartAt()) || !now.isBefore(r.getEndAt())) {
        continue;
      }
      if ("MAHA".equalsIgnoreCase(r.getLevelCode())) {
        maha = r.getLordCode();
      } else if ("ANTAR".equalsIgnoreCase(r.getLevelCode())) {
        antar = r.getLordCode();
      }
    }
    if (maha == null) {
      return null;
    }
    return antar != null ? maha + " / " + antar : maha;
  }

  private String currentGocharSummary(Long kundaliId, String tenantId) {
    Optional<TransitSnapshotEntity> snap =
        transitSnapshotRepository.findFirstByKundaliIdAndTenantIdOrderByTransitDateDescCreatedAtDesc(
            kundaliId, tenantId);
    if (snap.isEmpty()) {
      return null;
    }
    List<TransitPlanetPositionEntity> planets =
        transitPlanetRepository.findByTransitIdAndTenantIdOrderByPlanetCodeAsc(
            snap.get().getId(), tenantId);
    return planets.stream()
        .limit(6)
        .map(p -> p.getPlanetCode() + " " + p.getSignName() + " H" + p.getHouse())
        .reduce((a, b) -> a + "; " + b)
        .orElse(null);
  }

  private void applyPeriod(LifeAnalysisPeriodEntity e, UpsertPeriodRequest req) {
    e.setCategory(LifeCategory.parse(req.category()).code());
    e.setFromDate(req.fromDate());
    e.setToDate(req.toDate());
    e.setTopic(req.topic());
    e.setObservation(req.observation());
    e.setCalculationBasis(req.calculationBasis());
    e.setStatus(
        req.status() == null || req.status().isBlank()
            ? "PLANNED"
            : req.status().trim().toUpperCase(Locale.ROOT));
    e.setSortOrder(req.sortOrder() != null ? req.sortOrder() : 0);
    e.setUpdatedBy(req.updatedBy());
    if (e.getCreatedBy() == null) {
      e.setCreatedBy(req.updatedBy());
    }
  }

  private PeriodDto toPeriod(LifeAnalysisPeriodEntity e) {
    return new PeriodDto(
        e.getId(),
        e.getCategory(),
        e.getFromDate(),
        e.getToDate(),
        e.getTopic(),
        e.getObservation(),
        e.getCalculationBasis(),
        e.getStatus(),
        e.getSortOrder());
  }

  private void track(
      LifeAnalysisEntity row, String field, String oldVal, String newVal, String actor) {
    if (row.getId() == null) {
      return;
    }
    String o = oldVal == null ? "" : oldVal;
    String n = newVal == null ? "" : newVal;
    if (Objects.equals(o, n)) {
      return;
    }
    LifeAnalysisHistoryEntity h = new LifeAnalysisHistoryEntity();
    h.setTenantId(row.getTenantId());
    h.setLifeAnalysisId(row.getId());
    h.setFieldName(field);
    h.setOldValue(oldVal);
    h.setNewValue(newVal);
    h.setUpdatedBy(actor);
    historyRepository.save(h);
  }

  private Map<String, String> readSections(String json) {
    if (json == null || json.isBlank()) {
      return Map.of();
    }
    try {
      return objectMapper.readValue(json, new TypeReference<>() {});
    } catch (Exception ex) {
      return Map.of();
    }
  }

  private String writeSections(Map<String, String> sections) {
    if (sections == null || sections.isEmpty()) {
      return null;
    }
    try {
      return objectMapper.writeValueAsString(sections);
    } catch (Exception ex) {
      throw new IllegalArgumentException("Invalid sections map");
    }
  }

  private static boolean hasContent(UpsertAnalysisRequest req) {
    return !isBlank(req.pastNotes())
        || !isBlank(req.presentNotes())
        || !isBlank(req.futureNotes())
        || !isBlank(req.advice())
        || !isBlank(req.jyotishNotes())
        || (req.sections() != null && !req.sections().isEmpty());
  }

  private static boolean isBlankAll(UpsertAnalysisRequest req) {
    return !hasContent(req);
  }

  private static boolean isEffectivelyEmpty(LifeAnalysisEntity row) {
    return isBlank(row.getPastNotes())
        && isBlank(row.getPresentNotes())
        && isBlank(row.getFutureNotes())
        && isBlank(row.getAdvice())
        && isBlank(row.getJyotishNotes());
  }

  private static boolean isBlank(String s) {
    return s == null || s.isBlank();
  }

  private static String trimJoin(String a, String b) {
    if (isBlank(a)) {
      return b;
    }
    if (isBlank(b)) {
      return a;
    }
    return a + "\n\n" + b;
  }

  private void requireKundali(Long kundaliId, String tenantId) {
    if (kundaliId == null) {
      throw new IllegalArgumentException("kundaliId is required");
    }
    kundaliRepository
        .findByIdAndTenantId(kundaliId, tenantId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kundali not found"));
  }

  private static String requireTenant() {
    String tenantId = TenantContextFilter.getCurrentTenantId();
    if (tenantId == null || tenantId.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "X-Tenant-Id is required");
    }
    return tenantId;
  }
}
