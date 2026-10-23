package com.nhom11.qldt.feature.group.model.dto;

import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.group.model.entity.StudentGroup;
import com.nhom11.qldt.feature.group.model.entity.TopicRegistration;
import java.util.List;

/** Dữ liệu tổng hợp của nhóm dùng cho danh sách và trang công việc. */
public record GroupSummary(
    StudentGroup group, String leaderName, String leaderCode, int memberCount,
    List<User> students, TopicRegistration registration) {}
