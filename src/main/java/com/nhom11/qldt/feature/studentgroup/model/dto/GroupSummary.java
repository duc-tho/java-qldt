package com.nhom11.qldt.feature.studentgroup.model.dto;

import com.nhom11.qldt.feature.studentgroup.model.entity.StudentGroup;
import com.nhom11.qldt.feature.studentgroup.model.entity.TopicRegistration;

public record GroupSummary(
    StudentGroup group, String leaderName, int memberCount, TopicRegistration registration) {}
