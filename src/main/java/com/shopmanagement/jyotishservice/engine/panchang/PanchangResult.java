package com.shopmanagement.jyotishservice.engine.panchang;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.shopmanagement.jyotishservice.engine.model.AyanamsaMode;

/** Computed Panchang for one civil date at a place (compute-only MVP — no persistence). */
public final class PanchangResult {

  private final String engineVersion;
  private final LocalDate date;
  private final String timeZone;
  private final String placeName;
  private final double latitudeDeg;
  private final double longitudeDeg;
  private final AyanamsaMode ayanamsa;
  private final double ayanamsaDeg;
  private final double julianDayUt;
  private final Instant asOf;
  private final Limb tithi;
  private final Limb vara;
  private final Limb nakshatra;
  private final Limb yoga;
  private final Limb karana;
  private final SolarEvent sunrise;
  private final SolarEvent sunset;
  private final LunarEvent moonrise;
  private final LunarEvent moonset;
  private final List<PanchangFeature> catalog;
  private final List<PanchangFeature> comingSoon;
  private final MuhuratBundle muhurat;
  private final String notes;
  private final String disclaimer;

  public PanchangResult(
      String engineVersion,
      LocalDate date,
      String timeZone,
      String placeName,
      double latitudeDeg,
      double longitudeDeg,
      AyanamsaMode ayanamsa,
      double ayanamsaDeg,
      double julianDayUt,
      Instant asOf,
      Limb tithi,
      Limb vara,
      Limb nakshatra,
      Limb yoga,
      Limb karana,
      SolarEvent sunrise,
      SolarEvent sunset,
      LunarEvent moonrise,
      LunarEvent moonset,
      List<PanchangFeature> catalog,
      List<PanchangFeature> comingSoon,
      MuhuratBundle muhurat,
      String notes,
      String disclaimer) {
    this.engineVersion = engineVersion;
    this.date = date;
    this.timeZone = timeZone;
    this.placeName = placeName;
    this.latitudeDeg = latitudeDeg;
    this.longitudeDeg = longitudeDeg;
    this.ayanamsa = ayanamsa;
    this.ayanamsaDeg = ayanamsaDeg;
    this.julianDayUt = julianDayUt;
    this.asOf = asOf;
    this.tithi = tithi;
    this.vara = vara;
    this.nakshatra = nakshatra;
    this.yoga = yoga;
    this.karana = karana;
    this.sunrise = sunrise;
    this.sunset = sunset;
    this.moonrise = moonrise;
    this.moonset = moonset;
    this.catalog = List.copyOf(catalog);
    this.comingSoon = List.copyOf(comingSoon);
    this.muhurat = muhurat;
    this.notes = notes;
    this.disclaimer = disclaimer;
  }

  public String engineVersion() {
    return engineVersion;
  }

  public LocalDate date() {
    return date;
  }

  public String timeZone() {
    return timeZone;
  }

  public String placeName() {
    return placeName;
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

  public double ayanamsaDeg() {
    return ayanamsaDeg;
  }

  public double julianDayUt() {
    return julianDayUt;
  }

  public Instant asOf() {
    return asOf;
  }

  public Limb tithi() {
    return tithi;
  }

  public Limb vara() {
    return vara;
  }

  public Limb nakshatra() {
    return nakshatra;
  }

  public Limb yoga() {
    return yoga;
  }

  public Limb karana() {
    return karana;
  }

  public SolarEvent sunrise() {
    return sunrise;
  }

  public SolarEvent sunset() {
    return sunset;
  }

  public LunarEvent moonrise() {
    return moonrise;
  }

  public LunarEvent moonset() {
    return moonset;
  }

  public List<PanchangFeature> catalog() {
    return catalog;
  }

  public List<PanchangFeature> comingSoon() {
    return comingSoon;
  }

  public MuhuratBundle muhurat() {
    return muhurat;
  }

  public String notes() {
    return notes;
  }

  public String disclaimer() {
    return disclaimer;
  }

  /** One of the five classical limbs (or Vara). */
  public record Limb(
      int index,
      String name,
      String paksha,
      int pada,
      double progress,
      String detail) {}

  public record SolarEvent(boolean available, LocalTime localTime, Instant instant, String note) {
    public static SolarEvent of(LocalTime localTime, Instant instant) {
      return new SolarEvent(true, localTime, instant, null);
    }

    public static SolarEvent unavailable(String note) {
      return new SolarEvent(false, null, null, note);
    }
  }

  public record LunarEvent(boolean available, LocalTime localTime, Instant instant, String note) {
    public static LunarEvent comingSoon() {
      return new LunarEvent(
          false, null, null, "Moonrise/moonset Coming Soon — Meeus MVP lacks lunar declination.");
    }
  }

  public record PanchangFeature(
      String code, String displayName, boolean implemented, String status) {}

  /** One muhurat window (Rahu Kaal, Choghadiya slot, Hora, Abhijit, …). */
  public record MuhuratPeriod(
      String code, String name, Instant start, Instant end, String quality) {}

  public record MuhuratBundle(List<MuhuratPeriod> periods, String notes) {
    public MuhuratBundle {
      periods = periods == null ? List.of() : List.copyOf(periods);
      notes = notes == null ? "" : notes;
    }
  }
}
