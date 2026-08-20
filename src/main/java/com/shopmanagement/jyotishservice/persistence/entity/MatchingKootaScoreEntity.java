package com.shopmanagement.jyotishservice.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "matching_koota_score")
public class MatchingKootaScoreEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "tenant_id", nullable = false, length = 64)
  private String tenantId;

  @Column(name = "matching_id", nullable = false)
  private Long matchingId;

  @Column(name = "koota_code", nullable = false, length = 32)
  private String kootaCode;

  @Column(name = "display_name", nullable = false, length = 64)
  private String displayName;

  @Column(name = "obtained", nullable = false)
  private int obtained;

  @Column(name = "max_points", nullable = false)
  private int maxPoints;

  @Column(name = "explanation", nullable = false, columnDefinition = "text")
  private String explanation;

  @Column(name = "rule_id", nullable = false, length = 64)
  private String ruleId;

  @Column(name = "sort_order", nullable = false)
  private short sortOrder;

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

  public Long getMatchingId() {
    return matchingId;
  }

  public void setMatchingId(Long matchingId) {
    this.matchingId = matchingId;
  }

  public String getKootaCode() {
    return kootaCode;
  }

  public void setKootaCode(String kootaCode) {
    this.kootaCode = kootaCode;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public int getObtained() {
    return obtained;
  }

  public void setObtained(int obtained) {
    this.obtained = obtained;
  }

  public int getMaxPoints() {
    return maxPoints;
  }

  public void setMaxPoints(int maxPoints) {
    this.maxPoints = maxPoints;
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

  public short getSortOrder() {
    return sortOrder;
  }

  public void setSortOrder(short sortOrder) {
    this.sortOrder = sortOrder;
  }
}
