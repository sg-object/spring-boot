package com.sg.spring.validation.property;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("parent")
@RequiredArgsConstructor
@Validated
@Getter
public class NestedProperties {

  @Valid
  @NotNull
  private final Child child;

  @RequiredArgsConstructor
  @Getter
  public static class Child {

    @Length(min = 1, max = 10)
    @NotBlank
    private final String name;

    @Min(1)
    @Max(100)
    private final int age;
  }
}
