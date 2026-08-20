package com.shopmanagement.jyotishservice.engine.varga;

import com.shopmanagement.jyotishservice.engine.astro.AstroMath;
import com.shopmanagement.jyotishservice.engine.astro.ZodiacCatalog;

/**
 * D9 Navamsha (Parashara): each rashi split into 9 parts of 3°20'. Start sign by movable / fixed /
 * dual nature of the D1 sign.
 *
 * <p>Tolerance for regression tests: exact sign + ±0.01° on reconstructed longitude for known
 * boundary cases (see {@code NavamshaVargaMapperTest}).
 */
public final class NavamshaVargaMapper implements VargaMapper {

  public static final NavamshaVargaMapper INSTANCE = new NavamshaVargaMapper();

  /** Degrees per navamsha = 30/9 = 3°20'. */
  public static final double NAVAMSHA_SPAN = 30.0 / 9.0;

  private NavamshaVargaMapper() {}

  @Override
  public double mapLongitude(double d1LongitudeDeg) {
    double lon = AstroMath.norm360(d1LongitudeDeg);
    int sign = ZodiacCatalog.signIndex(lon);
    int start = VargaMath.movableFixedDualStart(sign);
    return VargaMath.mapEqualDivision(lon, 9, start);
  }

  /** 0-based navamsha index within the D1 sign (0–8). */
  public static int navamshaIndexInSign(double d1LongitudeDeg) {
    double degInSign = ZodiacCatalog.degreeInSign(d1LongitudeDeg);
    return Math.min(8, (int) Math.floor(degInSign / NAVAMSHA_SPAN));
  }
}
