package com.shopmanagement.jyotishservice.persistence.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "kundali_snapshot")
public class KundaliSnapshotEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "tenant_id", nullable = false, length = 64)
  private String tenantId;

  @Column(name = "birth_profile_id")
  private Long birthProfileId;

  @Column(name = "display_name", nullable = false, length = 256)
  private String displayName;

  @Column(name = "birth_date", nullable = false)
  private LocalDate birthDate;

  @Column(name = "birth_time")
  private LocalTime birthTime;

  @Column(name = "birth_time_unknown", nullable = false)
  private boolean birthTimeUnknown;

  @Column(name = "time_zone", nullable = false, length = 64)
  private String timeZone;

  @Column(name = "place_name", nullable = false, length = 256)
  private String placeName;

  @Column(nullable = false, precision = 10, scale = 7)
  private BigDecimal latitude;

  @Column(nullable = false, precision = 10, scale = 7)
  private BigDecimal longitude;

  @Column(name = "ayanamsa_code", nullable = false, length = 32)
  private String ayanamsaCode;

  @Column(name = "ayanamsa_deg", nullable = false, precision = 12, scale = 6)
  private BigDecimal ayanamsaDeg;

  @Column(name = "zodiac_system", nullable = false, length = 16)
  private String zodiacSystem = "SIDEREAL";

  @Column(name = "house_system", nullable = false, length = 32)
  private String houseSystem;

  @Column(name = "chart_style", nullable = false, length = 32)
  private String chartStyle = "NORTH_INDIAN";

  @Column(name = "calculation_engine_version", nullable = false, length = 16)
  private String calculationEngineVersion;

  @Column(name = "julian_day_ut", nullable = false, precision = 18, scale = 8)
  private BigDecimal julianDayUt;

  @Column(name = "ascendant_longitude", nullable = false, precision = 12, scale = 6)
  private BigDecimal ascendantLongitude;

  @Column(name = "ascendant_sign_index", nullable = false)
  private short ascendantSignIndex;

  @Column(columnDefinition = "TEXT")
  private String notes;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "input_json", nullable = false, columnDefinition = "jsonb")
  private String inputJson = "{}";

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @PrePersist
  void onCreate() {
    createdAt = Instant.now();
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

  public Long getBirthProfileId() {
    return birthProfileId;
  }

  public void setBirthProfileId(Long birthProfileId) {
    this.birthProfileId = birthProfileId;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public LocalDate getBirthDate() {
    return birthDate;
  }

  public void setBirthDate(LocalDate birthDate) {
    this.birthDate = birthDate;
  }

  public LocalTime getBirthTime() {
    return birthTime;
  }

  public void setBirthTime(LocalTime birthTime) {
    this.birthTime = birthTime;
  }

  public boolean isBirthTimeUnknown() {
    return birthTimeUnknown;
  }

  public void setBirthTimeUnknown(boolean birthTimeUnknown) {
    this.birthTimeUnknown = birthTimeUnknown;
  }

  public String getTimeZone() {
    return timeZone;
  }

  public void setTimeZone(String timeZone) {
    this.timeZone = timeZone;
  }

  public String getPlaceName() {
    return placeName;
  }

  public void setPlaceName(String placeName) {
    this.placeName = placeName;
  }

  public BigDecimal getLatitude() {
    return latitude;
  }

  public void setLatitude(BigDecimal latitude) {
    this.latitude = latitude;
  }

  public BigDecimal getLongitude() {
    return longitude;
  }

  public void setLongitude(BigDecimal longitude) {
    this.longitude = longitude;
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

  public String getZodiacSystem() {
    return zodiacSystem;
  }

  public void setZodiacSystem(String zodiacSystem) {
    this.zodiacSystem = zodiacSystem;
  }

  public String getHouseSystem() {
    return houseSystem;
  }

  public void setHouseSystem(String houseSystem) {
    this.houseSystem = houseSystem;
  }

  public String getChartStyle() {
    return chartStyle;
  }

  public void setChartStyle(String chartStyle) {
    this.chartStyle = chartStyle;
  }

  public String getCalculationEngineVersion() {
    return calculationEngineVersion;
  }

  public void setCalculationEngineVersion(String calculationEngineVersion) {
    this.calculationEngineVersion = calculationEngineVersion;
  }

  public BigDecimal getJulianDayUt() {
    return julianDayUt;
  }

  public void setJulianDayUt(BigDecimal julianDayUt) {
    this.julianDayUt = julianDayUt;
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

  public String getInputJson() {
    return inputJson;
  }

  public void setInputJson(String inputJson) {
    this.inputJson = inputJson;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
