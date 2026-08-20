package com.shopmanagement.jyotishservice.ai;

import java.util.Map;

/**
 * Pluggable LLM bridge (Phase 10). Default {@link HeuristicLlmProvider}; optional {@link
 * HttpLlmProvider} when {@code jyotish.ai.provider=HTTP}.
 *
 * <p>Providers must never invent planetary positions or dasha dates — callers pass only verified
 * engine/DB context, and heuristics reformulate that context.
 */
public interface LlmProvider {

  String code();

  /**
   * Optional remote completion. Empty / missing {@code body} means callers keep local heuristic
   * text built from verified context.
   */
  Map<String, Object> complete(String task, String language, Map<String, Object> context);
}
