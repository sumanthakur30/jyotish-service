package com.shopmanagement.jyotishservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PlaceCatalogServiceTest {

  private final PlaceCatalogService service = new PlaceCatalogService();

  @Test
  void findsDelhi() {
    var result = service.search("del");
    assertTrue(result.items().stream().anyMatch(p -> p.placeName().startsWith("Delhi")));
    assertEquals("Asia/Kolkata", result.items().get(0).timeZone());
  }

  @Test
  void emptyQueryReturnsCatalog() {
    assertTrue(service.search("").items().size() >= 10);
  }

  @Test
  void findsBiharDistrictsByStateName() {
    var result = service.search("Bihar");
    assertFalse(result.items().isEmpty());
    assertTrue(result.items().stream().anyMatch(p -> p.placeName().startsWith("Patna")));
    assertTrue(result.items().stream().anyMatch(p -> p.placeName().startsWith("Gaya")));
    assertTrue(result.items().stream().anyMatch(p -> p.placeName().startsWith("Muzaffarpur")));
  }

  @Test
  void findsPatnaByCityName() {
    var result = service.search("Patna");
    assertEquals(1, result.items().size());
    assertTrue(result.items().get(0).placeName().contains("Patna"));
    assertEquals("Asia/Kolkata", result.items().get(0).timeZone());
  }
}
