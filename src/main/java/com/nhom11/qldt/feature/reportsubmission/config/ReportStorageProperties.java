package com.nhom11.qldt.feature.reportsubmission.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "qldt.report-storage")
@Validated
@Getter
@Setter
public class ReportStorageProperties {
  @NotBlank private String root;
}
