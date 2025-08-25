package com.sg.boot.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sg.boot.validation.param.PostParam;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class ValidationApplicationTests {

  @Autowired
  private MockMvc mvc;

  @Autowired
  private ObjectMapper objectMapper;

  @DisplayName("GET Validation Test")
  @Test
  void testGet() throws Exception {
    this.mvc.perform(get("/test").param("name", "")).andExpect(status().isOk());
  }

  @DisplayName("POST Validation Test")
  @Test
  void testPost() throws Exception {
    final var param = PostParam.builder().age(100).sub(PostParam.SubParam.builder().build()).build();
    final var body = this.objectMapper.writeValueAsString(param);
    this.mvc.perform(post("/test").contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isOk());
  }

  @DisplayName("Multipart(File) Validation Test")
  @Test
  void testFile() throws Exception {
    final var file3 = new MockMultipartFile("file3", "build.gradle", "txt", "this is test".getBytes());
    this.mvc.perform(multipart("/test/file").file(file3)).andExpect(status().isOk());
  }
}
