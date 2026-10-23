package com.nhom11.qldt.feature.dashboard.model;

import com.nhom11.qldt.feature.announcement.model.dto.AnnouncementSummary;
import com.nhom11.qldt.feature.auth.model.entity.User;
import java.util.List;

/** Gom số liệu, thông báo và tác vụ cần hiển thị trên bảng điều khiển. */
public record DashboardData(
    User user, List<DashboardAction> actions, List<AnnouncementSummary> announcements) {}
