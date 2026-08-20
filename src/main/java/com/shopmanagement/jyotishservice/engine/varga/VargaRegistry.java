package com.shopmanagement.jyotishservice.engine.varga;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Catalog of varga algorithms. Implemented codes have a mapper; others are listed for Coming Soon
 * surfaces without UI changes when a mapper is added later.
 */
public final class VargaRegistry {

  private static final Map<VargaCode, VargaMapper> MAPPERS;

  static {
    EnumMap<VargaCode, VargaMapper> m = new EnumMap<>(VargaCode.class);
    m.put(VargaCode.D1, IdentityVargaMapper.INSTANCE);
    m.put(VargaCode.D2, HoraVargaMapper.INSTANCE);
    m.put(VargaCode.D3, DrekkanaVargaMapper.INSTANCE);
    m.put(VargaCode.D9, NavamshaVargaMapper.INSTANCE);
    m.put(VargaCode.D10, DasamsaVargaMapper.INSTANCE);
    MAPPERS = Collections.unmodifiableMap(m);
  }

  private VargaRegistry() {}

  public static Optional<VargaMapper> mapper(VargaCode code) {
    return Optional.ofNullable(MAPPERS.get(code));
  }

  public static VargaMapper requireMapper(VargaCode code) {
    return mapper(code)
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "Varga " + code.code() + " (" + code.displayName() + ") is Coming Soon"));
  }

  public static boolean isImplemented(VargaCode code) {
    return MAPPERS.containsKey(code);
  }

  public static Set<VargaCode> implemented() {
    return EnumSet.copyOf(MAPPERS.keySet());
  }

  public static VargaCode[] all() {
    return VargaCode.values();
  }
}
