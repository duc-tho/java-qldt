package com.nhom11.qldt.feature.dashboard.model;

import com.nhom11.qldt.feature.announcement.model.dto.AnnouncementSummary;
import com.nhom11.qldt.feature.auth.model.entity.User;
import java.util.List;

public record DashboardData(
    User user, List<DashboardAction> actions, List<AnnouncementSummary> announcements) {}
