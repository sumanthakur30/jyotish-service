package com.shopmanagement.jyotishservice.persistence.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "life_analysis")
public class LifeAnalysisEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "tenant_id", nullable = false, length = 64)
  private String tenantId;

  @Column(name = "kundali_id", nullable = false)
  private Long kundaliId;

  @Column(nullable = false, length = 32)
  private String category;

  @Column(name = "sub_category", length = 64)
  private String subCategory;

  @Column(nullable = false, length = 24)
  private String status = "NOT_STARTED";

  @Column(name = "past_notes", columnDefinition = "TEXT")
  private String pastNotes;

  @Column(name = "present_notes", columnDefinition = "TEXT")
  private String presentNotes;

  @Column(name = "future_notes", columnDefinition = "TEXT")
  private String futureNotes;

  @Column(name = "important_periods_notes", columnDefinition = "TEXT")
  private String importantPeriodsNotes;

  @Column(columnDefinition = "TEXT")
  private String advice;

  @Column(name = "jyotish_notes", columnDefinition = "TEXT")
  private String jyotishNotes;

  @Column(name = "sections_json", columnDefinition = "TEXT")
  private String sectionsJson;

  @Column(name = "include_in_report", nullable = false)
  private boolean includeInReport = true;

  @Column(name = "created_by", length = 128)
  private String createdBy;

  @Column(name = "updated_by", length = 128)
  private String updatedBy;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @PrePersist
  void onCreate() {
    Instant now = Instant.now();
    if (createdAt == null) {
      createdAt = now;
    }
    updatedAt = now;
    if (status == null || status.isBlank()) {
      status = "NOT_STARTED";
    }
  }

  @PreUpdate
  void onUpdate() {
    updatedAt = Instant.now();
  }

  public Long getId() {
    return id;
  }

  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  public Long getKundaliId() {
    return kundaliId;
  }

  public void setKundaliId(Long kundaliId) {
    this.kundaliId = kundaliId;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getSubCategory() {
    return subCategory;
  }

  public void setSubCategory(String subCategory) {
    this.subCategory = subCategory;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getPastNotes() {
    return pastNotes;
  }

  public void setPastNotes(String pastNotes) {
    this.pastNotes = pastNotes;
  }

  public String getPresentNotes() {
    return presentNotes;
  }

  public void setPresentNotes(String presentNotes) {
    this.presentNotes = presentNotes;
  }

  public String getFutureNotes() {
    return futureNotes;
  }

  public void setFutureNotes(String futureNotes) {
    this.futureNotes = futureNotes;
  }

  public String getImportantPeriodsNotes() {
    return importantPeriodsNotes;
  }

  public void setImportantPeriodsNotes(String importantPeriodsNotes) {
    this.importantPeriodsNotes = importantPeriodsNotes;
  }

  public String getAdvice() {
    return advice;
  }

  public void setAdvice(String advice) {
    this.advice = advice;
  }

  public String getJyotishNotes() {
    return jyotishNotes;
  }

  public void setJyotishNotes(String jyotishNotes) {
    this.jyotishNotes = jyotishNotes;
  }

  public String getSectionsJson() {
    return sectionsJson;
  }

  public void setSectionsJson(String sectionsJson) {
    this.sectionsJson = sectionsJson;
  }

  public boolean isIncludeInReport() {
    return includeInReport;
  }

  public void setIncludeInReport(boolean includeInReport) {
    this.includeInReport = includeInReport;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  public String getUpdatedBy() {
    return updatedBy;
  }

  public void setUpdatedBy(String updatedBy) {
    this.updatedBy = updatedBy;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
