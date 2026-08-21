package com.shopmanagement.jyotishservice.engine.panchang;

/**
 * Classical Panchang limb catalogs (names only — no interpretive scoring).
 *
 * <p>Tithi span = 12° elongation; Yoga/Nakshatra span = 13°20'; Karana = half-tithi (6°).
 */
public final class PanchangCatalog {

  public static final String[] VARA = {
    "Ravivara", "Somavara", "Mangalavara", "Budhavara", "Guruvara", "Shukravara", "Shanivara"
  };

  /** Index 0–13 within a paksha; 14th is Purnima (Shukla) or Amavasya (Krishna). */
  public static final String[] TITHI_BASE = {
    "Pratipada",
    "Dwitiya",
    "Tritiya",
    "Chaturthi",
    "Panchami",
    "Shashthi",
    "Saptami",
    "Ashtami",
    "Navami",
    "Dashami",
    "Ekadashi",
    "Dwadashi",
    "Trayodashi",
    "Chaturdashi"
  };

  public static final String[] YOGA = {
    "Vishkambha",
    "Priti",
    "Ayushman",
    "Saubhagya",
    "Shobhana",
    "Atiganda",
    "Sukarma",
    "Dhriti",
    "Shula",
    "Ganda",
    "Vriddhi",
    "Dhruva",
    "Vyaghata",
    "Harshana",
    "Vajra",
    "Siddhi",
    "Vyatipata",
    "Variyan",
    "Parigha",
    "Shiva",
    "Siddha",
    "Sadhya",
    "Shubha",
    "Shukla",
    "Brahma",
    "Indra",
    "Vaidhriti"
  };

  /** Seven movable karanas (cycle after Kimstughna). */
  public static final String[] MOVABLE_KARANA = {
    "Bava", "Balava", "Kaulava", "Taitila", "Gara", "Vanija", "Vishti"
  };

  public static final double TITHI_SPAN = 12.0;
  public static final double KARANA_SPAN = 6.0;

  private PanchangCatalog() {}

  /** Weekday index 0=Sunday … 6=Saturday → Sanskrit vara name. */
  public static String varaName(int sundayBasedIndex) {
    return VARA[Math.floorMod(sundayBasedIndex, 7)];
  }

  /**
   * Tithi display name for index 0–29 (Shukla Pratipada … Krishna Amavasya).
   */
  public static String tithiName(int tithiIndex) {
    int i = Math.floorMod(tithiIndex, 30);
    if (i == 14) {
      return "Purnima";
    }
    if (i == 29) {
      return "Amavasya";
    }
    return TITHI_BASE[i % 15];
  }

  public static String pakshaName(int tithiIndex) {
    return Math.floorMod(tithiIndex, 30) < 15 ? "Shukla" : "Krishna";
  }

  public static String yogaName(int yogaIndex) {
    return YOGA[Math.floorMod(yogaIndex, 27)];
  }

  /**
   * Karana for half-tithi index 0–59 in a lunar month (elongation / 6°).
   *
   * <p>Fixed: Kimstughna (0), Shakuni (57), Chatushpada (58), Nagava (59); else movable cycle.
   */
  public static String karanaName(int halfIndex) {
    int k = Math.floorMod(halfIndex, 60);
    if (k == 0) {
      return "Kimstughna";
    }
    if (k == 57) {
      return "Shakuni";
    }
    if (k == 58) {
      return "Chatushpada";
    }
    if (k == 59) {
      return "Nagava";
    }
    return MOVABLE_KARANA[(k - 1) % 7];
  }
}
