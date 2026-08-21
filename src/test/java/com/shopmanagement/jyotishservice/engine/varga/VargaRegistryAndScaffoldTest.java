package com.shopmanagement.jyotishservice.engine.varga;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.shopmanagement.jyotishservice.engine.astro.ZodiacCatalog;

class VargaRegistryAndScaffoldTest {

  @Test
  void allShodashaCodesHaveMappers() {
    for (VargaCode code : VargaCode.values()) {
      assertTrue(VargaRegistry.isImplemented(code), code.code());
    }
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

  @Test
  void chaturthamsaSecondPartIsFourth() {
    // Aries 8° → part 1 → Cancer (4th)
    double mapped = ParasharaEqualVargas.D4.mapLongitude(8.0);
    assertEquals(3, ZodiacCatalog.signIndex(mapped));
  }

  @Test
  void dwadasamsaStartsSameSign() {
    double mapped = ParasharaEqualVargas.D12.mapLongitude(1.0);
    assertEquals(0, ZodiacCatalog.signIndex(mapped));
  }

  @Test
  void trimsamsaOddFirstSpanIsAries() {
    double mapped = TrimsamsaVargaMapper.INSTANCE.mapLongitude(2.0); // Aries 2°
    assertEquals(0, ZodiacCatalog.signIndex(mapped));
  }

  @Test
  void shashtyamshaSecondHalfDegreeAdvancesOneSign() {
    // Aries 0.6° → part 1 → Taurus
    double mapped = ParasharaEqualVargas.D60.mapLongitude(0.6);
    assertEquals(1, ZodiacCatalog.signIndex(mapped));
  }
}
