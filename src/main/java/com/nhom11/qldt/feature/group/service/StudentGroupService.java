package com.nhom11.qldt.feature.group.service;

import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.auth.repository.UserRepository;
import com.nhom11.qldt.feature.council.repository.CouncilMemberRepository;
import com.nhom11.qldt.feature.period.model.entity.RegistrationPeriod;
import com.nhom11.qldt.feature.period.service.RegistrationPeriodService;
import com.nhom11.qldt.feature.group.model.dto.GroupSummary;
import com.nhom11.qldt.feature.group.model.enums.GroupActivityType;
import com.nhom11.qldt.feature.group.model.entity.GroupMember;
import com.nhom11.qldt.feature.group.model.entity.StudentGroup;
import com.nhom11.qldt.feature.group.model.entity.TopicRegistration;
import com.nhom11.qldt.feature.group.model.enums.TopicRegistrationStatus;
import com.nhom11.qldt.feature.group.repository.GroupMemberRepository;
import com.nhom11.qldt.feature.group.repository.StudentGroupRepository;
import com.nhom11.qldt.feature.group.repository.TopicRegistrationRepository;
import com.nhom11.qldt.feature.topic.model.entity.Topic;
import com.nhom11.qldt.feature.topic.model.enums.TopicStatus;
import com.nhom11.qldt.feature.topic.repository.TopicRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentGroupService {
  private static final int MIN_MEMBERS = 3;
  private static final int MAX_MEMBERS = 5;

  private final StudentGroupRepository studentGroupRepository;
  private final GroupMemberRepository groupMemberRepository;
  private final TopicRegistrationRepository topicRegistrationRepository;
  private final TopicRepository topicRepository;
  private final UserRepository userRepository;
  private final RegistrationPeriodService registrationPeriodService;
  private final GroupActivityService groupActivityService;
  private final CouncilMemberRepository councilMemberRepository;

  public StudentGroupService(
      StudentGroupRepository studentGroupRepository,
      GroupMemberRepository groupMemberRepository,
      TopicRegistrationRepository topicRegistrationRepository,
      TopicRepository topicRepository,
      UserRepository userRepository,
      RegistrationPeriodService registrationPeriodService,
      GroupActivityService groupActivityService,
      CouncilMemberRepository councilMemberRepository) {
    this.studentGroupRepository = studentGroupRepository;
    this.groupMemberRepository = groupMemberRepository;
    this.topicRegistrationRepository = topicRegistrationRepository;
    this.topicRepository = topicRepository;
    this.userRepository = userRepository;
    this.registrationPeriodService = registrationPeriodService;
    this.groupActivityService = groupActivityService;
    this.councilMemberRepository = councilMemberRepository;
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
    member.setRegistrationPeriod(period);
    member.setStudent(leader);
    member.setLeader(true);
    groupMemberRepository.save(member);
    groupActivityService.record(group, leader, null, GroupActivityType.CREATED, null);
    return group;
  }

  @Transactional
  public void addMember(Long groupId, User leader, String studentCode) {
    StudentGroup group = requireGroup(groupId);
    requireOpenWindow(group.getRegistrationPeriod());
    requireLeader(groupId, leader);
    requireEditableRegistration(groupId);
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
    member.setRegistrationPeriod(group.getRegistrationPeriod());
    member.setStudent(student);
    member.setLeader(false);
    groupMemberRepository.save(member);
    groupActivityService.record(group, leader, student, GroupActivityType.MEMBER_JOINED, null);
    resetRegistrationReview(groupId);
  }

  @Transactional
  public void removeMember(Long groupId, User leader, Long memberId) {
    StudentGroup group = requireGroup(groupId);
    requireOpenWindow(group.getRegistrationPeriod());
    requireLeader(groupId, leader);
    requireEditableRegistration(groupId);
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
    groupActivityService.record(group, leader, member.getStudent(), GroupActivityType.MEMBER_LEFT, null);
    resetRegistrationReview(groupId);
  }

  @Transactional
  public void transferLeadership(Long groupId, User leader, Long memberId) {
    StudentGroup group = requireGroup(groupId);
    requireOpenWindow(group.getRegistrationPeriod());
    GroupMember currentLeader = requireLeader(groupId, leader);
    requireEditableRegistration(groupId);
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
    groupActivityService.record(group, leader, newLeader.getStudent(), GroupActivityType.LEADER_CHANGED, null);
    resetRegistrationReview(groupId);
  }

  @Transactional
  public void selectTopic(Long groupId, User leader, Long topicId) {
    StudentGroup group = requireGroup(groupId);
    requireOpenWindow(group.getRegistrationPeriod());
    requireLeader(groupId, leader);
    requireEditableRegistration(groupId);
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
    groupActivityService.record(group, leader, null, GroupActivityType.TOPIC_SELECTED, topic.getTitle());
  }

  @Transactional
  public void approveRegistration(Long registrationId, User lecturer) {
    requireGroup(registrationGroupId(registrationId, lecturer));
    TopicRegistration registration = requireAdviserRegistration(registrationId, lecturer);
    requireOpenWindow(registration.getStudentGroup().getRegistrationPeriod());
    requirePending(registration);
    requireValidMemberCount(registration.getStudentGroup().getId());
    registration.setStatus(TopicRegistrationStatus.APPROVED);
    registration.setReviewedBy(lecturer);
    registration.setReviewedAt(LocalDateTime.now());
    groupActivityService.record(registration.getStudentGroup(), lecturer, null, GroupActivityType.APPROVED, registration.getTopic().getTitle());
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
    requireValidMemberCount(groupId);
    registration.setStatus(TopicRegistrationStatus.PENDING);
    groupActivityService.record(group, leader, null, GroupActivityType.APPROVAL_REQUESTED, registration.getTopic().getTitle());
  }

  @Transactional
  public void rejectRegistration(Long registrationId, User lecturer, String reason) {
    requireGroup(registrationGroupId(registrationId, lecturer));
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
    groupActivityService.record(registration.getStudentGroup(), lecturer, null, GroupActivityType.REJECTED, reason.trim());
  }

  public StudentGroup findCurrentGroup(Long periodId, User student) {
    return groupMemberRepository
        .findByStudentIdAndPeriodId(student.getId(), periodId)
        .map(member -> member.getStudentGroup())
        .orElse(null);
  }

  public List<StudentGroup> groupsForStudent(User student) {
    return groupMemberRepository.findAllByStudentIdOrderByCreatedAtDesc(student.getId()).stream()
        .map(member -> member.getStudentGroup()).toList();
  }

  public StudentGroup findGroup(Long groupId, Long periodId) {
    return studentGroupRepository.findByIdAndRegistrationPeriodId(groupId, periodId).orElse(null);
  }

  public boolean canViewGroup(StudentGroup group, User viewer) {
    if (viewer.getRoles().stream().anyMatch(role -> "HEAD_OF_FACULTY".equals(role.getName()) || "ADMIN".equals(role.getName()))) {
      return true;
    }
    if (groupMemberRepository.findByStudentGroupIdAndStudentId(group.getId(), viewer.getId()).isPresent()) {
      return true;
    }
    return topicRegistrationRepository.findByStudentGroupId(group.getId())
        .map(registration -> {
          Topic topic = registration.getTopic();
          return topic.getAdvisers().stream().anyMatch(adviser -> adviser.getId().equals(viewer.getId()))
              || (topic.getReviewer() != null && topic.getReviewer().getId().equals(viewer.getId()))
              || (topic.getCouncil() != null && councilMemberRepository.existsByCouncilIdAndLecturerId(topic.getCouncil().getId(), viewer.getId()));
        })
        .orElse(false);
  }

  public List<GroupMember> members(Long groupId) {
    return groupMemberRepository.findAllByStudentGroupIdOrderByIsLeaderDescIdAsc(groupId);
  }

  public TopicRegistration registration(Long groupId) {
    return topicRegistrationRepository.findByStudentGroupId(groupId).orElse(null);
  }

  public List<TopicRegistration> adviserRegistrations(User lecturer) {
    return topicRegistrationRepository.findAllByAdviserId(lecturer.getId());
  }

  public List<TopicRegistration> registrations(Long periodId) {
    return topicRegistrationRepository.findAllByRegistrationPeriodId(periodId);
  }

  public List<TopicRegistration> registrationsForTopic(Long topicId) {
    return topicRegistrationRepository.findAllByTopicId(topicId);
  }

  public List<GroupSummary> groupSummaries(Long periodId) {
    return summarizeGroups(studentGroupRepository.findAllByRegistrationPeriodIdOrderByName(periodId));
  }

  public long countByPeriod(Long periodId) {
    return studentGroupRepository.countByRegistrationPeriodId(periodId);
  }

  public List<GroupSummary> groupSummariesForStudent(User student) {
    return summarizeGroups(groupsForStudent(student));
  }

  public List<GroupSummary> groupSummariesForLecturer(Long periodId, User lecturer, boolean pendingOnly) {
    List<StudentGroup> groups = pendingOnly
        ? studentGroupRepository.findAllPendingForAdviser(periodId, lecturer.getId())
        : studentGroupRepository.findAllRelatedToLecturer(periodId, lecturer.getId());
    return summarizeGroups(groups);
  }

  private List<GroupSummary> summarizeGroups(List<StudentGroup> groups) {
    if (groups.isEmpty()) return List.of();

    var groupIds = groups.stream().map(group -> group.getId()).toList();
    Map<Long, List<GroupMember>> membersByGroup = groupMemberRepository.findAllByStudentGroupIdIn(groupIds)
        .stream().collect(Collectors.groupingBy(member -> member.getStudentGroup().getId()));
    Map<Long, TopicRegistration> registrationsByGroup = topicRegistrationRepository.findAllByStudentGroupIdIn(groupIds).stream()
        .collect(Collectors.toMap(registration -> registration.getStudentGroup().getId(), registration -> registration));

    return groups.stream().map(group -> {
      var members = membersByGroup.getOrDefault(group.getId(), List.of());
      String leaderName = members.stream().filter(member -> member.isLeader())
          .map(member -> member.getStudent().getFullName()).findFirst().orElse("");
      String leaderCode = members.stream().filter(member -> member.isLeader())
          .map(member -> member.getStudent().getUserCode()).filter(code -> code != null)
          .findFirst().orElse("");
      return new GroupSummary(group, leaderName, leaderCode, members.size(),
          members.stream().map(member -> member.getStudent()).toList(), registrationsByGroup.get(group.getId()));
    }).toList();
  }

  public TopicRegistration adviserRegistration(Long registrationId, User lecturer) {
    return topicRegistrationRepository
        .findByIdAndAdviserId(registrationId, lecturer.getId())
        .orElse(null);
  }

  public boolean isOpen(RegistrationPeriod period) {
    LocalDateTime now = LocalDateTime.now();
    return !now.isBefore(period.getStudentRegistrationStart())
        && !now.isAfter(period.getStudentRegistrationEnd());
  }

  private StudentGroup requireGroup(Long groupId) {
    return studentGroupRepository
        .findByIdForUpdate(groupId)
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
        .filter(member -> member.isLeader())
        .orElseThrow(() -> new IllegalArgumentException("student-groups.error.leader-required"));
  }

  private TopicRegistration requireAdviserRegistration(Long registrationId, User lecturer) {
    return topicRegistrationRepository
        .findByIdAndAdviserId(registrationId, lecturer.getId())
        .orElseThrow(
            () -> new IllegalArgumentException("topic-registrations.error.adviser-required"));
  }

  private Long registrationGroupId(Long registrationId, User lecturer) {
    return topicRegistrationRepository.findGroupIdByIdAndAdviserId(registrationId, lecturer.getId())
        .orElseThrow(() -> new IllegalArgumentException("topic-registrations.error.adviser-required"));
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

  private void requireValidMemberCount(Long groupId) {
    int count = members(groupId).size();
    if (count < MIN_MEMBERS || count > MAX_MEMBERS) {
      throw new IllegalArgumentException("student-groups.error.member-count");
    }
  }

  private void requireEditableRegistration(Long groupId) {
    topicRegistrationRepository.findByStudentGroupId(groupId).ifPresent(registration -> {
      if (registration.getStatus() == TopicRegistrationStatus.APPROVED) {
        throw new IllegalArgumentException("student-groups.error.approved-locked");
      }
    });
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
