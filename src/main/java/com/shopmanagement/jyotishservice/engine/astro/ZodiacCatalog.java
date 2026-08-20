package com.shopmanagement.jyotishservice.engine.astro;

/**
 * Rashi / Nakshatra catalogs for sidereal placements.
 *
 * <p>Nakshatra span = 13°20' (800'); pada = 3°20' (200').
 */
public final class ZodiacCatalog {

  public static final String[] SIGNS = {
    "Aries",
    "Taurus",
    "Gemini",
    "Cancer",
    "Leo",
    "Virgo",
    "Libra",
    "Scorpio",
    "Sagittarius",
    "Capricorn",
    "Aquarius",
    "Pisces"
  };

  public static final String[] NAKSHATRAS = {
    "Ashwini",
    "Bharani",
    "Krittika",
    "Rohini",
    "Mrigashira",
    "Ardra",
    "Punarvasu",
    "Pushya",
    "Ashlesha",
    "Magha",
    "Purva Phalguni",
    "Uttara Phalguni",
    "Hasta",
    "Chitra",
    "Swati",
    "Vishakha",
    "Anuradha",
    "Jyeshtha",
    "Mula",
    "Purva Ashadha",
    "Uttara Ashadha",
    "Shravana",
    "Dhanishta",
    "Shatabhisha",
    "Purva Bhadrapada",
    "Uttara Bhadrapada",
    "Revati"
  };

  /** Degrees per nakshatra = 360/27. */
  public static final double NAKSHATRA_SPAN = 360.0 / 27.0;

  /** Degrees per pada = nakshatra / 4. */
  public static final double PADA_SPAN = NAKSHATRA_SPAN / 4.0;

  private ZodiacCatalog() {}

  public static int signIndex(double longitudeDeg) {
    return (int) Math.floor(AstroMath.norm360(longitudeDeg) / 30.0) % 12;
  }

  public static String signName(int signIndex) {
    return SIGNS[Math.floorMod(signIndex, 12)];
  }

  public static double degreeInSign(double longitudeDeg) {
    return AstroMath.norm360(longitudeDeg) % 30.0;
  }

  public static int nakshatraIndex(double longitudeDeg) {
    return (int) Math.floor(AstroMath.norm360(longitudeDeg) / NAKSHATRA_SPAN) % 27;
  }

  public static String nakshatraName(int index) {
    return NAKSHATRAS[Math.floorMod(index, 27)];
  }

  public static int pada(double longitudeDeg) {
    double within = AstroMath.norm360(longitudeDeg) % NAKSHATRA_SPAN;
    return (int) Math.floor(within / PADA_SPAN) + 1;
  }

  /**
   * Whole-sign house number (1–12) given Lagna sign and planet sign.
   */
  public static int wholeSignHouse(int lagnaSignIndex, int planetSignIndex) {
    return Math.floorMod(planetSignIndex - lagnaSignIndex, 12) + 1;
  }
}
