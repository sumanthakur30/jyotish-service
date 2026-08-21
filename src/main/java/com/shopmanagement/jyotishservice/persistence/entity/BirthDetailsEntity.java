package com.shopmanagement.jyotishservice.persistence.entity;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "birth_details")
public class BirthDetailsEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "tenant_id", nullable = false, length = 64)
  private String tenantId;

  @Column(name = "profile_id", nullable = false)
  private Long profileId;

  @Column(name = "birth_date", nullable = false)
  private LocalDate birthDate;

  @Column(name = "birth_time")
  private LocalTime birthTime;

  @Column(name = "birth_time_unknown", nullable = false)
  private boolean birthTimeUnknown = false;

  /** EXACT | APPROXIMATE | UNKNOWN — null means derive from birthTimeUnknown. */
  @Column(name = "birth_time_accuracy", length = 16)
  private String birthTimeAccuracy;

  /** For APPROXIMATE: 5, 15, 30, or 60. */
  @Column(name = "uncertainty_minutes")
  private Integer uncertaintyMinutes;

  @Column(name = "dst_observed", nullable = false)
  private boolean dstObserved = false;

  @Column(name = "time_zone", nullable = false, length = 64)
  private String timeZone = "Asia/Kolkata";

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @PrePersist
  void onCreate() {
    Instant now = Instant.now();
    createdAt = now;
    updatedAt = now;
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

  public Long getProfileId() {
    return profileId;
  }

  public void setProfileId(Long profileId) {
    this.profileId = profileId;
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

  public String getBirthTimeAccuracy() {
    return birthTimeAccuracy;
  }

  public void setBirthTimeAccuracy(String birthTimeAccuracy) {
    this.birthTimeAccuracy = birthTimeAccuracy;
  }

  public Integer getUncertaintyMinutes() {
    return uncertaintyMinutes;
  }

  public void setUncertaintyMinutes(Integer uncertaintyMinutes) {
    this.uncertaintyMinutes = uncertaintyMinutes;
  }

  public boolean isDstObserved() {
    return dstObserved;
  }

  public void setDstObserved(boolean dstObserved) {
    this.dstObserved = dstObserved;
  }

  public String getTimeZone() {
    return timeZone;
  }

  public void setTimeZone(String timeZone) {
    this.timeZone = timeZone;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
