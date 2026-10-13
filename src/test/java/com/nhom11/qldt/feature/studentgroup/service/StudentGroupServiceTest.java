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
  @Mock private GroupActivityService groupActivityService;

  @InjectMocks private StudentGroupService service;

  private StudentGroup group;
  private User leader;

  @BeforeEach
  void setUp() {
    RegistrationPeriod period = new RegistrationPeriod();
    period.setStudentRegistrationStart(LocalDateTime.now().minusMinutes(1));
    period.setStudentRegistrationEnd(LocalDateTime.now().plusMinutes(1));

    group = new StudentGroup();
    group.setId(GROUP_ID);
    group.setRegistrationPeriod(period);

    leader = new User();
    leader.setId(LEADER_ID);

    when(studentGroupRepository.findByIdForUpdate(GROUP_ID)).thenReturn(Optional.of(group));
  }

  @Test
  void requestRegistrationApprovalMovesDraftRegistrationToPending() {
    stubLeaderMembership();
    TopicRegistration registration = new TopicRegistration();
    registration.setStatus(TopicRegistrationStatus.DRAFT);
    registration.setTopic(new Topic());
    registration.setStudentGroup(group);
    when(topicRegistrationRepository.findByStudentGroupId(GROUP_ID)).thenReturn(Optional.of(registration));
    when(groupMemberRepository.findAllByStudentGroupIdOrderByIsLeaderDescIdAsc(GROUP_ID))
        .thenReturn(List.of(new GroupMember(), new GroupMember(), new GroupMember()));

    service.requestRegistrationApproval(GROUP_ID, leader);

    assertThat(registration.getStatus()).isEqualTo(TopicRegistrationStatus.PENDING);
  }

  @Test
  void requestRegistrationApprovalRejectsGroupsWithFewerThanThreeMembers() {
    stubLeaderMembership();
    TopicRegistration registration = new TopicRegistration();
    registration.setStatus(TopicRegistrationStatus.DRAFT);
    when(topicRegistrationRepository.findByStudentGroupId(GROUP_ID)).thenReturn(Optional.of(registration));
    when(groupMemberRepository.findAllByStudentGroupIdOrderByIsLeaderDescIdAsc(GROUP_ID))
        .thenReturn(List.of(new GroupMember(), new GroupMember()));

    assertThatIllegalArgumentException()
        .isThrownBy(() -> service.requestRegistrationApproval(GROUP_ID, leader))
        .withMessage("student-groups.error.member-count");
  }

  @Test
  void requestRegistrationApprovalAcceptsFiveMembers() {
    stubLeaderMembership();
    TopicRegistration registration = new TopicRegistration();
    registration.setStatus(TopicRegistrationStatus.DRAFT);
    registration.setTopic(new Topic());
    registration.setStudentGroup(group);
    when(topicRegistrationRepository.findByStudentGroupId(GROUP_ID)).thenReturn(Optional.of(registration));
    when(groupMemberRepository.findAllByStudentGroupIdOrderByIsLeaderDescIdAsc(GROUP_ID))
        .thenReturn(List.of(new GroupMember(), new GroupMember(), new GroupMember(), new GroupMember(), new GroupMember()));

    service.requestRegistrationApproval(GROUP_ID, leader);

    assertThat(registration.getStatus()).isEqualTo(TopicRegistrationStatus.PENDING);
  }

  @Test
  void lecturerCannotApproveRegistrationAfterGroupDropsBelowThreeMembers() {
    TopicRegistration registration = new TopicRegistration();
    registration.setStudentGroup(group);
    registration.setStatus(TopicRegistrationStatus.PENDING);
    when(topicRegistrationRepository.findGroupIdByIdAndAdviserId(11L, LEADER_ID))
        .thenReturn(Optional.of(GROUP_ID));
    when(topicRegistrationRepository.findByIdAndAdviserId(11L, LEADER_ID))
        .thenReturn(Optional.of(registration));
    when(groupMemberRepository.findAllByStudentGroupIdOrderByIsLeaderDescIdAsc(GROUP_ID))
        .thenReturn(List.of(new GroupMember(), new GroupMember()));

    assertThatIllegalArgumentException()
        .isThrownBy(() -> service.approveRegistration(11L, leader))
        .withMessage("student-groups.error.member-count");
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
