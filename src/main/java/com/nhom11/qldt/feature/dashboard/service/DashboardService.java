package com.nhom11.qldt.feature.dashboard.service;

import com.nhom11.qldt.feature.announcement.service.AnnouncementService;
import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.auth.service.UserService;
import com.nhom11.qldt.feature.council.service.CouncilService;
import com.nhom11.qldt.feature.dashboard.config.DashboardProperties;
import com.nhom11.qldt.feature.dashboard.model.DashboardAction;
import com.nhom11.qldt.feature.dashboard.model.DashboardActionType;
import com.nhom11.qldt.feature.dashboard.model.DashboardData;
import com.nhom11.qldt.feature.department.service.DepartmentService;
import com.nhom11.qldt.feature.group.service.StudentGroupService;
import com.nhom11.qldt.feature.group.model.enums.StudentGroupProgress;
import com.nhom11.qldt.feature.period.model.entity.RegistrationPeriod;
import com.nhom11.qldt.feature.period.service.RegistrationPeriodService;
import com.nhom11.qldt.feature.topic.service.TopicService;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
/** Tổng hợp số liệu, công việc cần xử lý và thông báo trên dashboard theo từng vai trò. */
public class DashboardService {
  private final UserService userService;
  private final DepartmentService departmentService;
  private final RegistrationPeriodService periodService;
  private final TopicService topicService;
  private final StudentGroupService groupService;
  private final CouncilService councilService;
  private final AnnouncementService announcementService;
  private final DashboardProperties properties;

  public DashboardService(
      UserService userService,
      DepartmentService departmentService,
      RegistrationPeriodService periodService,
      TopicService topicService,
      StudentGroupService groupService,
      CouncilService councilService,
      AnnouncementService announcementService,
      DashboardProperties properties) {
    this.userService = userService;
    this.departmentService = departmentService;
    this.periodService = periodService;
    this.topicService = topicService;
    this.groupService = groupService;
    this.councilService = councilService;
    this.announcementService = announcementService;
    this.properties = properties;
  }

  @Transactional(readOnly = true)
  public DashboardData getDashboard(String email) {
    User user = userService.findByEmail(email).orElseThrow();

    // Gom vai trò thành tập để một tài khoản có thể nhận hành động từ nhiều nhóm quyền.
    Set<String> roleNames = user.getRoles().stream()
        .map(role -> role.getName())
        .collect(Collectors.toSet());
    Map<DashboardActionType, DashboardAction> actions = new LinkedHashMap<>();

    // LinkedHashMap vừa giữ thứ tự hiển thị vừa loại bỏ hành động trùng giữa các vai trò.
    if (roleNames.contains("ADMIN")) addAdminActions(actions);
    if (roleNames.contains("HEAD_OF_FACULTY")) addHeadOfFacultyActions(actions);
    if (roleNames.contains("LECTURER")) addLecturerActions(actions, user);
    if (roleNames.contains("STUDENT")) addStudentActions(actions, user);

    return new DashboardData(
        user,
        List.copyOf(actions.values()),
        announcementService.findRecentPublishedForRoles(
            roleNames, properties.getAnnouncementLimit()));
  }

  private void addAdminActions(Map<DashboardActionType, DashboardAction> actions) {
    add(actions, DashboardActionType.USERS, userService.count());
    add(actions, DashboardActionType.DEPARTMENTS, departmentService.count());
    add(actions, DashboardActionType.PERIODS, periodService.count());
  }

  private void addHeadOfFacultyActions(Map<DashboardActionType, DashboardAction> actions) {
    LocalDateTime now = LocalDateTime.now();
    List<RegistrationPeriod> periods = periodService.findAll();

    // Chỉ các đợt đã qua hạn báo cáo mới có khả năng đủ điều kiện công bố kết quả.
    List<RegistrationPeriod> publicationPeriods = periods.stream()
        .filter(period -> period.getReportDeadline() != null)
        .filter(period -> !now.isBefore(period.getReportDeadline()))
        .toList();
    List<Long> publicationPeriodIds = ids(publicationPeriods);
    Set<Long> publishedIds = councilService.publishedPeriodIds(publicationPeriodIds);

    // Loại các đợt đã công bố khỏi số lượng công việc đang chờ xử lý.
    long readyToPublish = councilService.publishablePeriodIds(publicationPeriodIds).stream()
        .filter(periodId -> !publishedIds.contains(periodId))
        .count();

    add(actions, DashboardActionType.TOPIC_APPROVAL, topicService.countPendingApprovals());
    add(actions, DashboardActionType.COUNCILS, periodService.countOpenCouncilPeriods());
    add(actions, DashboardActionType.PUBLICATION, readyToPublish);
  }

  private void addLecturerActions(
      Map<DashboardActionType, DashboardAction> actions, User lecturer) {
    add(
        actions,
        DashboardActionType.REGISTRATION_APPROVAL,
        groupService.countPendingAdviserRegistrations(lecturer.getId()));
    add(
        actions,
        DashboardActionType.TOPIC_PROPOSALS,
        topicService.countProposalsBy(lecturer.getId()));
    add(
        actions,
        DashboardActionType.EVALUATIONS,
        groupService.countPendingEvaluations(lecturer.getId()));
  }

  private void addStudentActions(Map<DashboardActionType, DashboardAction> actions, User student) {
    var groups = groupService.groupsForStudent(student);

    // Nạp trạng thái công bố theo lô để không truy vấn lại cho từng nhóm.
    Set<Long> publishedPeriodIds = councilService.publishedPeriodIds(groups.stream()
        .map(group -> group.getRegistrationPeriod().getId())
        .distinct()
        .toList());
    LocalDateTime now = LocalDateTime.now();

    // Phân nhóm một lần rồi dùng lại số lượng cho ba thẻ tiến độ.
    Map<StudentGroupProgress, Long> counts = groups.stream().collect(Collectors.groupingBy(
        group -> groupService.progress(group, publishedPeriodIds, now), Collectors.counting()));

    add(
        actions,
        DashboardActionType.GROUPS_IN_PROGRESS,
        counts.getOrDefault(StudentGroupProgress.IN_PROGRESS, 0L));
    add(
        actions,
        DashboardActionType.GROUPS_WAITING,
        counts.getOrDefault(StudentGroupProgress.WAITING, 0L));
    add(
        actions,
        DashboardActionType.GROUPS_PUBLISHED,
        counts.getOrDefault(StudentGroupProgress.PUBLISHED, 0L));
  }

  private void add(
      Map<DashboardActionType, DashboardAction> actions, DashboardActionType type, long value) {
    actions.putIfAbsent(type, new DashboardAction(type, value));
  }

  private List<Long> ids(List<RegistrationPeriod> periods) {
    return periods.stream().map(RegistrationPeriod::getId).toList();
  }
}
