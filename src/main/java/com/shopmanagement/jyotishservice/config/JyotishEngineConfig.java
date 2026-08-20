package com.shopmanagement.jyotishservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.shopmanagement.jyotishservice.engine.CalculationEngine;

@Configuration
public class JyotishEngineConfig {

  @Bean
  public CalculationEngine calculationEngine() {
    return new CalculationEngine();
  }
}
