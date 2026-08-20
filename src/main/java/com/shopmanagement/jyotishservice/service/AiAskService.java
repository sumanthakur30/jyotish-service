package com.shopmanagement.jyotishservice.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.shopmanagement.jyotishservice.ai.LlmProvider;
import com.shopmanagement.jyotishservice.api.AiApi.AskRequest;
import com.shopmanagement.jyotishservice.api.AiApi.AskResponse;
import com.shopmanagement.jyotishservice.config.JyotishAiProperties;
import com.shopmanagement.jyotishservice.filter.TenantContextFilter;
import com.shopmanagement.jyotishservice.persistence.entity.DashaPeriodEntity;
import com.shopmanagement.jyotishservice.persistence.entity.HousePositionEntity;
import com.shopmanagement.jyotishservice.persistence.entity.JyotishAiAskEntity;
import com.shopmanagement.jyotishservice.persistence.entity.KundaliSnapshotEntity;
import com.shopmanagement.jyotishservice.persistence.entity.PlanetaryPositionEntity;
import com.shopmanagement.jyotishservice.persistence.entity.TransitPlanetPositionEntity;
import com.shopmanagement.jyotishservice.persistence.entity.YogaResultEntity;
import com.shopmanagement.jyotishservice.persistence.repo.DashaPeriodRepository;
import com.shopmanagement.jyotishservice.persistence.repo.HousePositionRepository;
import com.shopmanagement.jyotishservice.persistence.repo.JyotishAiAskRepository;
import com.shopmanagement.jyotishservice.persistence.repo.KundaliSnapshotRepository;
import com.shopmanagement.jyotishservice.persistence.repo.PlanetaryPositionRepository;
import com.shopmanagement.jyotishservice.persistence.repo.TransitPlanetPositionRepository;
import com.shopmanagement.jyotishservice.persistence.repo.TransitSnapshotRepository;
import com.shopmanagement.jyotishservice.persistence.repo.YogaResultRepository;

/**
 * Phase 10 — grounded chart Q&amp;A. Context is built only from stored kundali / dasha / yoga /
 * optional transit rows. Never invents planetary positions or dasha dates.
 */
@Service
public class AiAskService {

  private static final String DISCLAIMER =
      "AI-assisted interpretation based only on verified Sugam Jyotish engine/DB outputs."
          + " Not a prediction of destiny; consult a qualified astrologer for personal decisions.";

  private static final Set<String> TOPICS =
      Set.of(
          "general",
          "career",
          "marriage",
          "finance",
          "health",
          "education",
          "family",
          "spirituality");

  private final JyotishAiProperties aiProperties;
  private final LlmProvider llmProvider;
  private final KundaliSnapshotRepository kundaliRepository;
  private final PlanetaryPositionRepository planetaryRepository;
  private final HousePositionRepository houseRepository;
  private final DashaPeriodRepository dashaPeriodRepository;
  private final YogaResultRepository yogaResultRepository;
  private final TransitSnapshotRepository transitSnapshotRepository;
  private final TransitPlanetPositionRepository transitPlanetRepository;
  private final JyotishAiAskRepository askRepository;

  public AiAskService(
      JyotishAiProperties aiProperties,
      LlmProvider llmProvider,
      KundaliSnapshotRepository kundaliRepository,
      PlanetaryPositionRepository planetaryRepository,
      HousePositionRepository houseRepository,
      DashaPeriodRepository dashaPeriodRepository,
      YogaResultRepository yogaResultRepository,
      TransitSnapshotRepository transitSnapshotRepository,
      TransitPlanetPositionRepository transitPlanetRepository,
      JyotishAiAskRepository askRepository) {
    this.aiProperties = aiProperties;
    this.llmProvider = llmProvider;
    this.kundaliRepository = kundaliRepository;
    this.planetaryRepository = planetaryRepository;
    this.houseRepository = houseRepository;
    this.dashaPeriodRepository = dashaPeriodRepository;
    this.yogaResultRepository = yogaResultRepository;
    this.transitSnapshotRepository = transitSnapshotRepository;
    this.transitPlanetRepository = transitPlanetRepository;
    this.askRepository = askRepository;
  }

