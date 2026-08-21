package com.shopmanagement.jyotishservice.service.report;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.shopmanagement.jyotishservice.persistence.entity.DashaPeriodEntity;
import com.shopmanagement.jyotishservice.persistence.entity.KundaliSnapshotEntity;
import com.shopmanagement.jyotishservice.persistence.entity.MatchingKootaScoreEntity;
import com.shopmanagement.jyotishservice.persistence.entity.MatchingSessionEntity;
import com.shopmanagement.jyotishservice.persistence.entity.PlanetaryPositionEntity;
import com.shopmanagement.jyotishservice.persistence.entity.TransitPlanetPositionEntity;
import com.shopmanagement.jyotishservice.persistence.entity.TransitSnapshotEntity;

/**
 * OpenPDF renderer for stored kundali / matching snapshots. No planetary math — report only.
 */
@Component
public class ReportPdfRenderer {

  public static final String DISCLAIMER =
      "Traditional Vedic indicators for reflection with a qualified astrologer. "
          + "Not fatalistic predictions of destiny, marriage outcome, or health.";

  private static final DateTimeFormatter DATE = DateTimeFormatter.ISO_LOCAL_DATE;
  private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");
  private static final DateTimeFormatter INSTANT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm 'UTC'").withZone(ZoneOffset.UTC);

  public byte[] renderBasicKundali(
      KundaliSnapshotEntity snap,
      List<PlanetaryPositionEntity> planets,
      List<DashaPeriodEntity> mahaPeriods,
      TransitSnapshotEntity transit,
      List<TransitPlanetPositionEntity> transitPlanets) {
    return renderBasicKundali(snap, planets, mahaPeriods, transit, transitPlanets, List.of());
  }

  public byte[] renderBasicKundali(
      KundaliSnapshotEntity snap,
      List<PlanetaryPositionEntity> planets,
      List<DashaPeriodEntity> mahaPeriods,
      TransitSnapshotEntity transit,
      List<TransitPlanetPositionEntity> transitPlanets,
      List<VargaChartFrame> vargaFrames) {
    return renderNamed(
        "Sugam Jyotish — Kundli Pack",
        snap,
        planets,
        mahaPeriods,
        transit,
        transitPlanets,
        vargaFrames);
  }

  public byte[] renderDashaSummary(
      KundaliSnapshotEntity snap, List<DashaPeriodEntity> mahaPeriods) {
    return renderNamed(
        "Sugam Jyotish — Dasha Summary",
        snap,
        List.of(),
        mahaPeriods,
        null,
        List.of(),
        List.of());
  }

  public byte[] renderTransit(
      KundaliSnapshotEntity snap,
      TransitSnapshotEntity transit,
      List<TransitPlanetPositionEntity> transitPlanets) {
    return renderNamed(
        "Sugam Jyotish — Transit Report",
        snap,
        List.of(),
        List.of(),
        transit,
        transitPlanets,
        List.of());
  }

