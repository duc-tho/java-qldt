package com.nhom11.qldt.feature.studentgroup.service;

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
import com.nhom11.qldt.feature.topic.model.entity.Topic;
import com.nhom11.qldt.feature.topic.model.enums.TopicStatus;
import com.nhom11.qldt.feature.topic.repository.TopicRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentGroupService {
  private static final int MAX_MEMBERS = 3;

  private final StudentGroupRepository studentGroupRepository;
  private final GroupMemberRepository groupMemberRepository;
  private final TopicRegistrationRepository topicRegistrationRepository;
  private final TopicRepository topicRepository;
  private final UserRepository userRepository;
  private final RegistrationPeriodService registrationPeriodService;

  public StudentGroupService(
      StudentGroupRepository studentGroupRepository,
      GroupMemberRepository groupMemberRepository,
      TopicRegistrationRepository topicRegistrationRepository,
      TopicRepository topicRepository,
      UserRepository userRepository,
      RegistrationPeriodService registrationPeriodService) {
    this.studentGroupRepository = studentGroupRepository;
    this.groupMemberRepository = groupMemberRepository;
    this.topicRegistrationRepository = topicRegistrationRepository;
    this.topicRepository = topicRepository;
    this.userRepository = userRepository;
    this.registrationPeriodService = registrationPeriodService;
  }

  @Transactional
  public StudentGroup createGroup(Long periodId, User leader, String name) {
    RegistrationPeriod period = requirePeriod(periodId);
    requireOpenWindow(period);
    requireStudent(leader);
    if (groupMemberRepository.findByStudentIdAndPeriodId(leader.getId(), periodId).isPresent()) {
      throw new IllegalArgumentException("student-groups.error.already-member");
    }

    StudentGroup group = new StudentGroup();
    group.setRegistrationPeriod(period);
    group.setName(name.trim());
    studentGroupRepository.save(group);

    GroupMember member = new GroupMember();
    member.setStudentGroup(group);
    member.setStudent(leader);
    member.setLeader(true);
    groupMemberRepository.save(member);
    return group;
  }

  @Transactional
  public void addMember(Long groupId, User leader, String studentCode) {
    StudentGroup group = requireGroup(groupId);
    requireOpenWindow(group.getRegistrationPeriod());
    requireLeader(groupId, leader);
    List<GroupMember> members = members(groupId);
    if (members.size() >= MAX_MEMBERS) {
      throw new IllegalArgumentException("student-groups.error.member-limit");
    }

    User student =
        userRepository
            .findByUserCode(studentCode.trim())
            .orElseThrow(
                () -> new IllegalArgumentException("student-groups.error.student-not-found"));
    requireStudent(student);
    if (groupMemberRepository
        .findByStudentIdAndPeriodId(student.getId(), group.getRegistrationPeriod().getId())
        .isPresent()) {
      throw new IllegalArgumentException("student-groups.error.already-member");
    }

    GroupMember member = new GroupMember();
    member.setStudentGroup(group);
    member.setStudent(student);
    member.setLeader(false);
    groupMemberRepository.save(member);
    resetRegistrationReview(groupId);
  }

  @Transactional
  public void removeMember(Long groupId, User leader, Long memberId) {
    StudentGroup group = requireGroup(groupId);
    requireOpenWindow(group.getRegistrationPeriod());
    requireLeader(groupId, leader);
    GroupMember member =
        groupMemberRepository
            .findById(memberId)
            .filter(value -> value.getStudentGroup().getId().equals(groupId))
            .orElseThrow(
                () -> new IllegalArgumentException("student-groups.error.member-not-found"));
    if (member.isLeader()) {
      throw new IllegalArgumentException("student-groups.error.leader-removal");
    }
    groupMemberRepository.delete(member);
    resetRegistrationReview(groupId);
  }

  @Transactional
  public void transferLeadership(Long groupId, User leader, Long memberId) {
    StudentGroup group = requireGroup(groupId);
    requireOpenWindow(group.getRegistrationPeriod());
    GroupMember currentLeader = requireLeader(groupId, leader);
    GroupMember newLeader =
        groupMemberRepository
            .findById(memberId)
            .filter(value -> value.getStudentGroup().getId().equals(groupId))
            .orElseThrow(
                () -> new IllegalArgumentException("student-groups.error.member-not-found"));
    if (newLeader.isLeader()) {
      throw new IllegalArgumentException("student-groups.error.leader-already-assigned");
    }
    currentLeader.setLeader(false);
    newLeader.setLeader(true);
    resetRegistrationReview(groupId);
  }

  @Transactional
  public void selectTopic(Long groupId, User leader, Long topicId) {
    StudentGroup group = requireGroup(groupId);
    requireOpenWindow(group.getRegistrationPeriod());
    requireLeader(groupId, leader);
    Topic topic =
        topicRepository
            .findByIdAndRegistrationPeriodId(topicId, group.getRegistrationPeriod().getId())
            .filter(value -> value.getStatus() == TopicStatus.APPROVED)
            .orElseThrow(
                () -> new IllegalArgumentException("student-groups.error.topic-not-available"));

    TopicRegistration registration =
        topicRegistrationRepository
            .findByStudentGroupId(groupId)
            .orElseGet(
                () -> {
                  TopicRegistration value = new TopicRegistration();
                  value.setStudentGroup(group);
                  return value;
                });
    registration.setTopic(topic);
    resetRegistrationReview(registration);
    topicRegistrationRepository.save(registration);
  }

  @Transactional
  public void approveRegistration(Long registrationId, User lecturer) {
    TopicRegistration registration = requireAdviserRegistration(registrationId, lecturer);
    requireOpenWindow(registration.getStudentGroup().getRegistrationPeriod());
    requirePending(registration);
    registration.setStatus(TopicRegistrationStatus.APPROVED);
    registration.setReviewedBy(lecturer);
    registration.setReviewedAt(LocalDateTime.now());
  }

  @Transactional
  public void requestRegistrationApproval(Long groupId, User leader) {
    StudentGroup group = requireGroup(groupId);
    requireOpenWindow(group.getRegistrationPeriod());
    requireLeader(groupId, leader);
    TopicRegistration registration =
        topicRegistrationRepository
            .findByStudentGroupId(groupId)
            .orElseThrow(
                () -> new IllegalArgumentException("topic-registrations.error.registration-required"));
    requireDraft(registration);
    registration.setStatus(TopicRegistrationStatus.PENDING);
  }

  @Transactional
  public void rejectRegistration(Long registrationId, User lecturer, String reason) {
    TopicRegistration registration = requireAdviserRegistration(registrationId, lecturer);
    requireOpenWindow(registration.getStudentGroup().getRegistrationPeriod());
    requirePending(registration);
    if (reason == null || reason.isBlank()) {
      throw new IllegalArgumentException("topic-registrations.validation.reason.required");
    }
    registration.setStatus(TopicRegistrationStatus.REJECTED);
    registration.setRejectionReason(reason.trim());
    registration.setReviewedBy(lecturer);
    registration.setReviewedAt(LocalDateTime.now());
  }

  public StudentGroup findCurrentGroup(Long periodId, User student) {
    return groupMemberRepository
        .findByStudentIdAndPeriodId(student.getId(), periodId)
        .map(GroupMember::getStudentGroup)
        .orElse(null);
  }

  public StudentGroup findGroup(Long groupId, Long periodId) {
    return studentGroupRepository.findByIdAndRegistrationPeriodId(groupId, periodId).orElse(null);
  }

  public List<GroupMember> members(Long groupId) {
    return groupMemberRepository.findAllByStudentGroupIdOrderByIsLeaderDescIdAsc(groupId);
  }

  public TopicRegistration registration(Long groupId) {
    return topicRegistrationRepository.findByStudentGroupId(groupId).orElse(null);
  }

  public List<Topic> approvedTopics(Long periodId, String search, Long departmentId) {
    return topicRepository.findAllByRegistrationPeriodIdAndStatus(
        periodId,
        TopicStatus.APPROVED,
        search == null || search.isBlank() ? null : search.trim(),
        departmentId);
  }

  public List<TopicRegistration> adviserRegistrations(User lecturer) {
    return topicRegistrationRepository.findAllByAdviserIdAndStatus(
        lecturer.getId(), TopicRegistrationStatus.PENDING);
  }

  public List<TopicRegistration> registrations(Long periodId) {
    return topicRegistrationRepository.findAllByRegistrationPeriodId(periodId);
  }

  public TopicRegistration adviserRegistration(Long registrationId, User lecturer) {
    return topicRegistrationRepository
        .findByIdAndAdviserId(registrationId, lecturer.getId())
        .filter(registration -> registration.getStatus() == TopicRegistrationStatus.PENDING)
        .orElse(null);
  }

  public boolean isOpen(RegistrationPeriod period) {
    LocalDateTime now = LocalDateTime.now();
    return !now.isBefore(period.getStudentRegistrationStart())
        && !now.isAfter(period.getStudentRegistrationEnd());
  }

  private StudentGroup requireGroup(Long groupId) {
    return studentGroupRepository
        .findById(groupId)
        .orElseThrow(() -> new IllegalArgumentException("student-groups.error.not-found"));
  }

  private RegistrationPeriod requirePeriod(Long periodId) {
    RegistrationPeriod period = registrationPeriodService.findById(periodId);
    if (period == null) {
      throw new IllegalArgumentException("student-groups.error.period-not-found");
    }
    return period;
  }

  private GroupMember requireLeader(Long groupId, User user) {
    return groupMemberRepository
        .findByStudentGroupIdAndStudentId(groupId, user.getId())
        .filter(GroupMember::isLeader)
        .orElseThrow(() -> new IllegalArgumentException("student-groups.error.leader-required"));
  }

  private TopicRegistration requireAdviserRegistration(Long registrationId, User lecturer) {
    return topicRegistrationRepository
        .findByIdAndAdviserId(registrationId, lecturer.getId())
        .orElseThrow(
            () -> new IllegalArgumentException("topic-registrations.error.adviser-required"));
  }

  private void requirePending(TopicRegistration registration) {
    if (registration.getStatus() != TopicRegistrationStatus.PENDING) {
      throw new IllegalArgumentException("topic-registrations.error.pending-required");
    }
  }

  private void requireDraft(TopicRegistration registration) {
    if (registration.getStatus() != TopicRegistrationStatus.DRAFT) {
      throw new IllegalArgumentException("topic-registrations.error.draft-required");
    }
  }

  private void requireStudent(User user) {
    boolean isStudent = user.getRoles().stream().anyMatch(role -> "STUDENT".equals(role.getName()));
    if (!isStudent) {
      throw new IllegalArgumentException("student-groups.error.student-required");
    }
  }

  private void requireOpenWindow(RegistrationPeriod period) {
    if (!isOpen(period)) {
      throw new IllegalArgumentException("student-groups.error.period-locked");
    }
  }

  private void resetRegistrationReview(Long groupId) {
    topicRegistrationRepository
        .findByStudentGroupId(groupId)
        .ifPresent(this::resetRegistrationReview);
  }

  private void resetRegistrationReview(TopicRegistration registration) {
    registration.setStatus(TopicRegistrationStatus.DRAFT);
    registration.setRejectionReason(null);
    registration.setReviewedBy(null);
    registration.setReviewedAt(null);
  }
}
