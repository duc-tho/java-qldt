package com.nhom11.qldt.feature.registrationperiod.model.entity;

import java.time.LocalDateTime;

import com.nhom11.qldt.core.entity.SoftDeleteEntity;
import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.registrationperiod.model.enums.PeriodType;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "registration_periods")
@Getter
@Setter
public class RegistrationPeriod extends SoftDeleteEntity {
    private String name;
    @Enumerated(EnumType.STRING) private PeriodType periodType;
    private LocalDateTime lecturerRegistrationStart;
    private LocalDateTime lecturerRegistrationEnd;
    private LocalDateTime topicPublicationAt;
    private LocalDateTime studentRegistrationStart;
    private LocalDateTime studentRegistrationEnd;
    private LocalDateTime reportDeadline;
    private LocalDateTime reviewerDeadline;
    private LocalDateTime councilDate;
    @ManyToOne(optional = false) @JoinColumn(name = "created_by", nullable = false) private User createdBy;
}
