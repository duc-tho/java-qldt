package com.nhom11.qldt.feature.council.model.dto;

import com.nhom11.qldt.feature.council.model.entity.Council;

public record CouncilSummary(
    Council council, String chairpersonName, int memberCount, long assignedCount) {}
