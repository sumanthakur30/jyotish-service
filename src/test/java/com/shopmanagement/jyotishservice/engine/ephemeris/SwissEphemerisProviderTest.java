package com.shopmanagement.jyotishservice.engine.ephemeris;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import com.shopmanagement.jyotishservice.config.JyotishEphemerisProperties;
import com.shopmanagement.jyotishservice.engine.astro.AstroMath;
import com.shopmanagement.jyotishservice.engine.model.Planet;

/**
 * Swiss Ephemeris path tests. When the optional Thomas Mack JAR is absent, Swiss-specific cases
 * are skipped so default {@code mvn test} (Meeus) always passes.
 */
class SwissEphemerisProviderTest {

  private static final Path DEFAULT_JAR =
      Path.of("third_party/swiss-ephemeris/swisseph-2.01.00-02.jar");

  @Test
  void factoryDefaultsToMeeus() {
    JyotishEphemerisProperties props = new JyotishEphemerisProperties();
    EphemerisProvider provider = EphemerisProviders.create(props);
    assertEquals(EphemerisProviders.MEEUS, provider.code());
  }

  @Test
  void swissWithoutJarFailsClearly() {
    JyotishEphemerisProperties props = new JyotishEphemerisProperties();
    props.setProvider("SWISS");
    props.setSwissJarPath("third_party/swiss-ephemeris/missing-swisseph.jar");
    EphemerisUnavailableException ex =
        assertThrows(EphemerisUnavailableException.class, () -> EphemerisProviders.create(props));
    assertTrue(ex.getMessage().contains("swiss-jar-path") || ex.getMessage().contains("Swiss"));
  }

  @Test
  void swissMoshierComputesSunAndAscendantWhenJarPresent() {
    assumeTrue(Files.isRegularFile(DEFAULT_JAR), "Swiss JAR not present — skip");

    JyotishEphemerisProperties props = new JyotishEphemerisProperties();
    props.setProvider("SWISS");
    props.setSwissJarPath(DEFAULT_JAR.toString());
    props.setSwissUseFiles(false);
    props.setSwissTrueNode(false);

    EphemerisProvider swiss = EphemerisProviders.create(props);
    assertEquals(EphemerisProviders.SWISS, swiss.code());

    // J2000.0 noon TT ≈ JD 2451545.0
    TropicalBody sun = swiss.position(Planet.SUN, 2451545.0);
    assertTrue(sun.longitudeDeg() > 270 && sun.longitudeDeg() < 290, "Sun near Capricorn at J2000");
    assertTrue(Math.abs(sun.speedDegPerDay() - 1.0) < 0.1);

    double asc = swiss.tropicalAscendant(2451545.0, 28.6139, 77.2090);
    assertTrue(asc >= 0 && asc < 360);

    TropicalBody rahu = swiss.position(Planet.RAHU, 2451545.0);
    TropicalBody ketu = swiss.position(Planet.KETU, 2451545.0);
    double sep = AstroMath.norm360(ketu.longitudeDeg() - rahu.longitudeDeg());
    assertEquals(180.0, sep, 0.001);
  }

  @Test
  void swissAvailableHelperMatchesJarPresence() {
    JyotishEphemerisProperties props = new JyotishEphemerisProperties();
    props.setSwissJarPath(DEFAULT_JAR.toString());
    if (Files.isRegularFile(DEFAULT_JAR)) {
      assertTrue(EphemerisProviders.swissAvailable(props));
    } else {
      props.setSwissJarPath("third_party/swiss-ephemeris/missing.jar");
      assertTrue(!EphemerisProviders.swissAvailable(props));
    }
  }
}
