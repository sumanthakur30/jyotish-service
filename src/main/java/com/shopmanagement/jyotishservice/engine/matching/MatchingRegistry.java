package com.shopmanagement.jyotishservice.engine.matching;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Catalog of matching calculators. Implemented systems have a calculator; others remain Coming
 * Soon.
 */
public final class MatchingRegistry {

  private static final Map<MatchingSystemCode, MatchingCalculator> CALCULATORS;

  static {
    EnumMap<MatchingSystemCode, MatchingCalculator> m = new EnumMap<>(MatchingSystemCode.class);
    m.put(MatchingSystemCode.ASHTA_KOOTA, AshtaKootaMatchingCalculator.INSTANCE);
    m.put(MatchingSystemCode.MANGLIK, ManglikMatchingCalculator.INSTANCE);
    CALCULATORS = Collections.unmodifiableMap(m);
  }

  private MatchingRegistry() {}

  public static Optional<MatchingCalculator> calculator(MatchingSystemCode code) {
    return Optional.ofNullable(CALCULATORS.get(code));
  }

  public static MatchingCalculator requireCalculator(MatchingSystemCode code) {
    return calculator(code)
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "Matching system "
                        + code.code()
                        + " ("
                        + code.displayName()
                        + ") is Coming Soon"));
  }

  public static boolean isImplemented(MatchingSystemCode code) {
    return CALCULATORS.containsKey(code);
  }

  public static Set<MatchingSystemCode> implemented() {
    return EnumSet.copyOf(CALCULATORS.keySet());
  }

  public static MatchingSystemCode[] all() {
    return MatchingSystemCode.values();
  }

  /**
   * Run all implemented calculators and build a full report. Unimplemented catalog entries are
   * omitted (Coming Soon via API catalog).
   */
  public static MatchingReport compute(
      MatchingPerson personA, MatchingPerson personB, String engineVersion) {
    MatchingReportBuilder builder =
        new MatchingReportBuilder()
            .persons(personA, personB)
            .notes(
                "Ashta Koota + Manglik V1.4 from D1 Moon/Mars whole-sign positions. Person A is the"
                    + " classical groom-side orientation for directional kootas; Person B is the"
                    + " bride-side. Manglik cancellations are Coming Soon.");
    for (MatchingSystemCode code : MatchingSystemCode.values()) {
      MatchingCalculator calc = CALCULATORS.get(code);
      if (calc != null) {
        calc.contribute(personA, personB, builder);
      }
    }
    return builder.build(engineVersion);
  }
}
