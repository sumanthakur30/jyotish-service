package com.shopmanagement.jyotishservice.engine.varga;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.shopmanagement.jyotishservice.engine.astro.ZodiacCatalog;

class VargaRegistryAndScaffoldTest {

  @Test
  void implementedCodesHaveMappers() {
    assertTrue(VargaRegistry.isImplemented(VargaCode.D1));
    assertTrue(VargaRegistry.isImplemented(VargaCode.D2));
    assertTrue(VargaRegistry.isImplemented(VargaCode.D3));
    assertTrue(VargaRegistry.isImplemented(VargaCode.D9));
    assertTrue(VargaRegistry.isImplemented(VargaCode.D10));
    assertFalse(VargaRegistry.isImplemented(VargaCode.D60));
  }

  @Test
  void comingSoonThrowsOnRequire() {
    assertThrows(IllegalArgumentException.class, () -> VargaRegistry.requireMapper(VargaCode.D12));
  }

  @Test
  void horaOddSignFirstHalfIsLeo() {
    double mapped = HoraVargaMapper.INSTANCE.mapLongitude(5.0); // Aries 5°
    assertEquals(4, ZodiacCatalog.signIndex(mapped)); // Leo
  }

  @Test
  void drekkanaSecondThirdIsFifthFromSign() {
    // Aries 15° → 2nd drekkana → Leo (5th)
    double mapped = DrekkanaVargaMapper.INSTANCE.mapLongitude(15.0);
    assertEquals(4, ZodiacCatalog.signIndex(mapped));
  }

  @Test
  void dasamsaOddStartsSameSign() {
    double mapped = DasamsaVargaMapper.INSTANCE.mapLongitude(0.0);
    assertEquals(0, ZodiacCatalog.signIndex(mapped));
  }
}
