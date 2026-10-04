package com.nhom11.qldt.feature.registrationperiod.service;

import com.nhom11.qldt.core.repository.BaseRepository;
import com.nhom11.qldt.core.service.BaseCrudService;
import com.nhom11.qldt.feature.registrationperiod.model.entity.RegistrationPeriod;
import com.nhom11.qldt.feature.registrationperiod.model.enums.PeriodType;
import com.nhom11.qldt.feature.registrationperiod.model.enums.RegistrationPeriodValidationError;
import com.nhom11.qldt.feature.registrationperiod.repository.RegistrationPeriodRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class RegistrationPeriodService extends BaseCrudService<RegistrationPeriod, Long> {
  private final RegistrationPeriodRepository repository;

  public RegistrationPeriodService(RegistrationPeriodRepository repository) {
    this.repository = repository;
  }

  @Override
  public BaseRepository<RegistrationPeriod, Long> getRepository() {
    return repository;
  }

  @Override
  protected void beforeCreate(RegistrationPeriod period) {
    validate(period);
  }

  @Override
  protected void beforeUpdate(RegistrationPeriod period) {
    validate(period);
  }

  private void validate(RegistrationPeriod period) {
    if (!validationErrors(period).isEmpty()) {
      throw new IllegalArgumentException("Invalid registration period");
    }
  }

  public List<RegistrationPeriodValidationError> validationErrors(RegistrationPeriod period) {
    List<RegistrationPeriodValidationError> errors = new ArrayList<>();

    if (period.getLecturerRegistrationStart().isAfter(period.getLecturerRegistrationEnd())
        || !period.getLecturerRegistrationEnd().isBefore(period.getTopicPublicationAt())
        || !period.getTopicPublicationAt().isBefore(period.getStudentRegistrationStart())
        || period.getStudentRegistrationStart().isAfter(period.getStudentRegistrationEnd())
        || !period.getStudentRegistrationEnd().isBefore(period.getReportDeadline())) {
      errors.add(RegistrationPeriodValidationError.TIMELINE);
    }

    boolean reviewerRequired =
        period.getPeriodType() == PeriodType.TLCN || period.getPeriodType() == PeriodType.KLTN;

    if (reviewerRequired != (period.getReviewerDeadline() != null)
        || reviewerRequired
            && period.getReviewerDeadline() != null
            && period.getReportDeadline().isAfter(period.getReviewerDeadline())) {
      errors.add(RegistrationPeriodValidationError.REVIEWER_DEADLINE);
    }

    boolean councilRequired = period.getPeriodType() == PeriodType.KLTN;
    if (councilRequired != (period.getCouncilDate() != null)
        || councilRequired
            && (period.getCouncilDate() == null
                || !period.getReportDeadline().isBefore(period.getCouncilDate())
                || period.getReviewerDeadline() == null
                || period.getCouncilDate().isAfter(period.getReviewerDeadline()))) {
      errors.add(RegistrationPeriodValidationError.COUNCIL_DATE);
    }

    return errors;
  }

  public List<RegistrationPeriod> findOpenForStudents() {
    return repository.findOpenForStudents(LocalDateTime.now());
  }

  public List<RegistrationPeriod> findVisibleForLecturers() {
    return repository.findVisibleForLecturers(LocalDateTime.now());
  }

  public List<RegistrationPeriod> findAll() {
    return repository.findAll();
  }
}
