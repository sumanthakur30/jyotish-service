package com.shopmanagement.jyotishservice.persistence.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "planetary_position")
public class PlanetaryPositionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "tenant_id", nullable = false, length = 64)
  private String tenantId;

  @Column(name = "kundali_id", nullable = false)
  private Long kundaliId;

  @Column(name = "planet_code", nullable = false, length = 32)
  private String planetCode;

  @Column(name = "longitude_deg", nullable = false, precision = 12, scale = 6)
  private BigDecimal longitudeDeg;

  @Column(name = "sign_index", nullable = false)
  private short signIndex;

  @Column(name = "sign_name", nullable = false, length = 32)
  private String signName;

  @Column(name = "degree_in_sign", nullable = false, precision = 12, scale = 6)
  private BigDecimal degreeInSign;

  @Column(nullable = false)
  private short house;

  @Column(name = "nakshatra_index", nullable = false)
  private short nakshatraIndex;

  @Column(name = "nakshatra_name", nullable = false, length = 64)
  private String nakshatraName;

  @Column(nullable = false)
  private short pada;

  @Column(nullable = false)
  private boolean retrograde;

  @Column(nullable = false)
  private boolean combust;

  @Column(name = "speed_deg_per_day", precision = 12, scale = 6)
  private BigDecimal speedDegPerDay;

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

  public String getPlanetCode() {
    return planetCode;
  }

  public void setPlanetCode(String planetCode) {
    this.planetCode = planetCode;
  }

  public BigDecimal getLongitudeDeg() {
    return longitudeDeg;
  }

  public void setLongitudeDeg(BigDecimal longitudeDeg) {
    this.longitudeDeg = longitudeDeg;
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

  public BigDecimal getDegreeInSign() {
    return degreeInSign;
  }

  public void setDegreeInSign(BigDecimal degreeInSign) {
    this.degreeInSign = degreeInSign;
  }

  public short getHouse() {
    return house;
  }

  public void setHouse(short house) {
    this.house = house;
  }

  public short getNakshatraIndex() {
    return nakshatraIndex;
  }

  public void setNakshatraIndex(short nakshatraIndex) {
    this.nakshatraIndex = nakshatraIndex;
  }

  public String getNakshatraName() {
    return nakshatraName;
  }

  public void setNakshatraName(String nakshatraName) {
    this.nakshatraName = nakshatraName;
  }

  public short getPada() {
    return pada;
  }

  public void setPada(short pada) {
    this.pada = pada;
  }

  public boolean isRetrograde() {
    return retrograde;
  }

  public void setRetrograde(boolean retrograde) {
    this.retrograde = retrograde;
  }

  public boolean isCombust() {
    return combust;
  }

  public void setCombust(boolean combust) {
    this.combust = combust;
  }

  public BigDecimal getSpeedDegPerDay() {
    return speedDegPerDay;
  }

  public void setSpeedDegPerDay(BigDecimal speedDegPerDay) {
    this.speedDegPerDay = speedDegPerDay;
  }
}
