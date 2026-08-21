package com.shopmanagement.jyotishservice.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.shopmanagement.jyotishservice.api.KundaliApi.KundaliResponse;
import com.shopmanagement.jyotishservice.api.KundaliApi.PlanetDto;
import com.shopmanagement.jyotishservice.api.KundaliApi.YogaDto;
import com.shopmanagement.jyotishservice.api.KundaliApi.YogaListResponse;
import com.shopmanagement.jyotishservice.api.LifeAnalysisApi.CalculatedDashaPeriod;
import com.shopmanagement.jyotishservice.api.LifeAnalysisApi.CalculatedTimelineStrip;
import com.shopmanagement.jyotishservice.api.LifeAnalysisApi.CategorySummary;
import com.shopmanagement.jyotishservice.api.LifeAnalysisApi.CurrentDashaStrip;
import com.shopmanagement.jyotishservice.api.LifeAnalysisApi.DashboardResponse;
import com.shopmanagement.jyotishservice.api.SimpleOverviewApi.CurrentLifePeriodCard;
import com.shopmanagement.jyotishservice.api.SimpleOverviewApi.ExplainedBlock;
import com.shopmanagement.jyotishservice.api.SimpleOverviewApi.GlanceCard;
import com.shopmanagement.jyotishservice.api.SimpleOverviewApi.LifeAreaCard;
import com.shopmanagement.jyotishservice.api.SimpleOverviewApi.LordTheme;
import com.shopmanagement.jyotishservice.api.SimpleOverviewApi.PresentYogaFact;
import com.shopmanagement.jyotishservice.api.SimpleOverviewApi.SimpleOverviewResponse;
import com.shopmanagement.jyotishservice.api.SimpleOverviewApi.SimplePeriodExplainResponse;
import com.shopmanagement.jyotishservice.api.SimpleOverviewApi.TechnicalDetails;
import com.shopmanagement.jyotishservice.api.SimpleOverviewApi.UpcomingItem;
import com.shopmanagement.jyotishservice.engine.explain.DashaLordThemes;
import com.shopmanagement.jyotishservice.engine.explain.DashaLordThemes.Theme;
import com.shopmanagement.jyotishservice.engine.explain.SimpleExplanationComposer;
import com.shopmanagement.jyotishservice.engine.explain.SimpleExplanationComposer.LordPlacement;
import com.shopmanagement.jyotishservice.engine.explain.SimpleLabels;
import com.shopmanagement.jyotishservice.filter.TenantContextFilter;
import com.shopmanagement.jyotishservice.persistence.entity.DashaPeriodEntity;
import com.shopmanagement.jyotishservice.persistence.repo.DashaPeriodRepository;

/**
 * Customer Simple View: reads existing Kundali / Life Analysis / Yoga facts and applies templated
 * bilingual copy. Does not recompute ephemeris or invent dasha dates.
 */
@Service
public class SimpleOverviewService {

  private static final String VIMSHOTTARI = "VIMSHOTTARI";

  private final KundaliService kundaliService;
  private final LifeAnalysisService lifeAnalysisService;
  private final DashaPeriodRepository dashaPeriodRepository;

  public SimpleOverviewService(
      KundaliService kundaliService,
      LifeAnalysisService lifeAnalysisService,
      DashaPeriodRepository dashaPeriodRepository) {
    this.kundaliService = kundaliService;
    this.lifeAnalysisService = lifeAnalysisService;
    this.dashaPeriodRepository = dashaPeriodRepository;
  }

