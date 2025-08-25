package com.sg.boot.validation.param;

import com.sg.boot.validation.annotation.File;
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
