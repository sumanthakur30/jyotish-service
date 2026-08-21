package com.shopmanagement.jyotishservice.engine.panchang;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

import com.shopmanagement.jyotishservice.engine.astro.AyanamsaCalculator;
import com.shopmanagement.jyotishservice.engine.astro.AstroMath;
import com.shopmanagement.jyotishservice.engine.astro.ZodiacCatalog;
import com.shopmanagement.jyotishservice.engine.ephemeris.EphemerisProvider;
import com.shopmanagement.jyotishservice.engine.model.Planet;

/**
 * Classical five-limb Panchang at sunrise (or local noon if sunrise unavailable) using sidereal
 * Sun/Moon from the active {@link EphemerisProvider} + configured ayanamsa.
 */
public final class PanchangCalculator {

  private PanchangCalculator() {}

  public static PanchangResult compute(
      PanchangRequest request, EphemerisProvider ephemeris, String engineVersion) {
    Objects.requireNonNull(request, "request");
    Objects.requireNonNull(ephemeris, "ephemeris");
    Objects.requireNonNull(engineVersion, "engineVersion");

    ZoneId zone;
    try {
      zone = ZoneId.of(request.timeZone());
    } catch (Exception ex) {
      throw new IllegalArgumentException("Invalid timezone: " + request.timeZone());
    }

    SolarEvents.Result solar =
        SolarEvents.compute(
            request.date(),
            request.latitudeDeg(),
            request.longitudeDeg(),
            zone,
            ephemeris);

    Instant asOf =
        solar.sunrise().available() && solar.sunrise().instant() != null
            ? solar.sunrise().instant()
            : solar.asOfFallback();

    double jd = AstroMath.julianDayUt(asOf);
    double ayanamsaDeg = AyanamsaCalculator.degrees(jd, request.ayanamsa());
    double sunSid =
        AstroMath.norm360(
            ephemeris.position(Planet.SUN, jd).longitudeDeg() - ayanamsaDeg);
    double moonSid =
        AstroMath.norm360(
            ephemeris.position(Planet.MOON, jd).longitudeDeg() - ayanamsaDeg);

    double elongation = AstroMath.norm360(moonSid - sunSid);
    int tithiIndex = (int) Math.floor(elongation / PanchangCatalog.TITHI_SPAN) % 30;
    double tithiProgress = (elongation % PanchangCatalog.TITHI_SPAN) / PanchangCatalog.TITHI_SPAN;

    int nakIndex = ZodiacCatalog.nakshatraIndex(moonSid);
    int pada = ZodiacCatalog.pada(moonSid);
    double nakProgress =
        (AstroMath.norm360(moonSid) % ZodiacCatalog.NAKSHATRA_SPAN) / ZodiacCatalog.NAKSHATRA_SPAN;

    double yogaLon = AstroMath.norm360(sunSid + moonSid);
    int yogaIndex = (int) Math.floor(yogaLon / ZodiacCatalog.NAKSHATRA_SPAN) % 27;
    double yogaProgress =
        (yogaLon % ZodiacCatalog.NAKSHATRA_SPAN) / ZodiacCatalog.NAKSHATRA_SPAN;

    int karanaHalf = (int) Math.floor(elongation / PanchangCatalog.KARANA_SPAN) % 60;
    double karanaProgress =
        (elongation % PanchangCatalog.KARANA_SPAN) / PanchangCatalog.KARANA_SPAN;

    DayOfWeek dow = asOf.atZone(zone).getDayOfWeek();
    int sundayBased = dow.getValue() % 7; // MON=1 → 1 … SUN=7 → 0

    PanchangResult.Limb tithi =
        new PanchangResult.Limb(
            tithiIndex,
            PanchangCatalog.tithiName(tithiIndex),
            PanchangCatalog.pakshaName(tithiIndex),
            0,
            tithiProgress,
            PanchangCatalog.pakshaName(tithiIndex)
                + " "
                + PanchangCatalog.tithiName(tithiIndex)
                + " · elongation "
                + round1(elongation)
                + "°");

    PanchangResult.Limb vara =
        new PanchangResult.Limb(
            sundayBased,
            PanchangCatalog.varaName(sundayBased),
            null,
            0,
            0,
            "Weekday of Vedic day starting at sunrise");

    PanchangResult.Limb nakshatra =
        new PanchangResult.Limb(
            nakIndex,
            ZodiacCatalog.nakshatraName(nakIndex),
            null,
            pada,
            nakProgress,
            "Moon "
                + ZodiacCatalog.signName(ZodiacCatalog.signIndex(moonSid))
                + " · pada "
                + pada);

    PanchangResult.Limb yoga =
        new PanchangResult.Limb(
            yogaIndex,
            PanchangCatalog.yogaName(yogaIndex),
            null,
            0,
            yogaProgress,
            "Sun+Moon sidereal " + round1(yogaLon) + "°");

    PanchangResult.Limb karana =
        new PanchangResult.Limb(
            karanaHalf,
            PanchangCatalog.karanaName(karanaHalf),
            null,
            0,
            karanaProgress,
            "Half-tithi index " + karanaHalf);

    List<PanchangResult.PanchangFeature> catalog = PanchangRegistry.catalog();
    List<PanchangResult.PanchangFeature> comingSoon = PanchangRegistry.comingSoon();

    boolean solarOk = solar.sunrise().available() && solar.sunset().available();
    PanchangResult.MuhuratBundle muhurat =
        MuhuratCalculator.compute(
            solar.sunrise().instant(),
            solar.sunset().instant(),
            zone,
            dow,
            solarOk);

    String notes =
        "Panchang limbs at sunrise (or local noon if polar); "
            + ephemeris.code()
            + " tropical + "
            + request.ayanamsa().name()
            + " ayanamsa. Compute-only (no cache table). Muhurat: Rahu Kaal, Yamaganda, Gulika,"
            + " Choghadiya, Hora, Abhijit READY. Moonrise/moonset Coming Soon.";

    return new PanchangResult(
        engineVersion,
        request.date(),
        request.timeZone(),
        request.placeName(),
        request.latitudeDeg(),
        request.longitudeDeg(),
        request.ayanamsa(),
        ayanamsaDeg,
        jd,
        asOf,
        tithi,
        vara,
        nakshatra,
        yoga,
        karana,
        solar.sunrise(),
        solar.sunset(),
        PanchangResult.LunarEvent.comingSoon(),
        PanchangResult.LunarEvent.comingSoon(),
        catalog,
        comingSoon,
        muhurat,
        notes,
        "Traditional calendar indicators for the selected place and date — not predictions.");
  }

  private static String round1(double v) {
    return String.format(java.util.Locale.ROOT, "%.1f", v);
  }
}
