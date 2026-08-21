package com.shopmanagement.jyotishservice.engine.yoga;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.shopmanagement.jyotishservice.engine.astro.ZodiacCatalog;
import com.shopmanagement.jyotishservice.engine.model.Planet;
import com.shopmanagement.jyotishservice.engine.model.PlanetPosition;

/** Yoga rule regressions using synthetic whole-sign placements (no ephemeris). */
class YogaDetectorTest {

  @Test
  void gajakesari_presentWhenMoonJupiterMutualKendra() {
    // Lagna Aries; Moon Cancer house 4; Jupiter Capricorn house 10 — mutual kendra + both Lagna
    // kendras → FULL
    YogaHit hit =
        GajakesariYogaDetector.INSTANCE.detect(
            ctx(
                0,
                p(Planet.MOON, 3),
                p(Planet.JUPITER, 9),
                p(Planet.MARS, 5),
                p(Planet.MERCURY, 1),
                p(Planet.SUN, 4),
                p(Planet.VENUS, 6),
                p(Planet.SATURN, 7)));
    assertTrue(hit.present());
    assertEquals(YogaStrength.FULL, hit.strength());
    assertTrue(hit.planetCodes().contains("MOON"));
    assertTrue(hit.planetCodes().contains("JUPITER"));
  }

  @Test
  void gajakesari_absentWhenNotMutualKendra() {
    YogaHit hit =
        GajakesariYogaDetector.INSTANCE.detect(
            ctx(
                0,
                p(Planet.MOON, 3),
                p(Planet.JUPITER, 4),
                p(Planet.MARS, 0),
                p(Planet.MERCURY, 1),
                p(Planet.SUN, 5),
                p(Planet.VENUS, 6),
                p(Planet.SATURN, 7)));
    assertFalse(hit.present());
    assertNull(hit.strength());
  }

  @Test
  void gajakesari_partialWhenMutualButNotBothLagnaKendra() {
    // Moon Taurus h2, Jupiter Leo h5 — mutual kendra (offset 3), not Lagna kendras → PARTIAL
    YogaHit hit =
        GajakesariYogaDetector.INSTANCE.detect(
            ctx(
                0,
                p(Planet.MOON, 1),
                p(Planet.JUPITER, 4),
                p(Planet.MARS, 5),
                p(Planet.MERCURY, 2),
                p(Planet.SUN, 3),
                p(Planet.VENUS, 6),
                p(Planet.SATURN, 7)));
    assertTrue(hit.present());
    assertEquals(YogaStrength.PARTIAL, hit.strength());
  }

  @Test
  void dharmaKarmadhipati_presentWhen9And10LordsAssociated() {
    // Lagna Aries: 9th lord Jupiter, 10th lord Saturn — conjunction in Gemini
    YogaHit hit =
        DharmaKarmadhipatiYogaDetector.INSTANCE.detect(
            ctx(
                0,
                p(Planet.JUPITER, 2),
                p(Planet.SATURN, 2),
                p(Planet.MOON, 3),
                p(Planet.MARS, 0),
                p(Planet.MERCURY, 1),
                p(Planet.SUN, 4),
                p(Planet.VENUS, 5)));
    assertTrue(hit.present());
    assertNull(hit.strength());
    assertTrue(hit.explanation().toLowerCase().contains("associated"));
  }

  @Test
  void dharmaKarmadhipati_absentWhenLordsNotAssociated() {
    assertFalse(
        DharmaKarmadhipatiYogaDetector.INSTANCE
            .detect(
                ctx(
                    0,
                    p(Planet.JUPITER, 2),
                    p(Planet.SATURN, 3),
                    p(Planet.MOON, 4),
                    p(Planet.MARS, 0),
                    p(Planet.MERCURY, 1),
                    p(Planet.SUN, 5),
                    p(Planet.VENUS, 6)))
            .present());
  }

