package com.shopmanagement.jyotishservice.engine.yoga;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Catalog of yoga detectors. Implemented yogas have a detector; others remain Coming Soon (no fake
 * presence rows).
 */
public final class YogaRegistry {

  private static final Map<YogaCode, YogaDetector> DETECTORS;

  static {
    EnumMap<YogaCode, YogaDetector> m = new EnumMap<>(YogaCode.class);
    m.put(YogaCode.GAJAKESARI, GajakesariYogaDetector.INSTANCE);
    m.put(YogaCode.DHARMA_KARMADHIPATI, DharmaKarmadhipatiYogaDetector.INSTANCE);
    m.put(YogaCode.DHANA_2_11, Dhana211YogaDetector.INSTANCE);
    m.put(YogaCode.RUCHAKA, RuchakaYogaDetector.INSTANCE);
    m.put(YogaCode.BHADRA, BhadraYogaDetector.INSTANCE);
    DETECTORS = Collections.unmodifiableMap(m);
  }

  private YogaRegistry() {}

  public static Optional<YogaDetector> detector(YogaCode code) {
    return Optional.ofNullable(DETECTORS.get(code));
  }

  public static YogaDetector requireDetector(YogaCode code) {
    return detector(code)
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "Yoga " + code.code() + " (" + code.displayName() + ") is Coming Soon"));
  }

  public static boolean isImplemented(YogaCode code) {
    return DETECTORS.containsKey(code);
  }

  public static Set<YogaCode> implemented() {
    return EnumSet.copyOf(DETECTORS.keySet());
  }

  public static YogaCode[] all() {
    return YogaCode.values();
  }

  /** Run all implemented detectors; order follows {@link YogaCode} declaration. */
  public static List<YogaHit> evaluateAll(YogaContext context) {
    List<YogaHit> hits = new ArrayList<>();
    for (YogaCode code : YogaCode.values()) {
      YogaDetector d = DETECTORS.get(code);
      if (d != null) {
        hits.add(d.detect(context));
      }
    }
    return hits;
  }
}
