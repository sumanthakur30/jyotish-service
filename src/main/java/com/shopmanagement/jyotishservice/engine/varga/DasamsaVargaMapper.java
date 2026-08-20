package com.shopmanagement.jyotishservice.engine.varga;

import com.shopmanagement.jyotishservice.engine.astro.AstroMath;
import com.shopmanagement.jyotishservice.engine.astro.ZodiacCatalog;

/**
 * D10 Dasamsha: each sign into 10 parts of 3°. Odd signs start from the same sign; even signs from
 * the 9th.
 */
public final class DasamsaVargaMapper implements VargaMapper {

  public static final DasamsaVargaMapper INSTANCE = new DasamsaVargaMapper();

  private DasamsaVargaMapper() {}

  @Override
  public double mapLongitude(double d1LongitudeDeg) {
    double lon = AstroMath.norm360(d1LongitudeDeg);
    int sign = ZodiacCatalog.signIndex(lon);
    int start = VargaMath.oddSign(sign) ? sign : Math.floorMod(sign + 8, 12);
    return VargaMath.mapEqualDivision(lon, 10, start);
  }
}
