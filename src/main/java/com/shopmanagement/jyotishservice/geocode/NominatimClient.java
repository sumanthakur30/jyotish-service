package com.shopmanagement.jyotishservice.geocode;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * OpenStreetMap Nominatim search. The public instance allows about one request per second, so
 * calls are paced and the service caches results.
 */
@Component
public class NominatimClient {

  private final RestTemplate restTemplate;
  private final String searchUrl;
  private final String userAgent;
  private final Object pace = new Object();
  private long lastCallAtMs;

  public NominatimClient(
      RestTemplate jyotishRestTemplate,
      @Value("${jyotish.geocode.nominatim-url:https://nominatim.openstreetmap.org/search}")
          String searchUrl,
      @Value("${jyotish.geocode.user-agent:SugamJyotish/1.0 (https://astro.sugamflow.com)}")
          String userAgent) {
    this.restTemplate = jyotishRestTemplate;
    this.searchUrl = searchUrl;
    this.userAgent = userAgent;
  }

  public List<NominatimHit> search(String query) {
    pace();
    var uri =
        UriComponentsBuilder.fromHttpUrl(searchUrl)
            .queryParam("q", query)
            .queryParam("format", "jsonv2")
            .queryParam("addressdetails", 1)
            .queryParam("limit", 8)
            .queryParam("dedupe", 1)
            .build()
            .encode()
            .toUri();
    RequestEntity<Void> request =
        RequestEntity.get(uri)
            .header(HttpHeaders.USER_AGENT, userAgent)
            .header(HttpHeaders.ACCEPT_LANGUAGE, "en")
            .build();
    NominatimHit[] body = restTemplate.exchange(request, NominatimHit[].class).getBody();
    if (body == null || body.length == 0) {
      return List.of();
    }
    return Arrays.asList(body);
  }

  private void pace() {
    synchronized (pace) {
      long wait = 1100L - (System.currentTimeMillis() - lastCallAtMs);
      if (wait > 0) {
        try {
          Thread.sleep(wait);
        } catch (InterruptedException ex) {
          Thread.currentThread().interrupt();
        }
      }
      lastCallAtMs = System.currentTimeMillis();
    }
  }
}
