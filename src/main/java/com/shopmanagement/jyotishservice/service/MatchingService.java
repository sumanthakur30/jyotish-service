package com.shopmanagement.jyotishservice.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.shopmanagement.jyotishservice.api.KundaliApi.GenerateRequest;
import com.shopmanagement.jyotishservice.api.KundaliApi.KundaliResponse;
import com.shopmanagement.jyotishservice.api.MatchingApi.KootaScoreDto;
import com.shopmanagement.jyotishservice.api.MatchingApi.ManglikDto;
import com.shopmanagement.jyotishservice.api.MatchingApi.MatchRequest;
import com.shopmanagement.jyotishservice.api.MatchingApi.MatchingCatalogItem;
import com.shopmanagement.jyotishservice.api.MatchingApi.MatchingResponse;
import com.shopmanagement.jyotishservice.api.MatchingApi.PersonSummaryDto;
import com.shopmanagement.jyotishservice.engine.CalculationEngine;
import com.shopmanagement.jyotishservice.engine.matching.KootaScore;
import com.shopmanagement.jyotishservice.engine.matching.ManglikAssessment;
import com.shopmanagement.jyotishservice.engine.matching.MatchingPerson;
import com.shopmanagement.jyotishservice.engine.matching.MatchingRegistry;
import com.shopmanagement.jyotishservice.engine.matching.MatchingReport;
import com.shopmanagement.jyotishservice.engine.matching.MatchingSystemCode;
import com.shopmanagement.jyotishservice.engine.model.Planet;
import com.shopmanagement.jyotishservice.engine.model.PlanetPosition;
import com.shopmanagement.jyotishservice.filter.TenantContextFilter;
import com.shopmanagement.jyotishservice.persistence.entity.BirthProfileEntity;
import com.shopmanagement.jyotishservice.persistence.entity.KundaliSnapshotEntity;
import com.shopmanagement.jyotishservice.persistence.entity.MatchingKootaScoreEntity;
import com.shopmanagement.jyotishservice.persistence.entity.MatchingSessionEntity;
import com.shopmanagement.jyotishservice.persistence.entity.PlanetaryPositionEntity;
import com.shopmanagement.jyotishservice.persistence.repo.BirthProfileRepository;
import com.shopmanagement.jyotishservice.persistence.repo.KundaliSnapshotRepository;
import com.shopmanagement.jyotishservice.persistence.repo.MatchingKootaScoreRepository;
import com.shopmanagement.jyotishservice.persistence.repo.MatchingSessionRepository;
import com.shopmanagement.jyotishservice.persistence.repo.PlanetaryPositionRepository;

@Service
public class MatchingService {

  private final CalculationEngine calculationEngine;
  private final KundaliService kundaliService;
  private final BirthProfileRepository profileRepository;
  private final KundaliSnapshotRepository kundaliRepository;
  private final PlanetaryPositionRepository planetaryRepository;
  private final MatchingSessionRepository matchingSessionRepository;
  private final MatchingKootaScoreRepository matchingKootaScoreRepository;

  public MatchingService(
      CalculationEngine calculationEngine,
      KundaliService kundaliService,
      BirthProfileRepository profileRepository,
      KundaliSnapshotRepository kundaliRepository,
      PlanetaryPositionRepository planetaryRepository,
      MatchingSessionRepository matchingSessionRepository,
      MatchingKootaScoreRepository matchingKootaScoreRepository) {
    this.calculationEngine = calculationEngine;
    this.kundaliService = kundaliService;
    this.profileRepository = profileRepository;
    this.kundaliRepository = kundaliRepository;
    this.planetaryRepository = planetaryRepository;
    this.matchingSessionRepository = matchingSessionRepository;
    this.matchingKootaScoreRepository = matchingKootaScoreRepository;
  }

