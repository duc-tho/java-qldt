package com.nhom11.qldt.feature.dashboard.config;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "qldt.dashboard")
@Validated
@Getter
@Setter
/** Ánh xạ các thuộc tính của chức năng bảng điều khiển từ cấu hình ứng dụng. */
public class DashboardProperties {
  @Min(1)
  private int announcementLimit;
}
