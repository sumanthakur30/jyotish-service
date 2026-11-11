package com.shopmanagement.jyotishservice.geocode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.shopmanagement.jyotishservice.api.BirthProfileApi.PlaceSuggestion;
import com.shopmanagement.jyotishservice.geocode.NominatimHit.Address;

/** Turns a Nominatim hit into the place label the profile form stores. */
public final class NominatimPlaceMapper {

  private static final Set<String> FINE_GRAIN =
      Set.of("village", "hamlet", "suburb", "locality", "neighbourhood", "quarter", "town");

  private NominatimPlaceMapper() {}

  public static PlaceSuggestion toSuggestion(NominatimHit hit, String timeZone) {
    if (hit == null || hit.lat() == null || hit.lon() == null) {
      return null;
    }
    BigDecimal lat;
    BigDecimal lon;
    try {
      lat = new BigDecimal(hit.lat().trim()).setScale(7, RoundingMode.HALF_UP);
      lon = new BigDecimal(hit.lon().trim()).setScale(7, RoundingMode.HALF_UP);
    } catch (NumberFormatException ex) {
      return null;
    }
    String label = label(hit);
    if (label.isBlank()) {
      return null;
    }
    if (label.length() > 256) {
      label = label.substring(0, 256);
    }
    return new PlaceSuggestion(label, countryCode(hit), lat, lon, timeZone);
  }

  static String label(NominatimHit hit) {
    Address address = hit.address();
    String name = text(hit.name());
    String kind = text(hit.addresstype()).toLowerCase(Locale.ROOT);
    String state = address == null ? "" : text(address.state());
    String country = address == null ? "" : text(address.country());
    String district = address == null ? "" : first(address.stateDistrict(), address.county());

    List<String> parts = new ArrayList<>();
    if ("state".equals(kind) || "country".equals(kind)) {
      add(parts, name.isEmpty() ? state : name);
      if (!same(name, country) && !same(state, country)) {
        add(parts, country);
      }
      return join(parts, hit.displayName());
    }
    if ("state_district".equals(kind) && same(name, district)) {
      add(parts, name + " district");
    } else {
      add(parts, name);
    }
    if (FINE_GRAIN.contains(kind) && !district.isEmpty() && !same(district, name) && !same(district, state)) {
      add(parts, district);
    }
    if (!state.isEmpty() && !same(state, name)) {
      add(parts, state);
    }
    if (!country.isEmpty() && !same(country, name)) {
      add(parts, country);
    }
    return join(parts, hit.displayName());
  }

  private static String countryCode(NominatimHit hit) {
    if (hit.address() == null || hit.address().countryCode() == null) {
      return "";
    }
    String code = hit.address().countryCode().trim().toUpperCase(Locale.ROOT);
    return code.length() > 8 ? code.substring(0, 8) : code;
  }

  private static String first(String primary, String fallback) {
    String value = text(primary);
    return value.isEmpty() ? text(fallback) : value;
  }

  private static void add(List<String> parts, String value) {
    String text = text(value);
    if (text.isEmpty()) {
      return;
    }
    if (!parts.isEmpty() && same(parts.get(parts.size() - 1), text)) {
      return;
    }
    parts.add(text);
  }

  private static String join(List<String> parts, String fallback) {
    if (parts.isEmpty()) {
      return text(fallback);
    }
    return String.join(", ", parts);
  }

  private static boolean same(String left, String right) {
    return text(left).equalsIgnoreCase(text(right));
  }

  private static String text(String value) {
    return value == null ? "" : value.trim();
  }
}
