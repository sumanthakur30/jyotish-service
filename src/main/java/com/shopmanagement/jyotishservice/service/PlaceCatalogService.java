package com.shopmanagement.jyotishservice.service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.shopmanagement.jyotishservice.api.BirthProfileApi.PlaceSearchResponse;
import com.shopmanagement.jyotishservice.api.BirthProfileApi.PlaceSuggestion;

/**
 * Built-in Indian city catalog for Phase 1. Replace/extend with geocoder later without changing API.
 */
@Service
public class PlaceCatalogService {

  private static final List<PlaceSuggestion> PLACES =
      List.of(
          place("Delhi, India", "IN", "28.6139000", "77.2090000", "Asia/Kolkata"),
          place("Mumbai, India", "IN", "19.0760000", "72.8777000", "Asia/Kolkata"),
          place("Kolkata, India", "IN", "22.5726000", "88.3639000", "Asia/Kolkata"),
          place("Chennai, India", "IN", "13.0827000", "80.2707000", "Asia/Kolkata"),
          place("Bengaluru, India", "IN", "12.9716000", "77.5946000", "Asia/Kolkata"),
          place("Hyderabad, India", "IN", "17.3850000", "78.4867000", "Asia/Kolkata"),
          place("Pune, India", "IN", "18.5204000", "73.8567000", "Asia/Kolkata"),
          place("Ahmedabad, India", "IN", "23.0225000", "72.5714000", "Asia/Kolkata"),
          place("Jaipur, India", "IN", "26.9124000", "75.7873000", "Asia/Kolkata"),
          place("Lucknow, India", "IN", "26.8467000", "80.9462000", "Asia/Kolkata"),
          place("Patna, India", "IN", "25.5941000", "85.1376000", "Asia/Kolkata"),
          place("Varanasi, India", "IN", "25.3176000", "82.9739000", "Asia/Kolkata"),
          place("Chandigarh, India", "IN", "30.7333000", "76.7794000", "Asia/Kolkata"),
          place("Kochi, India", "IN", "9.9312000", "76.2673000", "Asia/Kolkata"),
          place("Thiruvananthapuram, India", "IN", "8.5241000", "76.9366000", "Asia/Kolkata"),
          place("Guwahati, India", "IN", "26.1445000", "91.7362000", "Asia/Kolkata"),
          place("Bhopal, India", "IN", "23.2599000", "77.4126000", "Asia/Kolkata"),
          place("Indore, India", "IN", "22.7196000", "75.8577000", "Asia/Kolkata"),
          place("Nagpur, India", "IN", "21.1458000", "79.0882000", "Asia/Kolkata"),
          place("Surat, India", "IN", "21.1702000", "72.8311000", "Asia/Kolkata"));

  public PlaceSearchResponse search(String q) {
    String needle = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
    List<PlaceSuggestion> items =
        PLACES.stream()
            .filter(p -> needle.isEmpty() || p.placeName().toLowerCase(Locale.ROOT).contains(needle))
            .sorted(Comparator.comparing(PlaceSuggestion::placeName))
            .limit(20)
            .toList();
    return new PlaceSearchResponse(items);
  }

  private static PlaceSuggestion place(
      String name, String country, String lat, String lon, String tz) {
    return new PlaceSuggestion(
        name, country, new BigDecimal(lat), new BigDecimal(lon), tz);
  }
}