  @Transactional(readOnly = true)
  public SimpleOverviewResponse overview(Long kundaliId) {
    KundaliResponse kundali = kundaliService.get(kundaliId);
    DashboardResponse life = lifeAnalysisService.dashboard(kundaliId);
    YogaListResponse yogas = kundaliService.getYogas(kundaliId, null);

    PlanetDto asc = kundali.ascendant();
    PlanetDto moon = findPlanet(kundali.planets(), "MOON");

    GlanceCard lagna =
        glance(
            "LAGNA",
            asc != null && asc.signName() != null,
            asc != null ? asc.signName() : null,
            asc != null ? SimpleLabels.signHi(asc.signName()) : null,
            "Lagna is the rising sign at birth — a simple starting point for how you meet the world.",
            "लग्न जन्म के समय उदय राशि है — जीवन से मिलने के तरीके का सरल आरंभ बिंदु।");

    GlanceCard moonRashi =
        glance(
            "MOON_RASHI",
            moon != null && moon.signName() != null,
            moon != null ? moon.signName() : null,
            moon != null ? SimpleLabels.signHi(moon.signName()) : null,
            "Moon Rashi is the sign where the Moon sits — traditionally linked with mind and feelings.",
            "चंद्र राशि वह राशि है जहाँ चंद्र बैठा है — पारंपरिक रूप से मन और भावना से जुड़ी।");

    GlanceCard nakshatra =
        glance(
            "NAKSHATRA",
            moon != null && moon.nakshatraName() != null,
            moon != null
                ? moon.nakshatraName()
                    + (moon.pada() > 0 ? " · pada " + moon.pada() : "")
                : null,
            moon != null
                ? SimpleLabels.nakshatraHi(moon.nakshatraName())
                    + (moon.pada() > 0 ? " · पद " + moon.pada() : "")
                : null,
            "Nakshatra is the Moon’s finer star group — used for the life-period clock and gentle character themes.",
            "नक्षत्र चंद्र का सूक्ष्म तारा-समूह है — जीवन-अवधि घड़ी और स्वभाव के लिए उपयोग होता है।");

    CalculatedTimelineStrip strip = life.calculatedTimeline();
    CurrentDashaStrip current = strip != null ? strip.currentDasha() : null;
    String dashaValueEn = current != null ? current.summaryLine() : null;
    String dashaValueHi = dashaGlanceHi(current);
    GlanceCard dashaGlance =
        glance(
            "CURRENT_DASHA",
            dashaValueEn != null && !dashaValueEn.isBlank(),
            dashaValueEn,
            dashaValueHi != null ? dashaValueHi : dashaValueEn,
            "Current life period is the clock built from the Moon’s nakshatra in this chart.",
            "वर्तमान जीवन अवधि इस कुंडली में चंद्र नक्षत्र से बनी घड़ी है।");

    CurrentLifePeriodCard periodCard = buildCurrentPeriod(current, kundali);
    List<UpcomingItem> upcoming = buildUpcoming(strip != null ? strip.upcomingDasha() : List.of());
    List<LifeAreaCard> areas =
        buildLifeAreas(life.categories(), upcoming.isEmpty() ? null : upcoming.get(0));
    List<PresentYogaFact> presentYogas = buildYogas(yogas);
    List<LordTheme> themes = buildLordThemes();

    boolean missingCore =
        asc == null
            || moon == null
            || (current == null
                || (current.maha() == null && current.antar() == null));

    TechnicalDetails tech =
        new TechnicalDetails(
            kundali.ayanamsaCode(),
            kundali.houseSystem(),
            kundali.calculationEngineVersion(),
            kundali.zodiacSystem(),
            VIMSHOTTARI);

    return new SimpleOverviewResponse(
        kundali.id(),
        kundali.displayName(),
        strip != null && strip.asOf() != null ? strip.asOf() : Instant.now(),
        missingCore,
        lagna,
        moonRashi,
        nakshatra,
        dashaGlance,
        periodCard,
        areas,
        upcoming,
        presentYogas,
        themes,
        tech,
        SimpleExplanationComposer.GENERAL_DISCLAIMER_EN,
        SimpleExplanationComposer.GENERAL_DISCLAIMER_HI,
        LifeAnalysisService.HEALTH_DISCLAIMER_EN,
        LifeAnalysisService.HEALTH_DISCLAIMER_HI);
  }

