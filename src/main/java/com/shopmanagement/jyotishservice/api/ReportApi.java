package com.shopmanagement.jyotishservice.api;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;

public final class ReportApi {

  private ReportApi() {}

  public record CreateReportRequest(
      @NotBlank String type, Long kundaliId, Long matchingId) {}

  public record ReportResponse(
      Long id,
      String type,
      Long kundaliId,
      Long matchingId,
      String displayTitle,
      String storagePath,
      long fileSizeBytes,
      String contentType,
      String calculationEngineVersion,
      Instant generatedAt,
      String downloadPath) {}
}
