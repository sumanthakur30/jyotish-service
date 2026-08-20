package com.shopmanagement.jyotishservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class JyotishServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(JyotishServiceApplication.class, args);
  }
}
