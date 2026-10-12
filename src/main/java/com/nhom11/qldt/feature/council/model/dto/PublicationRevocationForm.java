package com.nhom11.qldt.feature.council.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PublicationRevocationForm {
  @NotBlank(message = "{result-publications.validation.reason.required}")
  @Size(max = 500, message = "{result-publications.validation.reason.max}")
  private String reason;
}
