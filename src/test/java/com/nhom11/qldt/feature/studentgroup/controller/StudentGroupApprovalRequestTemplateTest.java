package com.nhom11.qldt.feature.studentgroup.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class StudentGroupApprovalRequestTemplateTest {
  @Test
  void detailShowsRequestActionOnlyForEligibleDraftLeader() throws IOException {
    ClassPathResource template =
        new ClassPathResource("templates/fragment/page/student-group/detail.html");
    String content = new String(template.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

    assertThat(content).contains("isLeader and isOpen and isDraftRegistration");
    assertThat(content).contains("/request-approval");
    assertThat(content).contains("topic-registrations.action.request-approval");
  }
}
