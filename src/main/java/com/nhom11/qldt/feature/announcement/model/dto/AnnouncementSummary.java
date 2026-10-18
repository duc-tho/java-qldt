package com.nhom11.qldt.feature.announcement.model.dto;

import java.time.Instant;

public record AnnouncementSummary(Long id, String title, Instant createdAt) {}
