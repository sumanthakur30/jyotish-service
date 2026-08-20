package com.shopmanagement.jyotishservice.persistence.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "transit_planet_position")
public class TransitPlanetPositionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "tenant_id", nullable = false, length = 64)
  private String tenantId;

  @Column(name = "transit_id", nullable = false)
  private Long transitId;

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

  @Column(name = "house", nullable = false)
  private short house;

  @Column(name = "nakshatra_index", nullable = false)
  private short nakshatraIndex;

  @Column(name = "nakshatra_name", nullable = false, length = 64)
  private String nakshatraName;

  @Column(name = "pada", nullable = false)
  private short pada;

  @Column(name = "retrograde", nullable = false)
  private boolean retrograde;

  @Column(name = "speed_deg_per_day", precision = 12, scale = 6)
  private BigDecimal speedDegPerDay;

  @Column(name = "natal_longitude_deg", precision = 12, scale = 6)
  private BigDecimal natalLongitudeDeg;

  @Column(name = "natal_sign_index")
  private Short natalSignIndex;

  @Column(name = "natal_sign_name", length = 32)
  private String natalSignName;

  @Column(name = "natal_house")
  private Short natalHouse;

  @Column(name = "sign_changed", nullable = false)
  private boolean signChanged;

  @Column(name = "house_changed", nullable = false)
  private boolean houseChanged;

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

  public Long getTransitId() {
    return transitId;
  }

  public void setTransitId(Long transitId) {
    this.transitId = transitId;
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

  public BigDecimal getSpeedDegPerDay() {
    return speedDegPerDay;
  }

  public void setSpeedDegPerDay(BigDecimal speedDegPerDay) {
    this.speedDegPerDay = speedDegPerDay;
  }

  public BigDecimal getNatalLongitudeDeg() {
    return natalLongitudeDeg;
  }

  public void setNatalLongitudeDeg(BigDecimal natalLongitudeDeg) {
    this.natalLongitudeDeg = natalLongitudeDeg;
  }

  public Short getNatalSignIndex() {
    return natalSignIndex;
  }

  public void setNatalSignIndex(Short natalSignIndex) {
    this.natalSignIndex = natalSignIndex;
  }

  public String getNatalSignName() {
    return natalSignName;
  }

  public void setNatalSignName(String natalSignName) {
    this.natalSignName = natalSignName;
  }

  public Short getNatalHouse() {
    return natalHouse;
  }

  public void setNatalHouse(Short natalHouse) {
    this.natalHouse = natalHouse;
  }

  public boolean isSignChanged() {
    return signChanged;
  }

  public void setSignChanged(boolean signChanged) {
    this.signChanged = signChanged;
  }

  public boolean isHouseChanged() {
    return houseChanged;
  }

  public void setHouseChanged(boolean houseChanged) {
    this.houseChanged = houseChanged;
  }
}
