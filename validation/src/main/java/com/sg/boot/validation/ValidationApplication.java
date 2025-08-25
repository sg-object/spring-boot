package com.sg.boot.validation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class ValidationApplication {

  public static void main(String[] args) {
    SpringApplication.run(ValidationApplication.class, args);
  }
}
