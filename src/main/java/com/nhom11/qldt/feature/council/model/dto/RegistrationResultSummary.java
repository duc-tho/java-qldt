package com.nhom11.qldt.feature.council.model.dto;

import com.nhom11.qldt.feature.council.model.entity.FinalResult;
import com.nhom11.qldt.feature.group.model.entity.TopicRegistration;

/** Ghép đăng ký đề tài với kết quả cuối cùng để hiển thị tổng hợp. */
public record RegistrationResultSummary(TopicRegistration registration, FinalResult result) {}
