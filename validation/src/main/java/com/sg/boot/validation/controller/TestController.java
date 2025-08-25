package com.sg.boot.validation.controller;

import com.sg.boot.validation.param.GetParam;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RequestMapping("test")
@RestController
public class TestController {

  @GetMapping
  public void get(@Valid final GetParam param) {
  }
}
