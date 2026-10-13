package com.nhom11.qldt.feature.registrationperiod.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.nhom11.qldt.feature.council.model.entity.ResultPublication;
import com.nhom11.qldt.feature.council.model.enums.ResultPublicationAction;
import com.nhom11.qldt.feature.council.repository.ResultPublicationRepository;
import com.nhom11.qldt.feature.registrationperiod.model.entity.RegistrationPeriod;
import com.nhom11.qldt.feature.registrationperiod.model.enums.PeriodType;
import com.nhom11.qldt.feature.registrationperiod.model.enums.RegistrationPeriodValidationError;
import com.nhom11.qldt.feature.registrationperiod.model.enums.RegistrationPeriodStage;
import com.nhom11.qldt.feature.registrationperiod.repository.RegistrationPeriodRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegistrationPeriodServiceTest {
  @Mock private RegistrationPeriodRepository repository;
  @Mock private ResultPublicationRepository publicationRepository;

  @InjectMocks private RegistrationPeriodService service;

  @Test
  void findOpenForStudentsReturnsOnlyPeriodsProvidedByOpenWindowQuery() {
    RegistrationPeriod period = new RegistrationPeriod();
    List<RegistrationPeriod> expected = List.of(period);
    when(repository.findOpenForStudents(any(LocalDateTime.class))).thenReturn(expected);

    List<RegistrationPeriod> actual = service.findOpenForStudents();

    assertThat(actual).containsExactly(period);
    verify(repository).findOpenForStudents(any(LocalDateTime.class));
  }

  @Test
  void validKltnTimelinePlacesCouncilDateBeforeGradingDeadline() {
    RegistrationPeriod period = validKltnPeriod();

    assertThat(service.validationErrors(period)).isEmpty();
  }

  @Test
  void kltnTimelineRejectsCouncilDateAfterGradingDeadline() {
    RegistrationPeriod period = validKltnPeriod();
    period.setCouncilDate(period.getReviewerDeadline().plusMinutes(1));

    assertThat(service.validationErrors(period))
        .contains(RegistrationPeriodValidationError.COUNCIL_DATE);
  }

  @Test
  void studentRegistrationMayStartImmediatelyAfterLecturerRegistrationEnds() {
    RegistrationPeriod period = validKltnPeriod();
    period.setStudentRegistrationStart(period.getLecturerRegistrationEnd().plusMinutes(1));

    assertThat(service.validationErrors(period)).isEmpty();
  }

  @Test
  void studentRegistrationMustStartAfterLecturerRegistrationEnds() {
    RegistrationPeriod period = validKltnPeriod();
    period.setStudentRegistrationStart(period.getLecturerRegistrationEnd());

    assertThat(service.validationErrors(period))
        .contains(RegistrationPeriodValidationError.TIMELINE);
  }

  @Test
  void stageEntersEvaluationAfterReportDeadlineUntilResultsArePublished() {
    RegistrationPeriod period = validKltnPeriod();
    when(publicationRepository.findFirstByRegistrationPeriodIdOrderByIdDesc(period.getId()))
        .thenReturn(Optional.empty());

    assertThat(service.stage(period, period.getStudentRegistrationEnd().plusMinutes(1)))
        .isEqualTo(RegistrationPeriodStage.TOPIC_EXECUTION);
    verifyNoInteractions(publicationRepository);
    assertThat(service.stage(period, period.getReportDeadline().plusMinutes(1)))
        .isEqualTo(RegistrationPeriodStage.EVALUATION_AND_PUBLICATION);
    assertThat(service.stage(period, period.getReviewerDeadline().plusMinutes(1)))
        .isEqualTo(RegistrationPeriodStage.EVALUATION_AND_PUBLICATION);
  }

  @Test
  void stageClosesOnPublicationAndReopensWhenPublicationIsRevoked() {
    RegistrationPeriod period = validKltnPeriod();
    ResultPublication published = new ResultPublication();
    published.setAction(ResultPublicationAction.PUBLISHED);
    ResultPublication revoked = new ResultPublication();
    revoked.setAction(ResultPublicationAction.REVOKED);
    when(publicationRepository.findFirstByRegistrationPeriodIdOrderByIdDesc(period.getId()))
        .thenReturn(Optional.of(published), Optional.of(revoked));

    assertThat(service.stage(period, period.getReviewerDeadline().plusMinutes(1)))
        .isEqualTo(RegistrationPeriodStage.CLOSED);
    assertThat(service.stage(period, period.getReviewerDeadline().plusMinutes(1)))
        .isEqualTo(RegistrationPeriodStage.EVALUATION_AND_PUBLICATION);
  }

  private RegistrationPeriod validKltnPeriod() {
    LocalDateTime start = LocalDateTime.now().plusDays(1);
    RegistrationPeriod period = new RegistrationPeriod();
    period.setId(1L);
    period.setPeriodType(PeriodType.KLTN);
    period.setLecturerRegistrationStart(start);
    period.setLecturerRegistrationEnd(start.plusDays(1));
    period.setStudentRegistrationStart(start.plusDays(3));
    period.setStudentRegistrationEnd(start.plusDays(4));
    period.setReportDeadline(start.plusDays(5));
    period.setCouncilDate(start.plusDays(6));
    period.setReviewerDeadline(start.plusDays(7));
    return period;
  }
}
