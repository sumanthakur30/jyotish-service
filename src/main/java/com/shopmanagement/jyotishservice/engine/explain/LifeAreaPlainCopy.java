package com.shopmanagement.jyotishservice.engine.explain;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.shopmanagement.jyotishservice.engine.explain.DashaLordThemes.Theme;
import com.shopmanagement.jyotishservice.engine.life.LifeCategory;

/**
 * Plain bilingual “what this means” copy per life area. Qualified traditional reading — never a
 * luck score, promotion promise, or medical claim.
 */
public final class LifeAreaPlainCopy {

  private LifeAreaPlainCopy() {}

  public record Result(
      String meaningEn,
      String meaningHi,
      int emphasisLevel,
      String emphasisLabelEn,
      String emphasisLabelHi) {}

  public static Result of(
      LifeCategory category,
      Collection<Integer> occupiedFocusHouses,
      String mahaLordCode,
      Integer mahaHouse,
      String antarLordCode,
      Integer antarHouse) {
    LifeCategory cat = category == null ? LifeCategory.GENERAL : category;
    Set<Integer> occupied =
        occupiedFocusHouses == null ? Set.of() : Set.copyOf(occupiedFocusHouses);
    Set<Integer> focus = Set.copyOf(cat.indicatorHouses());
    String maha = norm(mahaLordCode);
    String antar = norm(antarLordCode);

    boolean mahaInFocus = mahaHouse != null && mahaHouse > 0 && focus.contains(mahaHouse);
    boolean antarInFocus = antarHouse != null && antarHouse > 0 && focus.contains(antarHouse);
    boolean mahaIsKaraka = karakas(cat).contains(maha);
    int emphasis = 2;
    if (!occupied.isEmpty()) {
      emphasis++;
    }
    if (mahaInFocus) {
      emphasis += 2;
    } else if (antarInFocus) {
      emphasis++;
    }
    if (mahaIsKaraka) {
      emphasis++;
    }
    emphasis = Math.max(1, Math.min(5, emphasis));

    String meaningEn =
        topicEn(cat)
            + " "
            + dashaEn(cat, maha, antar, mahaInFocus, mahaIsKaraka)
            + " "
            + closerEn(cat);
    String meaningHi =
        topicHi(cat)
            + " "
            + dashaHi(cat, maha, antar, mahaInFocus, mahaIsKaraka)
            + " "
            + closerHi(cat);

    String[] labels = emphasisLabels(emphasis);
    return new Result(meaningEn.trim(), meaningHi.trim(), emphasis, labels[0], labels[1]);
  }

  public static List<String> karakas(LifeCategory category) {
    return switch (category) {
      case CAREER -> List.of("SUN", "SATURN", "MERCURY");
      case JOB -> List.of("SUN", "SATURN");
      case BUSINESS -> List.of("MERCURY", "JUPITER");
      case FINANCE -> List.of("JUPITER", "VENUS");
      case MARRIAGE -> List.of("VENUS", "JUPITER");
      case FAMILY -> List.of("MOON", "VENUS");
      case CHILDREN -> List.of("JUPITER");
      case EDUCATION -> List.of("MERCURY", "JUPITER");
      case PROPERTY -> List.of("MARS", "VENUS");
      case FOREIGN -> List.of("RAHU", "JUPITER");
      case SPIRITUALITY -> List.of("JUPITER", "KETU");
      case HEALTH -> List.of("MARS", "SATURN");
      default -> List.of();
    };
  }

