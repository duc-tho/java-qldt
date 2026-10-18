package com.nhom11.qldt.feature.group.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class StudentGroupCreateTemplateTest {
  @Test
  void createFormUsesThePeriodIdProvidedByTheController() throws IOException {
    ClassPathResource template =
        new ClassPathResource("templates/page/group/create.html");
    String content = new String(template.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

    assertThat(content).doesNotContain("#pathVariables");
    assertThat(content).contains("id=${periodId}");
  }
}
