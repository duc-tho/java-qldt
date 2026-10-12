package com.nhom11.qldt.feature.council.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class CouncilTemplateTest {
  @Test
  void councilFormPreservesValidationAndUsesSharedDeleteConfirmation() throws IOException {
    String create = template("create.html");
    String detail = template("detail.html");

    assertThat(create)
        .contains("#fields.hasGlobalErrors()")
        .contains("th:errors=\"*{memberIds}\"")
        .contains("initialMembers");
    assertThat(detail).contains("crud-delete-request");
  }

  @Test
  void workspaceExposesDraftSubmitAndFinalizeActions() throws IOException {
    String workspace = template("workspace.html");

    assertThat(workspace)
        .contains("/draft")
        .contains("/submit")
        .contains("/finalize")
        .contains("th:if=\"${councilEvaluation and canFinalize}\"");
  }

  private String template(String name) throws IOException {
    return Files.readString(
        Path.of("src/main/resources/templates/fragment/page/council", name));
  }
}
