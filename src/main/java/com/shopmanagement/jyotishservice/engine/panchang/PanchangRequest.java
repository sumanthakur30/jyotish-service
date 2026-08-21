package com.shopmanagement.jyotishservice.engine.panchang;

import java.time.LocalDate;
import java.util.Objects;

import com.shopmanagement.jyotishservice.engine.model.AyanamsaMode;

/** Input for a civil-date Panchang at a geographic place. */
public final class PanchangRequest {

  private final LocalDate date;
  private final double latitudeDeg;
  private final double longitudeDeg;
  private final String timeZone;
  private final String placeName;
  private final AyanamsaMode ayanamsa;

  public PanchangRequest(
      LocalDate date,
      double latitudeDeg,
      double longitudeDeg,
      String timeZone,
      String placeName,
      AyanamsaMode ayanamsa) {
    this.date = Objects.requireNonNull(date, "date");
    if (latitudeDeg < -90 || latitudeDeg > 90) {
      throw new IllegalArgumentException("latitude must be between -90 and 90");
    }
    if (longitudeDeg < -180 || longitudeDeg > 180) {
      throw new IllegalArgumentException("longitude must be between -180 and 180");
    }
    this.latitudeDeg = latitudeDeg;
    this.longitudeDeg = longitudeDeg;
    this.timeZone = Objects.requireNonNull(timeZone, "timeZone");
    this.placeName = placeName == null || placeName.isBlank() ? null : placeName.trim();
    this.ayanamsa = ayanamsa == null ? AyanamsaMode.LAHIRI : ayanamsa;
  }

  public LocalDate date() {
    return date;
  }

  public double latitudeDeg() {
    return latitudeDeg;
  }

  public double longitudeDeg() {
    return longitudeDeg;
  }

  public String timeZone() {
    return timeZone;
  }

  public String placeName() {
    return placeName;
  }

  public AyanamsaMode ayanamsa() {
    return ayanamsa;
  }
}
