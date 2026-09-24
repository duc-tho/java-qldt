package com.nhom11.qldt.feature.registrationperiod.service;

import org.springframework.stereotype.Service;

import com.nhom11.qldt.core.repository.BaseRepository;
import com.nhom11.qldt.core.service.BaseCrudService;
import com.nhom11.qldt.feature.registrationperiod.model.entity.RegistrationPeriod;
import com.nhom11.qldt.feature.registrationperiod.model.enums.PeriodType;
import com.nhom11.qldt.feature.registrationperiod.repository.RegistrationPeriodRepository;

@Service
public class RegistrationPeriodService extends BaseCrudService<RegistrationPeriod, Long> {
    private final RegistrationPeriodRepository repository;
    public RegistrationPeriodService(RegistrationPeriodRepository repository) { this.repository = repository; }
    @Override public BaseRepository<RegistrationPeriod, Long> getRepository() { return repository; }
    @Override protected void beforeCreate(RegistrationPeriod period) { validate(period); }
    @Override protected void beforeUpdate(RegistrationPeriod period) { validate(period); }
    private void validate(RegistrationPeriod period) {
        if (period.getLecturerRegistrationStart().isAfter(period.getLecturerRegistrationEnd()) || !period.getLecturerRegistrationEnd().isBefore(period.getTopicPublicationAt()) || !period.getTopicPublicationAt().isBefore(period.getStudentRegistrationStart()) || period.getStudentRegistrationStart().isAfter(period.getStudentRegistrationEnd())) throw new IllegalArgumentException("Invalid registration period timeline");
        boolean reviewerRequired = period.getPeriodType() == PeriodType.TLCN || period.getPeriodType() == PeriodType.KLTN;
        if (reviewerRequired != (period.getReviewerDeadline() != null)) throw new IllegalArgumentException("Invalid reviewer deadline");
        boolean councilRequired = period.getPeriodType() == PeriodType.KLTN;
        if (councilRequired != (period.getCouncilDate() != null) || councilRequired && (!period.getStudentRegistrationEnd().isBefore(period.getCouncilDate()) || !period.getReviewerDeadline().isBefore(period.getCouncilDate()))) throw new IllegalArgumentException("Invalid council date");
    }
}
