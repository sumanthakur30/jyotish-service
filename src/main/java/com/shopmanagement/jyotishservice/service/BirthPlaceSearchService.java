package com.shopmanagement.jyotishservice.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.shopmanagement.jyotishservice.api.BirthProfileApi.PlaceSearchResponse;
import com.shopmanagement.jyotishservice.api.BirthProfileApi.PlaceSuggestion;
import com.shopmanagement.jyotishservice.geocode.CoordinateTimeZoneLookup;
import com.shopmanagement.jyotishservice.geocode.NominatimClient;
import com.shopmanagement.jyotishservice.geocode.NominatimHit;
import com.shopmanagement.jyotishservice.geocode.NominatimPlaceMapper;

/**
 * Birth-place search. OpenStreetMap Nominatim resolves states, cities, towns, and villages.
 * The built-in catalog is used only when that lookup fails.
 */
@Service
public class BirthPlaceSearchService {

  private static final Logger log = LoggerFactory.getLogger(BirthPlaceSearchService.class);
  private static final long SUCCESS_TTL_MS = 6L * 60L * 60L * 1000L;
  private static final int MAX_CACHE = 400;

  private final NominatimClient nominatimClient;
  private final CoordinateTimeZoneLookup timeZoneLookup;
  private final PlaceCatalogService catalog;
  private final ConcurrentHashMap<String, Cached> cache = new ConcurrentHashMap<>();

  public BirthPlaceSearchService(
      NominatimClient nominatimClient,
      CoordinateTimeZoneLookup timeZoneLookup,
      PlaceCatalogService catalog) {
    this.nominatimClient = nominatimClient;
    this.timeZoneLookup = timeZoneLookup;
    this.catalog = catalog;
  }

  public PlaceSearchResponse search(String query) {
    String q = query == null ? "" : query.trim();
    if (q.length() < 2) {
      return new PlaceSearchResponse(List.of());
    }
    String key = q.toLowerCase(Locale.ROOT);
    Cached cached = cache.get(key);
    long now = System.currentTimeMillis();
    if (cached != null && cached.expiresAtMs() > now) {
      return new PlaceSearchResponse(cached.items());
    }
    try {
      List<PlaceSuggestion> items = fromGeocoder(nominatimClient.search(q));
      if (!items.isEmpty()) {
        remember(key, items, now + SUCCESS_TTL_MS);
        return new PlaceSearchResponse(items);
      }
    } catch (Exception ex) {
      log.warn("Place search failed for '{}': {}", q, ex.getMessage());
    }
    return catalog.search(q);
  }

  private List<PlaceSuggestion> fromGeocoder(List<NominatimHit> hits) {
    List<PlaceSuggestion> items = new ArrayList<>();
    Set<String> seen = new LinkedHashSet<>();
    for (NominatimHit hit : hits) {
      BigDecimal lat = decimal(hit.lat());
      BigDecimal lon = decimal(hit.lon());
      String zone = null;
      if (lat != null && lon != null) {
        zone = timeZoneLookup.lookup(lat, lon).orElse(null);
      }
      PlaceSuggestion suggestion = NominatimPlaceMapper.toSuggestion(hit, zone);
      if (suggestion == null) {
        continue;
      }
      String identity =
          suggestion.placeName()
              + "|"
              + suggestion.latitude().toPlainString()
              + "|"
              + suggestion.longitude().toPlainString();
      if (seen.add(identity)) {
        items.add(suggestion);
      }
    }
    return items;
  }

  private void remember(String key, List<PlaceSuggestion> items, long expiresAtMs) {
    if (cache.size() > MAX_CACHE) {
      cache.clear();
    }
    cache.put(key, new Cached(expiresAtMs, List.copyOf(items)));
  }

  private static BigDecimal decimal(String raw) {
    if (raw == null || raw.isBlank()) {
      return null;
    }
    try {
      return new BigDecimal(raw.trim());
    } catch (NumberFormatException ex) {
      return null;
    }
  }

  private record Cached(long expiresAtMs, List<PlaceSuggestion> items) {}
}
