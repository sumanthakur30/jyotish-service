package com.shopmanagement.jyotishservice.engine.varga;

import com.shopmanagement.jyotishservice.engine.astro.AstroMath;

/**
 * D3 Drekkana: 10° thirds — 1st same sign, 2nd 5th from sign, 3rd 9th from sign.
 */
public final class DrekkanaVargaMapper implements VargaMapper {

  public static final DrekkanaVargaMapper INSTANCE = new DrekkanaVargaMapper();

  private DrekkanaVargaMapper() {}

  @Override
  public double mapLongitude(double d1LongitudeDeg) {
    double lon = AstroMath.norm360(d1LongitudeDeg);
    int sign = (int) Math.floor(lon / 30.0) % 12;
    double degInSign = lon % 30.0;
    int part = Math.min(2, (int) Math.floor(degInSign / 10.0));
    double within = degInSign - part * 10.0;
    double fraction = within / 10.0;
    int dSign = Math.floorMod(sign + part * 4, 12);
    return AstroMath.norm360(dSign * 30.0 + fraction * 30.0);
  }
}
