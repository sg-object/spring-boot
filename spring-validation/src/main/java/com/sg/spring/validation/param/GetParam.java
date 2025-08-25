package com.sg.spring.validation.param;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;
import java.util.List;

@RequiredArgsConstructor
@Getter
public class GetParam {

  @Length(min = 1, max = 10)
  @NotBlank
  private final String name;

  @Max(100)
  @Min(1)
  @NotNull
  private final Integer age;

  @Size(min = 1, max = 10)
  @NotNull
  private final List<@NotBlank String> names;
}
