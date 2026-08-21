package com.shopmanagement.jyotishservice.api;

import java.util.Locale;
import java.util.Set;

/**
 * Canonical PDF report types and request aliases.
 *
 * <p>Canonical values stored on {@code kundali_report.report_type}:
 *
 * <ul>
 *   <li>{@code BASIC_KUNDALI}
 *   <li>{@code MATCHING}
 *   <li>{@code DASHA_SUMMARY}
 *   <li>{@code TRANSIT}
 * </ul>
 *
 * <p>Aliases for basic kundali: {@code KUNDALI_SUMMARY}, {@code KUNDALI}, {@code BASIC}.
 */
public final class ReportTypes {

  public static final String BASIC_KUNDALI = "BASIC_KUNDALI";
  public static final String MATCHING = "MATCHING";
  public static final String DASHA_SUMMARY = "DASHA_SUMMARY";
  public static final String TRANSIT = "TRANSIT";

  public static final Set<String> CANONICAL =
      Set.of(BASIC_KUNDALI, MATCHING, DASHA_SUMMARY, TRANSIT);

  public static final String ALLOWED_MESSAGE =
      "Unsupported report type. Canonical types: BASIC_KUNDALI, MATCHING, DASHA_SUMMARY, TRANSIT"
          + " (aliases for BASIC_KUNDALI: KUNDALI_SUMMARY, KUNDALI, BASIC)";

  private ReportTypes() {}

  /**
   * Normalize a client-supplied type (case-insensitive) to a canonical value, or return the
   * uppercased token if unknown (caller should reject).
   */
  public static String resolve(String raw) {
    if (raw == null || raw.isBlank()) {
      throw new IllegalArgumentException("type is required");
    }
    String type = raw.trim().toUpperCase(Locale.ROOT);
    return switch (type) {
      case "KUNDALI_SUMMARY", "KUNDALI", "BASIC" -> BASIC_KUNDALI;
      default -> type;
    };
  }

  public static boolean isCanonical(String type) {
    return type != null && CANONICAL.contains(type);
  }
}
