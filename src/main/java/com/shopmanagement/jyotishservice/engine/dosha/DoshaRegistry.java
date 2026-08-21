package com.shopmanagement.jyotishservice.engine.dosha;

import java.util.ArrayList;
import java.util.List;

import com.shopmanagement.jyotishservice.engine.astro.ZodiacCatalog;
import com.shopmanagement.jyotishservice.engine.matching.ManglikAnalyzer;
import com.shopmanagement.jyotishservice.engine.matching.ManglikAssessment;
import com.shopmanagement.jyotishservice.engine.matching.MatchingPerson;
import com.shopmanagement.jyotishservice.engine.model.D1Chart;
import com.shopmanagement.jyotishservice.engine.model.Planet;
import com.shopmanagement.jyotishservice.engine.model.PlanetPosition;

/** Single-chart dosha screens — descriptive only, not predictions. */
public final class DoshaRegistry {

  private DoshaRegistry() {}

  public static List<DoshaHit> evaluate(D1Chart d1) {
    List<DoshaHit> hits = new ArrayList<>();
    hits.add(manglik(d1));
    hits.add(kaalSarp(d1));
    return hits;
  }

  private static DoshaHit manglik(D1Chart d1) {
    MatchingPerson person = MatchingPerson.fromD1(null, "chart", d1);
    ManglikAssessment a = ManglikAnalyzer.assess(person);
    String status =
        switch (a.status()) {
          case PRESENT -> "PRESENT";
          case CANCELLED -> "CANCELLED";
          case ABSENT -> "ABSENT";
        };
    List<String> conditions = new ArrayList<>();
    conditions.add(
        "Mars in house "
            + a.marsHouse()
            + " ("
            + a.marsSignName()
            + "). Relevant houses: "
            + a.relevantHouses());
    conditions.add(a.reasoning());
    for (var c : a.appliedCancellations()) {
        conditions.add("Cancellation " + c.code() + ": " + c.label() + " — " + c.detail());
    }
    return new DoshaHit(
        "MANGLIK",
        "Mangal Dosha",
        "मंगल दोष",
        status,
        a.present() ? "MODERATE" : (a.cancelled() ? "CANCELLED" : null),
        List.of("MARS"),
        List.of(a.marsHouse()),
        conditions,
        "According to the selected Jyotish rules (Mars in 1/2/4/7/8/12 with documented"
            + " cancellations). Descriptive assessment only — not a marriage verdict.",
        "MANGLIK_D1_V1",
        true);
  }

  /**
   * Kaal Sarp (simplified whole-sign screen): all seven classical planets lie on one arc between
   * Rahu and Ketu (either direction). Does not invent named subtypes yet.
   */
  private static DoshaHit kaalSarp(D1Chart d1) {
    PlanetPosition rahu =
        d1.planets().stream()
            .filter(p -> p.planet() == Planet.RAHU)
            .findFirst()
            .orElse(null);
    PlanetPosition ketu =
        d1.planets().stream()
            .filter(p -> p.planet() == Planet.KETU)
            .findFirst()
            .orElse(null);
    if (rahu == null || ketu == null) {
      return new DoshaHit(
          "KAAL_SARP",
          "Kaal Sarp",
          "काल सर्प",
          "UNAVAILABLE",
          null,
          List.of(),
          List.of(),
          List.of("Rahu/Ketu positions required."),
          "Cannot assess Kaal Sarp without nodes.",
          "KAAL_SARP_NODES_ARC_D1_V1",
          true);
    }
    int r = rahu.signIndex();
    int k = ketu.signIndex();
    List<Planet> classical =
        List.of(
            Planet.SUN,
            Planet.MOON,
            Planet.MARS,
            Planet.MERCURY,
            Planet.JUPITER,
            Planet.VENUS,
            Planet.SATURN);
    boolean allInArcA = true;
    boolean allInArcB = true;
    List<String> placements = new ArrayList<>();
    for (Planet pl : classical) {
      PlanetPosition pos =
          d1.planets().stream().filter(p -> p.planet() == pl).findFirst().orElse(null);
      if (pos == null) {
        allInArcA = false;
        allInArcB = false;
        continue;
      }
      placements.add(pl.displayName() + " " + ZodiacCatalog.signName(pos.signIndex()));
      if (!inArc(r, k, pos.signIndex())) {
        allInArcA = false;
      }
      if (!inArc(k, r, pos.signIndex())) {
        allInArcB = false;
      }
    }
    boolean present = allInArcA || allInArcB;
    List<String> conditions = new ArrayList<>();
    conditions.add(
        "Rahu "
            + ZodiacCatalog.signName(r)
            + ", Ketu "
            + ZodiacCatalog.signName(k)
            + " (whole-sign).");
    conditions.add("Classical planets: " + String.join(", ", placements));
    conditions.add(
        present
            ? "All seven lie on one Rahu–Ketu arc under this screen."
            : "Planets span both arcs — pattern not present under this screen.");
    return new DoshaHit(
        "KAAL_SARP",
        "Kaal Sarp",
        "काल सर्प",
        present ? "PRESENT" : "ABSENT",
        present ? "MODERATE" : null,
        List.of("RAHU", "KETU"),
        List.of(rahu.house(), ketu.house()),
        conditions,
        "According to the selected Jyotish rules (simplified node-arc screen). Subtypes and"
            + " exceptions are not fully modelled yet. Descriptive only — not a prediction of"
            + " hardship.",
        "KAAL_SARP_NODES_ARC_D1_V1",
        true);
  }

  /** Signs strictly between start and end walking forward (excluding endpoints optional). */
  private static boolean inArc(int start, int end, int sign) {
    int s = Math.floorMod(sign, 12);
    int a = Math.floorMod(start, 12);
    int b = Math.floorMod(end, 12);
    if (a == b) {
      return false;
    }
    int cur = Math.floorMod(a + 1, 12);
    while (cur != b) {
      if (cur == s) {
        return true;
      }
      cur = Math.floorMod(cur + 1, 12);
    }
    return false;
  }

  public record DoshaHit(
      String doshaCode,
      String displayNameEn,
      String displayNameHi,
      String status,
      String severityCode,
      List<String> planets,
      List<Integer> houses,
      List<String> conditions,
      String explanation,
      String ruleId,
      boolean implemented) {}
}
