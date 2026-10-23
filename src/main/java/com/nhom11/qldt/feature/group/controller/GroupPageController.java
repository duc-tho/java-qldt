package com.nhom11.qldt.feature.group.controller;

import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.auth.service.UserService;
import com.nhom11.qldt.feature.group.model.dto.GroupSummary;
import com.nhom11.qldt.feature.group.service.StudentGroupService;
import com.nhom11.qldt.feature.period.model.entity.RegistrationPeriod;
import com.nhom11.qldt.feature.period.service.RegistrationPeriodService;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@Controller
/** Điều phối các yêu cầu HTTP của chức năng nhóm sinh viên và đăng ký đề tài. */
public class GroupPageController {
  private final StudentGroupService groupService;
  private final RegistrationPeriodService periodService;
  private final UserService userService;

  public GroupPageController(
      StudentGroupService groupService,
      RegistrationPeriodService periodService,
      UserService userService) {
    this.groupService = groupService;
    this.periodService = periodService;
    this.userService = userService;
  }

  @GetMapping("/dot/{periodId}/nhom")
  /** Hiển thị danh sách nhóm sinh viên và đăng ký đề tài mà người dùng được phép truy cập. */
  public String list(
      @PathVariable Long periodId,
      @RequestParam(name = "canDuyet", defaultValue = "false") boolean pendingOnly,
      Authentication authentication,
      Model model) {
    RegistrationPeriod period = periodService.findById(periodId);
    if (period == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);

    boolean isStaff = hasRole(authentication, "ROLE_ADMIN")
        || hasRole(authentication, "ROLE_HEAD_OF_FACULTY");
    boolean isLecturer = hasRole(authentication, "ROLE_LECTURER");
    boolean hasStudentRole = hasRole(authentication, "ROLE_STUDENT");
    boolean isStudent = !isStaff && !isLecturer && hasStudentRole;
    if (!isStaff && !isLecturer && !isStudent) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    List<GroupSummary> groups;
    if (isStaff) {
      groups = groupService.groupSummaries(periodId);
    } else {
      User viewer = userService.findByEmail(authentication.getName()).orElseThrow();
      boolean visibleAsLecturer = isLecturer && periodService.findVisibleForLecturer(viewer.getId())
          .stream().anyMatch(item -> item.getId().equals(periodId));
      boolean visibleAsStudent = hasStudentRole && periodService.findVisibleForStudent(viewer.getId())
          .stream().anyMatch(item -> item.getId().equals(periodId));
      if (!visibleAsLecturer && !visibleAsStudent) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
      }
      if (isLecturer) {
        groups = visibleAsLecturer
            ? groupService.groupSummariesForLecturer(periodId, viewer, pendingOnly) : List.of();
        if (visibleAsStudent && !pendingOnly) {
          List<GroupSummary> ownGroups = groupService.groupSummariesForStudent(viewer).stream()
              .filter(summary -> summary.group().getRegistrationPeriod().getId().equals(periodId))
              .toList();
          Set<Long> relatedIds = groups.stream()
              .map(summary -> summary.group().getId()).collect(Collectors.toSet());
          groups = Stream.concat(groups.stream(), ownGroups.stream()
              .filter(summary -> !relatedIds.contains(summary.group().getId()))).toList();
          model.addAttribute("canCreateGroup", ownGroups.isEmpty() && groupService.isOpen(period));
        }
      } else {
        groups = groupService.groupSummariesForStudent(viewer).stream()
            .filter(summary -> summary.group().getRegistrationPeriod().getId().equals(periodId))
            .toList();
        model.addAttribute("canCreateGroup", groups.isEmpty() && groupService.isOpen(period));
      }
    }

    model.addAttribute("groupSummaries", groups);
    model.addAttribute("isStudent", isStudent);
    model.addAttribute("period", period);
    model.addAttribute("pageTitle", "student-groups.page.list.title");
    model.addAttribute("pageIcon", "bi:people");
    model.addAttribute("breadcrumbParentUrl", "/dot/" + periodId);
    model.addAttribute("breadcrumbParentLabel", period.getName());
    return "page/group/list";
  }

  private boolean hasRole(Authentication authentication, String role) {
    return authentication.getAuthorities().stream()
        .anyMatch(authority -> authority.getAuthority().equals(role));
  }
}