  /**
   * Explains a period only if it matches a stored dasha row for this kundali (dates/lords from DB).
   */
  @Transactional(readOnly = true)
  public SimplePeriodExplainResponse explainStoredPeriod(
      Long kundaliId, String levelCode, String mahaLord, String antarLord) {
    String tenantId = requireTenant();
    KundaliResponse kundali = kundaliService.get(kundaliId); // tenant-scoped 404 if missing

    String level = levelCode == null ? "" : levelCode.trim().toUpperCase(Locale.ROOT);
    String maha = norm(mahaLord);
    String antar = norm(antarLord);

    List<DashaPeriodEntity> rows =
        dashaPeriodRepository.findByKundaliIdAndTenantIdAndSystemCodeOrderByStartAtAscSequenceNoAsc(
            kundaliId, tenantId, VIMSHOTTARI);

    DashaPeriodEntity match = null;
    Instant asOf = Instant.now();
    for (DashaPeriodEntity r : rows) {
      if (!level.equalsIgnoreCase(nullToEmpty(r.getLevelCode()))) {
        continue;
      }
      if (maha != null && !maha.equalsIgnoreCase(nullToEmpty(r.getMahaLordCode()))
          && !maha.equalsIgnoreCase(nullToEmpty(r.getLordCode()))) {
        continue;
      }
      if ("ANTAR".equals(level) || "PRATYANTAR".equals(level)) {
        if (antar != null
            && !antar.equalsIgnoreCase(nullToEmpty(r.getAntarLordCode()))
            && !antar.equalsIgnoreCase(nullToEmpty(r.getLordCode()))) {
          continue;
        }
      }
      // Prefer the current-containing row when several match
      if (containsNow(r, asOf)) {
        match = r;
        break;
      }
      if (match == null) {
        match = r;
      }
    }

    if (match == null) {
      ExplainedBlock empty = SimpleExplanationComposer.emptyUnavailable();
      return new SimplePeriodExplainResponse(
          kundaliId,
          true,
          level.isBlank() ? null : level,
          maha,
          null,
          antar,
          null,
          null,
          null,
          empty,
          SimpleExplanationComposer.GENERAL_DISCLAIMER_EN,
          SimpleExplanationComposer.GENERAL_DISCLAIMER_HI);
    }

    String mahaCode =
        match.getMahaLordCode() != null ? match.getMahaLordCode() : match.getLordCode();
    String antarCode =
        match.getAntarLordCode() != null
            ? match.getAntarLordCode()
            : ("ANTAR".equalsIgnoreCase(match.getLevelCode()) ? match.getLordCode() : null);
    Theme mahaTheme = DashaLordThemes.themeOrNull(mahaCode);
    Theme antarTheme = DashaLordThemes.themeOrNull(antarCode);

    ExplainedBlock block =
        SimpleExplanationComposer.explainDashaPeriod(
            mahaCode,
            mahaTheme != null ? mahaTheme.nameEn() : mahaCode,
            antarCode,
            antarTheme != null ? antarTheme.nameEn() : antarCode,
            match.getStartAt(),
            match.getEndAt(),
            VIMSHOTTARI,
            placementOf(kundali, mahaCode),
            placementOf(kundali, antarCode));

    return new SimplePeriodExplainResponse(
        kundaliId,
        false,
        match.getLevelCode(),
        mahaCode,
        mahaTheme != null ? mahaTheme.nameEn() : mahaCode,
        antarCode,
        antarTheme != null ? antarTheme.nameEn() : antarCode,
        match.getStartAt(),
        match.getEndAt(),
        block,
        SimpleExplanationComposer.GENERAL_DISCLAIMER_EN,
        SimpleExplanationComposer.GENERAL_DISCLAIMER_HI);
  }

