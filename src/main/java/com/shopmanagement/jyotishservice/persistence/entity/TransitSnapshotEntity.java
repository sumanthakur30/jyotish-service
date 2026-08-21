package com.shopmanagement.jyotishservice.persistence.entity;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "transit_snapshot")
public class TransitSnapshotEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "tenant_id", nullable = false, length = 64)
  private String tenantId;

  @Column(name = "kundali_id", nullable = false)
  private Long kundaliId;

  @Column(name = "transit_date", nullable = false)
  private LocalDate transitDate;

  @Column(name = "transit_time", nullable = false)
  private LocalTime transitTime;

  @Column(name = "time_zone", nullable = false, length = 64)
  private String timeZone;

  @Column(name = "julian_day_ut", nullable = false, precision = 16, scale = 8)
  private BigDecimal julianDayUt;

  @Column(name = "ayanamsa_code", nullable = false, length = 32)
  private String ayanamsaCode;

  @Column(name = "ayanamsa_deg", nullable = false, precision = 12, scale = 6)
  private BigDecimal ayanamsaDeg;

  @Column(name = "natal_lagna_sign_index", nullable = false)
  private short natalLagnaSignIndex;

  @Column(name = "system_code", nullable = false, length = 32)
  private String systemCode = "GOCHAR";

  @Column(name = "calculation_engine_version", nullable = false, length = 16)
  private String calculationEngineVersion;

  @Column(name = "notes", columnDefinition = "text")
  private String notes;

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
    if (systemCode == null) {
      systemCode = "GOCHAR";
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

  public LocalDate getTransitDate() {
    return transitDate;
  }

  public void setTransitDate(LocalDate transitDate) {
    this.transitDate = transitDate;
  }

  public LocalTime getTransitTime() {
    return transitTime;
  }

  public void setTransitTime(LocalTime transitTime) {
    this.transitTime = transitTime;
  }

  public String getTimeZone() {
    return timeZone;
  }

  public void setTimeZone(String timeZone) {
    this.timeZone = timeZone;
  }

  public BigDecimal getJulianDayUt() {
    return julianDayUt;
  }

  public void setJulianDayUt(BigDecimal julianDayUt) {
    this.julianDayUt = julianDayUt;
  }

  public String getAyanamsaCode() {
    return ayanamsaCode;
  }

  public void setAyanamsaCode(String ayanamsaCode) {
    this.ayanamsaCode = ayanamsaCode;
  }

  public BigDecimal getAyanamsaDeg() {
    return ayanamsaDeg;
  }

  public void setAyanamsaDeg(BigDecimal ayanamsaDeg) {
    this.ayanamsaDeg = ayanamsaDeg;
  }

  public short getNatalLagnaSignIndex() {
    return natalLagnaSignIndex;
  }

  public void setNatalLagnaSignIndex(short natalLagnaSignIndex) {
    this.natalLagnaSignIndex = natalLagnaSignIndex;
  }

  public String getSystemCode() {
    return systemCode;
  }

  public void setSystemCode(String systemCode) {
    this.systemCode = systemCode;
  }

  public String getCalculationEngineVersion() {
    return calculationEngineVersion;
  }

  public void setCalculationEngineVersion(String calculationEngineVersion) {
    this.calculationEngineVersion = calculationEngineVersion;
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

