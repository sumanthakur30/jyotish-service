package com.shopmanagement.jyotishservice.persistence.entity;

import java.time.Instant;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "jyotish_workspace")
public class JyotishWorkspaceEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "tenant_id", nullable = false, length = 64)
  private String tenantId;

  @Column(nullable = false, length = 128)
  private String name;

  @Column(nullable = false, length = 64)
  private String timezone = "Asia/Kolkata";

  @Column(nullable = false, length = 16)
  private String locale = "en";

  @Column(name = "ayanamsa_code", nullable = false, length = 32)
  private String ayanamsaCode = "LAHIRI";

  @Column(name = "zodiac_system", nullable = false, length = 16)
  private String zodiacSystem = "SIDEREAL";

  @Column(name = "chart_style", nullable = false, length = 32)
  private String chartStyle = "NORTH_INDIAN";

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "settings_json", nullable = false, columnDefinition = "jsonb")
  private String settingsJson = "{}";

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Column(name = "deleted_at")
  private Instant deletedAt;

  @PrePersist
  void onCreate() {
    Instant now = Instant.now();
    createdAt = now;
    updatedAt = now;
    if (settingsJson == null || settingsJson.isBlank()) {
      settingsJson = "{}";
    }
  }

  @PreUpdate
  void onUpdate() {
    updatedAt = Instant.now();
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

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getTimezone() {
    return timezone;
  }

  public void setTimezone(String timezone) {
    this.timezone = timezone;
  }

  public String getLocale() {
    return locale;
  }

  public void setLocale(String locale) {
    this.locale = locale;
  }

  public String getAyanamsaCode() {
    return ayanamsaCode;
  }

  public void setAyanamsaCode(String ayanamsaCode) {
    this.ayanamsaCode = ayanamsaCode;
  }

  public String getZodiacSystem() {
    return zodiacSystem;
  }

  public void setZodiacSystem(String zodiacSystem) {
    this.zodiacSystem = zodiacSystem;
  }

  public String getChartStyle() {
    return chartStyle;
  }

  public void setChartStyle(String chartStyle) {
    this.chartStyle = chartStyle;
  }

  public String getSettingsJson() {
    return settingsJson;
  }

  public void setSettingsJson(String settingsJson) {
    this.settingsJson = settingsJson;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public Instant getDeletedAt() {
    return deletedAt;
  }

  public void setDeletedAt(Instant deletedAt) {
    this.deletedAt = deletedAt;
  }
}
