package com.shopmanagement.jyotishservice.geocode;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** One Nominatim search hit. Unknown fields are ignored. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record NominatimHit(
    String name,
    @JsonProperty("display_name") String displayName,
    String lat,
    String lon,
    String addresstype,
    Address address) {

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Address(
      String city,
      String town,
      String village,
      String hamlet,
      String suburb,
      String county,
      @JsonProperty("state_district") String stateDistrict,
      String state,
      String country,
      @JsonProperty("country_code") String countryCode) {}
}
