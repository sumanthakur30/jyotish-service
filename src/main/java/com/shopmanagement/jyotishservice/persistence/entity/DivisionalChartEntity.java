package com.shopmanagement.jyotishservice.persistence.entity;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "divisional_chart")
public class DivisionalChartEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "tenant_id", nullable = false, length = 64)
  private String tenantId;

  @Column(name = "kundali_id", nullable = false)
  private Long kundaliId;

  @Column(name = "varga_code", nullable = false, length = 8)
  private String vargaCode;

  @Column(name = "calculation_engine_version", nullable = false, length = 16)
  private String calculationEngineVersion;

  @Column(name = "house_system", nullable = false, length = 32)
  private String houseSystem;

  @Column(name = "ascendant_longitude", nullable = false, precision = 12, scale = 6)
  private BigDecimal ascendantLongitude;

  @Column(name = "ascendant_sign_index", nullable = false)
  private short ascendantSignIndex;

  @Column private String notes;

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

  public String getVargaCode() {
    return vargaCode;
  }

  public void setVargaCode(String vargaCode) {
    this.vargaCode = vargaCode;
  }

  public String getCalculationEngineVersion() {
    return calculationEngineVersion;
  }

  public void setCalculationEngineVersion(String calculationEngineVersion) {
    this.calculationEngineVersion = calculationEngineVersion;
  }

  public String getHouseSystem() {
    return houseSystem;
  }

  public void setHouseSystem(String houseSystem) {
    this.houseSystem = houseSystem;
  }

  public BigDecimal getAscendantLongitude() {
    return ascendantLongitude;
  }

  public void setAscendantLongitude(BigDecimal ascendantLongitude) {
    this.ascendantLongitude = ascendantLongitude;
  }

  public short getAscendantSignIndex() {
    return ascendantSignIndex;
  }

  public void setAscendantSignIndex(short ascendantSignIndex) {
    this.ascendantSignIndex = ascendantSignIndex;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
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

