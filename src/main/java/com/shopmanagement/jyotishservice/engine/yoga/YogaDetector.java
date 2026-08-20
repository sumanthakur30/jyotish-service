package com.shopmanagement.jyotishservice.engine.yoga;

/** Pluggable yoga rule. Register in {@link YogaRegistry}; no Spring / UI imports. */
public interface YogaDetector {

  YogaCode code();

  YogaHit detect(YogaContext context);
}
