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
@Table(name = "dasha_period")
public class DashaPeriodEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "tenant_id", nullable = false, length = 64)
  private String tenantId;

  @Column(name = "kundali_id", nullable = false)
  private Long kundaliId;

  @Column(name = "system_code", nullable = false, length = 32)
  private String systemCode;

  @Column(name = "level_code", nullable = false, length = 16)
  private String levelCode;

  @Column(name = "lord_code", nullable = false, length = 32)
  private String lordCode;

  @Column(name = "maha_lord_code", nullable = false, length = 32)
  private String mahaLordCode;

  @Column(name = "antar_lord_code", length = 32)
  private String antarLordCode;

  @Column(name = "pratyantar_lord_code", length = 32)
  private String pratyantarLordCode;

  @Column(name = "sequence_no", nullable = false)
  private int sequenceNo;

  @Column(name = "start_at", nullable = false)
  private Instant startAt;

  @Column(name = "end_at", nullable = false)
  private Instant endAt;

  @Column(name = "calculation_engine_version", nullable = false, length = 16)
  private String calculationEngineVersion;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "meta_json", nullable = false, columnDefinition = "jsonb")
  private String metaJson = "{}";

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @PrePersist
  void onCreate() {
    if (createdAt == null) {
      createdAt = Instant.now();
    }
    if (metaJson == null) {
      metaJson = "{}";
    }
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
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

  public String getSystemCode() {
    return systemCode;
  }

  public void setSystemCode(String systemCode) {
    this.systemCode = systemCode;
  }

  public String getLevelCode() {
    return levelCode;
  }

  public void setLevelCode(String levelCode) {
    this.levelCode = levelCode;
  }

  public String getLordCode() {
    return lordCode;
  }

  public void setLordCode(String lordCode) {
    this.lordCode = lordCode;
  }

  public String getMahaLordCode() {
    return mahaLordCode;
  }

  public void setMahaLordCode(String mahaLordCode) {
    this.mahaLordCode = mahaLordCode;
  }

  public String getAntarLordCode() {
    return antarLordCode;
  }

  public void setAntarLordCode(String antarLordCode) {
    this.antarLordCode = antarLordCode;
  }

  public String getPratyantarLordCode() {
    return pratyantarLordCode;
  }

  public void setPratyantarLordCode(String pratyantarLordCode) {
    this.pratyantarLordCode = pratyantarLordCode;
  }

  public int getSequenceNo() {
    return sequenceNo;
  }

  public void setSequenceNo(int sequenceNo) {
    this.sequenceNo = sequenceNo;
  }

  public Instant getStartAt() {
    return startAt;
  }

  public void setStartAt(Instant startAt) {
    this.startAt = startAt;
  }

  public Instant getEndAt() {
    return endAt;
  }

  public void setEndAt(Instant endAt) {
    this.endAt = endAt;
  }

  public String getCalculationEngineVersion() {
    return calculationEngineVersion;
  }

  public void setCalculationEngineVersion(String calculationEngineVersion) {
    this.calculationEngineVersion = calculationEngineVersion;
  }

  public String getMetaJson() {
    return metaJson;
  }

  public void setMetaJson(String metaJson) {
    this.metaJson = metaJson;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }
}

