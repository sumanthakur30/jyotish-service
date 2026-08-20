package com.shopmanagement.jyotishservice.engine.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;

/** Civil birth instant + geographic place used for chart calculation. */
public final class BirthMoment {

  private final LocalDate birthDate;
  private final LocalTime birthTime;
  private final ZoneId zoneId;
  private final double latitudeDeg;
  private final double longitudeDeg;
  private final String placeName;

  public BirthMoment(
      LocalDate birthDate,
      LocalTime birthTime,
      ZoneId zoneId,
      double latitudeDeg,
      double longitudeDeg,
      String placeName) {
    this.birthDate = Objects.requireNonNull(birthDate, "birthDate");
    this.birthTime = birthTime != null ? birthTime : LocalTime.NOON;
    this.zoneId = Objects.requireNonNull(zoneId, "zoneId");
    this.latitudeDeg = latitudeDeg;
    this.longitudeDeg = longitudeDeg;
    this.placeName = placeName;
  }

  public LocalDate birthDate() {
    return birthDate;
  }

  public LocalTime birthTime() {
    return birthTime;
  }

  public ZoneId zoneId() {
    return zoneId;
  }

  public double latitudeDeg() {
    return latitudeDeg;
  }

  public double longitudeDeg() {
    return longitudeDeg;
  }

  public String placeName() {
    return placeName;
  }

  public ZonedDateTime toZonedDateTime() {
    return ZonedDateTime.of(birthDate, birthTime, zoneId);
  }
}
