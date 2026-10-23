package com.nhom11.qldt.feature.period.model.dto;

import com.nhom11.qldt.feature.period.model.entity.RegistrationPeriod;

/** Dữ liệu của một thẻ công việc theo đợt trên giao diện. */
public record PeriodWorkCard(
    RegistrationPeriod period,
    String destination,
    String status,
    String statusClass,
    String primaryMetric,
    String secondaryMetric,
    String actionLabel) {}
