package com.shopmanagement.jyotishservice.persistence.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "divisional_house_position")
public class DivisionalHousePositionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "tenant_id", nullable = false, length = 64)
  private String tenantId;

  @Column(name = "divisional_chart_id", nullable = false)
  private Long divisionalChartId;

  @Column(nullable = false)
  private short house;

  @Column(name = "sign_index", nullable = false)
  private short signIndex;

  @Column(name = "sign_name", nullable = false, length = 32)
  private String signName;

  @Column(name = "cusp_longitude_deg", nullable = false, precision = 12, scale = 6)
  private BigDecimal cuspLongitudeDeg;

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

  public Long getDivisionalChartId() {
    return divisionalChartId;
  }

  public void setDivisionalChartId(Long divisionalChartId) {
    this.divisionalChartId = divisionalChartId;
  }

  public short getHouse() {
    return house;
  }

  public void setHouse(short house) {
    this.house = house;
  }

  public short getSignIndex() {
    return signIndex;
  }

  public void setSignIndex(short signIndex) {
    this.signIndex = signIndex;
  }

  public String getSignName() {
    return signName;
  }

  public void setSignName(String signName) {
    this.signName = signName;
  }

  public BigDecimal getCuspLongitudeDeg() {
    return cuspLongitudeDeg;
  }

  public void setCuspLongitudeDeg(BigDecimal cuspLongitudeDeg) {
    this.cuspLongitudeDeg = cuspLongitudeDeg;
  }
}
