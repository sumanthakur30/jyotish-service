package com.shopmanagement.jyotishservice.engine.explain;

import java.util.Locale;
import java.util.Map;

/** Bilingual display helpers for Simple View — labels only, never invents placements. */
public final class SimpleLabels {

  private SimpleLabels() {}

  private static final Map<String, String> SIGN_HI =
      Map.ofEntries(
          Map.entry("Aries", "मेष"),
          Map.entry("Taurus", "वृषभ"),
          Map.entry("Gemini", "मिथुन"),
          Map.entry("Cancer", "कर्क"),
          Map.entry("Leo", "सिंह"),
          Map.entry("Virgo", "कन्या"),
          Map.entry("Libra", "तुला"),
          Map.entry("Scorpio", "वृश्चिक"),
          Map.entry("Sagittarius", "धनु"),
          Map.entry("Capricorn", "मकर"),
          Map.entry("Aquarius", "कुंभ"),
          Map.entry("Pisces", "मीन"));

  private static final Map<String, String> NAKSHATRA_HI =
      Map.ofEntries(
          Map.entry("Ashwini", "अश्विनी"),
          Map.entry("Bharani", "भरणी"),
          Map.entry("Krittika", "कृत्तिका"),
          Map.entry("Rohini", "रोहिणी"),
          Map.entry("Mrigashira", "मृगशिरा"),
          Map.entry("Ardra", "आर्द्रा"),
          Map.entry("Punarvasu", "पुनर्वसु"),
          Map.entry("Pushya", "पुष्य"),
          Map.entry("Ashlesha", "आश्लेषा"),
          Map.entry("Magha", "मघा"),
          Map.entry("Purva Phalguni", "पूर्व फाल्गुनी"),
          Map.entry("Uttara Phalguni", "उत्तर फाल्गुनी"),
          Map.entry("Hasta", "हस्त"),
          Map.entry("Chitra", "चित्रा"),
          Map.entry("Swati", "स्वाती"),
          Map.entry("Vishakha", "विशाखा"),
          Map.entry("Anuradha", "अनुराधा"),
          Map.entry("Jyeshtha", "ज्येष्ठा"),
          Map.entry("Mula", "मूल"),
          Map.entry("Purva Ashadha", "पूर्वाषाढ़ा"),
          Map.entry("Uttara Ashadha", "उत्तराषाढ़ा"),
          Map.entry("Shravana", "श्रवण"),
          Map.entry("Dhanishta", "धनिष्ठा"),
          Map.entry("Shatabhisha", "शतभिषा"),
          Map.entry("Purva Bhadrapada", "पूर्व भाद्रपद"),
          Map.entry("Uttara Bhadrapada", "उत्तर भाद्रपद"),
          Map.entry("Revati", "रेवती"));

  public static String signHi(String signEn) {
    if (signEn == null || signEn.isBlank()) {
      return null;
    }
    return SIGN_HI.getOrDefault(signEn.trim(), signEn);
  }

  public static String nakshatraHi(String nameEn) {
    if (nameEn == null || nameEn.isBlank()) {
      return null;
    }
    return NAKSHATRA_HI.getOrDefault(nameEn.trim(), nameEn);
  }

  public static String statusLineEn(String status) {
    String s = normStatus(status);
    return switch (s) {
      case "IN_PROGRESS" -> "Notes in progress";
      case "COMPLETED" -> "Notes completed";
      default -> "Notes not started yet";
    };
  }

  public static String statusLineHi(String status) {
    String s = normStatus(status);
    return switch (s) {
      case "IN_PROGRESS" -> "टिप्पणियाँ प्रगति पर";
      case "COMPLETED" -> "टिप्पणियाँ पूर्ण";
      default -> "टिप्पणियाँ अभी शुरू नहीं";
    };
  }

  private static String normStatus(String status) {
    return status == null ? "" : status.trim().toUpperCase(Locale.ROOT);
  }
}
