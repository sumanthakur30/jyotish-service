package com.shopmanagement.jyotishservice.geocode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.shopmanagement.jyotishservice.geocode.NominatimHit.Address;

class NominatimPlaceMapperTest {

  @Test
  void stateUsesRepresentativePointAndCountry() {
    var hit =
        new NominatimHit(
            "Jharkhand",
            "Jharkhand, India",
            "23.4559809",
            "85.2557301",
            "state",
            new Address(null, null, null, null, null, null, null, "Jharkhand", "India", "in"));
    var place = NominatimPlaceMapper.toSuggestion(hit, "Asia/Kolkata");
    assertEquals("Jharkhand, India", place.placeName());
    assertEquals("IN", place.countryCode());
    assertEquals("Asia/Kolkata", place.timeZone());
    assertEquals("23.4559809", place.latitude().toPlainString());
    assertEquals("85.2557301", place.longitude().toPlainString());
  }

  @Test
  void cityAndDistrictStayDistinct() {
    var city =
        new NominatimHit(
            "Bokaro",
            "Bokaro, Chas, Bokaro, Jharkhand, India",
            "23.6544338",
            "86.1456444",
            "city",
            new Address("Bokaro", null, null, null, null, "Chas", "Bokaro", "Jharkhand", "India", "in"));
    var district =
        new NominatimHit(
            "Bokaro",
            "Bokaro, Jharkhand, India",
            "23.6991279",
            "85.9910689",
            "state_district",
            new Address(null, null, null, null, null, null, "Bokaro", "Jharkhand", "India", "in"));
    assertEquals("Bokaro, Jharkhand, India", NominatimPlaceMapper.label(city));
    assertEquals("Bokaro district, Jharkhand, India", NominatimPlaceMapper.label(district));
  }

  @Test
  void steelCityKeepsItsOwnName() {
    var hit =
        new NominatimHit(
            "Bokaro Steel City",
            "Bokaro Steel City, Bokaro, Jharkhand, India",
            "23.6693000",
            "86.1511000",
            "city",
            new Address(null, null, null, null, null, null, "Bokaro", "Jharkhand", "India", "in"));
    assertEquals("Bokaro Steel City, Jharkhand, India", NominatimPlaceMapper.label(hit));
  }

  @Test
  void villageIncludesDistrict() {
    var hit =
        new NominatimHit(
            "Chas",
            "Chas, Bokaro, Jharkhand, India",
            "23.6356000",
            "86.1671000",
            "town",
            new Address(null, "Chas", null, null, null, null, "Bokaro", "Jharkhand", "India", "in"));
    assertTrue(NominatimPlaceMapper.label(hit).startsWith("Chas, Bokaro, Jharkhand"));
  }

  @Test
  void rejectsMissingCoordinates() {
    var hit = new NominatimHit("Patna", "Patna, Bihar, India", null, "85.1", "city", null);
    assertNull(NominatimPlaceMapper.toSuggestion(hit, "Asia/Kolkata"));
  }
}
