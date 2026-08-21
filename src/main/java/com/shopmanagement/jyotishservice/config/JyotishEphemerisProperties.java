package com.shopmanagement.jyotishservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Ephemeris selection. Default {@code MEEUS} keeps the service runnable without native libs or an
 * optional Swiss Ephemeris JAR. Set {@code jyotish.ephemeris.provider=SWISS} to use Thomas Mack's
 * pure-Java Swiss Ephemeris port (see {@code third_party/swiss-ephemeris/README.md}).
 */
@ConfigurationProperties(prefix = "jyotish.ephemeris")
public class JyotishEphemerisProperties {

  /** {@code MEEUS} (default) or {@code SWISS}. */
  private String provider = "MEEUS";

  /**
   * Optional filesystem path to {@code swisseph-*.jar} (Thomas Mack pure-Java port). When blank,
   * Swiss classes must already be on the application classpath.
   */
  private String swissJarPath = "";

  /**
   * Optional Swiss Ephemeris data-file directory ({@code seas_*.se1} etc.). When blank, the Java
   * port uses built-in <em>Moshier</em> mode (no SE files; still far more accurate than truncated
   * Meeus). Set a path and {@link #swissUseFiles} to prefer full SE files.
   */
  private String swissEphePath = "";

  /**
   * When true and {@link #swissEphePath} is set, request {@code SEFLG_SWIEPH}; otherwise use Moshier
   * ({@code SEFLG_MOSEPH}).
   */
  private boolean swissUseFiles = false;

  /**
   * Use Swiss true lunar node ({@code SE_TRUE_NODE}) when true; mean node when false. Meeus path
   * always uses mean node — keep false if comparing providers apples-to-apples.
   */
  private boolean swissTrueNode = false;

  public String getProvider() {
    return provider;
  }

  public void setProvider(String provider) {
    this.provider = provider;
  }

  public String getSwissJarPath() {
    return swissJarPath;
  }

  public void setSwissJarPath(String swissJarPath) {
    this.swissJarPath = swissJarPath;
  }

  public String getSwissEphePath() {
    return swissEphePath;
  }

  public void setSwissEphePath(String swissEphePath) {
    this.swissEphePath = swissEphePath;
  }

  public boolean isSwissUseFiles() {
    return swissUseFiles;
  }

  public void setSwissUseFiles(boolean swissUseFiles) {
    this.swissUseFiles = swissUseFiles;
  }

  public boolean isSwissTrueNode() {
    return swissTrueNode;
  }

  public void setSwissTrueNode(boolean swissTrueNode) {
    this.swissTrueNode = swissTrueNode;
  }
}
