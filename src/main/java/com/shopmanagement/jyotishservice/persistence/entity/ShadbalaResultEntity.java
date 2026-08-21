package com.shopmanagement.jyotishservice.persistence.entity;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "shadbala_result")
public class ShadbalaResultEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "tenant_id", nullable = false, length = 64)
  private String tenantId;

  @Column(name = "kundali_id", nullable = false)
  private Long kundaliId;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "result_json", nullable = false, columnDefinition = "jsonb")
  private String resultJson = "{}";

  @Column(name = "calculation_engine_version", nullable = false, length = 16)
  private String calculationEngineVersion;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @PrePersist
  void onCreate() {
    if (createdAt == null) {
      createdAt = Instant.now();
    }
    if (resultJson == null) {
      resultJson = "{}";
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

  public String getResultJson() {
    return resultJson;
  }

  public void setResultJson(String resultJson) {
    this.resultJson = resultJson;
  }

  public String getCalculationEngineVersion() {
    return calculationEngineVersion;
  }

  public void setCalculationEngineVersion(String calculationEngineVersion) {
    this.calculationEngineVersion = calculationEngineVersion;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
