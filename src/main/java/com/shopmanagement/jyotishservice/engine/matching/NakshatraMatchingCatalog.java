package com.shopmanagement.jyotishservice.engine.matching;

import com.shopmanagement.jyotishservice.engine.astro.ZodiacCatalog;
import com.shopmanagement.jyotishservice.engine.model.Planet;
import com.shopmanagement.jyotishservice.engine.yoga.YogaChartMath;

/**
 * Classical lookup tables for Ashta Koota (nakshatra / rashi attributes). Indexes match {@link
 * ZodiacCatalog} (nakshatra 0–26, sign 0–11).
 */
public final class NakshatraMatchingCatalog {

  public enum Gana {
    DEVA,
    MANUSHYA,
    RAKSHASA
  }

  public enum Nadi {
    ADI,
    MADHYA,
    ANTYA
  }

  public enum Varna {
    BRAHMIN(4),
    KSHATRIYA(3),
    VAISHYA(2),
    SHUDRA(1);

    private final int rank;

    Varna(int rank) {
      this.rank = rank;
    }

    public int rank() {
      return rank;
    }
  }

  public enum Vashya {
    MANAV,
    VANCHAR,
    CHATUSHPAD,
    JALCHAR,
    KEET
  }

  public enum Yoni {
    HORSE,
    ELEPHANT,
    SHEEP,
    SERPENT,
    DOG,
    CAT,
    GOAT,
    RAT,
    COW,
    BUFFALO,
    TIGER,
    DEER,
    MONKEY,
    MONGOOSE,
    LION
  }

  /** Gana per nakshatra 0–26. */
  private static final Gana[] GANA = {
    Gana.DEVA, // Ashwini
    Gana.MANUSHYA, // Bharani
    Gana.RAKSHASA, // Krittika
    Gana.MANUSHYA, // Rohini
    Gana.DEVA, // Mrigashira
    Gana.MANUSHYA, // Ardra
    Gana.DEVA, // Punarvasu
    Gana.DEVA, // Pushya
    Gana.RAKSHASA, // Ashlesha
    Gana.RAKSHASA, // Magha
    Gana.MANUSHYA, // Purva Phalguni
    Gana.MANUSHYA, // Uttara Phalguni
    Gana.DEVA, // Hasta
    Gana.RAKSHASA, // Chitra
    Gana.DEVA, // Swati
    Gana.RAKSHASA, // Vishakha
    Gana.DEVA, // Anuradha
    Gana.RAKSHASA, // Jyeshtha
    Gana.RAKSHASA, // Mula
    Gana.MANUSHYA, // Purva Ashadha
    Gana.MANUSHYA, // Uttara Ashadha
    Gana.DEVA, // Shravana
    Gana.RAKSHASA, // Dhanishta
    Gana.RAKSHASA, // Shatabhisha
    Gana.MANUSHYA, // Purva Bhadrapada
    Gana.MANUSHYA, // Uttara Bhadrapada
    Gana.DEVA // Revati
  };

  /** Nadi cycles Adi / Madhya / Antya. */
  private static final Nadi[] NADI = {
    Nadi.ADI, Nadi.MADHYA, Nadi.ANTYA,
    Nadi.ADI, Nadi.MADHYA, Nadi.ANTYA,
    Nadi.ADI, Nadi.MADHYA, Nadi.ANTYA,
    Nadi.ADI, Nadi.MADHYA, Nadi.ANTYA,
    Nadi.ADI, Nadi.MADHYA, Nadi.ANTYA,
    Nadi.ADI, Nadi.MADHYA, Nadi.ANTYA,
    Nadi.ADI, Nadi.MADHYA, Nadi.ANTYA,
    Nadi.ADI, Nadi.MADHYA, Nadi.ANTYA,
    Nadi.ADI, Nadi.MADHYA, Nadi.ANTYA
  };

  private static final Yoni[] YONI = {
    Yoni.HORSE, // Ashwini
    Yoni.ELEPHANT, // Bharani
    Yoni.SHEEP, // Krittika
    Yoni.SERPENT, // Rohini
    Yoni.SERPENT, // Mrigashira
    Yoni.DOG, // Ardra
    Yoni.CAT, // Punarvasu
    Yoni.GOAT, // Pushya
    Yoni.CAT, // Ashlesha
    Yoni.RAT, // Magha
    Yoni.RAT, // Purva Phalguni
    Yoni.COW, // Uttara Phalguni
    Yoni.BUFFALO, // Hasta
    Yoni.TIGER, // Chitra
    Yoni.BUFFALO, // Swati
    Yoni.TIGER, // Vishakha
    Yoni.DEER, // Anuradha
    Yoni.DEER, // Jyeshtha
    Yoni.DOG, // Mula
    Yoni.MONKEY, // Purva Ashadha
    Yoni.MONGOOSE, // Uttara Ashadha
    Yoni.MONKEY, // Shravana
    Yoni.LION, // Dhanishta
    Yoni.HORSE, // Shatabhisha
    Yoni.LION, // Purva Bhadrapada
    Yoni.COW, // Uttara Bhadrapada
    Yoni.ELEPHANT // Revati
  };

