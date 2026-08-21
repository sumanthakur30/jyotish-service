package com.shopmanagement.jyotishservice.engine.explain;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Templated, qualified bilingual meanings for Vimshottari lords. Themes are traditional teaching
 * aids — not destiny claims and not computed from chart inventiveness.
 */
public final class DashaLordThemes {

  private DashaLordThemes() {}

  public record Theme(
      String code, String nameEn, String nameHi, List<String> meaningEn, List<String> meaningHi) {}

  private static final Map<String, Theme> BY_CODE = new LinkedHashMap<>();

  static {
    put(
        "SUN",
        "Sun",
        "सूर्य",
        List.of(
            "Sun periods may be considered times when confidence, visibility, and leadership themes come forward.",
            "People sometimes notice more focus on authority, father figures, or public standing during these years.",
            "Results still depend on the full chart and should be read with a qualified Jyotish."),
        List.of(
            "सूर्य काल को आत्मविश्वास, नेतृत्व और सार्वजनिक भूमिका से जुड़ा माना जा सकता है।",
            "इस अवधि में अधिकार, पितृ पक्ष या प्रतिष्ठा के विषय अधिक ध्यान में आ सकते हैं।",
            "फल पूरे कुंडली और योग्य ज्योतिषी की सलाह पर निर्भर करते हैं।"));
    put(
        "MOON",
        "Moon",
        "चंद्र",
        List.of(
            "Moon periods may be considered emotionally active times, with emphasis on mind, home, and care.",
            "Changes in routine, family closeness, or public mood may feel more noticeable.",
            "These are general traditional themes, not guarantees of any specific event."),
        List.of(
            "चंद्र काल भावना, मन, घर और देखभाल से जुड़ा माना जा सकता है।",
            "दिनचर्या, परिवार या मनःस्थिति में बदलाव अधिक महसूस हो सकते हैं।",
            "ये पारंपरिक संकेत हैं, किसी घटना की गारंटी नहीं।"));
    put(
        "MARS",
        "Mars",
        "मंगल",
        List.of(
            "Mars periods may be considered times of drive, courage, and decisive action.",
            "Energy, competition, or property/sibling themes may come into focus, depending on the chart.",
            "Care with haste and conflict is often advised in classical teaching."),
        List.of(
            "मंगल काल उत्साह, साहस और निर्णायक कर्म से जुड़ा माना जा सकता है।",
            "ऊर्जा, प्रतिस्पर्धा या संपत्ति/भाई-बहन के विषय सामने आ सकते हैं।",
            "शास्त्रीय दृष्टि में जल्दबाजी और विवाद से सावधानी रखने की सलाह मिलती है।"));
    put(
        "MERCURY",
        "Mercury",
        "बुध",
        List.of(
            "Mercury periods may be considered favourable for learning, communication, trade, and analysis.",
            "Study, writing, travel for work, or business talks may feel more active.",
            "Outcomes vary with Mercury’s strength and associations in the birth chart."),
        List.of(
            "बुध काल अध्ययन, संचार, व्यापार और विश्लेषण के लिए अनुकूल माना जा सकता है।",
            "पढ़ाई, लेखन, कार्य यात्रा या व्यावसायिक बातचीत अधिक सक्रिय लग सकती है।",
            "परिणाम जन्म कुंडली में बुध की स्थिति पर निर्भर करते हैं।"));
    put(
        "JUPITER",
        "Jupiter",
        "गुरु",
        List.of(
            "Jupiter periods may be considered expansive times linked with wisdom, guidance, and growth.",
            "Education, mentors, faith, or children-related themes may come forward for some charts.",
            "Expansion can mean opportunity or extra responsibility — context matters."),
        List.of(
            "गुरु काल ज्ञान, मार्गदर्शन और विस्तार से जुड़ा माना जा सकता है।",
            "शिक्षा, गुरुजन, आस्था या संतान संबंधी विषय कुछ कुंडलियों में सामने आ सकते हैं।",
            "विस्तार अवसर भी हो सकता है और जिम्मेदारी भी — संदर्भ महत्वपूर्ण है।"));
    put(
        "VENUS",
        "Venus",
        "शुक्र",
        List.of(
            "Venus periods may be considered times of harmony, relationships, comfort, and creative taste.",
            "Partnership, arts, or enjoyment themes may feel more present, depending on the chart.",
            "Balance and moderation are often emphasized in traditional guidance."),
        List.of(
            "शुक्र काल सामंजस्य, संबंध, सुख और कलात्मक रुचि से जुड़ा माना जा सकता है।",
            "साझेदारी, कला या आनंद के विषय अधिक ध्यान में आ सकते हैं।",
            "पारंपरिक सलाह में संतुलन और संयम पर बल दिया जाता है।"));
    put(
        "SATURN",
        "Saturn",
        "शनि",
        List.of(
            "Saturn periods may be considered times of discipline, patience, and structured effort.",
            "Responsibility, delays that teach endurance, or long-term work may feel more central.",
            "Steady habits are often described as more helpful than rushing outcomes."),
        List.of(
            "शनि काल अनुशासन, धैर्य और व्यवस्थित प्रयास से जुड़ा माना जा सकता है।",
            "जिम्मेदारी, धीरज सिखाने वाली देरी या दीर्घकालिक कार्य केंद्र में आ सकते हैं।",
            "जल्दबाजी से अधिक नियमित आदतें सहायक मानी जाती हैं।"));
    put(
        "RAHU",
        "Rahu",
        "राहु",
        List.of(
            "Rahu periods may be considered times of change, unconventional paths, and new directions.",
            "Ambition, foreign or unfamiliar settings, and sudden turns may feel more noticeable.",
            "Classical teaching urges clarity and ethical grounding amid rapid shifts."),
        List.of(
            "राहु काल बदलाव, असामान्य मार्ग और नई दिशाओं से जुड़ा माना जा सकता है।",
            "महत्वाकांक्षा, अपरिचित परिवेश या अचानक मोड़ अधिक महसूस हो सकते हैं।",
            "शास्त्रीय दृष्टि में तीव्र बदलाव के बीच स्पष्टता और नैतिक आधार रखने की सलाह है।"));
    put(
        "KETU",
        "Ketu",
        "केतु",
        List.of(
            "Ketu periods may be considered inwardly focused times linked with detachment and insight.",
            "Spiritual study, simplification, or letting go of old patterns may come into view.",
            "These themes are interpretive aids, not predictions of loss or gain."),
        List.of(
            "केतु काल अंतर्मुखी दृष्टि, वैराग्य और अंतर्दृष्टि से जुड़ा माना जा सकता है।",
            "अध्यात्म, सरलता या पुरानी आदतों को छोड़ने के विषय सामने आ सकते हैं।",
            "ये व्याख्यात्मक संकेत हैं, लाभ-हानि की भविष्यवाणी नहीं।"));
  }

  private static void put(
      String code, String nameEn, String nameHi, List<String> en, List<String> hi) {
    BY_CODE.put(code, new Theme(code, nameEn, nameHi, List.copyOf(en), List.copyOf(hi)));
  }

  public static Theme themeOrNull(String lordCode) {
    if (lordCode == null || lordCode.isBlank()) {
      return null;
    }
    return BY_CODE.get(lordCode.trim().toUpperCase(Locale.ROOT));
  }

  public static List<Theme> all() {
    return List.copyOf(BY_CODE.values());
  }
}
