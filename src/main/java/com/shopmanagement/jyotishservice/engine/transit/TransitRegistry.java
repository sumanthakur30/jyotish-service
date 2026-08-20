package com.shopmanagement.jyotishservice.engine.transit;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Catalog of transit calculators. Implemented systems have a calculator; others remain Coming Soon
 * (e.g. detailed Sade Sati).
 */
public final class TransitRegistry {

  private static final Map<TransitSystemCode, TransitCalculator> CALCULATORS;

  static {
    EnumMap<TransitSystemCode, TransitCalculator> m = new EnumMap<>(TransitSystemCode.class);
    m.put(TransitSystemCode.GOCHAR, GocharTransitCalculator.INSTANCE);
    CALCULATORS = Collections.unmodifiableMap(m);
  }

  private TransitRegistry() {}

  public static Optional<TransitCalculator> calculator(TransitSystemCode code) {
    return Optional.ofNullable(CALCULATORS.get(code));
  }

  public static TransitCalculator requireCalculator(TransitSystemCode code) {
    return calculator(code)
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "Transit "
                        + code.code()
                        + " ("
                        + code.displayName()
                        + ") is Coming Soon"));
  }

  public static boolean isImplemented(TransitSystemCode code) {
    return CALCULATORS.containsKey(code);
  }

  public static Set<TransitSystemCode> implemented() {
    return EnumSet.copyOf(CALCULATORS.keySet());
  }

  public static TransitSystemCode[] all() {
    return TransitSystemCode.values();
  }
}
