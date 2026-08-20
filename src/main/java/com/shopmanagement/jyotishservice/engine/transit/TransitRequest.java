package com.shopmanagement.jyotishservice.engine.transit;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Objects;

import com.shopmanagement.jyotishservice.engine.model.AyanamsaMode;

/** Inputs for a Gochar calculation at a civil date/time over a birth place. */
public final class TransitRequest {

  private final LocalDate transitDate;
  private final LocalTime transitTime;
  private final ZoneId zoneId;
  private final double latitudeDeg;
  private final double longitudeDeg;
  private final AyanamsaMode ayanamsa;
  /** Natal Lagna sign index (0–11) used for whole-sign transit houses. */
  private final int natalLagnaSignIndex;

  public TransitRequest(
      LocalDate transitDate,
      LocalTime transitTime,
      ZoneId zoneId,
      double latitudeDeg,
      double longitudeDeg,
      AyanamsaMode ayanamsa,
      int natalLagnaSignIndex) {
    this.transitDate = Objects.requireNonNull(transitDate, "transitDate");
    this.transitTime = transitTime != null ? transitTime : LocalTime.NOON;
    this.zoneId = Objects.requireNonNull(zoneId, "zoneId");
    this.latitudeDeg = latitudeDeg;
    this.longitudeDeg = longitudeDeg;
    this.ayanamsa = ayanamsa != null ? ayanamsa : AyanamsaMode.LAHIRI;
    if (natalLagnaSignIndex < 0 || natalLagnaSignIndex > 11) {
      throw new IllegalArgumentException("natalLagnaSignIndex must be 0–11");
    }
    this.natalLagnaSignIndex = natalLagnaSignIndex;
  }

  public LocalDate transitDate() {
    return transitDate;
  }

  public LocalTime transitTime() {
    return transitTime;
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

  public AyanamsaMode ayanamsa() {
    return ayanamsa;
  }

  public int natalLagnaSignIndex() {
    return natalLagnaSignIndex;
  }
}
