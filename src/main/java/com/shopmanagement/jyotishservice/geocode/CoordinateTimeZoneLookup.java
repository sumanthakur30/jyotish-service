package com.shopmanagement.jyotishservice.geocode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/** IANA timezone for a coordinate. Results are cached; a failed lookup does not invent a zone. */
@Component
public class CoordinateTimeZoneLookup {

  private static final Logger log = LoggerFactory.getLogger(CoordinateTimeZoneLookup.class);

  private final RestTemplate restTemplate;
  private final String timezoneUrl;
  private final Map<String, String> cache = new ConcurrentHashMap<>();

  public CoordinateTimeZoneLookup(
      RestTemplate jyotishRestTemplate,
      @Value("${jyotish.geocode.timezone-url:https://timeapi.io/api/timezone/coordinate}")
          String timezoneUrl) {
    this.restTemplate = jyotishRestTemplate;
    this.timezoneUrl = timezoneUrl;
  }

  public Optional<String> lookup(BigDecimal latitude, BigDecimal longitude) {
    if (latitude == null || longitude == null) {
      return Optional.empty();
    }
    String key =
        latitude.setScale(2, RoundingMode.HALF_UP)
            + ","
            + longitude.setScale(2, RoundingMode.HALF_UP);
    String cached = cache.get(key);
    if (cached != null) {
      return Optional.of(cached);
    }
    try {
      var uri =
          UriComponentsBuilder.fromHttpUrl(timezoneUrl)
              .queryParam("latitude", latitude.toPlainString())
              .queryParam("longitude", longitude.toPlainString())
              .build()
              .encode()
              .toUri();
      @SuppressWarnings("unchecked")
      Map<String, Object> body = restTemplate.getForObject(uri, Map.class);
      if (body == null || body.get("timeZone") == null) {
        return Optional.empty();
      }
      String zone = String.valueOf(body.get("timeZone")).trim();
      ZoneId.of(zone);
      cache.put(key, zone);
      return Optional.of(zone);
    } catch (Exception ex) {
      log.warn("Timezone lookup failed for {}, {}: {}", latitude, longitude, ex.getMessage());
      return Optional.empty();
    }
  }
}
