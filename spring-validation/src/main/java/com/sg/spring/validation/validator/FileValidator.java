package com.sg.spring.validation.validator;

import com.sg.spring.validation.annotation.File;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

public class FileValidator implements ConstraintValidator<File, MultipartFile> {

  private String[] extensions;

  private boolean required;

  @Override
  public void initialize(File constraintAnnotation) {
    this.extensions = constraintAnnotation.extensions();
    this.required = constraintAnnotation.required();
  }

  @Override
  public boolean isValid(MultipartFile value, ConstraintValidatorContext context) {
    if (value != null && !value.isEmpty()) {
      if (this.extensions.length > 0) {
        final var fileExtension =
                StringUtils.getFilenameExtension(value.getOriginalFilename()).toLowerCase();
        for (final var extension : this.extensions) {
          if (extension.equals(fileExtension)) {
            return true;
          }
        }
        return false;
      } else {
        return true;
      }
    }
    return !this.required;
  }
}
