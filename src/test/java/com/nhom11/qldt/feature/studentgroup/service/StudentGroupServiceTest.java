package com.nhom11.qldt.feature.studentgroup.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.Mockito.when;

import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.auth.repository.UserRepository;
import com.nhom11.qldt.feature.registrationperiod.model.entity.RegistrationPeriod;
import com.nhom11.qldt.feature.registrationperiod.service.RegistrationPeriodService;
import com.nhom11.qldt.feature.studentgroup.model.entity.GroupMember;
import com.nhom11.qldt.feature.studentgroup.model.entity.StudentGroup;
import com.nhom11.qldt.feature.studentgroup.model.entity.TopicRegistration;
import com.nhom11.qldt.feature.studentgroup.model.enums.TopicRegistrationStatus;
import com.nhom11.qldt.feature.studentgroup.repository.GroupMemberRepository;
import com.nhom11.qldt.feature.studentgroup.repository.StudentGroupRepository;
import com.nhom11.qldt.feature.studentgroup.repository.TopicRegistrationRepository;
import com.nhom11.qldt.feature.topic.repository.TopicRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StudentGroupServiceTest {
  private static final long GROUP_ID = 1L;
  private static final long LEADER_ID = 2L;

  @Mock private StudentGroupRepository studentGroupRepository;
  @Mock private GroupMemberRepository groupMemberRepository;
  @Mock private TopicRegistrationRepository topicRegistrationRepository;
  @Mock private TopicRepository topicRepository;
  @Mock private UserRepository userRepository;
  @Mock private RegistrationPeriodService registrationPeriodService;

  @InjectMocks private StudentGroupService service;

  private StudentGroup group;
  private User leader;

  @BeforeEach
  void setUp() {
    RegistrationPeriod period = new RegistrationPeriod();
    period.setStudentRegistrationStart(LocalDateTime.now().minusMinutes(1));
    period.setStudentRegistrationEnd(LocalDateTime.now().plusMinutes(1));

    group = new StudentGroup();
    group.setRegistrationPeriod(period);

    leader = new User();
    leader.setId(LEADER_ID);

    when(studentGroupRepository.findById(GROUP_ID)).thenReturn(Optional.of(group));
  }

  @Test
  void requestRegistrationApprovalMovesDraftRegistrationToPending() {
    stubLeaderMembership();
    TopicRegistration registration = new TopicRegistration();
    registration.setStatus(TopicRegistrationStatus.DRAFT);
    when(topicRegistrationRepository.findByStudentGroupId(GROUP_ID)).thenReturn(Optional.of(registration));

    service.requestRegistrationApproval(GROUP_ID, leader);

    assertThat(registration.getStatus()).isEqualTo(TopicRegistrationStatus.PENDING);
  }

  @Test
  void requestRegistrationApprovalRejectsNonDraftRegistration() {
    stubLeaderMembership();
    TopicRegistration registration = new TopicRegistration();
    registration.setStatus(TopicRegistrationStatus.REJECTED);
    when(topicRegistrationRepository.findByStudentGroupId(GROUP_ID)).thenReturn(Optional.of(registration));

    assertThatIllegalArgumentException()
        .isThrownBy(() -> service.requestRegistrationApproval(GROUP_ID, leader))
        .withMessage("topic-registrations.error.draft-required");
  }

  @Test
  void requestRegistrationApprovalRequiresSelectedTopic() {
    stubLeaderMembership();
    when(topicRegistrationRepository.findByStudentGroupId(GROUP_ID)).thenReturn(Optional.empty());

    assertThatIllegalArgumentException()
        .isThrownBy(() -> service.requestRegistrationApproval(GROUP_ID, leader))
        .withMessage("topic-registrations.error.registration-required");
  }

  @Test
  void requestRegistrationApprovalRequiresGroupLeader() {
    User member = new User();
    member.setId(3L);
    when(groupMemberRepository.findByStudentGroupIdAndStudentId(GROUP_ID, 3L))
        .thenReturn(Optional.empty());

    assertThatIllegalArgumentException()
        .isThrownBy(() -> service.requestRegistrationApproval(GROUP_ID, member))
        .withMessage("student-groups.error.leader-required");
  }

  private void stubLeaderMembership() {
    GroupMember leaderMember = new GroupMember();
    leaderMember.setLeader(true);
    when(groupMemberRepository.findByStudentGroupIdAndStudentId(GROUP_ID, LEADER_ID))
        .thenReturn(Optional.of(leaderMember));
  }
}
