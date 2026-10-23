package com.nhom11.qldt.feature.topic.model.dto;

import com.nhom11.qldt.feature.topic.model.enums.TopicStatus;

/** Dữ liệu rút gọn của đề xuất đề tài dùng trên trang công việc. */
public record TopicProposalSummary(Long id, Long periodId, String title, TopicStatus status) {}
