package com.petadoption.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class FileControllerTest {
  @TempDir
  static Path storageRoot;

  @Autowired MockMvc mockMvc;

  @DynamicPropertySource
  static void configureStorageRoot(DynamicPropertyRegistry registry) {
    registry.add("file.storage.root", () -> storageRoot.toString());
  }

  @Test
  void uploadNonEmptyFileReturnsMetadata() throws Exception {
    MockMultipartFile file = new MockMultipartFile(
        "file",
        "cat-photo.png",
        MediaType.IMAGE_PNG_VALUE,
        "image-content".getBytes());

    mockMvc.perform(multipart("/api/v1/files").file(file))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.id").isNotEmpty())
        .andExpect(jsonPath("$.data.originalName").value("cat-photo.png"))
        .andExpect(jsonPath("$.data.contentType").value(MediaType.IMAGE_PNG_VALUE));
  }

  @Test
  void uploadWritesFileContentToDisk() throws Exception {
    byte[] content = "adoption-file-content".getBytes();
    MockMultipartFile file = new MockMultipartFile(
        "file",
        "../unsafe.txt",
        MediaType.TEXT_PLAIN_VALUE,
        content);

    MvcResult result = mockMvc.perform(multipart("/api/v1/files").file(file))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.storagePath", not(startsWith("../"))))
        .andReturn();

    String storagePath = JsonPath.read(result.getResponse().getContentAsString(), "$.data.storagePath");
    Path savedPath = storageRoot.resolve(storagePath).normalize();
    assertThat(savedPath).startsWith(storageRoot);
    assertThat(Files.readAllBytes(savedPath)).isEqualTo(content);
  }

  @Test
  void getUploadedFileMetadata() throws Exception {
    MockMultipartFile file = new MockMultipartFile(
        "file",
        "medical.pdf",
        MediaType.APPLICATION_PDF_VALUE,
        "pdf-content".getBytes());

    MvcResult uploadResult = mockMvc.perform(multipart("/api/v1/files").file(file))
        .andExpect(status().isOk())
        .andReturn();
    String id = JsonPath.read(uploadResult.getResponse().getContentAsString(), "$.data.id");

    mockMvc.perform(get("/api/v1/files/{id}", id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.id").value(id))
        .andExpect(jsonPath("$.data.originalName").value("medical.pdf"))
        .andExpect(jsonPath("$.data.contentType").value(MediaType.APPLICATION_PDF_VALUE));
  }

  @Test
  void uploadEmptyFileReturnsBadRequest() throws Exception {
    MockMultipartFile file = new MockMultipartFile(
        "file",
        "empty.txt",
        MediaType.TEXT_PLAIN_VALUE,
        new byte[0]);

    mockMvc.perform(multipart("/api/v1/files").file(file))
        .andExpect(status().isBadRequest());
  }

  @Test
  void uploadMissingFilePartReturnsBadRequest() throws Exception {
    mockMvc.perform(multipart("/api/v1/files"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false));
  }

  @Test
  void getMissingFileReturnsNotFound() throws Exception {
    mockMvc.perform(get("/api/v1/files/{id}", "11111111-1111-1111-1111-111111111111"))
        .andExpect(status().isNotFound());
  }

  @Test
  void getInvalidUuidReturnsBadRequest() throws Exception {
    mockMvc.perform(get("/api/v1/files/{id}", "not-a-uuid"))
        .andExpect(status().isBadRequest());
  }
}
