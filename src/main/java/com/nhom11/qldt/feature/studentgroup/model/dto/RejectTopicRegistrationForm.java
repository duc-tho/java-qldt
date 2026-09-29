package com.nhom11.qldt.feature.studentgroup.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RejectTopicRegistrationForm {
  @NotBlank(message = "{topic-registrations.validation.reason.required}")
  private String reason;
}
