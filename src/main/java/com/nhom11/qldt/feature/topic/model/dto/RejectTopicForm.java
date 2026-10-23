package com.nhom11.qldt.feature.topic.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/** Nhận và kiểm tra lý do trưởng khoa từ chối đề xuất đề tài. */
public class RejectTopicForm {

  @NotBlank(message = "{topics.validation.rejection-reason.required}")
  @Size(max = 500, message = "{topics.validation.rejection-reason.max}")
  private String reason;
}
