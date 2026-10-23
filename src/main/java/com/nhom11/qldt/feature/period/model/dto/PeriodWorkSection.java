package com.nhom11.qldt.feature.period.model.dto;

import java.util.List;

/** Nhóm các thẻ công việc theo đợt dùng trên giao diện. */
public record PeriodWorkSection(
    String id, String title, String emptyMessage, List<PeriodWorkCard> cards) {}
