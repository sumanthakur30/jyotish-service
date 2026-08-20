package com.shopmanagement.jyotishservice.engine.matching;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.shopmanagement.jyotishservice.engine.model.Planet;

/**
 * Classical Ashta Koota (36-point Guna Milan). Person A is treated as the traditional "groom" side
 * and Person B as the "bride" side for directional factors (Varna, Vashya, Tara).
 */
public final class AshtaKootaCalculator {

  private static final String RULE = "ASHTA_KOOTA_V1";

  /** Permanent friendship: friends of each planet (excluding self). */
  private static final Map<Planet, Set<Planet>> FRIENDS = new EnumMap<>(Planet.class);

  private static final Map<Planet, Set<Planet>> ENEMIES = new EnumMap<>(Planet.class);

  static {
    FRIENDS.put(Planet.SUN, EnumSet.of(Planet.MOON, Planet.MARS, Planet.JUPITER));
    FRIENDS.put(Planet.MOON, EnumSet.of(Planet.SUN, Planet.MERCURY));
    FRIENDS.put(Planet.MARS, EnumSet.of(Planet.SUN, Planet.MOON, Planet.JUPITER));
    FRIENDS.put(Planet.MERCURY, EnumSet.of(Planet.SUN, Planet.VENUS));
    FRIENDS.put(Planet.JUPITER, EnumSet.of(Planet.SUN, Planet.MOON, Planet.MARS));
    FRIENDS.put(Planet.VENUS, EnumSet.of(Planet.MERCURY, Planet.SATURN));
    FRIENDS.put(Planet.SATURN, EnumSet.of(Planet.MERCURY, Planet.VENUS));

    ENEMIES.put(Planet.SUN, EnumSet.of(Planet.VENUS, Planet.SATURN));
    ENEMIES.put(Planet.MOON, EnumSet.noneOf(Planet.class));
    ENEMIES.put(Planet.MARS, EnumSet.of(Planet.MERCURY));
    ENEMIES.put(Planet.MERCURY, EnumSet.of(Planet.MOON));
    ENEMIES.put(Planet.JUPITER, EnumSet.of(Planet.MERCURY, Planet.VENUS));
    ENEMIES.put(Planet.VENUS, EnumSet.of(Planet.SUN, Planet.MOON));
    ENEMIES.put(Planet.SATURN, EnumSet.of(Planet.SUN, Planet.MOON, Planet.MARS));
  }

  /** Vashya boy×girl points (rows = A/groom, cols = B/bride). Order: Manav, Vanchar, Chatushpad, Jalchar, Keet. */
  private static final int[][] VASHYA_MATRIX = {
    // Man Van Chat Jal Keet
    {2, 1, 1, 1, 1}, // Manav
    {0, 2, 1, 1, 2}, // Vanchar
    {0, 1, 2, 1, 1}, // Chatushpad
    {0, 1, 1, 2, 0}, // Jalchar
    {0, 1, 1, 0, 2} // Keet
  };

  private AshtaKootaCalculator() {}

  public static List<KootaScore> score(MatchingPerson personA, MatchingPerson personB) {
    List<KootaScore> scores = new ArrayList<>(8);
    scores.add(varna(personA, personB));
    scores.add(vashya(personA, personB));
    scores.add(tara(personA, personB));
    scores.add(yoni(personA, personB));
    scores.add(grahaMaitri(personA, personB));
    scores.add(gana(personA, personB));
    scores.add(bhakoot(personA, personB));
    scores.add(nadi(personA, personB));
    return scores;
  }

  public static int totalObtained(List<KootaScore> scores) {
    return scores.stream().mapToInt(KootaScore::obtained).sum();
  }

  static KootaScore varna(MatchingPerson a, MatchingPerson b) {
    var va = NakshatraMatchingCatalog.varnaOfSign(a.moonSignIndex());
    var vb = NakshatraMatchingCatalog.varnaOfSign(b.moonSignIndex());
    int pts = va.rank() >= vb.rank() ? 1 : 0;
    return KootaScore.of(
        KootaCode.VARNA,
        pts,
        "Moon signs "
            + a.moonSignName()
            + " ("
            + va
            + ") and "
            + b.moonSignName()
            + " ("
            + vb
            + "). Traditional indicator awards the point when Person A's Varna rank is equal or"
            + " higher than Person B's.",
        RULE + "_VARNA");
  }

  static KootaScore vashya(MatchingPerson a, MatchingPerson b) {
    var va = NakshatraMatchingCatalog.vashyaOfSign(a.moonSignIndex());
    var vb = NakshatraMatchingCatalog.vashyaOfSign(b.moonSignIndex());
    int pts = VASHYA_MATRIX[va.ordinal()][vb.ordinal()];
    return KootaScore.of(
        KootaCode.VASHYA,
        pts,
        "Vashya classes "
            + va
            + " ("
            + a.moonSignName()
            + ") × "
            + vb
            + " ("
            + b.moonSignName()
            + ") → "
            + pts
            + "/"
            + KootaCode.VASHYA.maxPoints()
            + ".",
        RULE + "_VASHYA");
  }

  static KootaScore tara(MatchingPerson a, MatchingPerson b) {
    // Count from B (bride) to A (groom): offset 0 = Janma (1st Tara).
    int offset = Math.floorMod(a.moonNakshatraIndex() - b.moonNakshatraIndex(), 27);
    int taraNumber = (offset % 9) + 1;
    boolean favorable =
        taraNumber == 2 || taraNumber == 4 || taraNumber == 6 || taraNumber == 8 || taraNumber == 9;
    int pts = favorable ? 3 : 0;
    String[] names = {
      "Janma", "Sampat", "Vipat", "Kshema", "Pratyak", "Sadhana", "Naidhana", "Mitra", "Param Mitra"
    };
    return KootaScore.of(
        KootaCode.TARA,
        pts,
        "Nakshatra count from "
            + b.moonNakshatraName()
            + " to "
            + a.moonNakshatraName()
            + " → Tara "
            + taraNumber
            + " ("
            + names[taraNumber - 1]
            + "). Favorable Taras (2,4,6,8,9) score full points.",
        RULE + "_TARA");
  }

