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
    m.put(VargaCode.D4, ParasharaEqualVargas.D4);
    m.put(VargaCode.D7, ParasharaEqualVargas.D7);
    m.put(VargaCode.D9, NavamshaVargaMapper.INSTANCE);
    m.put(VargaCode.D10, DasamsaVargaMapper.INSTANCE);
    m.put(VargaCode.D12, ParasharaEqualVargas.D12);
    m.put(VargaCode.D16, ParasharaEqualVargas.D16);
    m.put(VargaCode.D20, ParasharaEqualVargas.D20);
    m.put(VargaCode.D24, ParasharaEqualVargas.D24);
    m.put(VargaCode.D27, ParasharaEqualVargas.D27);
    m.put(VargaCode.D30, TrimsamsaVargaMapper.INSTANCE);
    m.put(VargaCode.D40, ParasharaEqualVargas.D40);
    m.put(VargaCode.D45, ParasharaEqualVargas.D45);
    m.put(VargaCode.D60, ParasharaEqualVargas.D60);
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