  public byte[] renderMatching(
      MatchingSessionEntity session, List<MatchingKootaScoreEntity> kootas) {
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      Document doc = new Document();
      PdfWriter.getInstance(doc, out);
      doc.open();

      Font title = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
      Font heading = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
      Font bold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
      Font normal = FontFactory.getFont(FontFactory.HELVETICA, 10);
      Font small = FontFactory.getFont(FontFactory.HELVETICA, 8);

      doc.add(new Paragraph("Sugam Jyotish — Matching Report", title));
      doc.add(new Paragraph(" ", normal));
      doc.add(
          new Paragraph(
              session.getDisplayNameA() + "  ×  " + session.getDisplayNameB(), heading));
      doc.add(
          new Paragraph(
              "Total: "
                  + session.getTotalScore()
                  + " / "
                  + session.getMaxScore()
                  + " ("
                  + fmt(session.getPercentage())
                  + "%)",
              bold));
      doc.add(new Paragraph(nullSafe(session.getSummary()), normal));
      doc.add(
          new Paragraph(
              "Engine: "
                  + nullSafe(session.getCalculationEngineVersion())
                  + " · Session #"
                  + session.getId()
                  + " · Generated "
                  + INSTANT.format(Instant.now()),
              normal));
      doc.add(new Paragraph(" ", normal));

      doc.add(new Paragraph("Ashta Koota", heading));
      PdfPTable table = new PdfPTable(3);
      table.setWidthPercentage(100);
      table.setWidths(new float[] {1.4f, 0.8f, 3.2f});
      table.addCell(header("Koota", bold));
      table.addCell(header("Score", bold));
      table.addCell(header("Notes", bold));
      for (MatchingKootaScoreEntity k : kootas) {
        table.addCell(cell(k.getDisplayName(), normal));
        table.addCell(cell(k.getObtained() + " / " + k.getMaxPoints(), normal));
        table.addCell(cell(k.getExplanation(), small));
      }
      doc.add(table);

      doc.add(new Paragraph(" ", normal));
      doc.add(new Paragraph("Manglik comparison", heading));
      doc.add(
          new Paragraph(
              session.getDisplayNameA()
                  + ": "
                  + session.getManglikStatusA()
                  + " (Mars house "
                  + session.getManglikMarsHouseA()
                  + ")",
              normal));
      doc.add(
          new Paragraph(
              session.getDisplayNameB()
                  + ": "
                  + session.getManglikStatusB()
                  + " (Mars house "
                  + session.getManglikMarsHouseB()
                  + ")",
              normal));
      if (session.getNotes() != null && !session.getNotes().isBlank()) {
        doc.add(new Paragraph(session.getNotes(), small));
      }

      doc.add(new Paragraph(" ", normal));
      doc.add(new Paragraph("Disclaimer", bold));
      String disclaimer =
          session.getDisclaimer() != null && !session.getDisclaimer().isBlank()
              ? session.getDisclaimer()
              : DISCLAIMER;
      doc.add(new Paragraph(disclaimer, small));
      doc.close();
      return out.toByteArray();
    } catch (DocumentException ex) {
      throw new IllegalStateException("PDF generation failed", ex);
    }
  }

  private byte[] renderNamed(
      String docTitle,
      KundaliSnapshotEntity snap,
      List<PlanetaryPositionEntity> planets,
      List<DashaPeriodEntity> mahaPeriods,
      TransitSnapshotEntity transit,
      List<TransitPlanetPositionEntity> transitPlanets,
      List<VargaChartFrame> vargaFrames) {
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      Document doc = new Document();
      PdfWriter writer = PdfWriter.getInstance(doc, out);
      doc.open();

      Font title = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
      Font heading = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
      Font bold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
      Font normal = FontFactory.getFont(FontFactory.HELVETICA, 9);
      Font small = FontFactory.getFont(FontFactory.HELVETICA, 8);
      Font chartFont = FontFactory.getFont(FontFactory.HELVETICA, 7);

      doc.add(new Paragraph(docTitle, title));
      doc.add(new Paragraph(" ", normal));
      doc.add(new Paragraph(nullSafe(snap.getDisplayName()), heading));
      doc.add(
          new Paragraph(
              "Birth: "
                  + DATE.format(snap.getBirthDate())
                  + timePart(snap.getBirthTime(), snap.isBirthTimeUnknown())
                  + " · "
                  + nullSafe(snap.getTimeZone()),
              normal));
      doc.add(
          new Paragraph(
              "Place: "
                  + nullSafe(snap.getPlaceName())
                  + " ("
                  + fmt(snap.getLatitude())
                  + ", "
                  + fmt(snap.getLongitude())
                  + ")",
              normal));
      doc.add(
          new Paragraph(
              "Ayanamsa: "
                  + nullSafe(snap.getAyanamsaCode())
                  + " "
                  + fmt(snap.getAyanamsaDeg())
                  + "° · Houses: "
                  + nullSafe(snap.getHouseSystem())
                  + " · Zodiac: "
                  + nullSafe(snap.getZodiacSystem()),
              normal));
      doc.add(
          new Paragraph(
              "Engine: "
                  + nullSafe(snap.getCalculationEngineVersion())
                  + " · Snapshot #"
                  + snap.getId()
                  + " · Generated "
                  + INSTANT.format(Instant.now()),
              bold));
      doc.add(new Paragraph(" ", normal));

      if (planets != null && !planets.isEmpty()) {
        doc.add(new Paragraph("Nirayana Graha Spashta (D1)", heading));
        PdfPTable table = new PdfPTable(8);
        table.setWidthPercentage(100);
        table.setWidths(new float[] {1.3f, 1.2f, 1.3f, 1.0f, 0.7f, 1.5f, 0.5f, 0.6f});
        table.addCell(header("Planet", bold));
        table.addCell(header("Sign", bold));
        table.addCell(header("D:M:S", bold));
        table.addCell(header("Motion", bold));
        table.addCell(header("H", bold));
        table.addCell(header("Nakshatra", bold));
        table.addCell(header("Pada", bold));
        table.addCell(header("R", bold));

        table.addCell(cell("Lagna", normal));
        table.addCell(cell(signName(snap.getAscendantSignIndex()), normal));
        table.addCell(cell(fmtDms(degreeInSign(snap.getAscendantLongitude())), normal));
        table.addCell(cell("—", normal));
        table.addCell(cell("1", normal));
        table.addCell(cell("—", normal));
        table.addCell(cell("—", normal));
        table.addCell(cell("", normal));

        for (PlanetaryPositionEntity p : planets) {
          if ("ASCENDANT".equalsIgnoreCase(p.getPlanetCode())) {
            continue;
          }
          table.addCell(cell(p.getPlanetCode(), normal));
          table.addCell(cell(p.getSignName(), normal));
          table.addCell(cell(fmtDms(p.getDegreeInSign()), normal));
          table.addCell(cell(p.isRetrograde() ? "Retrograde" : "Direct", normal));
          table.addCell(cell(String.valueOf(p.getHouse()), normal));
          table.addCell(cell(nullSafe(p.getNakshatraName()), normal));
          table.addCell(cell(String.valueOf(p.getPada()), normal));
          table.addCell(cell(p.isRetrograde() ? "R" : "", normal));
        }
        doc.add(table);

        // Page 2: Lagna + Chandra charts
        doc.newPage();
        doc.add(new Paragraph(nullSafe(snap.getDisplayName()) + " — Charts", heading));
        doc.add(new Paragraph(" ", small));
        Map<String, Integer> natal = NorthIndianPdfDrawer.natalHouses(planets);
        natal.put("ASCENDANT", 1);
        int lagnaSign = snap.getAscendantSignIndex();
        float pageW = doc.getPageSize().getWidth();
        float margin = 40f;
        float chartSize = Math.min(220f, (pageW - 3 * margin) / 2f);

        NorthIndianPdfDrawer.draw(
            doc,
            writer,
            chartFont,
            margin,
            doc.getPageSize().getHeight() - margin - chartSize - 30,
            chartSize,
            "Lagna Kundli",
            lagnaSign,
            natal);

        Integer moonHouse = natal.get("MOON");
        Integer moonSign = null;
        for (PlanetaryPositionEntity p : planets) {
          if ("MOON".equalsIgnoreCase(p.getPlanetCode())) {
            moonSign = (int) p.getSignIndex();
            break;
          }
        }
        if (moonHouse != null && moonSign != null) {
          Map<String, Integer> chandra = NorthIndianPdfDrawer.rehouse(natal, moonHouse);
          chandra.put("ASCENDANT", 1);
          NorthIndianPdfDrawer.draw(
              doc,
              writer,
              chartFont,
              margin + chartSize + margin,
              doc.getPageSize().getHeight() - margin - chartSize - 30,
              chartSize,
              "Chandra Kundli",
              moonSign,
              chandra);
        }

        Integer sunHouse = natal.get("SUN");
        Integer sunSign = null;
        for (PlanetaryPositionEntity p : planets) {
          if ("SUN".equalsIgnoreCase(p.getPlanetCode())) {
            sunSign = (int) p.getSignIndex();
            break;
          }
        }
        if (sunHouse != null && sunSign != null) {
          Map<String, Integer> surya = NorthIndianPdfDrawer.rehouse(natal, sunHouse);
          surya.put("ASCENDANT", 1);
          NorthIndianPdfDrawer.draw(
              doc,
              writer,
              chartFont,
              margin,
              doc.getPageSize().getHeight() - margin - 2 * chartSize - 70,
              chartSize,
              "Surya Kundli",
              sunSign,
              surya);
        }

        doc.add(new Paragraph(" ", normal));
        doc.add(
            new Paragraph(
                "Sign numbers 1–12 = Aries–Pisces. Charts use whole-sign houses (engine default).",
                small));
      }

      if (vargaFrames != null && !vargaFrames.isEmpty()) {
        doc.newPage();
        doc.add(new Paragraph("Shodashavarga — chart grid", heading));
        doc.add(new Paragraph(" ", small));
        float pageW = doc.getPageSize().getWidth();
        float pageH = doc.getPageSize().getHeight();
        float margin = 36f;
        float gap = 18f;
        float chartSize = Math.min(230f, (pageW - 2 * margin - gap) / 2f);
        int i = 0;
        for (VargaChartFrame frame : vargaFrames) {
          if (i > 0 && i % 4 == 0) {
            doc.newPage();
            doc.add(new Paragraph("Shodashavarga — continued", heading));
            doc.add(new Paragraph(" ", small));
          }
          int slot = i % 4;
          float col = slot % 2;
          float row = slot / 2;
          float x = margin + col * (chartSize + gap);
          float y = pageH - margin - 40 - (row + 1) * (chartSize + 28);
          if (i >= 4 && slot < 4) {
            // y already relative to top of continued page
          }
          NorthIndianPdfDrawer.draw(
              doc,
              writer,
              chartFont,
              x,
              y,
              chartSize,
              frame.title(),
              frame.lagnaSignIndex(),
              frame.houseOfPlanet());
          i++;
        }
        doc.add(new Paragraph(" ", normal));
        doc.add(
            new Paragraph(
                "Vargas projected from D1 longitudes (Parashara). Full set available in Charts tab.",
                small));
      }

      if (mahaPeriods != null && !mahaPeriods.isEmpty()) {
        doc.newPage();
        doc.add(new Paragraph("Vimshottari Mahadasha summary", heading));
        PdfPTable dasha = new PdfPTable(3);
        dasha.setWidthPercentage(100);
        dasha.addCell(header("Lord", bold));
        dasha.addCell(header("Start (UTC)", bold));
        dasha.addCell(header("End (UTC)", bold));
        Instant now = Instant.now();
        int shown = 0;
        for (DashaPeriodEntity row : mahaPeriods) {
          if (!"MAHA".equalsIgnoreCase(row.getLevelCode())) {
            continue;
          }
          boolean current =
              !now.isBefore(row.getStartAt()) && now.isBefore(row.getEndAt());
          String lord = row.getLordCode() + (current ? " (current)" : "");
          dasha.addCell(cell(lord, normal));
          dasha.addCell(cell(INSTANT.format(row.getStartAt()), normal));
          dasha.addCell(cell(INSTANT.format(row.getEndAt()), normal));
          if (++shown >= 12) {
            break;
          }
        }
        if (shown > 0) {
          doc.add(dasha);
        } else {
          doc.add(new Paragraph("No Mahadasha rows stored for this snapshot.", normal));
        }
      }

      if (transit != null && transitPlanets != null && !transitPlanets.isEmpty()) {
        doc.add(new Paragraph(" ", normal));
        doc.add(
            new Paragraph(
                "Gochar transit · "
                    + DATE.format(transit.getTransitDate())
                    + (transit.getTransitTime() != null
                        ? " " + TIME.format(transit.getTransitTime())
                        : ""),
                heading));
        PdfPTable ttable = new PdfPTable(4);
        ttable.setWidthPercentage(100);
        ttable.addCell(header("Planet", bold));
        ttable.addCell(header("Transit sign", bold));
        ttable.addCell(header("House", bold));
        ttable.addCell(header("Nakshatra", bold));
        for (TransitPlanetPositionEntity tp : transitPlanets) {
          ttable.addCell(cell(tp.getPlanetCode(), normal));
          ttable.addCell(cell(tp.getSignName(), normal));
          ttable.addCell(cell(String.valueOf(tp.getHouse()), normal));
          ttable.addCell(cell(tp.getNakshatraName(), normal));
        }
        doc.add(ttable);
      }

      doc.add(new Paragraph(" ", normal));
      doc.add(new Paragraph("Sugam Jyotish · Professional desk report", bold));
      doc.add(new Paragraph(DISCLAIMER, small));
      doc.close();
      return out.toByteArray();
    } catch (DocumentException ex) {
      throw new IllegalStateException("PDF generation failed", ex);
    }
  }

  private static String fmtDms(BigDecimal degInSign) {
    if (degInSign == null) {
      return "—";
    }
    double n = degInSign.doubleValue();
    int d = (int) Math.floor(n);
    double mf = (n - d) * 60.0;
    int m = (int) Math.floor(mf);
    int s = (int) Math.floor((mf - m) * 60.0);
    return String.format("%02d:%02d:%02d", d, m, s);
  }

  private static String timePart(LocalTime time, boolean unknown) {
    if (unknown) {
      return " · time unknown";
    }
    if (time == null) {
      return "";
    }
    return " · " + TIME.format(time);
  }

  private static String fmt(BigDecimal v) {
    if (v == null) {
      return "—";
    }
    return v.stripTrailingZeros().toPlainString();
  }

  private static String nullSafe(String v) {
    return v == null || v.isBlank() ? "—" : v;
  }

  private static BigDecimal degreeInSign(BigDecimal longitude) {
    if (longitude == null) {
      return BigDecimal.ZERO;
    }
    double lon = longitude.doubleValue() % 360.0;
    if (lon < 0) {
      lon += 360.0;
    }
    return BigDecimal.valueOf(lon % 30.0).setScale(2, java.math.RoundingMode.HALF_UP);
  }

  private static String signName(short signIndex) {
    String[] names = {
      "Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo",
      "Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces"
    };
    int i = signIndex;
    if (i < 0 || i >= names.length) {
      return "Sign " + i;
    }
    return names[i];
  }

  private static PdfPCell header(String text, Font font) {
    PdfPCell cell = new PdfPCell(new Phrase(text, font));
    cell.setPadding(4);
    return cell;
  }

  private static PdfPCell cell(String text, Font font) {
    PdfPCell cell = new PdfPCell(new Phrase(text == null ? "" : text, font));
    cell.setPadding(3);
    return cell;
  }
}
