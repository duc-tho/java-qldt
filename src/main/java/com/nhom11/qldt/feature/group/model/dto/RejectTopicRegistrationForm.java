package com.nhom11.qldt.feature.group.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/** Nhận lý do giảng viên hướng dẫn từ chối đăng ký đề tài của nhóm. */
public class RejectTopicRegistrationForm {
  @NotBlank(message = "{topic-registrations.validation.reason.required}")
  private String reason;
}
