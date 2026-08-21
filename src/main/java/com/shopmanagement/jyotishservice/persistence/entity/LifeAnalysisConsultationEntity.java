package com.shopmanagement.jyotishservice.persistence.entity;

import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "life_analysis_consultation")
public class LifeAnalysisConsultationEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "tenant_id", nullable = false, length = 64)
  private String tenantId;

  @Column(name = "kundali_id", nullable = false)
  private Long kundaliId;

  @Column(nullable = false, length = 32)
  private String category;

  @Column(columnDefinition = "TEXT")
  private String observation;

  @Column(name = "dasha_snapshot", columnDefinition = "TEXT")
  private String dashaSnapshot;

  @Column(name = "gochar_snapshot", columnDefinition = "TEXT")
  private String gocharSnapshot;

  @Column(columnDefinition = "TEXT")
  private String advice;

  @Column(name = "follow_up_date")
  private LocalDate followUpDate;

  @Column(name = "created_by", length = 128)
  private String createdBy;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @PrePersist
  void onCreate() {
    if (createdAt == null) {
      createdAt = Instant.now();
    }
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

  public String getObservation() {
    return observation;
  }

  public void setObservation(String observation) {
    this.observation = observation;
  }

  public String getDashaSnapshot() {
    return dashaSnapshot;
  }

  public void setDashaSnapshot(String dashaSnapshot) {
    this.dashaSnapshot = dashaSnapshot;
  }

  public String getGocharSnapshot() {
    return gocharSnapshot;
  }

  public void setGocharSnapshot(String gocharSnapshot) {
    this.gocharSnapshot = gocharSnapshot;
  }

  public String getAdvice() {
    return advice;
  }

  public void setAdvice(String advice) {
    this.advice = advice;
  }

  public LocalDate getFollowUpDate() {
    return followUpDate;
  }

  public void setFollowUpDate(LocalDate followUpDate) {
    this.followUpDate = followUpDate;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
