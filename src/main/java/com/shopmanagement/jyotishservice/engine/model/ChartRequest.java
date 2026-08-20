package com.shopmanagement.jyotishservice.engine.model;

import java.util.Objects;

public final class ChartRequest {

  private final BirthMoment birth;
  private final AyanamsaMode ayanamsa;
  private final boolean birthTimeUnknown;

  public ChartRequest(BirthMoment birth, AyanamsaMode ayanamsa, boolean birthTimeUnknown) {
    this.birth = Objects.requireNonNull(birth, "birth");
    this.ayanamsa = ayanamsa != null ? ayanamsa : AyanamsaMode.LAHIRI;
    this.birthTimeUnknown = birthTimeUnknown;
  }

  public BirthMoment birth() {
    return birth;
  }

  public AyanamsaMode ayanamsa() {
    return ayanamsa;
  }

  public boolean birthTimeUnknown() {
    return birthTimeUnknown;
  }
}