  /** Male (+1) / female (−1) polarity for same-animal sworn-enemy rule. */
  private static final int[] YONI_GENDER = {
    1, -1, -1, -1, 1, -1, -1, 1, -1, 1, -1, 1, -1, -1, 1, -1, -1, 1, 1, -1, 1, 1, -1, -1, 1, -1, -1
  };

  private NakshatraMatchingCatalog() {}

  public static Gana gana(int nakshatraIndex) {
    return GANA[Math.floorMod(nakshatraIndex, 27)];
  }

  public static Nadi nadi(int nakshatraIndex) {
    return NADI[Math.floorMod(nakshatraIndex, 27)];
  }

  public static Yoni yoni(int nakshatraIndex) {
    return YONI[Math.floorMod(nakshatraIndex, 27)];
  }

  public static int yoniGender(int nakshatraIndex) {
    return YONI_GENDER[Math.floorMod(nakshatraIndex, 27)];
  }

  public static Varna varnaOfSign(int signIndex) {
    return switch (Math.floorMod(signIndex, 12)) {
      case 3, 7, 11 -> Varna.BRAHMIN;
      case 0, 4, 8 -> Varna.KSHATRIYA;
      case 1, 5, 9 -> Varna.VAISHYA;
      default -> Varna.SHUDRA;
    };
  }

  public static Vashya vashyaOfSign(int signIndex) {
    return switch (Math.floorMod(signIndex, 12)) {
      case 0, 1, 9 -> Vashya.CHATUSHPAD;
      case 2, 5, 6, 8, 10 -> Vashya.MANAV;
      case 3, 11 -> Vashya.JALCHAR;
      case 4 -> Vashya.VANCHAR;
      case 7 -> Vashya.KEET;
      default -> Vashya.MANAV;
    };
  }

  public static Planet moonSignLord(int signIndex) {
    return YogaChartMath.lordOfSign(signIndex);
  }

  /**
   * Yoni points: same animal same polarity=4; opposite polarity=0; sworn enemies=1; friends=3;
   * otherwise neutral=2. Friend and enemy pairs are disjoint.
   */
  public static int yoniPoints(int nakA, int nakB) {
    Yoni ya = yoni(nakA);
    Yoni yb = yoni(nakB);
    if (ya == yb) {
      return yoniGender(nakA) == yoniGender(nakB) ? 4 : 0;
    }
    if (yoniEnemyPair(ya, yb)) {
      return 1;
    }
    if (yoniFriendPair(ya, yb)) {
      return 3;
    }
    return 2;
  }

  private static boolean yoniEnemyPair(Yoni a, Yoni b) {
    return pair(a, b, Yoni.HORSE, Yoni.BUFFALO)
        || pair(a, b, Yoni.ELEPHANT, Yoni.LION)
        || pair(a, b, Yoni.SHEEP, Yoni.MONKEY)
        || pair(a, b, Yoni.SERPENT, Yoni.MONGOOSE)
        || pair(a, b, Yoni.DOG, Yoni.DEER)
        || pair(a, b, Yoni.CAT, Yoni.RAT)
        || pair(a, b, Yoni.COW, Yoni.TIGER)
        || pair(a, b, Yoni.GOAT, Yoni.MONKEY);
  }

  private static boolean yoniFriendPair(Yoni a, Yoni b) {
    return pair(a, b, Yoni.HORSE, Yoni.SHEEP)
        || pair(a, b, Yoni.ELEPHANT, Yoni.SHEEP)
        || pair(a, b, Yoni.COW, Yoni.GOAT)
        || pair(a, b, Yoni.BUFFALO, Yoni.GOAT)
        || pair(a, b, Yoni.LION, Yoni.TIGER)
        || pair(a, b, Yoni.MONKEY, Yoni.DEER)
        || pair(a, b, Yoni.DOG, Yoni.HORSE)
        || pair(a, b, Yoni.CAT, Yoni.MONGOOSE);
  }

  private static boolean pair(Yoni a, Yoni b, Yoni x, Yoni y) {
    return (a == x && b == y) || (a == y && b == x);
  }
}
