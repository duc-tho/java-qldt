package com.nhom11.qldt.feature.council.service;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.Mockito.when;

import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.auth.repository.UserRepository;
import com.nhom11.qldt.feature.council.model.dto.CouncilForm;
import com.nhom11.qldt.feature.council.model.dto.EvaluationForm;
import com.nhom11.qldt.feature.council.model.entity.Council;
import com.nhom11.qldt.feature.council.model.entity.CouncilMember;
import com.nhom11.qldt.feature.council.repository.CouncilMemberRepository;
import com.nhom11.qldt.feature.council.repository.CouncilRepository;
import com.nhom11.qldt.feature.council.repository.EvaluationRepository;
import com.nhom11.qldt.feature.council.repository.FinalResultRepository;
import com.nhom11.qldt.feature.council.repository.ResultPublicationRepository;
import com.nhom11.qldt.feature.registrationperiod.model.entity.RegistrationPeriod;
import com.nhom11.qldt.feature.registrationperiod.model.enums.PeriodType;
import com.nhom11.qldt.feature.registrationperiod.repository.RegistrationPeriodRepository;
import com.nhom11.qldt.feature.reportsubmission.service.ReportSubmissionService;
import com.nhom11.qldt.feature.studentgroup.model.entity.TopicRegistration;
import com.nhom11.qldt.feature.studentgroup.model.enums.TopicRegistrationStatus;
import com.nhom11.qldt.feature.studentgroup.repository.TopicRegistrationRepository;
import com.nhom11.qldt.feature.topic.model.entity.Topic;
import com.nhom11.qldt.feature.topic.repository.TopicRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CouncilServiceTest {
  private static final long PERIOD_ID = 1L;
  private static final long COUNCIL_ID = 2L;
  private static final long REGISTRATION_ID = 3L;

  @Mock private CouncilRepository councilRepository;
  @Mock private CouncilMemberRepository councilMemberRepository;
  @Mock private EvaluationRepository evaluationRepository;
  @Mock private FinalResultRepository finalResultRepository;
  @Mock private ResultPublicationRepository publicationRepository;
  @Mock private RegistrationPeriodRepository periodRepository;
  @Mock private TopicRepository topicRepository;
  @Mock private TopicRegistrationRepository registrationRepository;
  @Mock private UserRepository userRepository;
  @Mock private ReportSubmissionService reportSubmissionService;

  @InjectMocks private CouncilService service;

  @Test
  void createCouncilRejectsRequestOutsideManagementWindow() {
    RegistrationPeriod period = councilPeriod();
    period.setStudentRegistrationEnd(LocalDateTime.now().plusDays(1));
    when(periodRepository.findById(PERIOD_ID)).thenReturn(Optional.of(period));

    assertThatIllegalArgumentException()
        .isThrownBy(() -> service.createCouncil(PERIOD_ID, new User(), validForm()))
        .withMessage("councils.error.management-window");
  }

  @Test
  void updateCouncilRejectsAdviserAddedToAssignedCouncil() {
    RegistrationPeriod period = councilPeriod();
    Council council = council(period);
    CouncilForm form = validForm();
    List<User> lecturers = form.getMemberIds().stream().map(this::user).toList();
    Topic topic = new Topic();
    topic.setCouncil(council);
    topic.setRegistrationPeriod(period);
    topic.setAdvisers(Set.of(lecturers.getFirst()));
    TopicRegistration registration = new TopicRegistration();
    registration.setTopic(topic);

    when(councilRepository.findByIdAndRegistrationPeriodId(COUNCIL_ID, PERIOD_ID))
        .thenReturn(Optional.of(council));
    when(councilRepository.findById(COUNCIL_ID)).thenReturn(Optional.of(council));
    when(userRepository.findAllLecturersByIdIn(Set.copyOf(form.getMemberIds())))
        .thenReturn(lecturers);
    when(registrationRepository.findAllByRegistrationPeriodId(PERIOD_ID))
        .thenReturn(List.of(registration));

    assertThatIllegalArgumentException()
        .isThrownBy(() -> service.updateCouncil(PERIOD_ID, COUNCIL_ID, form))
        .withMessage("councils.error.adviser-member-conflict");
  }

  @Test
  void submitEvaluationUsesReviewerDeadline() {
    RegistrationPeriod period = councilPeriod();
    period.setReviewerDeadline(LocalDateTime.now().minusMinutes(1));
    Council council = council(period);
    Topic topic = new Topic();
    topic.setRegistrationPeriod(period);
    topic.setCouncil(council);
    TopicRegistration registration = new TopicRegistration();
    registration.setId(REGISTRATION_ID);
    registration.setTopic(topic);
    registration.setStatus(TopicRegistrationStatus.APPROVED);
    User evaluator = user(10L);
    EvaluationForm form = new EvaluationForm();
    form.setScore(BigDecimal.valueOf(8));

    when(registrationRepository.findById(REGISTRATION_ID)).thenReturn(Optional.of(registration));
    CouncilMember member = new CouncilMember();
    member.setLecturer(evaluator);
    when(councilMemberRepository.findAllByCouncilId(COUNCIL_ID)).thenReturn(List.of(member));
    when(reportSubmissionService.hasCurrentSubmission(REGISTRATION_ID)).thenReturn(true);

    assertThatIllegalArgumentException()
        .isThrownBy(() -> service.saveEvaluation(REGISTRATION_ID, evaluator, form, true))
        .withMessage("evaluations.error.deadline-passed");
  }

  @Test
  void assignmentRejectsCouncilOutsideRoutePeriod() {
    when(councilRepository.findByIdAndRegistrationPeriodId(COUNCIL_ID, PERIOD_ID))
        .thenReturn(Optional.empty());

    assertThatIllegalArgumentException()
        .isThrownBy(() -> service.assignTopic(PERIOD_ID, COUNCIL_ID, 9L))
        .withMessage("councils.error.not-found");
  }

  private RegistrationPeriod councilPeriod() {
    RegistrationPeriod period = new RegistrationPeriod();
    period.setId(PERIOD_ID);
    period.setPeriodType(PeriodType.KLTN);
    period.setStudentRegistrationEnd(LocalDateTime.now().minusDays(1));
    period.setCouncilDate(LocalDateTime.now().plusDays(1));
    period.setReviewerDeadline(LocalDateTime.now().plusDays(2));
    return period;
  }

  private Council council(RegistrationPeriod period) {
    Council council = new Council();
    council.setId(COUNCIL_ID);
    council.setRegistrationPeriod(period);
    return council;
  }

  private CouncilForm validForm() {
    CouncilForm form = new CouncilForm();
    form.setName("Council 1");
    form.setMemberIds(List.of(10L, 11L, 12L));
    form.setChairpersonId(10L);
    form.setSecretaryId(11L);
    return form;
  }

  private User user(Long id) {
    User user = new User();
    user.setId(id);
    return user;
  }
}