  private CurrentLifePeriodCard buildCurrentPeriod(
      CurrentDashaStrip current, KundaliResponse kundali) {
    if (current == null || (current.maha() == null && current.antar() == null)) {
      return null;
    }
    CalculatedDashaPeriod maha = current.maha();
    CalculatedDashaPeriod antar = current.antar();
    String mahaCode = maha != null ? maha.lordCode() : null;
    String mahaName = maha != null ? maha.lordName() : null;
    String antarCode = antar != null ? antar.lordCode() : null;
    String antarName = antar != null ? antar.lordName() : null;

    Instant start =
        antar != null && antar.startAt() != null
            ? antar.startAt()
            : (maha != null ? maha.startAt() : null);
    Instant end =
        antar != null && antar.endAt() != null
            ? antar.endAt()
            : (maha != null ? maha.endAt() : null);

    String titleEn;
    String titleHi;
    Theme mt = DashaLordThemes.themeOrNull(mahaCode);
    Theme at = DashaLordThemes.themeOrNull(antarCode);
    if (mahaName != null && antarName != null) {
      titleEn = mahaName + " major period · " + antarName + " chapter";
      titleHi =
          (mt != null ? mt.nameHi() : mahaName)
              + " मुख्य अवधि · "
              + (at != null ? at.nameHi() : antarName)
              + " अध्याय";
    } else if (mahaName != null) {
      titleEn = mahaName + " major period";
      titleHi = (mt != null ? mt.nameHi() : mahaName) + " मुख्य अवधि";
    } else {
      titleEn = antarName + " chapter";
      titleHi = (at != null ? at.nameHi() : antarName) + " अध्याय";
    }

    ExplainedBlock explanation =
        SimpleExplanationComposer.explainDashaPeriod(
            mahaCode,
            mahaName,
            antarCode,
            antarName,
            start,
            end,
            current.systemCode(),
            placementOf(kundali, mahaCode),
            placementOf(kundali, antarCode));

    return new CurrentLifePeriodCard(
        titleEn, titleHi, start, end, mahaCode, mahaName, antarCode, antarName, explanation);
  }

  private static List<LifeAreaCard> buildLifeAreas(
      List<CategorySummary> categories, UpcomingItem next) {
    if (categories == null || categories.isEmpty()) {
      return List.of();
    }
    Instant nextAt = next != null ? next.startAt() : null;
    String nextEn = next != null ? "Next: " + next.labelEn() : null;
    String nextHi = next != null ? "अगला: " + next.labelHi() : null;
    List<LifeAreaCard> out = new ArrayList<>();
    for (CategorySummary c : categories) {
      out.add(
          new LifeAreaCard(
              c.category(),
              c.labelEn(),
              c.labelHi(),
              c.status(),
              SimpleLabels.statusLineEn(c.status()),
              SimpleLabels.statusLineHi(c.status()),
              c.currentDashaLine(),
              c.currentDashaEndAt(),
              nextAt,
              nextEn,
              nextHi));
    }
    return List.copyOf(out);
  }

  private static String dashaGlanceHi(CurrentDashaStrip current) {
    if (current == null) {
      return null;
    }
    CalculatedDashaPeriod maha = current.maha();
    CalculatedDashaPeriod antar = current.antar();
    Theme mt = maha != null ? DashaLordThemes.themeOrNull(maha.lordCode()) : null;
    Theme at = antar != null ? DashaLordThemes.themeOrNull(antar.lordCode()) : null;
    Instant end =
        antar != null && antar.endAt() != null
            ? antar.endAt()
            : (maha != null ? maha.endAt() : null);
    String until =
        end != null
            ? " · " + end.atZone(java.time.ZoneOffset.UTC).toLocalDate()
            : "";
    if (mt != null && at != null) {
      return mt.nameHi() + " / " + at.nameHi() + until;
    }
    if (mt != null) {
      return mt.nameHi() + until;
    }
    return current.summaryLine();
  }

  private static LordPlacement placementOf(KundaliResponse kundali, String lordCode) {
    if (kundali == null || lordCode == null || lordCode.isBlank()) {
      return null;
    }
    PlanetDto p = findPlanet(kundali.planets(), lordCode);
    if (p == null || p.signName() == null) {
      return null;
    }
    return new LordPlacement(p.signName(), p.house(), p.nakshatraName());
  }

