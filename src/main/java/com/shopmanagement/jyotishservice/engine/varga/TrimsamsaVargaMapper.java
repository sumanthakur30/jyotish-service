package com.shopmanagement.jyotishservice.engine.varga;

import com.shopmanagement.jyotishservice.engine.astro.AstroMath;
import com.shopmanagement.jyotishservice.engine.astro.ZodiacCatalog;

/**
 * D30 Trimshamsha — unequal spans with planet-owned signs (Parashara).
 *
 * <p>Odd signs: Ma 5° · Sa 5° · Ju 8° · Me 7° · Ve 5°. Even: Ve 5° · Me 7° · Ju 8° · Sa 5° · Ma
 * 5°.
 */
public final class TrimsamsaVargaMapper implements VargaMapper {

  public static final TrimsamsaVargaMapper INSTANCE = new TrimsamsaVargaMapper();

  /** Odd-sign lord order → sign index (Aries, Aquarius, Sagittarius, Gemini, Libra). */
  private static final int[] ODD_SIGNS = {0, 10, 8, 2, 6};

  /** Even-sign lord order → sign index (Taurus, Virgo, Pisces, Capricorn, Scorpio). */
  private static final int[] EVEN_SIGNS = {1, 5, 11, 9, 7};

  private static final double[] ODD_SPANS = {5, 5, 8, 7, 5};
  private static final double[] EVEN_SPANS = {5, 7, 8, 5, 5};

  private TrimsamsaVargaMapper() {}

  @Override
  public double mapLongitude(double d1LongitudeDeg) {
    double lon = AstroMath.norm360(d1LongitudeDeg);
    int sign = ZodiacCatalog.signIndex(lon);
    double deg = lon % 30.0;
    boolean odd = VargaMath.oddSign(sign);
    double[] spans = odd ? ODD_SPANS : EVEN_SPANS;
    int[] signs = odd ? ODD_SIGNS : EVEN_SIGNS;
    double cursor = 0;
    for (int i = 0; i < 5; i++) {
      double end = cursor + spans[i];
      if (deg < end || i == 4) {
        double within = Math.min(deg, end) - cursor;
        double fraction = within / spans[i];
        return AstroMath.norm360(signs[i] * 30.0 + fraction * 30.0);
      }
      cursor = end;
    }
    return AstroMath.norm360(signs[4] * 30.0);
  }
}
