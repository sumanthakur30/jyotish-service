package com.shopmanagement.jyotishservice.engine.dasha;

import java.time.Instant;
import java.util.List;

import com.shopmanagement.jyotishservice.engine.model.Planet;

/**
 * One timed dasha segment. Children are the next level (maha→antar→pratyantar). Empty children
 * for leaf periods.
 */
public final class DashaPeriod {

  private final DashaLevel level;
  private final Planet lord;
  private final Planet mahaLord;
  private final Planet antarLord;
  private final Planet pratyantarLord;
  private final Instant startAt;
  private final Instant endAt;
  private final int sequenceNo;
  private final List<DashaPeriod> children;

  public DashaPeriod(
      DashaLevel level,
      Planet lord,
      Planet mahaLord,
      Planet antarLord,
      Planet pratyantarLord,
      Instant startAt,
      Instant endAt,
      int sequenceNo,
      List<DashaPeriod> children) {
    this.level = level;
    this.lord = lord;
    this.mahaLord = mahaLord;
    this.antarLord = antarLord;
    this.pratyantarLord = pratyantarLord;
    this.startAt = startAt;
    this.endAt = endAt;
    this.sequenceNo = sequenceNo;
    this.children = children == null ? List.of() : List.copyOf(children);
  }

  public DashaLevel level() {
    return level;
  }

  public Planet lord() {
    return lord;
  }

  public Planet mahaLord() {
    return mahaLord;
  }

  public Planet antarLord() {
    return antarLord;
  }

  public Planet pratyantarLord() {
    return pratyantarLord;
  }

  public Instant startAt() {
    return startAt;
  }

  public Instant endAt() {
    return endAt;
  }

  public int sequenceNo() {
    return sequenceNo;
  }

  public List<DashaPeriod> children() {
    return children;
  }

  public boolean contains(Instant instant) {
    return !instant.isBefore(startAt) && instant.isBefore(endAt);
  }
}
