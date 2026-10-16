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
        .contains("initialMembers");
    assertThat(Files.readString(Path.of("src/main/resources/templates/fragment/feature/council/member-picker.html")))
        .contains("th:errors=\"*{memberIds}\"");
    assertThat(detail).contains("fragment/crud/delete-action :: delete(");
    assertThat(Files.readString(Path.of("src/main/resources/templates/fragment/crud/delete-action.html")))
        .contains("crud-delete-request");
  }

  @Test
  void workspaceExposesDraftSubmitAndFinalizeActions() throws IOException {
    String workspace = Files.readString(
        Path.of("src/main/resources/templates/fragment/feature/evaluation/score-form.html"))
        + Files.readString(
            Path.of("src/main/resources/templates/fragment/feature/evaluation/final-result.html"));

    assertThat(workspace)
        .contains("/luu-nhap")
        .contains("/nop")
        .contains("/chot")
        .contains("th:if=\"${councilEvaluation and canFinalize}\"");
  }

  private String template(String name) throws IOException {
    return Files.readString(
        Path.of("src/main/resources/templates/page/council", name));
  }
}
