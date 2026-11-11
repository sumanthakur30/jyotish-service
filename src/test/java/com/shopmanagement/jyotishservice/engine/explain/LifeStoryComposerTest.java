package com.shopmanagement.jyotishservice.engine.explain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.shopmanagement.jyotishservice.api.LifeAnalysisApi.LifeStoryChapter;
import com.shopmanagement.jyotishservice.engine.life.LifeCategory;
import com.shopmanagement.jyotishservice.persistence.entity.DashaPeriodEntity;

class LifeStoryComposerTest {

  @Test
  void venusChapter_highlightsMarriageFinanceFamily_andMarksCurrent() {
    Instant start = Instant.parse("2009-08-06T00:00:00Z");
    Instant end = Instant.parse("2027-03-15T00:00:00Z");
    DashaPeriodEntity venus = maha("VENUS", start, end);
    Instant asOf = Instant.parse("2026-08-21T00:00:00Z");

    List<LifeStoryChapter> story = LifeStoryComposer.fromMaha(List.of(venus), asOf);
    assertEquals(1, story.size());
    LifeStoryChapter ch = story.get(0);
    assertTrue(ch.current());
    assertEquals(2009, ch.fromYear());
    assertEquals(2027, ch.toYear());
    assertTrue(ch.focusCategories().contains(LifeCategory.MARRIAGE.code()));
    assertTrue(ch.meaningHi().contains("शुक्र"));
    assertTrue(ch.focusLineEn().toLowerCase().contains("marriage"));
  }

  private static DashaPeriodEntity maha(String lord, Instant start, Instant end) {
    DashaPeriodEntity e = new DashaPeriodEntity();
    e.setLevelCode("MAHA");
    e.setLordCode(lord);
    e.setMahaLordCode(lord);
    e.setStartAt(start);
    e.setEndAt(end);
    return e;
  }
}