  private static String topicEn(LifeCategory cat) {
    return switch (cat) {
      case CAREER ->
          "Career is the wider path of work and public standing (houses 10, 6, 2 and 11).";
      case JOB ->
          "Job / service is the daily role — 10th house (position) and 6th (work, duty, competition).";
      case BUSINESS ->
          "Business is read from resources (2), partnership (7), status (10) and gains (11).";
      case FINANCE -> "Money and income are read mainly from the 2nd (savings) and 11th (gains).";
      case MARRIAGE -> "Marriage and partnership are read from the 7th house and Venus/Jupiter.";
      case FAMILY -> "Family and home are read from the 4th house (peace, mother, property) and 2nd.";
      case CHILDREN -> "Children-related themes are read from the 5th house and Jupiter.";
      case EDUCATION -> "Study is read from 4th (foundations), 5th (intelligence) and 9th (higher learning).";
      case PROPERTY -> "Home and property are read from the 4th house, with Mars and Venus as helpers.";
      case FOREIGN -> "Travel and foreign links are read from 12th, 9th and 7th houses.";
      case SPIRITUALITY -> "Inner life is read from 5th, 9th and 12th, with Jupiter and Ketu.";
      case HEALTH ->
          "Vitality in Jyotish is read from 1st, 6th and 8th houses — chart facts only, not medical advice.";
      default -> "This topic uses the houses stored on this kundali.";
    };
  }

  private static String topicHi(LifeCategory cat) {
    return switch (cat) {
      case CAREER ->
          "करियर काम और सार्वजनिक भूमिका का व्यापक मार्ग है (10, 6, 2 और 11 भाव)।";
      case JOB -> "नौकरी/सेवा दैनिक भूमिका है — 10वाँ भाव (पद) और 6वाँ (कार्य, कर्तव्य, प्रतिस्पर्धा)।";
      case BUSINESS -> "व्यवसाय संसाधन (2), साझेदारी (7), स्थिति (10) और लाभ (11) से देखा जाता है।";
      case FINANCE -> "धन और आय मुख्यतः द्वितीय (बचत) और एकादश (लाभ) भाव से देखी जाती है।";
      case MARRIAGE -> "विवाह और साझेदारी सप्तम भाव तथा शुक्र/गुरु से देखी जाती है।";
      case FAMILY -> "परिवार और घर चतुर्थ भाव (शांति, माता, संपत्ति) और द्वितीय से देखे जाते हैं।";
      case CHILDREN -> "संतान संबंधी विषय पंचम भाव और गुरु से देखे जाते हैं।";
      case EDUCATION -> "शिक्षा चतुर्थ (आधार), पंचम (बुद्धि) और नवम (उच्च अध्ययन) से देखी जाती है।";
      case PROPERTY -> "घर और संपत्ति चतुर्थ भाव से, मंगल और शुक्र सहायक ग्रह माने जाते हैं।";
      case FOREIGN -> "यात्रा और विदेश 12, 9 और 7 भाव से जुड़े माने जाते हैं।";
      case SPIRITUALITY -> "अंतर्जीवन 5, 9, 12 भाव तथा गुरु और केतु से देखा जाता है।";
      case HEALTH ->
          "ज्योतिष में स्वास्थ्य संकेत 1, 6, 8 भाव से — केवल कुंडली तथ्य, चिकित्सा सलाह नहीं।";
      default -> "यह विषय इस कुंडली में संग्रहीत भावों से देखा जाता है।";
    };
  }

  private static String dashaEn(
      LifeCategory cat, String maha, String antar, boolean mahaInFocus, boolean mahaIsKaraka) {
    if (maha.isBlank()) {
      return "Current dasha is not stored on this kundali yet.";
    }
    Theme theme = DashaLordThemes.themeOrNull(maha);
    String name = theme != null ? theme.nameEn() : maha;
    String antarName = antar.isBlank() ? "" : "–" + lordEn(antar);
    if (mahaInFocus || mahaIsKaraka) {
      return "Your current period is "
          + name
          + antarName
          + ", which traditional teaching links with "
          + topicLinkEn(cat)
          + " — so this area may feel more active now.";
    }
    return "Your current period is "
        + name
        + antarName
        + ". That clock is not the main karaka for "
        + cat.labelEn().toLowerCase(Locale.ROOT)
        + ", so this area may stay in the background unless other chart factors highlight it.";
  }

