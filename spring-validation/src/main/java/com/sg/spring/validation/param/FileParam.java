package com.sg.spring.validation.param;

import com.sg.spring.validation.annotation.File;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Getter
public class FileParam {

  @File
  private final MultipartFile file1;

  @File(required = false)
  private final MultipartFile file2;

  @File(extensions = {"txt"})
  private final MultipartFile file3;
}
