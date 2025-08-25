package com.sg.boot.validation.property;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import java.util.List;

@ConfigurationProperties("test")
@RequiredArgsConstructor
@Validated
@Getter
public class TestProperties {

  @NotBlank
  private final String name;

  @PositiveOrZero
  @NotNull
  private final Integer age;

  @Size(min = 1, max = 10)
  @NotNull
  private final List<@NotBlank String> names;
}