  private static String dashaHi(
      LifeCategory cat, String maha, String antar, boolean mahaInFocus, boolean mahaIsKaraka) {
    if (maha.isBlank()) {
      return "वर्तमान दशा इस कुंडली पर अभी संग्रहीत नहीं है।";
    }
    Theme theme = DashaLordThemes.themeOrNull(maha);
    String name = theme != null ? theme.nameHi() : maha;
    String antarName = antar.isBlank() ? "" : "–" + lordHi(antar);
    if (mahaInFocus || mahaIsKaraka) {
      return "आपकी वर्तमान अवधि "
          + name
          + antarName
          + " है, जिसे शास्त्र "
          + topicLinkHi(cat)
          + " से जोड़ता है — इसलिए यह क्षेत्र अभी अधिक सक्रिय महसूस हो सकता है।";
    }
    return "आपकी वर्तमान अवधि "
        + name
        + antarName
        + " है। यह घड़ी "
        + cat.labelHi()
        + " की मुख्य कारक नहीं मानी जाती, अतः यह क्षेत्र पृष्ठभूमि में रह सकता है जब तक अन्य योग इसे न उठाएँ।";
  }

  private static String topicLinkEn(LifeCategory cat) {
    return switch (cat) {
      case CAREER, JOB -> "work, duty, and public role";
      case BUSINESS -> "trade, talks, and expansion";
      case FINANCE -> "comfort, income, and resources";
      case MARRIAGE -> "relationship, harmony, and partnership";
      case FAMILY -> "home, mind, and care";
      case CHILDREN, EDUCATION -> "wisdom, learning, and guidance";
      case PROPERTY -> "land, home, and comforts";
      case FOREIGN -> "distance, travel, and other places";
      case SPIRITUALITY -> "faith, release, and inner search";
      case HEALTH -> "energy, discipline, and the body";
      default -> "this topic";
    };
  }

  private static String topicLinkHi(LifeCategory cat) {
    return switch (cat) {
      case CAREER, JOB -> "काम, कर्तव्य और सार्वजनिक भूमिका";
      case BUSINESS -> "व्यापार, बातचीत और विस्तार";
      case FINANCE -> "सुख, आय और संसाधन";
      case MARRIAGE -> "संबंध, सामंजस्य और साझेदारी";
      case FAMILY -> "घर, मन और देखभाल";
      case CHILDREN, EDUCATION -> "ज्ञान, अध्ययन और मार्गदर्शन";
      case PROPERTY -> "भूमि, घर और सुख";
      case FOREIGN -> "दूरी, यात्रा और अन्य स्थान";
      case SPIRITUALITY -> "आस्था, त्याग और अंतःखोज";
      case HEALTH -> "ऊर्जा, अनुशासन और शरीर";
      default -> "इस विषय";
    };
  }

  private static String closerEn(LifeCategory cat) {
    if (cat == LifeCategory.HEALTH) {
      return "Open details for house facts. This is not a diagnosis.";
    }
    return "Open details for the house and graha facts. This is emphasis in the chart, not a guarantee.";
  }

  private static String closerHi(LifeCategory cat) {
    if (cat == LifeCategory.HEALTH) {
      return "भाव तथ्य देखने के लिए विवरण खोलें। यह निदान नहीं है।";
    }
    return "भाव और ग्रह तथ्य के लिए विवरण खोलें। यह कुंडली में जोर है, किसी घटना की गारंटी नहीं।";
  }

  private static String[] emphasisLabels(int level) {
    if (level >= 4) {
      return new String[] {"In focus now", "इस समय अधिक ध्यान"};
    }
    if (level <= 2) {
      return new String[] {"Quiet in this period", "इस अवधि में कम संकेत"};
    }
    return new String[] {"Mixed signals", "मिश्रित संकेत"};
  }

  private static String lordEn(String code) {
    Theme t = DashaLordThemes.themeOrNull(code);
    return t != null ? t.nameEn() : code;
  }

  private static String lordHi(String code) {
    Theme t = DashaLordThemes.themeOrNull(code);
    return t != null ? t.nameHi() : code;
  }

  private static String norm(String code) {
    return code == null ? "" : code.trim().toUpperCase(Locale.ROOT);
  }
}
