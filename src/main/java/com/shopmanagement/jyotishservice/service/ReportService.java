package com.shopmanagement.jyotishservice.service;



import java.io.IOException;

import java.nio.file.Files;

import java.nio.file.Path;

import java.nio.file.Paths;

import java.time.Instant;

import java.util.List;

import java.util.Locale;

import java.util.UUID;



import org.springframework.http.HttpStatus;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.server.ResponseStatusException;



import com.shopmanagement.jyotishservice.api.ReportApi.CreateReportRequest;

import com.shopmanagement.jyotishservice.api.ReportApi.ReportResponse;

import com.shopmanagement.jyotishservice.api.ReportTypes;

import com.shopmanagement.jyotishservice.config.JyotishReportProperties;

import com.shopmanagement.jyotishservice.filter.TenantContextFilter;

import com.shopmanagement.jyotishservice.persistence.entity.DashaPeriodEntity;

import com.shopmanagement.jyotishservice.persistence.entity.KundaliReportEntity;

import com.shopmanagement.jyotishservice.persistence.entity.KundaliSnapshotEntity;

import com.shopmanagement.jyotishservice.persistence.entity.MatchingKootaScoreEntity;

import com.shopmanagement.jyotishservice.persistence.entity.MatchingSessionEntity;

import com.shopmanagement.jyotishservice.persistence.entity.PlanetaryPositionEntity;

import com.shopmanagement.jyotishservice.persistence.entity.TransitPlanetPositionEntity;

import com.shopmanagement.jyotishservice.persistence.entity.TransitSnapshotEntity;

import com.shopmanagement.jyotishservice.persistence.repo.DashaPeriodRepository;

import com.shopmanagement.jyotishservice.persistence.repo.KundaliReportRepository;

import com.shopmanagement.jyotishservice.persistence.repo.KundaliSnapshotRepository;

import com.shopmanagement.jyotishservice.persistence.repo.MatchingKootaScoreRepository;

import com.shopmanagement.jyotishservice.persistence.repo.MatchingSessionRepository;

import com.shopmanagement.jyotishservice.persistence.repo.PlanetaryPositionRepository;

import com.shopmanagement.jyotishservice.persistence.repo.TransitPlanetPositionRepository;

import com.shopmanagement.jyotishservice.persistence.repo.TransitSnapshotRepository;

import com.shopmanagement.jyotishservice.service.report.ReportPdfRenderer;



@Service

public class ReportService {



  public static final String TYPE_BASIC_KUNDALI = ReportTypes.BASIC_KUNDALI;

  public static final String TYPE_MATCHING = ReportTypes.MATCHING;

  public static final String TYPE_DASHA_SUMMARY = ReportTypes.DASHA_SUMMARY;

  public static final String TYPE_TRANSIT = ReportTypes.TRANSIT;



  private final KundaliReportRepository reportRepository;

  private final KundaliSnapshotRepository kundaliRepository;

  private final PlanetaryPositionRepository planetaryRepository;

  private final DashaPeriodRepository dashaPeriodRepository;

  private final MatchingSessionRepository matchingSessionRepository;

  private final MatchingKootaScoreRepository matchingKootaScoreRepository;

  private final TransitSnapshotRepository transitSnapshotRepository;

  private final TransitPlanetPositionRepository transitPlanetPositionRepository;

  private final ReportPdfRenderer pdfRenderer;

  private final JyotishReportProperties reportProperties;

  private final com.shopmanagement.jyotishservice.engine.CalculationEngine calculationEngine;



  public ReportService(

      KundaliReportRepository reportRepository,

      KundaliSnapshotRepository kundaliRepository,

      PlanetaryPositionRepository planetaryRepository,

      DashaPeriodRepository dashaPeriodRepository,

      MatchingSessionRepository matchingSessionRepository,

      MatchingKootaScoreRepository matchingKootaScoreRepository,

      TransitSnapshotRepository transitSnapshotRepository,

      TransitPlanetPositionRepository transitPlanetPositionRepository,

      ReportPdfRenderer pdfRenderer,

      JyotishReportProperties reportProperties,

      com.shopmanagement.jyotishservice.engine.CalculationEngine calculationEngine) {

    this.reportRepository = reportRepository;

    this.kundaliRepository = kundaliRepository;

    this.planetaryRepository = planetaryRepository;

    this.dashaPeriodRepository = dashaPeriodRepository;

    this.matchingSessionRepository = matchingSessionRepository;

    this.matchingKootaScoreRepository = matchingKootaScoreRepository;

    this.transitSnapshotRepository = transitSnapshotRepository;

    this.transitPlanetPositionRepository = transitPlanetPositionRepository;

    this.pdfRenderer = pdfRenderer;

    this.reportProperties = reportProperties;

    this.calculationEngine = calculationEngine;

  }



