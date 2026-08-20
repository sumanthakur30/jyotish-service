package com.shopmanagement.jyotishservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
}
