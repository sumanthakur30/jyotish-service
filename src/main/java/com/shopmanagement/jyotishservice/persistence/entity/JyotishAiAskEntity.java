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
@Table(name = "jyotish_ai_ask")
public class JyotishAiAskEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "tenant_id", nullable = false, length = 64)
  private String tenantId;

  @Column(name = "kundali_id", nullable = false)
  private Long kundaliId;

  @Column(length = 64)
  private String topic;

  @Column(nullable = false, columnDefinition = "text")
  private String question;

  @Column(name = "provider_code", nullable = false, length = 32)
  private String providerCode;

  @Column(name = "model_code", length = 64)
  private String modelCode;

  @Column(name = "latency_ms")
  private Integer latencyMs;

  @Column(name = "context_summary", columnDefinition = "text")
  private String contextSummary;

  @Column(name = "answer_preview", columnDefinition = "text")
  private String answerPreview;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @PrePersist
  void onCreate() {
    if (createdAt == null) {
      createdAt = Instant.now();
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

  public String getTopic() {
    return topic;
  }

  public void setTopic(String topic) {
    this.topic = topic;
  }

  public String getQuestion() {
    return question;
  }

  public void setQuestion(String question) {
    this.question = question;
  }

  public String getProviderCode() {
    return providerCode;
  }

  public void setProviderCode(String providerCode) {
    this.providerCode = providerCode;
  }

  public String getModelCode() {
    return modelCode;
  }

  public void setModelCode(String modelCode) {
    this.modelCode = modelCode;
  }

  public Integer getLatencyMs() {
    return latencyMs;
  }

  public void setLatencyMs(Integer latencyMs) {
    this.latencyMs = latencyMs;
  }

  public String getContextSummary() {
    return contextSummary;
  }

  public void setContextSummary(String contextSummary) {
    this.contextSummary = contextSummary;
  }

  public String getAnswerPreview() {
    return answerPreview;
  }

  public void setAnswerPreview(String answerPreview) {
    this.answerPreview = answerPreview;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }
}
