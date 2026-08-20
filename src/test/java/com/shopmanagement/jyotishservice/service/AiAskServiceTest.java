package com.shopmanagement.jyotishservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.shopmanagement.jyotishservice.ai.LlmProvider;
import com.shopmanagement.jyotishservice.api.AiApi.AskRequest;
import com.shopmanagement.jyotishservice.api.AiApi.AskResponse;
import com.shopmanagement.jyotishservice.config.JyotishAiProperties;
import com.shopmanagement.jyotishservice.filter.TenantContextFilter;
import com.shopmanagement.jyotishservice.persistence.entity.DashaPeriodEntity;
import com.shopmanagement.jyotishservice.persistence.entity.HousePositionEntity;
import com.shopmanagement.jyotishservice.persistence.entity.JyotishAiAskEntity;
import com.shopmanagement.jyotishservice.persistence.entity.KundaliSnapshotEntity;
import com.shopmanagement.jyotishservice.persistence.entity.PlanetaryPositionEntity;
import com.shopmanagement.jyotishservice.persistence.entity.YogaResultEntity;
import com.shopmanagement.jyotishservice.persistence.repo.DashaPeriodRepository;
import com.shopmanagement.jyotishservice.persistence.repo.HousePositionRepository;
import com.shopmanagement.jyotishservice.persistence.repo.JyotishAiAskRepository;
import com.shopmanagement.jyotishservice.persistence.repo.KundaliSnapshotRepository;
import com.shopmanagement.jyotishservice.persistence.repo.PlanetaryPositionRepository;
import com.shopmanagement.jyotishservice.persistence.repo.TransitPlanetPositionRepository;
import com.shopmanagement.jyotishservice.persistence.repo.TransitSnapshotRepository;
import com.shopmanagement.jyotishservice.persistence.repo.YogaResultRepository;

@ExtendWith(MockitoExtension.class)
class AiAskServiceTest {

  @Mock private KundaliSnapshotRepository kundaliRepository;
  @Mock private PlanetaryPositionRepository planetaryRepository;
  @Mock private HousePositionRepository houseRepository;
  @Mock private DashaPeriodRepository dashaPeriodRepository;
  @Mock private YogaResultRepository yogaResultRepository;
  @Mock private TransitSnapshotRepository transitSnapshotRepository;
  @Mock private TransitPlanetPositionRepository transitPlanetRepository;
  @Mock private JyotishAiAskRepository askRepository;
  @Mock private LlmProvider llmProvider;

  private AiAskService aiAskService;

  @BeforeEach
  void setUp() {
    JyotishAiProperties props = new JyotishAiProperties();
    props.setProvider("HEURISTIC");
    props.setModelCode("HEURISTIC_V1");
    aiAskService =
        new AiAskService(
            props,
            llmProvider,
            kundaliRepository,
            planetaryRepository,
            houseRepository,
            dashaPeriodRepository,
            yogaResultRepository,
            transitSnapshotRepository,
            transitPlanetRepository,
            askRepository);
    TenantContextFilter.bindTenantForTests("TENANT-A");
  }

  @AfterEach
  void tearDown() {
    TenantContextFilter.clearTenantForTests();
  }