  static KootaScore yoni(MatchingPerson a, MatchingPerson b) {
    int pts =
        NakshatraMatchingCatalog.yoniPoints(a.moonNakshatraIndex(), b.moonNakshatraIndex());
    var ya = NakshatraMatchingCatalog.yoni(a.moonNakshatraIndex());
    var yb = NakshatraMatchingCatalog.yoni(b.moonNakshatraIndex());
    return KootaScore.of(
        KootaCode.YONI,
        pts,
        "Yoni "
            + ya
            + " ("
            + a.moonNakshatraName()
            + ") vs "
            + yb
            + " ("
            + b.moonNakshatraName()
            + ") → "
            + pts
            + "/"
            + KootaCode.YONI.maxPoints()
            + ".",
        RULE + "_YONI");
  }

  static KootaScore grahaMaitri(MatchingPerson a, MatchingPerson b) {
    Planet la = NakshatraMatchingCatalog.moonSignLord(a.moonSignIndex());
    Planet lb = NakshatraMatchingCatalog.moonSignLord(b.moonSignIndex());
    int pts;
    String rel;
    if (la == lb) {
      pts = 5;
      rel = "same lord";
    } else if (isFriend(la, lb) && isFriend(lb, la)) {
      pts = 5;
      rel = "mutual friends";
    } else if (isFriend(la, lb) || isFriend(lb, la)) {
      pts = 4;
      rel = "one-sided friend";
    } else if (isEnemy(la, lb) || isEnemy(lb, la)) {
      pts = 0;
      rel = "enemy";
    } else {
      pts = 3;
      rel = "neutral";
    }
    return KootaScore.of(
        KootaCode.GRAHA_MAITRI,
        pts,
        "Moon-sign lords "
            + la.name()
            + " ("
            + a.moonSignName()
            + ") and "
            + lb.name()
            + " ("
            + b.moonSignName()
            + ") — "
            + rel
            + ".",
        RULE + "_GRAHA_MAITRI");
  }

  static KootaScore gana(MatchingPerson a, MatchingPerson b) {
    var ga = NakshatraMatchingCatalog.gana(a.moonNakshatraIndex());
    var gb = NakshatraMatchingCatalog.gana(b.moonNakshatraIndex());
    int pts;
    if (ga == gb) {
      pts = 6;
    } else if ((ga == NakshatraMatchingCatalog.Gana.DEVA
            && gb == NakshatraMatchingCatalog.Gana.MANUSHYA)
        || (ga == NakshatraMatchingCatalog.Gana.MANUSHYA
            && gb == NakshatraMatchingCatalog.Gana.DEVA)) {
      pts = 5;
    } else if ((ga == NakshatraMatchingCatalog.Gana.MANUSHYA
            && gb == NakshatraMatchingCatalog.Gana.RAKSHASA)
        || (ga == NakshatraMatchingCatalog.Gana.RAKSHASA
            && gb == NakshatraMatchingCatalog.Gana.MANUSHYA)) {
      pts = 1;
    } else {
      pts = 0; // Deva–Rakshasa
    }
    return KootaScore.of(
        KootaCode.GANA,
        pts,
        "Gana "
            + ga
            + " ("
            + a.moonNakshatraName()
            + ") vs "
            + gb
            + " ("
            + b.moonNakshatraName()
            + ").",
        RULE + "_GANA");
  }

  static KootaScore bhakoot(MatchingPerson a, MatchingPerson b) {
    int diff = Math.floorMod(b.moonSignIndex() - a.moonSignIndex(), 12);
    // 2/12 → diff 1 or 11; 5/9 → 4 or 8; 6/8 → 5 or 7
    boolean dosha = diff == 1 || diff == 11 || diff == 4 || diff == 8 || diff == 5 || diff == 7;
    int pts = dosha ? 0 : 7;
    return KootaScore.of(
        KootaCode.BHAKOOT,
        pts,
        "Moon signs "
            + a.moonSignName()
            + " → "
            + b.moonSignName()
            + " (offset "
            + diff
            + "). Classical 2/12, 5/9, and 6/8 combinations score zero; others score full Bhakoot.",
        RULE + "_BHAKOOT");
  }

  static KootaScore nadi(MatchingPerson a, MatchingPerson b) {
    var na = NakshatraMatchingCatalog.nadi(a.moonNakshatraIndex());
    var nb = NakshatraMatchingCatalog.nadi(b.moonNakshatraIndex());
    int pts = na == nb ? 0 : 8;
    return KootaScore.of(
        KootaCode.NADI,
        pts,
        "Nadi "
            + na
            + " ("
            + a.moonNakshatraName()
            + ") vs "
            + nb
            + " ("
            + b.moonNakshatraName()
            + "). Same Nadi scores zero; different Nadis score full points.",
        RULE + "_NADI");
  }

  private static boolean isFriend(Planet from, Planet to) {
    Set<Planet> f = FRIENDS.get(from);
    return f != null && f.contains(to);
  }

  private static boolean isEnemy(Planet from, Planet to) {
    Set<Planet> e = ENEMIES.get(from);
    return e != null && e.contains(to);
  }
}
