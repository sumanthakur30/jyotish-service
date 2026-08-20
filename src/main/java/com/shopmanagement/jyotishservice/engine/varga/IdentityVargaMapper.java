package com.shopmanagement.jyotishservice.engine.varga;

/** D1 identity — Rashi longitudes pass through unchanged. */
public final class IdentityVargaMapper implements VargaMapper {

  public static final IdentityVargaMapper INSTANCE = new IdentityVargaMapper();

  private IdentityVargaMapper() {}

  @Override
  public double mapLongitude(double d1LongitudeDeg) {
    return d1LongitudeDeg;
  }
}
