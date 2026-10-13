package com.nhom11.qldt.feature.studentgroup.model.dto;

import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.studentgroup.model.entity.StudentGroup;
import com.nhom11.qldt.feature.studentgroup.model.entity.TopicRegistration;
import java.util.List;

public record GroupSummary(
    StudentGroup group, String leaderName, String leaderCode, int memberCount,
    List<User> students, TopicRegistration registration) {}
