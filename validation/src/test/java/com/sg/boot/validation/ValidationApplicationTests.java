package com.sg.boot.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class ValidationApplicationTests {

  @Autowired
  private MockMvc mvc;

  @DisplayName("GET Validation Test")
  @Test
  void testGet() throws Exception {
    this.mvc.perform(get("/test").param("name", "")).andExpect(status().isOk());
  }
}
