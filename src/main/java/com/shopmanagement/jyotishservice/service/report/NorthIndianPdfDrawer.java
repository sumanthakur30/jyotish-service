package com.shopmanagement.jyotishservice.service.report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import com.shopmanagement.jyotishservice.persistence.entity.PlanetaryPositionEntity;

/** Draws a North-Indian diamond chart into the current PDF page. */
final class NorthIndianPdfDrawer {

  private static final String[] SIGN_EN = {
    "Ar", "Ta", "Ge", "Cn", "Le", "Vi", "Li", "Sc", "Sg", "Cp", "Aq", "Pi"
  };

  private static final Map<String, String> PLANET_SHORT = Map.ofEntries(
      Map.entry("SUN", "Su"),
      Map.entry("MOON", "Mo"),
      Map.entry("MARS", "Ma"),
      Map.entry("MERCURY", "Me"),
      Map.entry("JUPITER", "Ju"),
      Map.entry("VENUS", "Ve"),
      Map.entry("SATURN", "Sa"),
      Map.entry("RAHU", "Ra"),
      Map.entry("KETU", "Ke"),
      Map.entry("ASCENDANT", "As"));

  private NorthIndianPdfDrawer() {}

  /**
   * @param lagnaSignIndex 0–11 for house 1
   * @param houseOfPlanet map planetCode → house 1–12 (already oriented for this chart)
   */
  static void draw(
      Document doc,
      PdfWriter writer,
      Font font,
      float left,
      float bottom,
      float size,
      String title,
      int lagnaSignIndex,
      Map<String, Integer> houseOfPlanet)
      throws DocumentException {
    PdfContentByte cb = writer.getDirectContent();
    float right = left + size;
    float top = bottom + size;
    float midX = (left + right) / 2f;
    float midY = (bottom + top) / 2f;

    cb.saveState();
    cb.setLineWidth(1f);
    cb.rectangle(left, bottom, size, size);
    cb.moveTo(left, bottom);
    cb.lineTo(right, top);
    cb.moveTo(left, top);
    cb.lineTo(right, bottom);
    cb.moveTo(midX, bottom);
    cb.lineTo(midX, top);
    cb.moveTo(left, midY);
    cb.lineTo(right, midY);
    cb.stroke();
    cb.restoreState();

    ColumnText.showTextAligned(
        cb, com.lowagie.text.Element.ALIGN_CENTER, new Phrase(title, font), midX, top + 8, 0);

    // House cell centers (approximate safe zones) for NI layout
    float[][] centers = houseCenters(left, bottom, size);
    for (int h = 1; h <= 12; h++) {
      int sign = Math.floorMod(lagnaSignIndex + h - 1, 12);
      float x = centers[h - 1][0];
      float y = centers[h - 1][1];
      ColumnText.showTextAligned(
          cb,
          com.lowagie.text.Element.ALIGN_CENTER,
          new Phrase(String.valueOf(sign + 1), font),
          x,
          y + 8,
          0);
      List<String> labels = new ArrayList<>();
      for (Map.Entry<String, Integer> e : houseOfPlanet.entrySet()) {
        if (e.getValue() != null && e.getValue() == h) {
          labels.add(PLANET_SHORT.getOrDefault(e.getKey(), e.getKey().substring(0, 2)));
        }
      }
      if (!labels.isEmpty()) {
        ColumnText.showTextAligned(
            cb,
            com.lowagie.text.Element.ALIGN_CENTER,
            new Phrase(String.join(" ", labels), font),
            x,
            y - 4,
            0);
      }
    }
  }

  static Map<String, Integer> natalHouses(List<PlanetaryPositionEntity> planets) {
    Map<String, Integer> m = new HashMap<>();
    for (PlanetaryPositionEntity p : planets) {
      if (p.getPlanetCode() != null) {
        m.put(p.getPlanetCode(), (int) p.getHouse());
      }
    }
    return m;
  }

  /** Re-house so pivot natal house becomes 1. */
  static Map<String, Integer> rehouse(Map<String, Integer> natal, int pivotHouse) {
    Map<String, Integer> m = new HashMap<>();
    for (Map.Entry<String, Integer> e : natal.entrySet()) {
      int h = e.getValue();
      m.put(e.getKey(), ((h - pivotHouse + 12) % 12) + 1);
    }
    return m;
  }

  static String signAbbrev(int signIndex) {
    return SIGN_EN[Math.floorMod(signIndex, 12)];
  }

  private static float[][] houseCenters(float left, float bottom, float size) {
    float q = size / 4f;
    // Order h1..h12 matching UI layout
    return new float[][] {
      {left + 2 * q, bottom + 3.2f * q}, // 1 top
      {left + 0.7f * q, bottom + 3.2f * q}, // 2
      {left + 0.7f * q, bottom + 2.2f * q}, // 3
      {left + 0.7f * q, bottom + 1.2f * q}, // 4
      {left + 0.7f * q, bottom + 0.35f * q}, // 5
      {left + 1.5f * q, bottom + 0.35f * q}, // 6
      {left + 2.5f * q, bottom + 0.35f * q}, // 7
      {left + 3.3f * q, bottom + 0.35f * q}, // 8
      {left + 3.3f * q, bottom + 1.2f * q}, // 9
      {left + 3.3f * q, bottom + 2.2f * q}, // 10
      {left + 3.3f * q, bottom + 3.2f * q}, // 11
      {left + 2.5f * q, bottom + 3.2f * q}, // 12
    };
  }
}
