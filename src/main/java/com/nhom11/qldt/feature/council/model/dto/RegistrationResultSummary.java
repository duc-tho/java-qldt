package com.nhom11.qldt.feature.council.model.dto;

import com.nhom11.qldt.feature.council.model.entity.FinalResult;
import com.nhom11.qldt.feature.group.model.entity.TopicRegistration;

public record RegistrationResultSummary(TopicRegistration registration, FinalResult result) {}