  @Transactional

  public ReportResponse create(CreateReportRequest request) {

    String tenantId = requireTenant();

    if (request == null) {

      throw new IllegalArgumentException("type is required");

    }

    String type = ReportTypes.resolve(request.type());

    if (!ReportTypes.isCanonical(type)) {

      throw new IllegalArgumentException(ReportTypes.ALLOWED_MESSAGE);

    }



    byte[] pdf;

    String title;

    Long kundaliId = null;

    Long matchingId = null;

    String engineVersion;



    switch (type) {

      case TYPE_BASIC_KUNDALI -> {

        KundaliSnapshotEntity snap = requireKundali(request.kundaliId(), tenantId);

        kundaliId = snap.getId();

        List<PlanetaryPositionEntity> planets =

            planetaryRepository.findByKundaliIdAndTenantIdOrderByPlanetCodeAsc(

                snap.getId(), tenantId);

        List<DashaPeriodEntity> dashas =

            dashaPeriodRepository

                .findByKundaliIdAndTenantIdAndSystemCodeOrderByStartAtAscSequenceNoAsc(

                    snap.getId(), tenantId, "VIMSHOTTARI");

        TransitSnapshotEntity transit =

            transitSnapshotRepository

                .findFirstByKundaliIdAndTenantIdOrderByTransitDateDescCreatedAtDesc(

                    snap.getId(), tenantId)

                .orElse(null);

        List<TransitPlanetPositionEntity> transitPlanets =

            transit == null

                ? List.of()

                : transitPlanetPositionRepository.findByTransitIdAndTenantIdOrderByPlanetCodeAsc(

                    transit.getId(), tenantId);

        pdf =
            pdfRenderer.renderBasicKundali(
                snap,
                planets,
                dashas,
                transit,
                transitPlanets,
                com.shopmanagement.jyotishservice.service.report.VargaPdfPackBuilder.build(
                    calculationEngine, snap, planets));

        title = "Basic Kundali — " + snap.getDisplayName();

        engineVersion = snap.getCalculationEngineVersion();

      }

      case TYPE_MATCHING -> {

        MatchingSessionEntity session = requireMatching(request.matchingId(), tenantId);

        matchingId = session.getId();

        kundaliId = session.getKundaliIdA();

        List<MatchingKootaScoreEntity> kootas =

            matchingKootaScoreRepository.findByMatchingIdOrderBySortOrderAsc(session.getId());

        pdf = pdfRenderer.renderMatching(session, kootas);

        title =

            "Matching — " + session.getDisplayNameA() + " × " + session.getDisplayNameB();

        engineVersion = session.getCalculationEngineVersion();

      }

      case TYPE_DASHA_SUMMARY -> {

        KundaliSnapshotEntity snap = requireKundali(request.kundaliId(), tenantId);

        kundaliId = snap.getId();

        List<DashaPeriodEntity> dashas =

            dashaPeriodRepository

                .findByKundaliIdAndTenantIdAndSystemCodeOrderByStartAtAscSequenceNoAsc(

                    snap.getId(), tenantId, "VIMSHOTTARI");

        if (dashas.isEmpty()) {

          throw new ResponseStatusException(

              HttpStatus.NOT_FOUND, "No dasha periods stored for this kundali");

        }

        pdf = pdfRenderer.renderDashaSummary(snap, dashas);

        title = "Dasha summary — " + snap.getDisplayName();

        engineVersion = snap.getCalculationEngineVersion();

      }

      case TYPE_TRANSIT -> {

        KundaliSnapshotEntity snap = requireKundali(request.kundaliId(), tenantId);

        kundaliId = snap.getId();

        TransitSnapshotEntity transit =

            transitSnapshotRepository

                .findFirstByKundaliIdAndTenantIdOrderByTransitDateDescCreatedAtDesc(

                    snap.getId(), tenantId)

                .orElseThrow(

                    () ->

                        new ResponseStatusException(

                            HttpStatus.NOT_FOUND, "No transit snapshot for this kundali"));

        List<TransitPlanetPositionEntity> transitPlanets =

            transitPlanetPositionRepository.findByTransitIdAndTenantIdOrderByPlanetCodeAsc(

                transit.getId(), tenantId);

        pdf = pdfRenderer.renderTransit(snap, transit, transitPlanets);

        title = "Transit — " + snap.getDisplayName();

        engineVersion = transit.getCalculationEngineVersion();

      }

      default ->

          throw new IllegalArgumentException(ReportTypes.ALLOWED_MESSAGE);

    }



    if (pdf == null || pdf.length == 0) {

      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "PDF generation produced empty output");

    }



