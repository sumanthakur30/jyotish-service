package com.shopmanagement.jyotishservice.engine.varga;

import com.shopmanagement.jyotishservice.engine.astro.AstroMath;

/**
 * D2 Hora (Parashara): each sign split at 15°. Odd signs — first half Sun (Leo), second Moon
 * (Cancer); even signs reverse. Longitude is placed proportionally within the Hora sign.
 */
public final class HoraVargaMapper implements VargaMapper {

  public static final HoraVargaMapper INSTANCE = new HoraVargaMapper();

  private static final int LEO = 4;
  private static final int CANCER = 3;

  private HoraVargaMapper() {}

  @Override
  public double mapLongitude(double d1LongitudeDeg) {
    double lon = AstroMath.norm360(d1LongitudeDeg);
    int sign = (int) Math.floor(lon / 30.0) % 12;
    double degInSign = lon % 30.0;
    boolean firstHalf = degInSign < 15.0;
    double within = firstHalf ? degInSign : degInSign - 15.0;
    double fraction = within / 15.0;

    int horaSign;
    if (VargaMath.oddSign(sign)) {
      horaSign = firstHalf ? LEO : CANCER;
    } else {
      horaSign = firstHalf ? CANCER : LEO;
    }
    return AstroMath.norm360(horaSign * 30.0 + fraction * 30.0);
  }
}
