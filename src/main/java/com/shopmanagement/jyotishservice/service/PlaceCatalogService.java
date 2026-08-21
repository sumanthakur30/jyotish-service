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
          place("Varanasi, India", "IN", "25.3176000", "82.9739000", "Asia/Kolkata"),
          place("Chandigarh, India", "IN", "30.7333000", "76.7794000", "Asia/Kolkata"),
          place("Kochi, India", "IN", "9.9312000", "76.2673000", "Asia/Kolkata"),
          place("Thiruvananthapuram, India", "IN", "8.5241000", "76.9366000", "Asia/Kolkata"),
          place("Guwahati, India", "IN", "26.1445000", "91.7362000", "Asia/Kolkata"),
          place("Bhopal, India", "IN", "23.2599000", "77.4126000", "Asia/Kolkata"),
          place("Indore, India", "IN", "22.7196000", "75.8577000", "Asia/Kolkata"),
          place("Nagpur, India", "IN", "21.1458000", "79.0882000", "Asia/Kolkata"),
          place("Surat, India", "IN", "21.1702000", "72.8311000", "Asia/Kolkata"),
          // Bihar — district HQs / major cities (search "Bihar" or city name)
          place("Patna, Bihar, India", "IN", "25.5941000", "85.1376000", "Asia/Kolkata"),
          place("Gaya, Bihar, India", "IN", "24.7961000", "85.0070000", "Asia/Kolkata"),
          place("Muzaffarpur, Bihar, India", "IN", "26.1209000", "85.3647000", "Asia/Kolkata"),
          place("Bhagalpur, Bihar, India", "IN", "25.2425000", "86.9842000", "Asia/Kolkata"),
          place("Darbhanga, Bihar, India", "IN", "26.1542000", "85.8918000", "Asia/Kolkata"),
          place("Purnia, Bihar, India", "IN", "25.7771000", "87.4753000", "Asia/Kolkata"),
          place("Arrah, Bihar, India", "IN", "25.5560000", "84.6670000", "Asia/Kolkata"),
          place("Begusarai, Bihar, India", "IN", "25.4182000", "86.1272000", "Asia/Kolkata"),
          place("Katihar, Bihar, India", "IN", "25.5394000", "87.5713000", "Asia/Kolkata"),
          place("Munger, Bihar, India", "IN", "25.3748000", "86.4735000", "Asia/Kolkata"),
          place("Chapra, Bihar, India", "IN", "25.7815000", "84.7499000", "Asia/Kolkata"),
          place("Sasaram, Bihar, India", "IN", "24.9531000", "84.0167000", "Asia/Kolkata"),
          place("Motihari, Bihar, India", "IN", "26.6460000", "84.9087000", "Asia/Kolkata"),
          place("Bettiah, Bihar, India", "IN", "26.8023000", "84.5092000", "Asia/Kolkata"),
          place("Bihar Sharif, Bihar, India", "IN", "25.1973000", "85.5239000", "Asia/Kolkata"),
          place("Saharsa, Bihar, India", "IN", "25.8835000", "86.6005000", "Asia/Kolkata"),
          place("Samastipur, Bihar, India", "IN", "25.8629000", "85.7810000", "Asia/Kolkata"),
          place("Siwan, Bihar, India", "IN", "26.2190000", "84.3567000", "Asia/Kolkata"),
          place("Hajipur, Bihar, India", "IN", "25.6854000", "85.2083000", "Asia/Kolkata"),
          place("Madhubani, Bihar, India", "IN", "26.3537000", "86.0770000", "Asia/Kolkata"),
          place("Sitamarhi, Bihar, India", "IN", "26.5933000", "85.5039000", "Asia/Kolkata"),
          place("Aurangabad, Bihar, India", "IN", "24.7521000", "84.3742000", "Asia/Kolkata"),
          place("Jehanabad, Bihar, India", "IN", "25.2136000", "84.9870000", "Asia/Kolkata"),
          place("Nawada, Bihar, India", "IN", "24.8867000", "85.5434000", "Asia/Kolkata"),
          place("Jamui, Bihar, India", "IN", "24.9257000", "86.2247000", "Asia/Kolkata"),
          place("Danapur, Bihar, India", "IN", "25.6341000", "85.0450000", "Asia/Kolkata"),
          place("Dehri, Bihar, India", "IN", "24.9025000", "84.1822000", "Asia/Kolkata"),
          place("Kishanganj, Bihar, India", "IN", "26.1025000", "87.9550000", "Asia/Kolkata"),
          place("Forbesganj, Bihar, India", "IN", "26.3005000", "87.2655000", "Asia/Kolkata"));

  public PlaceSearchResponse search(String q) {
    String needle = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
    List<PlaceSuggestion> items =
        PLACES.stream()
            .filter(p -> matches(p.placeName(), needle))
            .sorted(Comparator.comparing(PlaceSuggestion::placeName))
            .limit(30)
            .toList();
    return new PlaceSearchResponse(items);
  }

  /** Empty query lists catalog; otherwise each whitespace token must appear in the name. */
  private static boolean matches(String placeName, String needle) {
    if (needle.isEmpty()) {
      return true;
    }
    String hay = placeName.toLowerCase(Locale.ROOT);
    if (hay.contains(needle)) {
      return true;
    }
    String[] tokens = needle.split("\\s+");
    for (String token : tokens) {
      if (!token.isEmpty() && !hay.contains(token)) {
        return false;
      }
    }
    return tokens.length > 0;
  }

  private static PlaceSuggestion place(
      String name, String country, String lat, String lon, String tz) {
    return new PlaceSuggestion(
        name, country, new BigDecimal(lat), new BigDecimal(lon), tz);
  }
}
