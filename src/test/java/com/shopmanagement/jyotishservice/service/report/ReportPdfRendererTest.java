package com.shopmanagement.jyotishservice.service.report;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.shopmanagement.jyotishservice.persistence.entity.KundaliSnapshotEntity;
import com.shopmanagement.jyotishservice.persistence.entity.MatchingKootaScoreEntity;
import com.shopmanagement.jyotishservice.persistence.entity.MatchingSessionEntity;
import com.shopmanagement.jyotishservice.persistence.entity.PlanetaryPositionEntity;

class ReportPdfRendererTest {

  private final ReportPdfRenderer renderer = new ReportPdfRenderer();

  @Test
  void basicKundaliProducesNonEmptyPdf() {
    KundaliSnapshotEntity snap = fixtureKundali();
    PlanetaryPositionEntity moon = new PlanetaryPositionEntity();
    moon.setPlanetCode("MOON");
    moon.setSignName("Taurus");
    moon.setSignIndex((short) 1);
    moon.setDegreeInSign(new BigDecimal("12.500000"));
    moon.setHouse((short) 2);
    moon.setNakshatraName("Rohini");
    moon.setNakshatraIndex((short) 3);
    moon.setPada((short) 2);
    moon.setRetrograde(false);
    moon.setCombust(false);
    moon.setLongitudeDeg(new BigDecimal("42.500000"));

    byte[] pdf = renderer.renderBasicKundali(snap, List.of(moon), List.of(), null, List.of());

    assertTrue(pdf.length > 200, "PDF should be non-empty");
    assertTrue(startsWithPdfHeader(pdf), "bytes should start with %PDF");
  }

  @Test
  void matchingProducesNonEmptyPdf() {
    MatchingSessionEntity session = new MatchingSessionEntity();
    session.setId(9L);
    session.setDisplayNameA("Asha");
    session.setDisplayNameB("Rohan");
    session.setTotalScore(24);
    session.setMaxScore(36);
    session.setPercentage(new BigDecimal("66.67"));
    session.setSummary("Traditional compatibility indicators suggest a moderate match.");
    session.setNotes("Manglik cancellations Coming Soon.");
    session.setDisclaimer(ReportPdfRenderer.DISCLAIMER);
    session.setManglikStatusA("ABSENT");
    session.setManglikStatusB("PRESENT");
    session.setManglikMarsHouseA((short) 3);
    session.setManglikMarsHouseB((short) 7);
    session.setCalculationEngineVersion("V1.5");

    MatchingKootaScoreEntity koota = new MatchingKootaScoreEntity();
    koota.setDisplayName("Nadi");
    koota.setObtained(0);
    koota.setMaxPoints(8);
    koota.setExplanation("Same Nadi — traditional indicator awards 0.");

    byte[] pdf = renderer.renderMatching(session, List.of(koota));

    assertTrue(pdf.length > 200, "PDF should be non-empty");
    assertTrue(startsWithPdfHeader(pdf), "bytes should start with %PDF");
  }

  private static KundaliSnapshotEntity fixtureKundali() {
    KundaliSnapshotEntity snap = new KundaliSnapshotEntity();
    snap.setId(1L);
    snap.setDisplayName("Demo Native");
    snap.setBirthDate(LocalDate.of(1990, 5, 15));
    snap.setBirthTime(LocalTime.of(10, 30));
    snap.setBirthTimeUnknown(false);
    snap.setTimeZone("Asia/Kolkata");
    snap.setPlaceName("Patna");
    snap.setLatitude(new BigDecimal("25.5941000"));
    snap.setLongitude(new BigDecimal("85.1376000"));
    snap.setAyanamsaCode("LAHIRI");
    snap.setAyanamsaDeg(new BigDecimal("23.850000"));
    snap.setZodiacSystem("SIDEREAL");
    snap.setHouseSystem("WHOLE_SIGN");
    snap.setCalculationEngineVersion("V1.5");
    snap.setAscendantLongitude(new BigDecimal("95.200000"));
    snap.setAscendantSignIndex((short) 3);
    return snap;
  }

  private static boolean startsWithPdfHeader(byte[] pdf) {
    return pdf.length >= 4
        && pdf[0] == '%'
        && pdf[1] == 'P'
        && pdf[2] == 'D'
        && pdf[3] == 'F';
  }
}