  @Transactional
  public MatchingResponse match(MatchRequest request) {
    String tenantId = requireTenant();
    if (request == null || request.profileIdA() == null || request.profileIdB() == null) {
      throw new IllegalArgumentException("profileIdA and profileIdB are required");
    }
    if (request.profileIdA().equals(request.profileIdB())) {
      throw new IllegalArgumentException("Select two different birth profiles for matching");
    }

    BirthProfileEntity profileA = requireProfile(request.profileIdA(), tenantId);
    BirthProfileEntity profileB = requireProfile(request.profileIdB(), tenantId);

    ResolvedChart chartA = resolveChart(tenantId, profileA);
    ResolvedChart chartB = resolveChart(tenantId, profileB);

    MatchingPerson personA =
        MatchingPerson.fromPositions(
            profileA.getId(), profileA.getDisplayName(), chartA.lagnaSign(), chartA.planets());
    MatchingPerson personB =
        MatchingPerson.fromPositions(
            profileB.getId(), profileB.getDisplayName(), chartB.lagnaSign(), chartB.planets());

    MatchingReport report =
        MatchingRegistry.compute(personA, personB, calculationEngine.version());

    MatchingSessionEntity session = new MatchingSessionEntity();
    session.setTenantId(tenantId);
    session.setProfileIdA(profileA.getId());
    session.setProfileIdB(profileB.getId());
    session.setKundaliIdA(chartA.kundaliId());
    session.setKundaliIdB(chartB.kundaliId());
    session.setDisplayNameA(profileA.getDisplayName());
    session.setDisplayNameB(profileB.getDisplayName());
    session.setTotalScore(report.totalScore());
    session.setMaxScore(report.maxScore());
    session.setPercentage(bd(report.percentage(), 2));
    session.setManglikStatusA(report.manglikA().status().code());
    session.setManglikStatusB(report.manglikB().status().code());
    session.setManglikMarsHouseA((short) report.manglikA().marsHouse());
    session.setManglikMarsHouseB((short) report.manglikB().marsHouse());
    session.setSummary(report.summary());
    session.setNotes(report.notes());
    session.setDisclaimer(report.disclaimer());
    session.setCalculationEngineVersion(report.engineVersion());
    session.setResultJson(compactMetaJson(personA, personB, report));
    session = matchingSessionRepository.save(session);

    short order = 0;
    for (KootaScore score : report.kootas()) {
      MatchingKootaScoreEntity row = new MatchingKootaScoreEntity();
      row.setTenantId(tenantId);
      row.setMatchingId(session.getId());
      row.setKootaCode(score.koota().code());
      row.setDisplayName(score.koota().displayName());
      row.setObtained(score.obtained());
      row.setMaxPoints(score.maxPoints());
      row.setExplanation(score.explanation());
      row.setRuleId(score.ruleId());
      row.setSortOrder(order++);
      matchingKootaScoreRepository.save(row);
    }

    return toResponse(
        session, report, personA, personB, chartA.kundaliId(), chartB.kundaliId());
  }

