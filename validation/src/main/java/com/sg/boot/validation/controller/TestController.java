package com.sg.boot.validation.controller;

import com.sg.boot.validation.param.GetParam;
import com.sg.boot.validation.param.PostParam;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RequestMapping("test")
@RestController
public class TestController {

  @GetMapping
  public void get(@Valid final GetParam param) {
  }

  @PostMapping("json")
  public void post(@RequestBody @Valid final PostParam param) {
  }
}
