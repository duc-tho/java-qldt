package com.nhom11.qldt.feature.announcement.model.dto;

import java.time.Instant;

/** Dữ liệu tóm tắt phục vụ hiển thị chức năng thông báo. */
public record AnnouncementSummary(Long id, String title, Instant createdAt) {}