  private static List<UpcomingItem> buildUpcoming(List<CalculatedDashaPeriod> upcoming) {
    if (upcoming == null || upcoming.isEmpty()) {
      return List.of();
    }
    List<UpcomingItem> out = new ArrayList<>();
    for (CalculatedDashaPeriod u : upcoming) {
      Theme lord = DashaLordThemes.themeOrNull(u.lordCode());
      Theme maha = DashaLordThemes.themeOrNull(u.mahaLordCode());
      String level = u.levelCode() == null ? "" : u.levelCode().toUpperCase(Locale.ROOT);
      String labelEn;
      String labelHi;
      if ("MAHA".equals(level)) {
        labelEn = (u.lordName() != null ? u.lordName() : u.lordCode()) + " major period";
        labelHi = (lord != null ? lord.nameHi() : u.lordName()) + " मुख्य अवधि";
      } else {
        String mahaPart = u.mahaLordName() != null ? u.mahaLordName() : u.mahaLordCode();
        labelEn =
            (mahaPart != null ? mahaPart + " · " : "")
                + (u.lordName() != null ? u.lordName() : u.lordCode())
                + " chapter";
        labelHi =
            (maha != null ? maha.nameHi() : mahaPart)
                + " · "
                + (lord != null ? lord.nameHi() : u.lordName())
                + " अध्याय";
      }
      out.add(
          new UpcomingItem(
              u.levelCode(),
              labelEn,
              labelHi,
              u.lordCode(),
              u.lordName(),
              u.mahaLordCode(),
              u.mahaLordName(),
              u.startAt(),
              u.endAt()));
    }
    return List.copyOf(out);
  }

  private static List<PresentYogaFact> buildYogas(YogaListResponse yogas) {
    if (yogas == null || yogas.yogas() == null) {
      return List.of();
    }
    List<PresentYogaFact> out = new ArrayList<>();
    for (YogaDto y : yogas.yogas()) {
      if (y == null || !y.present()) {
        continue;
      }
      out.add(
          new PresentYogaFact(
              y.yogaCode(),
              y.displayName(),
              y.strengthCode(),
              y.planets() != null ? y.planets() : List.of()));
      if (out.size() >= 8) {
        break;
      }
    }
    return List.copyOf(out);
  }

  private static List<LordTheme> buildLordThemes() {
    List<LordTheme> out = new ArrayList<>();
    for (Theme t : DashaLordThemes.all()) {
      out.add(new LordTheme(t.code(), t.nameEn(), t.nameHi(), t.meaningEn(), t.meaningHi()));
    }
    return List.copyOf(out);
  }

  private static GlanceCard glance(
      String code,
      boolean available,
      String valueEn,
      String valueHi,
      String whatEn,
      String whatHi) {
    if (!available) {
      return new GlanceCard(
          code, false, null, null, whatEn, whatHi);
    }
    return new GlanceCard(code, true, valueEn, valueHi, whatEn, whatHi);
  }

  private static PlanetDto findPlanet(List<PlanetDto> planets, String code) {
    if (planets == null) {
      return null;
    }
    for (PlanetDto p : planets) {
      if (p != null && code.equalsIgnoreCase(p.planetCode())) {
        return p;
      }
    }
    return null;
  }

  private static boolean containsNow(DashaPeriodEntity r, Instant asOf) {
    if (r.getStartAt() == null || r.getEndAt() == null) {
      return false;
    }
    return !asOf.isBefore(r.getStartAt()) && asOf.isBefore(r.getEndAt());
  }

  private static String norm(String raw) {
    if (raw == null || raw.isBlank()) {
      return null;
    }
    return raw.trim().toUpperCase(Locale.ROOT);
  }

  private static String nullToEmpty(String s) {
    return s == null ? "" : s;
  }

  private static String requireTenant() {
    String tenantId = TenantContextFilter.getCurrentTenantId();
    if (tenantId == null || tenantId.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "X-Tenant-Id required");
    }
    return tenantId;
  }
}
