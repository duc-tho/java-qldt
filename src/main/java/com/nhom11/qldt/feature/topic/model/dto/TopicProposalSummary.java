package com.nhom11.qldt.feature.topic.model.dto;

import com.nhom11.qldt.feature.topic.model.enums.TopicStatus;

public record TopicProposalSummary(Long id, Long periodId, String title, TopicStatus status) {}
