package com.shopmanagement.jyotishservice.engine.dasha;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Catalog of dasha calculators. Implemented systems have a calculator; others are listed for Coming
 * Soon surfaces without UI changes when a calculator is added later.
 */
public final class DashaRegistry {

  private static final Map<DashaSystemCode, DashaCalculator> CALCULATORS;

  static {
    EnumMap<DashaSystemCode, DashaCalculator> m = new EnumMap<>(DashaSystemCode.class);
    m.put(DashaSystemCode.VIMSHOTTARI, VimshottariDashaCalculator.INSTANCE);
    CALCULATORS = Collections.unmodifiableMap(m);
  }

  private DashaRegistry() {}

  public static Optional<DashaCalculator> calculator(DashaSystemCode code) {
    return Optional.ofNullable(CALCULATORS.get(code));
  }

  public static DashaCalculator requireCalculator(DashaSystemCode code) {
    return calculator(code)
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "Dasha " + code.code() + " (" + code.displayName() + ") is Coming Soon"));
  }

  public static boolean isImplemented(DashaSystemCode code) {
    return CALCULATORS.containsKey(code);
  }

  public static Set<DashaSystemCode> implemented() {
    return EnumSet.copyOf(CALCULATORS.keySet());
  }

  public static DashaSystemCode[] all() {
    return DashaSystemCode.values();
  }
}