  @Test
  void heuristicPathReturnsNonEmptyAnswerWhenKundaliExists() {
    KundaliSnapshotEntity snap = sampleSnap(42L);
    when(kundaliRepository.findByIdAndTenantId(42L, "TENANT-A")).thenReturn(Optional.of(snap));
    when(planetaryRepository.findByKundaliIdAndTenantIdOrderByPlanetCodeAsc(42L, "TENANT-A"))
        .thenReturn(List.of(samplePlanet("JU", "Sagittarius", (short) 10)));
    when(houseRepository.findByKundaliIdAndTenantIdOrderByHouseAsc(42L, "TENANT-A"))
        .thenReturn(List.of(sampleHouse((short) 1, "Aries"), sampleHouse((short) 10, "Capricorn")));
    Instant now = Instant.now();
    DashaPeriodEntity maha = new DashaPeriodEntity();
    maha.setLevelCode("MAHA");
    maha.setLordCode("JU");
    maha.setMahaLordCode("JU");
    maha.setStartAt(now.minusSeconds(86_400));
    maha.setEndAt(now.plusSeconds(86_400 * 365));
    when(dashaPeriodRepository.findByKundaliIdAndTenantIdAndSystemCodeOrderByStartAtAscSequenceNoAsc(
            42L, "TENANT-A", "VIMSHOTTARI"))
        .thenReturn(List.of(maha));
    YogaResultEntity yoga = new YogaResultEntity();
    yoga.setPresent(true);
    yoga.setYogaCode("GAJAKESARI");
    yoga.setDisplayName("Gajakesari");
    yoga.setCategoryCode("RAJA");
    yoga.setExplanation("Moon–Jupiter mutual kendra (stored).");
    when(yogaResultRepository.findByKundaliIdAndTenantIdOrderByYogaCodeAsc(42L, "TENANT-A"))
        .thenReturn(List.of(yoga));
    when(transitSnapshotRepository.findFirstByKundaliIdAndTenantIdOrderByTransitDateDescCreatedAtDesc(
            42L, "TENANT-A"))
        .thenReturn(Optional.empty());
    when(llmProvider.code()).thenReturn("HEURISTIC");
    when(llmProvider.complete(eq("JYOTISH_ASK"), eq("en"), any())).thenReturn(Map.of());
    when(askRepository.save(any(JyotishAiAskEntity.class)))
        .thenAnswer(
            inv -> {
              JyotishAiAskEntity e = inv.getArgument(0);
              e.setId(7L);
              return e;
            });

    AskResponse res =
        aiAskService.ask(new AskRequest(42L, "How is my career looking?", "career"));

    assertTrue(res.aiGenerated());
    assertFalse(res.answer() == null || res.answer().isBlank());
    assertTrue(res.answer().toLowerCase().contains("ai-assisted"));
    assertEquals("HEURISTIC", res.providerCode());
    assertEquals("career", res.topic());
    assertFalse(res.findings().isEmpty());
    assertEquals(42L, res.kundaliId());
    assertTrue(res.contextUsed().containsKey("planetCount"));
    ArgumentCaptor<JyotishAiAskEntity> captor = ArgumentCaptor.forClass(JyotishAiAskEntity.class);
    verify(askRepository).save(captor.capture());
    assertEquals("TENANT-A", captor.getValue().getTenantId());
  }

  @Test
  void refusesWithoutKundali() {
    when(kundaliRepository.findByIdAndTenantId(99L, "TENANT-A")).thenReturn(Optional.empty());

    ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class,
            () -> aiAskService.ask(new AskRequest(99L, "Anything?", "general")));
    assertEquals(404, ex.getStatusCode().value());
    assertTrue(ex.getReason().toLowerCase().contains("kundali"));
  }

  @Test
  void refusesWhenKundaliIdMissing() {
    ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class,
            () -> aiAskService.ask(new AskRequest(null, "Anything?", null)));
    assertEquals(400, ex.getStatusCode().value());
  }

  private static KundaliSnapshotEntity sampleSnap(long id) {
    KundaliSnapshotEntity snap = new KundaliSnapshotEntity();
    snap.setId(id);
    snap.setTenantId("TENANT-A");
    snap.setDisplayName("Demo Native");
    snap.setBirthDate(LocalDate.of(1990, 5, 15));
    snap.setPlaceName("Patna");
    snap.setAyanamsaCode("LAHIRI");
    snap.setHouseSystem("WHOLE_SIGN");
    snap.setCalculationEngineVersion("V1.5");
    snap.setAscendantLongitude(BigDecimal.valueOf(12.5));
    return snap;
  }

  private static PlanetaryPositionEntity samplePlanet(String code, String sign, short house) {
    PlanetaryPositionEntity p = new PlanetaryPositionEntity();
    p.setPlanetCode(code);
    p.setSignName(sign);
    p.setDegreeInSign(BigDecimal.valueOf(10.0));
    p.setHouse(house);
    p.setNakshatraName("Purva Ashadha");
    p.setPada((short) 2);
    p.setRetrograde(false);
    return p;
  }

  private static HousePositionEntity sampleHouse(short house, String sign) {
    HousePositionEntity h = new HousePositionEntity();
    h.setHouse(house);
    h.setSignName(sign);
    h.setSignIndex((short) 0);
    h.setCuspLongitudeDeg(BigDecimal.ZERO);
    return h;
  }
}
