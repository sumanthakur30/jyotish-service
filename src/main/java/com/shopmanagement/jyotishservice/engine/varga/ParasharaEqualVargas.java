package com.shopmanagement.jyotishservice.engine.varga;

import com.shopmanagement.jyotishservice.engine.astro.AstroMath;
import com.shopmanagement.jyotishservice.engine.astro.ZodiacCatalog;

/**
 * Parashara equal-division Vargas beyond D2/D3/D9/D10. Each mapper maps D1 longitude → varga
 * longitude (0–360°).
 */
public final class ParasharaEqualVargas {

  private ParasharaEqualVargas() {}

  /** D4 Chaturthamsha: 7.5° parts → same, 4th, 7th, 10th from natal sign. */
  public static final VargaMapper D4 =
      lon -> {
        double n = AstroMath.norm360(lon);
        int sign = ZodiacCatalog.signIndex(n);
        double deg = n % 30.0;
        int part = Math.min(3, (int) Math.floor(deg / 7.5));
        double within = deg - part * 7.5;
        int dSign = Math.floorMod(sign + part * 3, 12);
        return AstroMath.norm360(dSign * 30.0 + (within / 7.5) * 30.0);
      };

  /** D7 Saptamsha: odd from natal, even from 7th. */
  public static final VargaMapper D7 =
      lon -> {
        double n = AstroMath.norm360(lon);
        int sign = ZodiacCatalog.signIndex(n);
        int start = VargaMath.oddSign(sign) ? sign : Math.floorMod(sign + 6, 12);
        return VargaMath.mapEqualDivision(n, 7, start);
      };

  /** D12 Dwadasamsha: 2.5° parts counting from natal sign. */
  public static final VargaMapper D12 =
      lon -> {
        double n = AstroMath.norm360(lon);
        int sign = ZodiacCatalog.signIndex(n);
        return VargaMath.mapEqualDivision(n, 12, sign);
      };

  /** D16 Shodashamsha: odd from Aries, even from Libra. */
  public static final VargaMapper D16 =
      lon -> {
        double n = AstroMath.norm360(lon);
        int sign = ZodiacCatalog.signIndex(n);
        int start = VargaMath.oddSign(sign) ? 0 : 6;
        return VargaMath.mapEqualDivision(n, 16, start);
      };

  /** D20 Vimshamsha: odd from Aries, even from Sagittarius. */
  public static final VargaMapper D20 =
      lon -> {
        double n = AstroMath.norm360(lon);
        int sign = ZodiacCatalog.signIndex(n);
        int start = VargaMath.oddSign(sign) ? 0 : 8;
        return VargaMath.mapEqualDivision(n, 20, start);
      };

  /** D24 Chaturvimshamsha: odd from Leo, even from Cancer. */
  public static final VargaMapper D24 =
      lon -> {
        double n = AstroMath.norm360(lon);
        int sign = ZodiacCatalog.signIndex(n);
        int start = VargaMath.oddSign(sign) ? 4 : 3;
        return VargaMath.mapEqualDivision(n, 24, start);
      };

  /** D27 Nakshatramsha / Bhamsha: fire→Ar, earth→Cn, air→Li, water→Cp. */
  public static final VargaMapper D27 =
      lon -> {
        double n = AstroMath.norm360(lon);
        int sign = ZodiacCatalog.signIndex(n);
        int element = Math.floorMod(sign, 4);
        int start =
            switch (element) {
              case 0 -> 0; // fire
              case 1 -> 3; // earth
              case 2 -> 6; // air
              default -> 9; // water
            };
        return VargaMath.mapEqualDivision(n, 27, start);
      };

  /** D40 Khavedamsha: odd from Aries, even from Libra. */
  public static final VargaMapper D40 =
      lon -> {
        double n = AstroMath.norm360(lon);
        int sign = ZodiacCatalog.signIndex(n);
        int start = VargaMath.oddSign(sign) ? 0 : 6;
        return VargaMath.mapEqualDivision(n, 40, start);
      };

  /** D45 Akshavedamsha: movable→Ar, fixed→Le, dual→Sg. */
  public static final VargaMapper D45 =
      lon -> {
        double n = AstroMath.norm360(lon);
        int sign = ZodiacCatalog.signIndex(n);
        int mod = Math.floorMod(sign, 3);
        int start = mod == 0 ? 0 : (mod == 1 ? 4 : 8);
        return VargaMath.mapEqualDivision(n, 45, start);
      };

  /** D60 Shashtyamsha: 0.5° parts counting from natal sign. */
  public static final VargaMapper D60 =
      lon -> {
        double n = AstroMath.norm360(lon);
        int sign = ZodiacCatalog.signIndex(n);
        return VargaMath.mapEqualDivision(n, 60, sign);
      };
}