  @Test
  void dhana211_presentWhen2And11LordsAssociated() {
    // Lagna Aries: 2nd Venus, 11th Saturn — opposition (mutual kendra)
    YogaHit hit =
        Dhana211YogaDetector.INSTANCE.detect(
            ctx(
                0,
                p(Planet.VENUS, 9),
                p(Planet.SATURN, 3),
                p(Planet.MOON, 1),
                p(Planet.MARS, 0),
                p(Planet.MERCURY, 2),
                p(Planet.SUN, 4),
                p(Planet.JUPITER, 5)));
    assertTrue(hit.present());
    assertNull(hit.strength());
  }

  @Test
  void ruchaka_moderateWhenMarsOwnSignInKendra() {
    YogaHit hit =
        RuchakaYogaDetector.INSTANCE.detect(
            ctx(
                0,
                p(Planet.MARS, 0),
                p(Planet.MOON, 2),
                p(Planet.JUPITER, 3),
                p(Planet.MERCURY, 1),
                p(Planet.SUN, 4),
                p(Planet.VENUS, 5),
                p(Planet.SATURN, 6)));
    assertTrue(hit.present());
    assertEquals(YogaStrength.MODERATE, hit.strength());
  }

  @Test
  void ruchaka_fullWhenMarsExaltedInKendra() {
    YogaHit hit =
        RuchakaYogaDetector.INSTANCE.detect(
            ctx(
                0,
                p(Planet.MARS, 9),
                p(Planet.MOON, 2),
                p(Planet.JUPITER, 3),
                p(Planet.MERCURY, 1),
                p(Planet.SUN, 4),
                p(Planet.VENUS, 5),
                p(Planet.SATURN, 6)));
    assertTrue(hit.present());
    assertEquals(YogaStrength.FULL, hit.strength());
  }

  @Test
  void bhadra_presentWhenMercuryOwnInKendra() {
    // Lagna Gemini; Mercury Gemini own, house 1
    assertTrue(
        BhadraYogaDetector.INSTANCE
            .detect(
                ctx(
                    2,
                    p(Planet.MERCURY, 2),
                    p(Planet.MOON, 3),
                    p(Planet.JUPITER, 4),
                    p(Planet.MARS, 5),
                    p(Planet.SUN, 6),
                    p(Planet.VENUS, 7),
                    p(Planet.SATURN, 8)))
            .present());
  }

  @Test
  void registry_evaluateAll_includesImplementedOnly() {
    List<YogaHit> hits =
        YogaRegistry.evaluateAll(
            ctx(
                0,
                p(Planet.MOON, 3),
                p(Planet.JUPITER, 9),
                p(Planet.MARS, 0),
                p(Planet.MERCURY, 1),
                p(Planet.SUN, 4),
                p(Planet.VENUS, 5),
                p(Planet.SATURN, 6)));
    assertEquals(10, hits.size());
    assertTrue(YogaRegistry.isImplemented(YogaCode.GAJAKESARI));
    assertTrue(YogaRegistry.isImplemented(YogaCode.KEMADRUMA));
    assertFalse(YogaRegistry.isImplemented(YogaCode.NEECHA_BHANGA));
    assertNotNull(hits.get(0).explanation());
  }

  private static YogaContext ctx(int lagnaSign, Spec... specs) {
    List<PlanetPosition> planets = new ArrayList<>();
    for (Spec s : specs) {
      int house = ZodiacCatalog.wholeSignHouse(lagnaSign, s.signIndex);
      double lon = s.signIndex * 30.0 + 10.0;
      int nak = ZodiacCatalog.nakshatraIndex(lon);
      planets.add(
          new PlanetPosition(
              s.planet,
              lon,
              s.signIndex,
              ZodiacCatalog.signName(s.signIndex),
              10.0,
              house,
              nak,
              ZodiacCatalog.nakshatraName(nak),
              ZodiacCatalog.pada(lon),
              false,
              false,
              0.0));
    }
    return new YogaContext(lagnaSign, planets, null);
  }

  private static Spec p(Planet planet, int signIndex) {
    return new Spec(planet, signIndex);
  }

  private record Spec(Planet planet, int signIndex) {}
}
