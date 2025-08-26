package com.sg.spring.validation.annotation;

import com.sg.spring.validation.validator.FileValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FileValidator.class)
public @interface File {

  String message() default "Invalid File";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  String[] extensions() default {};

  boolean required() default true;
}
