package com.shopmanagement.jyotishservice.engine.ephemeris;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import com.shopmanagement.jyotishservice.config.JyotishEphemerisProperties;

/**
 * Reflective bootstrap for Thomas Mack's pure-Java Swiss Ephemeris ({@code swisseph.SwissEph}).
 *
 * <p>No compile-time Maven dependency: the JAR is either on the application classpath or loaded
 * from {@code jyotish.ephemeris.swiss-jar-path}. Fails with a clear message when unavailable.
 */
final class SwissEphemerisSupport {

  /** Stable Swiss Ephemeris public constants (Astrodienst / Thomas Mack port). */
  static final int SE_SUN = 0;

  static final int SE_MOON = 1;
  static final int SE_MERCURY = 2;
  static final int SE_VENUS = 3;
  static final int SE_MARS = 4;
  static final int SE_JUPITER = 5;
  static final int SE_SATURN = 6;
  static final int SE_MEAN_NODE = 10;
  static final int SE_TRUE_NODE = 11;
  static final int SEFLG_SWIEPH = 2;
  static final int SEFLG_MOSEPH = 4;
  static final int SEFLG_SPEED = 256;

  private SwissEphemerisSupport() {}

  static Handle probe(JyotishEphemerisProperties props) {
    Objects.requireNonNull(props, "props");
    ClassLoader loader = resolveLoader(props.getSwissJarPath());
    try {
      Class<?> swissEphClass = Class.forName("swisseph.SwissEph", true, loader);
      Constructor<?> ctorPath = swissEphClass.getConstructor(String.class);
      Constructor<?> ctorDefault = swissEphClass.getConstructor();
      Method calcUt =
          swissEphClass.getMethod(
              "swe_calc_ut",
              double.class,
              int.class,
              int.class,
              double[].class,
              StringBuffer.class);
      Method houses =
          swissEphClass.getMethod(
              "swe_houses",
              double.class,
              int.class,
              double.class,
              double.class,
              int.class,
              double[].class,
              double[].class);
      String ephePath =
          props.getSwissEphePath() == null ? "" : props.getSwissEphePath().trim();
      Object swissEph;
      if (ephePath.isEmpty()) {
        swissEph = ctorDefault.newInstance();
      } else {
        if (!Files.isDirectory(Path.of(ephePath))) {
          throw new EphemerisUnavailableException(
              "jyotish.ephemeris.swiss-ephe-path is not a directory: " + ephePath);
        }
        swissEph = ctorPath.newInstance(ephePath);
      }
      boolean useFiles = props.isSwissUseFiles() && !ephePath.isEmpty();
      int iflag = (useFiles ? SEFLG_SWIEPH : SEFLG_MOSEPH) | SEFLG_SPEED;
      return new Handle(swissEph, calcUt, houses, iflag, props.isSwissTrueNode(), useFiles);
    } catch (EphemerisUnavailableException ex) {
      throw ex;
    } catch (ClassNotFoundException ex) {
      throw new EphemerisUnavailableException(
          "Swiss Ephemeris classes not found (swisseph.SwissEph). Download Thomas Mack's "
              + "swisseph JAR into third_party/swiss-ephemeris/ and set "
              + "jyotish.ephemeris.swiss-jar-path, or add the JAR to the classpath. "
              + "See third_party/swiss-ephemeris/README.md. Cause: "
              + ex.getMessage(),
          ex);
    } catch (ReflectiveOperationException ex) {
      throw new EphemerisUnavailableException(
          "Failed to initialize Swiss Ephemeris: " + rootMessage(ex), ex);
    }
  }

  private static ClassLoader resolveLoader(String jarPath) {
    if (jarPath != null && !jarPath.isBlank()) {
      Path path = Path.of(jarPath.trim());
      if (!Files.isRegularFile(path)) {
        throw new EphemerisUnavailableException(
            "jyotish.ephemeris.swiss-jar-path does not exist: " + path.toAbsolutePath());
      }
      try {
        URL url = path.toUri().toURL();
        ClassLoader parent = Thread.currentThread().getContextClassLoader();
        if (parent == null) {
          parent = SwissEphemerisSupport.class.getClassLoader();
        }
        return new URLClassLoader(new URL[] {url}, parent);
      } catch (Exception ex) {
        throw new EphemerisUnavailableException(
            "Cannot load Swiss Ephemeris JAR from " + path.toAbsolutePath() + ": " + ex.getMessage(),
            ex);
      }
    }
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    if (cl == null) {
      cl = SwissEphemerisSupport.class.getClassLoader();
    }
    return cl;
  }

  private static String rootMessage(Throwable ex) {
    Throwable cur = ex;
    while (cur.getCause() != null && cur.getCause() != cur) {
      cur = cur.getCause();
    }
    return cur.getMessage() != null ? cur.getMessage() : cur.toString();
  }

  record Handle(
      Object swissEph,
      Method calcUt,
      Method houses,
      int iflag,
      boolean trueNode,
      boolean usingFiles) {}
}
