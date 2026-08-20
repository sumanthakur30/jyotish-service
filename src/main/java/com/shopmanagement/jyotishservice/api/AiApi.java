package com.shopmanagement.jyotishservice.api;

import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class AiApi {

  private AiApi() {}

  public record AskRequest(
      @NotNull Long kundaliId,
      @NotBlank @Size(max = 2000) String question,
      @Size(max = 64) String topic) {}

  public record AskResponse(
      Long kundaliId,
      String topic,
      String question,
      String answer,
      List<String> findings,
      Map<String, Object> contextUsed,
      boolean aiGenerated,
      String providerCode,
      String disclaimer,
      Long askId) {}
}
