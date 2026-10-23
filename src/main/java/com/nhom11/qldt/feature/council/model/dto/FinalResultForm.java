package com.nhom11.qldt.feature.council.model.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/** Nhận nhận xét tổng kết khi chủ tịch hội đồng chốt kết quả KLTN. */
public class FinalResultForm {
  @Size(max = 2000, message = "{final-results.validation.summary.max}")
  private String summaryComment;
}
