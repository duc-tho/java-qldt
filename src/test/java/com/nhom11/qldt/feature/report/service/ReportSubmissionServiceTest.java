package com.nhom11.qldt.feature.report.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.Mockito.when;

import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.council.repository.CouncilMemberRepository;
import com.nhom11.qldt.feature.period.model.entity.RegistrationPeriod;
import com.nhom11.qldt.feature.report.config.ReportStorageProperties;
import com.nhom11.qldt.feature.report.repository.ReportFileRepository;
import com.nhom11.qldt.feature.report.repository.ReportSubmissionRepository;
import com.nhom11.qldt.feature.group.model.entity.GroupMember;
import com.nhom11.qldt.feature.group.model.entity.StudentGroup;
import com.nhom11.qldt.feature.group.model.entity.TopicRegistration;
import com.nhom11.qldt.feature.group.model.enums.TopicRegistrationStatus;
import com.nhom11.qldt.feature.group.repository.GroupMemberRepository;
import com.nhom11.qldt.feature.group.repository.TopicRegistrationRepository;
import com.nhom11.qldt.feature.topic.model.entity.Topic;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class ReportSubmissionServiceTest {
  private static final long PERIOD_ID = 1L;
  private static final long GROUP_ID = 2L;
  private static final long REGISTRATION_ID = 3L;
  private static final long LEADER_ID = 4L;

  @Mock private ReportSubmissionRepository submissionRepository;
  @Mock private ReportFileRepository fileRepository;
  @Mock private TopicRegistrationRepository registrationRepository;
  @Mock private GroupMemberRepository groupMemberRepository;
  @Mock private CouncilMemberRepository councilMemberRepository;
  @Mock private ReportStorageProperties storageProperties;

  @InjectMocks private ReportSubmissionService service;

  private TopicRegistration registration;
  private User leader;

  @BeforeEach
  void setUp() {
    RegistrationPeriod period = new RegistrationPeriod();
    period.setId(PERIOD_ID);
    period.setReportDeadline(LocalDateTime.now().minusMinutes(1));
    StudentGroup group = new StudentGroup();
    group.setId(GROUP_ID);
    group.setRegistrationPeriod(period);
    Topic topic = new Topic();
    topic.setRegistrationPeriod(period);
    registration = new TopicRegistration();
    registration.setId(REGISTRATION_ID);
    registration.setStudentGroup(group);
    registration.setTopic(topic);
    registration.setStatus(TopicRegistrationStatus.APPROVED);
    leader = new User();
    leader.setId(LEADER_ID);
  }

  @Test
  void submitRejectsFilesAfterReportDeadline() {
    GroupMember membership = new GroupMember();
    membership.setLeader(true);
    when(registrationRepository.findById(REGISTRATION_ID)).thenReturn(Optional.of(registration));
    when(groupMemberRepository.findByStudentGroupIdAndStudentId(GROUP_ID, LEADER_ID))
        .thenReturn(Optional.of(membership));

    assertThatIllegalArgumentException()
        .isThrownBy(
            () ->
                service.submit(
                    PERIOD_ID,
                    GROUP_ID,
                    REGISTRATION_ID,
                    leader,
                    List.<MultipartFile>of()))
        .withMessage("report-submissions.error.deadline-passed");
  }

  @Test
  void canUploadIsFalseAfterReportDeadline() {
    assertThat(service.canUpload(registration, leader)).isFalse();
  }
}
