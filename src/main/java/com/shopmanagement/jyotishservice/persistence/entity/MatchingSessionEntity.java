package com.shopmanagement.jyotishservice.persistence.entity;

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
@Table(name = "matching_session")
public class MatchingSessionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "tenant_id", nullable = false, length = 64)
  private String tenantId;

  @Column(name = "profile_id_a", nullable = false)
  private Long profileIdA;

  @Column(name = "profile_id_b", nullable = false)
  private Long profileIdB;

  @Column(name = "kundali_id_a")
  private Long kundaliIdA;

  @Column(name = "kundali_id_b")
  private Long kundaliIdB;

  @Column(name = "display_name_a", nullable = false, length = 256)
  private String displayNameA;

  @Column(name = "display_name_b", nullable = false, length = 256)
  private String displayNameB;

  @Column(name = "total_score", nullable = false)
  private int totalScore;

  @Column(name = "max_score", nullable = false)
  private int maxScore;

  @Column(name = "percentage", nullable = false, precision = 6, scale = 2)
  private BigDecimal percentage;

  @Column(name = "manglik_status_a", nullable = false, length = 16)
  private String manglikStatusA;

  @Column(name = "manglik_status_b", nullable = false, length = 16)
  private String manglikStatusB;

  @Column(name = "manglik_mars_house_a", nullable = false)
  private short manglikMarsHouseA;

  @Column(name = "manglik_mars_house_b", nullable = false)
  private short manglikMarsHouseB;

  @Column(name = "summary", nullable = false, columnDefinition = "text")
  private String summary;

  @Column(name = "notes", nullable = false, columnDefinition = "text")
  private String notes;

  @Column(name = "disclaimer", nullable = false, columnDefinition = "text")
  private String disclaimer;

  @Column(name = "calculation_engine_version", nullable = false, length = 16)
  private String calculationEngineVersion;

  @Column(name = "result_json", nullable = false, columnDefinition = "jsonb")
  private String resultJson = "{}";

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

  public void setId(Long id) {
    this.id = id;
  }

  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  public Long getProfileIdA() {
    return profileIdA;
  }

  public void setProfileIdA(Long profileIdA) {
    this.profileIdA = profileIdA;
  }

  public Long getProfileIdB() {
    return profileIdB;
  }

  public void setProfileIdB(Long profileIdB) {
    this.profileIdB = profileIdB;
  }

  public Long getKundaliIdA() {
    return kundaliIdA;
  }

  public void setKundaliIdA(Long kundaliIdA) {
    this.kundaliIdA = kundaliIdA;
  }

  public Long getKundaliIdB() {
    return kundaliIdB;
  }

  public void setKundaliIdB(Long kundaliIdB) {
    this.kundaliIdB = kundaliIdB;
  }

  public String getDisplayNameA() {
    return displayNameA;
  }

  public void setDisplayNameA(String displayNameA) {
    this.displayNameA = displayNameA;
  }

  public String getDisplayNameB() {
    return displayNameB;
  }

  public void setDisplayNameB(String displayNameB) {
    this.displayNameB = displayNameB;
  }

  public int getTotalScore() {
    return totalScore;
  }

  public void setTotalScore(int totalScore) {
    this.totalScore = totalScore;
  }

  public int getMaxScore() {
    return maxScore;
  }

  public void setMaxScore(int maxScore) {
    this.maxScore = maxScore;
  }

  public BigDecimal getPercentage() {
    return percentage;
  }

  public void setPercentage(BigDecimal percentage) {
    this.percentage = percentage;
  }

  public String getManglikStatusA() {
    return manglikStatusA;
  }

  public void setManglikStatusA(String manglikStatusA) {
    this.manglikStatusA = manglikStatusA;
  }

  public String getManglikStatusB() {
    return manglikStatusB;
  }

  public void setManglikStatusB(String manglikStatusB) {
    this.manglikStatusB = manglikStatusB;
  }

  public short getManglikMarsHouseA() {
    return manglikMarsHouseA;
  }

  public void setManglikMarsHouseA(short manglikMarsHouseA) {
    this.manglikMarsHouseA = manglikMarsHouseA;
  }

  public short getManglikMarsHouseB() {
    return manglikMarsHouseB;
  }

  public void setManglikMarsHouseB(short manglikMarsHouseB) {
    this.manglikMarsHouseB = manglikMarsHouseB;
  }

  public String getSummary() {
    return summary;
  }

  public void setSummary(String summary) {
    this.summary = summary;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public String getDisclaimer() {
    return disclaimer;
  }

  public void setDisclaimer(String disclaimer) {
    this.disclaimer = disclaimer;
  }

  public String getCalculationEngineVersion() {
    return calculationEngineVersion;
  }

  public void setCalculationEngineVersion(String calculationEngineVersion) {
    this.calculationEngineVersion = calculationEngineVersion;
  }

  public String getResultJson() {
    return resultJson;
  }

  public void setResultJson(String resultJson) {
    this.resultJson = resultJson;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }
}
