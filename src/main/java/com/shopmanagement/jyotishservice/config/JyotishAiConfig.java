package com.shopmanagement.jyotishservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class JyotishAiConfig {

  @Bean
  public RestTemplate jyotishAiRestTemplate() {
    return new RestTemplate();
  }
}
