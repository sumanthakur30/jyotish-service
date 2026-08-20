package com.shopmanagement.jyotishservice.ai;

import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.shopmanagement.jyotishservice.integration.AiHttpClient;

/** HTTP LLM bridge when {@code jyotish.ai.provider=HTTP}. */
@Component
@ConditionalOnProperty(name = "jyotish.ai.provider", havingValue = "HTTP")
public class HttpLlmProvider implements LlmProvider {

  private final AiHttpClient aiHttpClient;

  public HttpLlmProvider(AiHttpClient aiHttpClient) {
    this.aiHttpClient = aiHttpClient;
  }

  @Override
  public String code() {
    return "HTTP";
  }

  @Override
  public Map<String, Object> complete(String task, String language, Map<String, Object> context) {
    return aiHttpClient.complete(task, language, context);
  }
}
