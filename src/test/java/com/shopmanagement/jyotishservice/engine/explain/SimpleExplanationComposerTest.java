package com.shopmanagement.jyotishservice.engine.explain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.shopmanagement.jyotishservice.api.SimpleOverviewApi.ExplainedBlock;

class SimpleExplanationComposerTest {

  @Test
  void missingLords_returnsUnavailable() {
    ExplainedBlock block =
        SimpleExplanationComposer.explainDashaPeriod(null, null, null, null, null, null, "VIMSHOTTARI");
    assertTrue(block.calculationNotAvailable());
    assertFalse(block.paragraphsEn().isEmpty());
  }

  @Test
  void rahuAntar_usesQualifiedLanguageAndFacts() {
    Instant start = Instant.parse("2024-01-01T00:00:00Z");
    Instant end = Instant.parse("2026-06-01T00:00:00Z");
    ExplainedBlock block =
        SimpleExplanationComposer.explainDashaPeriod(
            "RAHU",
            "Rahu",
            "RAHU",
            "Rahu",
            start,
            end,
            "VIMSHOTTARI",
            new SimpleExplanationComposer.LordPlacement("Aquarius", 11, "Shatabhisha"),
            new SimpleExplanationComposer.LordPlacement("Aquarius", 11, "Shatabhisha"));
    assertFalse(block.calculationNotAvailable());
    assertTrue(block.paragraphsEn().size() >= 3 && block.paragraphsEn().size() <= 5);
    assertTrue(block.paragraphsHi().size() >= 3);
    String joined = String.join(" ", block.paragraphsEn()).toLowerCase();
    assertTrue(joined.contains("may be considered") || joined.contains("traditional"));
    assertFalse(joined.contains("will definitely"));
    assertTrue(block.whyFacts().stream().anyMatch(f -> "MAHA".equals(f.code())));
    assertTrue(block.whyFacts().stream().anyMatch(f -> "START".equals(f.code())));
    assertTrue(block.whyFacts().stream().anyMatch(f -> "MAHA_PLACE".equals(f.code())));
  }
}
