package com.shopmanagement.jyotishservice.engine.ephemeris;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;

import com.shopmanagement.jyotishservice.config.JyotishEphemerisProperties;

/**
 * Factory for {@link EphemerisProvider} implementations selected by {@code
 * jyotish.ephemeris.provider}.
 */
public final class EphemerisProviders {

  public static final String MEEUS = "MEEUS";
  public static final String SWISS = "SWISS";

  private EphemerisProviders() {}

  public static EphemerisProvider create(JyotishEphemerisProperties props) {
    Objects.requireNonNull(props, "props");
    String code = normalize(props.getProvider());
    return switch (code) {
      case MEEUS -> new MeeusEphemeris();
      case SWISS -> SwissEphemerisProvider.create(props);
      default ->
          throw new EphemerisUnavailableException(
              "Unknown jyotish.ephemeris.provider='"
                  + props.getProvider()
                  + "'. Use MEEUS or SWISS.");
    };
  }

  public static String normalize(String provider) {
    if (provider == null || provider.isBlank()) {
      return MEEUS;
    }
    return provider.trim().toUpperCase(Locale.ROOT);
  }

  /** True when Swiss classes are loadable from optional JAR path or the current classpath. */
  public static boolean swissAvailable(JyotishEphemerisProperties props) {
    try {
      SwissEphemerisSupport.probe(props);
      return true;
    } catch (EphemerisUnavailableException ex) {
      return false;
    }
  }

  public static boolean swissJarPresent(String jarPath) {
    if (jarPath == null || jarPath.isBlank()) {
      return false;
    }
    return Files.isRegularFile(Path.of(jarPath.trim()));
  }
}
