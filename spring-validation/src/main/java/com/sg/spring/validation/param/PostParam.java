package com.sg.spring.validation.param;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PostParam {

  @NotBlank
  private String name;

  @Max(100)
  @Positive
  private int age;

  @Valid
  @NotNull
  private SubParam sub;

  @Builder
  @Getter
  public static class SubParam {

    @Email
    @NotBlank
    private String email;
  }
}
