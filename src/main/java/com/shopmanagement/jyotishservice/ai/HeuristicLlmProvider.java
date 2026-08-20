package com.shopmanagement.jyotishservice.ai;

import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Default provider — no remote call; AiAskService keeps heuristic copy from verified context. */
@Component
@ConditionalOnProperty(name = "jyotish.ai.provider", havingValue = "HEURISTIC", matchIfMissing = true)
public class HeuristicLlmProvider implements LlmProvider {

  @Override
  public String code() {
    return "HEURISTIC";
  }

  @Override
  public Map<String, Object> complete(String task, String language, Map<String, Object> context) {
    return Map.of();
  }
}
