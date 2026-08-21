package com.shopmanagement.jyotishservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.shopmanagement.jyotishservice.engine.CalculationEngine;
import com.shopmanagement.jyotishservice.engine.ephemeris.EphemerisProvider;
import com.shopmanagement.jyotishservice.engine.ephemeris.EphemerisProviders;

@Configuration
public class JyotishEngineConfig {

  @Bean
  public EphemerisProvider ephemerisProvider(JyotishEphemerisProperties ephemerisProperties) {
    return EphemerisProviders.create(ephemerisProperties);
  }

  @Bean
  public CalculationEngine calculationEngine(EphemerisProvider ephemerisProvider) {
    return new CalculationEngine(ephemerisProvider);
  }
}
