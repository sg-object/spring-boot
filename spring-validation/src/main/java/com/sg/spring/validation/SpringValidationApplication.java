package com.sg.spring.validation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class SpringValidationApplication {

  public static void main(String[] args) {
    SpringApplication.run(SpringValidationApplication.class, args);
  }
}
