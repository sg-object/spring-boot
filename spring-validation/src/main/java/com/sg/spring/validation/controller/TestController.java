package com.sg.spring.validation.controller;

import com.sg.spring.validation.param.FileParam;
import com.sg.spring.validation.param.GetParam;
import com.sg.spring.validation.param.PostParam;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RequestMapping("test")
@RestController
public class TestController {

  @GetMapping
  public void get(@Valid final GetParam param) {
  }

  @PostMapping
  public void post(@RequestBody @Valid final PostParam param) {
  }

  @PostMapping(value = "file", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
  public void file(@Valid final FileParam param) {
  }
}