  @Transactional
  public AskResponse ask(AskRequest request) {
    String tenantId = requireTenant();
    if (request == null || request.kundaliId() == null) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "kundaliId is required — cannot answer without a stored chart.");
    }
    if (request.question() == null || request.question().isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "question is required.");
    }

    KundaliSnapshotEntity snap =
        kundaliRepository
            .findByIdAndTenantId(request.kundaliId(), tenantId)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Kundali not found for this tenant — generate a chart before asking."));

    String topic = normalizeTopic(request.topic());
    Map<String, Object> context = buildVerifiedContext(tenantId, snap, topic, request.question().trim());
    List<String> findings = buildFindings(context, topic);
    String heuristicAnswer = buildHeuristicAnswer(request.question().trim(), topic, context, findings);

    long t0 = System.currentTimeMillis();
    Map<String, Object> remote =
        llmProvider.complete(
            "JYOTISH_ASK",
            "en",
            Map.of(
                "topic",
                topic,
                "question",
                request.question().trim(),
                "verifiedContext",
                context,
                "heuristicFindings",
                findings,
                "heuristicAnswer",
                heuristicAnswer,
                "rule",
                "Never invent planetary positions or dasha dates; use only verifiedContext."));
    long latencyMs = System.currentTimeMillis() - t0;

    String answer = heuristicAnswer;
    if (remote.get("body") != null && !String.valueOf(remote.get("body")).isBlank()) {
      answer = String.valueOf(remote.get("body")).trim();
      if (!answer.toLowerCase(Locale.ROOT).contains("ai-assisted")) {
        answer = "AI-assisted interpretation:\n\n" + answer;
      }
    }

    Map<String, Object> contextUsed = contextUsedSummary(context);

    JyotishAiAskEntity audit = new JyotishAiAskEntity();
    audit.setTenantId(tenantId);
    audit.setKundaliId(snap.getId());
    audit.setTopic(topic);
    audit.setQuestion(request.question().trim());
    audit.setProviderCode(llmProvider.code());
    audit.setModelCode(aiProperties.getModelCode());
    audit.setLatencyMs((int) Math.min(latencyMs, Integer.MAX_VALUE));
    audit.setContextSummary(String.valueOf(contextUsed));
    audit.setAnswerPreview(answer.length() > 500 ? answer.substring(0, 500) : answer);
    askRepository.save(audit);

    return new AskResponse(
        snap.getId(),
        topic,
        request.question().trim(),
        answer,
        findings,
        contextUsed,
        true,
        llmProvider.code(),
        DISCLAIMER,
        audit.getId());
  }

  private Map<String, Object> buildVerifiedContext(
      String tenantId, KundaliSnapshotEntity snap, String topic, String question) {
    Map<String, Object> ctx = new LinkedHashMap<>();
    ctx.put("source", "VERIFIED_ENGINE_DB");
    ctx.put("kundaliId", snap.getId());
    ctx.put("displayName", snap.getDisplayName());
    ctx.put("birthDate", snap.getBirthDate() == null ? null : snap.getBirthDate().toString());
    ctx.put("placeName", snap.getPlaceName());
    ctx.put("ayanamsaCode", snap.getAyanamsaCode());
    ctx.put("houseSystem", snap.getHouseSystem());
    ctx.put("calculationEngineVersion", snap.getCalculationEngineVersion());
    ctx.put("topic", topic);
    ctx.put("question", question);

    List<PlanetaryPositionEntity> planets =
        planetaryRepository.findByKundaliIdAndTenantIdOrderByPlanetCodeAsc(snap.getId(), tenantId);
    List<Map<String, Object>> planetRows = new ArrayList<>();
    for (PlanetaryPositionEntity p : planets) {
      Map<String, Object> row = new LinkedHashMap<>();
      row.put("planetCode", p.getPlanetCode());
      row.put("signName", p.getSignName());
      row.put("degreeInSign", bd(p.getDegreeInSign()));
      row.put("house", (int) p.getHouse());
      row.put("nakshatraName", p.getNakshatraName());
      row.put("pada", (int) p.getPada());
      row.put("retrograde", p.isRetrograde());
      planetRows.add(row);
    }
    ctx.put("planets", planetRows);

    if (snap.getAscendantLongitude() != null) {
      Map<String, Object> lagna = new LinkedHashMap<>();
      lagna.put("longitudeDeg", bd(snap.getAscendantLongitude()));
      planets.stream()
          .filter(p -> "ASC".equalsIgnoreCase(p.getPlanetCode()) || "LAGNA".equalsIgnoreCase(p.getPlanetCode()))
          .findFirst()
          .ifPresent(
              a -> {
                lagna.put("signName", a.getSignName());
                lagna.put("nakshatraName", a.getNakshatraName());
                lagna.put("house", (int) a.getHouse());
              });
      // Prefer first planet in house 1 as lagna sign hint when ASC row absent
      if (!lagna.containsKey("signName")) {
        houseRepository.findByKundaliIdAndTenantIdOrderByHouseAsc(snap.getId(), tenantId).stream()
            .filter(h -> h.getHouse() == 1)
            .findFirst()
            .ifPresent(
                h -> {
                  lagna.put("signName", h.getSignName());
                  lagna.put("house", 1);
                });
      }
      ctx.put("lagna", lagna);
    }

    List<HousePositionEntity> houses =
        houseRepository.findByKundaliIdAndTenantIdOrderByHouseAsc(snap.getId(), tenantId);
    List<Map<String, Object>> houseRows = new ArrayList<>();
    for (HousePositionEntity h : houses) {
      Map<String, Object> row = new LinkedHashMap<>();
      row.put("house", (int) h.getHouse());
      row.put("signName", h.getSignName());
      houseRows.add(row);
    }
    ctx.put("houses", houseRows);

    Instant now = Instant.now();
    List<DashaPeriodEntity> periods =
        dashaPeriodRepository.findByKundaliIdAndTenantIdAndSystemCodeOrderByStartAtAscSequenceNoAsc(
            snap.getId(), tenantId, "VIMSHOTTARI");
    Map<String, Object> dashaCurrent = new LinkedHashMap<>();
    DashaPeriodEntity curMaha = null;
    DashaPeriodEntity curAntar = null;
    DashaPeriodEntity curPratyantar = null;
    for (DashaPeriodEntity d : periods) {
      if (!contains(d, now)) {
        continue;
      }
      String level = d.getLevelCode() == null ? "" : d.getLevelCode().toUpperCase(Locale.ROOT);
      if ("MAHA".equals(level) || "MAHADASHA".equals(level)) {
        curMaha = d;
      } else if ("ANTAR".equals(level) || "ANTARDASHA".equals(level)) {
        curAntar = d;
      } else if ("PRATYANTAR".equals(level) || "PRATYANTARDASHA".equals(level) || "PD".equals(level)) {
        curPratyantar = d;
      }
    }
    if (curMaha != null) {
      dashaCurrent.put("maha", dashaRow(curMaha));
    }
    if (curAntar != null) {
      dashaCurrent.put("antar", dashaRow(curAntar));
    }
    if (curPratyantar != null) {
      dashaCurrent.put("pratyantar", dashaRow(curPratyantar));
    }
    dashaCurrent.put("systemCode", "VIMSHOTTARI");
    dashaCurrent.put("asOf", now.toString());
    dashaCurrent.put("available", !periods.isEmpty());
    ctx.put("dashaCurrent", dashaCurrent);

    List<YogaResultEntity> yogas =
        yogaResultRepository.findByKundaliIdAndTenantIdOrderByYogaCodeAsc(snap.getId(), tenantId);
    List<Map<String, Object>> presentYogas = new ArrayList<>();
    for (YogaResultEntity y : yogas) {
      if (!y.isPresent()) {
        continue;
      }
      Map<String, Object> row = new LinkedHashMap<>();
      row.put("yogaCode", y.getYogaCode());
      row.put("displayName", y.getDisplayName());
      row.put("categoryCode", y.getCategoryCode());
      row.put("strengthCode", y.getStrengthCode());
      row.put("explanation", y.getExplanation());
      presentYogas.add(row);
    }
    ctx.put("yogasPresent", presentYogas);

    transitSnapshotRepository
        .findFirstByKundaliIdAndTenantIdOrderByTransitDateDescCreatedAtDesc(snap.getId(), tenantId)
        .ifPresent(
            t -> {
              Map<String, Object> transit = new LinkedHashMap<>();
              transit.put("transitDate", t.getTransitDate() == null ? null : t.getTransitDate().toString());
              transit.put("systemCode", t.getSystemCode());
              transit.put("calculationEngineVersion", t.getCalculationEngineVersion());
              List<TransitPlanetPositionEntity> tp =
                  transitPlanetRepository.findByTransitIdAndTenantIdOrderByPlanetCodeAsc(
                      t.getId(), tenantId);
              List<Map<String, Object>> tRows = new ArrayList<>();
              for (TransitPlanetPositionEntity p : tp) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("planetCode", p.getPlanetCode());
                row.put("signName", p.getSignName());
                row.put("house", (int) p.getHouse());
                row.put("retrograde", p.isRetrograde());
                tRows.add(row);
              }
              transit.put("planets", tRows);
              ctx.put("transit", transit);
            });

    ctx.put("focusHouses", focusHousesForTopic(topic));
    return ctx;
  }

  @SuppressWarnings("unchecked")
  private List<String> buildFindings(Map<String, Object> context, String topic) {
    List<String> findings = new ArrayList<>();
    List<Integer> focus = (List<Integer>) context.getOrDefault("focusHouses", List.of());
    List<Map<String, Object>> planets =
        (List<Map<String, Object>>) context.getOrDefault("planets", List.of());
    for (Map<String, Object> p : planets) {
      Object houseObj = p.get("house");
      if (houseObj instanceof Number n && focus.contains(n.intValue())) {
        findings.add(
            p.get("planetCode")
                + " in "
                + p.get("signName")
                + " (house "
                + n.intValue()
                + ", "
                + p.get("nakshatraName")
                + " pada "
                + p.get("pada")
                + ")"
                + (Boolean.TRUE.equals(p.get("retrograde")) ? " [R]" : "")
                + " — from stored D1.");
      }
    }

    Map<String, Object> dasha = (Map<String, Object>) context.getOrDefault("dashaCurrent", Map.of());
    if (Boolean.TRUE.equals(dasha.get("available"))) {
      Map<String, Object> maha = (Map<String, Object>) dasha.get("maha");
      Map<String, Object> antar = (Map<String, Object>) dasha.get("antar");
      if (maha != null) {
        findings.add(
            "Current Mahadasha "
                + maha.get("lordCode")
                + " ("
                + maha.get("startAt")
                + " → "
                + maha.get("endAt")
                + ") — from stored Vimshottari.");
      }
      if (antar != null) {
        findings.add(
            "Current Antardasha "
                + antar.get("lordCode")
                + " ("
                + antar.get("startAt")
                + " → "
                + antar.get("endAt")
                + ") — from stored Vimshottari.");
      }
    } else {
      findings.add("No Vimshottari dasha rows stored for this kundali yet.");
    }

    List<Map<String, Object>> yogas =
        (List<Map<String, Object>>) context.getOrDefault("yogasPresent", List.of());
    for (Map<String, Object> y : yogas) {
      findings.add(
          "Yoga present: "
              + y.get("displayName")
              + " ("
              + y.get("yogaCode")
              + ") — "
              + y.get("explanation"));
    }
    if (yogas.isEmpty()) {
      findings.add("No implemented yogas marked present on this snapshot.");
    }

    Map<String, Object> transit = (Map<String, Object>) context.get("transit");
    if (transit != null) {
      findings.add(
          "Latest stored Gochar date "
              + transit.get("transitDate")
              + " (optional transit context).");
    }

    if (findings.isEmpty()) {
      findings.add("Chart snapshot loaded but no topic-specific planet/dasha/yoga rows to cite.");
    }
    return findings;
  }

  @SuppressWarnings("unchecked")
  private String buildHeuristicAnswer(
      String question, String topic, Map<String, Object> context, List<String> findings) {
    StringBuilder sb = new StringBuilder();
    sb.append("AI-assisted interpretation (heuristic — no remote LLM).\n\n");
    sb.append("Question (").append(topic).append("): ").append(question).append("\n\n");
    sb.append("Grounded in verified chart data for \"")
        .append(context.get("displayName"))
        .append("\" (engine ")
        .append(context.get("calculationEngineVersion"))
        .append("). Planetary positions and dasha dates below are copied from storage — none are invented.\n\n");

    Map<String, Object> lagna = (Map<String, Object>) context.get("lagna");
    if (lagna != null && lagna.get("signName") != null) {
      sb.append("Lagna (from snapshot): ").append(lagna.get("signName"));
      if (lagna.get("nakshatraName") != null) {
        sb.append(" · ").append(lagna.get("nakshatraName"));
      }
      sb.append(".\n");
    }

    List<Integer> focus = (List<Integer>) context.getOrDefault("focusHouses", List.of());
    sb.append("Topic focus houses: ")
        .append(focus.stream().map(String::valueOf).collect(Collectors.joining(", ")))
        .append(".\n\n");

    sb.append("Structured findings:\n");
    for (int i = 0; i < findings.size(); i++) {
      sb.append(i + 1).append(". ").append(findings.get(i)).append("\n");
    }
    sb.append("\n");
    sb.append(DISCLAIMER);
    return sb.toString();
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> contextUsedSummary(Map<String, Object> context) {
    Map<String, Object> summary = new LinkedHashMap<>();
    summary.put("source", context.get("source"));
    summary.put("kundaliId", context.get("kundaliId"));
    summary.put("displayName", context.get("displayName"));
    summary.put("calculationEngineVersion", context.get("calculationEngineVersion"));
    summary.put("topic", context.get("topic"));
    summary.put("planetCount", ((List<?>) context.getOrDefault("planets", List.of())).size());
    summary.put("houseCount", ((List<?>) context.getOrDefault("houses", List.of())).size());
    Map<String, Object> dasha = (Map<String, Object>) context.getOrDefault("dashaCurrent", Map.of());
    summary.put("dashaAvailable", dasha.get("available"));
    if (dasha.get("maha") instanceof Map<?, ?> m) {
      summary.put("currentMahaLord", m.get("lordCode"));
    }
    summary.put("yogasPresentCount", ((List<?>) context.getOrDefault("yogasPresent", List.of())).size());
    summary.put("transitIncluded", context.containsKey("transit"));
    summary.put("focusHouses", context.get("focusHouses"));
    return summary;
  }

  private static Map<String, Object> dashaRow(DashaPeriodEntity d) {
    Map<String, Object> row = new LinkedHashMap<>();
    row.put("levelCode", d.getLevelCode());
    row.put("lordCode", d.getLordCode());
    row.put("mahaLordCode", d.getMahaLordCode());
    row.put("antarLordCode", d.getAntarLordCode());
    row.put("pratyantarLordCode", d.getPratyantarLordCode());
    row.put("startAt", d.getStartAt() == null ? null : d.getStartAt().toString());
    row.put("endAt", d.getEndAt() == null ? null : d.getEndAt().toString());
    return row;
  }

  private static boolean contains(DashaPeriodEntity d, Instant now) {
    if (d.getStartAt() == null || d.getEndAt() == null) {
      return false;
    }
    return !now.isBefore(d.getStartAt()) && now.isBefore(d.getEndAt());
  }

  private static List<Integer> focusHousesForTopic(String topic) {
    return switch (topic) {
      case "career" -> List.of(10, 6, 2);
      case "marriage" -> List.of(7, 2, 11);
      case "finance" -> List.of(2, 11, 5);
      case "health" -> List.of(1, 6, 8);
      case "education" -> List.of(4, 5, 9);
      case "family" -> List.of(2, 4, 7);
      case "spirituality" -> List.of(9, 12, 5);
      default -> List.of(1, 10, 7, 2);
    };
  }

  private static String normalizeTopic(String raw) {
    if (raw == null || raw.isBlank()) {
      return "general";
    }
    String t = raw.trim().toLowerCase(Locale.ROOT);
    return TOPICS.contains(t) ? t : "general";
  }

  private static Double bd(BigDecimal v) {
    return v == null ? null : v.doubleValue();
  }

  private static String requireTenant() {
    String tenantId = TenantContextFilter.getCurrentTenantId();
    if (tenantId == null || tenantId.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing tenant context.");
    }
    return tenantId;
  }
}
