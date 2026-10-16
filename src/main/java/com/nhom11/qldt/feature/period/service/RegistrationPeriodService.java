package com.nhom11.qldt.feature.period.service;

import com.nhom11.qldt.core.repository.BaseRepository;
import com.nhom11.qldt.core.service.BaseCrudService;
import com.nhom11.qldt.feature.council.model.enums.ResultPublicationAction;
import com.nhom11.qldt.feature.council.repository.ResultPublicationRepository;
import com.nhom11.qldt.feature.period.model.entity.RegistrationPeriod;
import com.nhom11.qldt.feature.period.model.enums.PeriodType;
import com.nhom11.qldt.feature.period.model.enums.RegistrationPeriodValidationError;
import com.nhom11.qldt.feature.period.model.enums.RegistrationPeriodStage;
import com.nhom11.qldt.feature.period.repository.RegistrationPeriodRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class RegistrationPeriodService extends BaseCrudService<RegistrationPeriod, Long> {
  private final RegistrationPeriodRepository repository;
  private final ResultPublicationRepository publicationRepository;

  public RegistrationPeriodService(
      RegistrationPeriodRepository repository, ResultPublicationRepository publicationRepository) {
    this.repository = repository;
    this.publicationRepository = publicationRepository;
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

    if (!period.getLecturerRegistrationStart().isBefore(period.getLecturerRegistrationEnd())
        || !period.getLecturerRegistrationEnd().isBefore(period.getStudentRegistrationStart())
        || !period.getStudentRegistrationStart().isBefore(period.getStudentRegistrationEnd())
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

  public List<RegistrationPeriod> findVisibleForLecturer(Long lecturerId) {
    return repository.findVisibleForLecturer(LocalDateTime.now(), lecturerId);
  }

  public List<RegistrationPeriod> findVisibleForStudent(Long studentId) {
    return repository.findVisibleForStudent(LocalDateTime.now(), studentId);
  }

  public List<RegistrationPeriod> findAll() {
    return repository.findAll();
  }

  public RegistrationPeriodStage stage(RegistrationPeriod period, LocalDateTime now) {
    if (now.isBefore(period.getLecturerRegistrationStart())) {
      return RegistrationPeriodStage.UPCOMING;
    }
    if (!now.isAfter(period.getLecturerRegistrationEnd())) {
      return RegistrationPeriodStage.TOPIC_PROPOSAL;
    }
    if (now.isBefore(period.getStudentRegistrationStart())) {
      return RegistrationPeriodStage.AWAITING_STUDENT_REGISTRATION;
    }
    if (now.isBefore(period.getReportDeadline())) {
      return RegistrationPeriodStage.TOPIC_EXECUTION;
    }
    return publicationRepository
        .findFirstByRegistrationPeriodIdOrderByIdDesc(period.getId())
        .filter(publication -> publication.getAction() == ResultPublicationAction.PUBLISHED)
        .map(publication -> RegistrationPeriodStage.CLOSED)
        .orElse(RegistrationPeriodStage.EVALUATION_AND_PUBLICATION);
  }
}