  @Transactional(readOnly = true)
  public MatchingResponse get(Long id) {
    String tenantId = requireTenant();
    MatchingSessionEntity session =
        matchingSessionRepository
            .findByIdAndTenantId(id, tenantId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Matching session not found"));

    List<MatchingKootaScoreEntity> rows =
        matchingKootaScoreRepository.findByMatchingIdOrderBySortOrderAsc(session.getId());
    List<KootaScoreDto> kootas = new ArrayList<>();
    for (MatchingKootaScoreEntity row : rows) {
      kootas.add(
          new KootaScoreDto(
              row.getKootaCode(),
              row.getDisplayName(),
              row.getObtained(),
              row.getMaxPoints(),
              row.getExplanation(),
              row.getRuleId()));
    }

    PersonSummaryDto personA =
        new PersonSummaryDto(
            session.getProfileIdA(),
            session.getDisplayNameA(),
            session.getKundaliIdA(),
            -1,
            "",
            -1,
            "");
    PersonSummaryDto personB =
        new PersonSummaryDto(
            session.getProfileIdB(),
            session.getDisplayNameB(),
            session.getKundaliIdB(),
            -1,
            "",
            -1,
            "");

    // Enrich Moon details from linked kundalis when still available.
    if (session.getKundaliIdA() != null) {
      personA = enrichPerson(personA, session.getKundaliIdA(), tenantId);
    }
    if (session.getKundaliIdB() != null) {
      personB = enrichPerson(personB, session.getKundaliIdB(), tenantId);
    }

    ManglikDto manglikA =
        manglikFromSession(
            session.getManglikStatusA(), session.getManglikMarsHouseA(), personA.displayName());
    ManglikDto manglikB =
        manglikFromSession(
            session.getManglikStatusB(), session.getManglikMarsHouseB(), personB.displayName());

    return new MatchingResponse(
        session.getId(),
        personA,
        personB,
        kootas,
        session.getTotalScore(),
        session.getMaxScore(),
        session.getPercentage(),
        manglikA,
        manglikB,
        session.getSummary(),
        session.getNotes(),
        session.getDisclaimer(),
        session.getCalculationEngineVersion(),
        catalog(),
        session.getCreatedAt());
  }

  private MatchingResponse toResponse(
      MatchingSessionEntity session,
      MatchingReport report,
      MatchingPerson personA,
      MatchingPerson personB,
      Long kundaliIdA,
      Long kundaliIdB) {
    List<KootaScoreDto> dto = new ArrayList<>();
    for (KootaScore s : report.kootas()) {
      dto.add(
          new KootaScoreDto(
              s.koota().code(),
              s.koota().displayName(),
              s.obtained(),
              s.maxPoints(),
              s.explanation(),
              s.ruleId()));
    }
    return new MatchingResponse(
        session.getId(),
        new PersonSummaryDto(
            personA.profileId(),
            personA.displayName(),
            kundaliIdA,
            personA.moonSignIndex(),
            personA.moonSignName(),
            personA.moonNakshatraIndex(),
            personA.moonNakshatraName()),
        new PersonSummaryDto(
            personB.profileId(),
            personB.displayName(),
            kundaliIdB,
            personB.moonSignIndex(),
            personB.moonSignName(),
            personB.moonNakshatraIndex(),
            personB.moonNakshatraName()),
        dto,
        session.getTotalScore(),
        session.getMaxScore(),
        session.getPercentage(),
        toManglikDto(report.manglikA()),
        toManglikDto(report.manglikB()),
        session.getSummary(),
        session.getNotes(),
        session.getDisclaimer(),
        session.getCalculationEngineVersion(),
        catalog(),
        session.getCreatedAt());
  }

  private static ManglikDto toManglikDto(ManglikAssessment a) {
    return new ManglikDto(
        a.status().code(),
        a.status().label(),
        a.present(),
        a.marsHouse(),
        a.marsSignIndex(),
        a.marsSignName(),
        a.relevantHouses(),
        a.reasoning(),
        a.cancellationsComingSoon(),
        a.cancellationsNote());
  }

  private static ManglikDto manglikFromSession(String status, short marsHouse, String name) {
    boolean present = "PRESENT".equalsIgnoreCase(status);
    return new ManglikDto(
        status,
        present ? "Present" : "Absent",
        present,
        marsHouse,
        -1,
        "",
        com.shopmanagement.jyotishservice.engine.matching.ManglikAnalyzer.RELEVANT_HOUSES,
        "Mars house "
            + marsHouse
            + " for "
            + name
            + " (stored session). Cancellation rules: Coming Soon.",
        true,
        "Manglik cancellation / exception rules are Coming Soon and are not applied here.");
  }

  private PersonSummaryDto enrichPerson(PersonSummaryDto base, Long kundaliId, String tenantId) {
    List<PlanetaryPositionEntity> planets =
        planetaryRepository.findByKundaliIdAndTenantIdOrderByPlanetCodeAsc(kundaliId, tenantId);
    PlanetaryPositionEntity moon =
        planets.stream()
            .filter(p -> "MOON".equalsIgnoreCase(p.getPlanetCode()))
            .findFirst()
            .orElse(null);
    if (moon == null) {
      return base;
    }
    return new PersonSummaryDto(
        base.profileId(),
        base.displayName(),
        kundaliId,
        moon.getSignIndex(),
        moon.getSignName(),
        moon.getNakshatraIndex(),
        moon.getNakshatraName());
  }

  private ResolvedChart resolveChart(String tenantId, BirthProfileEntity profile) {
    List<KundaliSnapshotEntity> existing =
        kundaliRepository.findByTenantIdAndBirthProfileIdOrderByCreatedAtDesc(
            tenantId, profile.getId());
    if (!existing.isEmpty()) {
      KundaliSnapshotEntity snap = existing.get(0);
      List<PlanetPosition> planets = loadPlanetPositions(snap.getId(), tenantId);
      return new ResolvedChart(snap.getId(), snap.getAscendantSignIndex(), planets);
    }
    KundaliResponse generated =
        kundaliService.generate(new GenerateRequest(profile.getId(), null, null));
    List<PlanetPosition> planets = loadPlanetPositions(generated.id(), tenantId);
    KundaliSnapshotEntity snap =
        kundaliRepository
            .findByIdAndTenantId(generated.id(), tenantId)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Generated kundali missing"));
    return new ResolvedChart(snap.getId(), (int) snap.getAscendantSignIndex(), planets);
  }

  private List<PlanetPosition> loadPlanetPositions(Long kundaliId, String tenantId) {
    List<PlanetPosition> out = new ArrayList<>();
    for (PlanetaryPositionEntity e :
        planetaryRepository.findByKundaliIdAndTenantIdOrderByPlanetCodeAsc(kundaliId, tenantId)) {
      Planet planet = Planet.valueOf(e.getPlanetCode().toUpperCase(Locale.ROOT));
      Double speed = e.getSpeedDegPerDay() == null ? null : e.getSpeedDegPerDay().doubleValue();
      out.add(
          new PlanetPosition(
              planet,
              e.getLongitudeDeg().doubleValue(),
              e.getSignIndex(),
              e.getSignName(),
              e.getDegreeInSign().doubleValue(),
              e.getHouse(),
              e.getNakshatraIndex(),
              e.getNakshatraName(),
              e.getPada(),
              e.isRetrograde(),
              e.isCombust(),
              speed));
    }
    return out;
  }

  private BirthProfileEntity requireProfile(Long id, String tenantId) {
    return profileRepository
        .findByIdAndTenantIdAndDeletedAtIsNull(id, tenantId)
        .orElseThrow(
            () ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Birth profile not found for this tenant"));
  }

  private static List<MatchingCatalogItem> catalog() {
    List<MatchingCatalogItem> items = new ArrayList<>();
    for (MatchingSystemCode code : MatchingRegistry.all()) {
      boolean impl = MatchingRegistry.isImplemented(code);
      items.add(
          new MatchingCatalogItem(
              code.code(),
              code.displayName(),
              impl,
              impl ? "READY" : "COMING_SOON"));
    }
    return items;
  }

  private static String compactMetaJson(
      MatchingPerson a, MatchingPerson b, MatchingReport report) {
    return "{\"moonA\":\""
        + a.moonNakshatraName()
        + "\",\"moonB\":\""
        + b.moonNakshatraName()
        + "\",\"total\":"
        + report.totalScore()
        + ",\"max\":"
        + report.maxScore()
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

  private record ResolvedChart(Long kundaliId, int lagnaSign, List<PlanetPosition> planets) {}
}
