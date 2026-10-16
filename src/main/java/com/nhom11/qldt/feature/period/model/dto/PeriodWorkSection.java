package com.nhom11.qldt.feature.period.model.dto;

import java.util.List;

public record PeriodWorkSection(
    String id, String title, String emptyMessage, List<PeriodWorkCard> cards) {}