    Path stored = writePdf(tenantId, type, pdf);

    Instant generatedAt = Instant.now();



    KundaliReportEntity entity = new KundaliReportEntity();

    entity.setTenantId(tenantId);

    entity.setReportType(type);

    entity.setKundaliId(kundaliId);

    entity.setMatchingId(matchingId);

    entity.setDisplayTitle(title);

    entity.setStoragePath(stored.toAbsolutePath().normalize().toString());

    entity.setFileSizeBytes(pdf.length);

    entity.setContentType("application/pdf");

    entity.setCalculationEngineVersion(engineVersion);

    entity.setGeneratedAt(generatedAt);

    entity = reportRepository.save(entity);



    return toResponse(entity);

  }



  @Transactional(readOnly = true)

  public ReportResponse get(Long id) {

    return toResponse(requireReport(id, requireTenant()));

  }



  @Transactional(readOnly = true)

  public byte[] download(Long id) {

    KundaliReportEntity report = requireReport(id, requireTenant());

    Path path = Paths.get(report.getStoragePath());

    if (!Files.isRegularFile(path)) {

      throw new ResponseStatusException(

          HttpStatus.NOT_FOUND, "Report file missing on disk: " + report.getStoragePath());

    }

    try {

      return Files.readAllBytes(path);

    } catch (IOException ex) {

      throw new ResponseStatusException(

          HttpStatus.INTERNAL_SERVER_ERROR, "Could not read report file", ex);

    }

  }



  private Path writePdf(String tenantId, String type, byte[] pdf) {

    try {

      Path root = Paths.get(reportProperties.getStorageDir()).toAbsolutePath().normalize();

      Path tenantDir = root.resolve(sanitize(tenantId));

      Files.createDirectories(tenantDir);

      String fileName =

          type.toLowerCase(Locale.ROOT)

              + "-"

              + UUID.randomUUID().toString().replace("-", "")

              + ".pdf";

      Path file = tenantDir.resolve(fileName);

      Files.write(file, pdf);

      return file;

    } catch (IOException ex) {

      throw new ResponseStatusException(

          HttpStatus.INTERNAL_SERVER_ERROR, "Could not store report PDF", ex);

    }

  }



  private KundaliReportEntity requireReport(Long id, String tenantId) {

    if (id == null) {

      throw new IllegalArgumentException("Report id is required");

    }

    return reportRepository

        .findByIdAndTenantId(id, tenantId)

        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Report not found"));

  }



  private KundaliSnapshotEntity requireKundali(Long kundaliId, String tenantId) {

    if (kundaliId == null) {

      throw new IllegalArgumentException("kundaliId is required for this report type");

    }

    return kundaliRepository

        .findByIdAndTenantId(kundaliId, tenantId)

        .orElseThrow(

            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kundali snapshot not found"));

  }



  private MatchingSessionEntity requireMatching(Long matchingId, String tenantId) {

    if (matchingId == null) {

      throw new IllegalArgumentException("matchingId is required for MATCHING reports");

    }

    return matchingSessionRepository

        .findByIdAndTenantId(matchingId, tenantId)

        .orElseThrow(

            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Matching session not found"));

  }



  private static ReportResponse toResponse(KundaliReportEntity e) {

    return new ReportResponse(

        e.getId(),

        e.getReportType(),

        e.getKundaliId(),

        e.getMatchingId(),

        e.getDisplayTitle(),

        e.getStoragePath(),

        e.getFileSizeBytes(),

        e.getContentType(),

        e.getCalculationEngineVersion(),

        e.getGeneratedAt(),

        "/api/v1/jyotish/reports/" + e.getId() + "/download");

  }



  private static String sanitize(String tenantId) {

    return tenantId.replaceAll("[^a-zA-Z0-9._-]", "_");

  }



  private static String requireTenant() {

    String tenantId = TenantContextFilter.getCurrentTenantId();

    if (tenantId == null || tenantId.isBlank()) {

      throw new IllegalArgumentException("Missing tenant context header: X-Tenant-Id");

    }

    return tenantId;

  }

}


