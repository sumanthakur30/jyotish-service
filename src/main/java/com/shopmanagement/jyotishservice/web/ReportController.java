package com.shopmanagement.jyotishservice.web;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.shopmanagement.jyotishservice.api.ReportApi.CreateReportRequest;
import com.shopmanagement.jyotishservice.api.ReportApi.ReportResponse;
import com.shopmanagement.jyotishservice.service.ReportService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/jyotish/reports")
public class ReportController {

  private final ReportService reportService;

  public ReportController(ReportService reportService) {
    this.reportService = reportService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ReportResponse create(@Valid @RequestBody CreateReportRequest body) {
    return reportService.create(body);
  }

  @GetMapping("/{id}")
  public ReportResponse get(@PathVariable Long id) {
    return reportService.get(id);
  }

  @GetMapping("/{id}/download")
  public ResponseEntity<byte[]> download(@PathVariable Long id) {
    ReportResponse meta = reportService.get(id);
    byte[] bytes = reportService.download(id);
    String filename =
        "jyotish-report-"
            + id
            + "-"
            + meta.type().toLowerCase().replace('_', '-')
            + ".pdf";
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
        .contentType(MediaType.APPLICATION_PDF)
        .contentLength(bytes.length)
        .body(bytes);
  }
}
