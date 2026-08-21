package com.shopmanagement.jyotishservice.api;



import java.time.Instant;



import jakarta.validation.constraints.NotBlank;



public final class ReportApi {



  private ReportApi() {}



  public record CreateReportRequest(

      /**
       * Canonical: {@code BASIC_KUNDALI}, {@code MATCHING}, {@code DASHA_SUMMARY}, {@code TRANSIT}.
       * Aliases for basic kundali: {@code KUNDALI_SUMMARY}, {@code KUNDALI}, {@code BASIC}.
       */
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


