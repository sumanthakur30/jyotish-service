package com.shopmanagement.jyotishservice.persistence.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "yoga_result")
public class YogaResultEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "tenant_id", nullable = false, length = 64)
  private String tenantId;

  @Column(name = "kundali_id", nullable = false)
  private Long kundaliId;

  @Column(name = "yoga_code", nullable = false, length = 64)
  private String yogaCode;

  @Column(name = "category_code", nullable = false, length = 32)
  private String categoryCode;

  @Column(name = "display_name", nullable = false, length = 128)
  private String displayName;

  @Column(name = "present", nullable = false)
  private boolean present;

  @Column(name = "strength_code", length = 32)
  private String strengthCode;

  @Column(name = "planet_codes_json", nullable = false, columnDefinition = "jsonb")
  private String planetCodesJson = "[]";

  @Column(name = "houses_json", nullable = false, columnDefinition = "jsonb")
  private String housesJson = "[]";

  @Column(name = "explanation", nullable = false, columnDefinition = "text")
  private String explanation;

  @Column(name = "rule_id", nullable = false, length = 64)
  private String ruleId;

  @Column(name = "calculation_engine_version", nullable = false, length = 16)
  private String calculationEngineVersion;

  @Column(name = "meta_json", nullable = false, columnDefinition = "jsonb")
  private String metaJson = "{}";

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @PrePersist
  void onCreate() {
    if (createdAt == null) {
      createdAt = Instant.now();
    }
    if (planetCodesJson == null) {
      planetCodesJson = "[]";
    }
    if (housesJson == null) {
      housesJson = "[]";
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

  public String getYogaCode() {
    return yogaCode;
  }

  public void setYogaCode(String yogaCode) {
    this.yogaCode = yogaCode;
  }

  public String getCategoryCode() {
    return categoryCode;
  }

  public void setCategoryCode(String categoryCode) {
    this.categoryCode = categoryCode;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public boolean isPresent() {
    return present;
  }

  public void setPresent(boolean present) {
    this.present = present;
  }

  public String getStrengthCode() {
    return strengthCode;
  }

  public void setStrengthCode(String strengthCode) {
    this.strengthCode = strengthCode;
  }

  public String getPlanetCodesJson() {
    return planetCodesJson;
  }

  public void setPlanetCodesJson(String planetCodesJson) {
    this.planetCodesJson = planetCodesJson;
  }

  public String getHousesJson() {
    return housesJson;
  }

  public void setHousesJson(String housesJson) {
    this.housesJson = housesJson;
  }

  public String getExplanation() {
    return explanation;
  }

  public void setExplanation(String explanation) {
    this.explanation = explanation;
  }

  public String getRuleId() {
    return ruleId;
  }

  public void setRuleId(String ruleId) {
    this.ruleId = ruleId;
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
