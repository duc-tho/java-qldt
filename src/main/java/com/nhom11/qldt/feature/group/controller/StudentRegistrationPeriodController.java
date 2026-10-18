package com.nhom11.qldt.feature.group.controller;

import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.auth.service.UserService;
import com.nhom11.qldt.feature.council.service.CouncilService;
import com.nhom11.qldt.feature.group.model.dto.GroupSummary;
import com.nhom11.qldt.feature.group.model.enums.StudentGroupProgress;
import com.nhom11.qldt.feature.group.service.StudentGroupService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StudentRegistrationPeriodController {
  private final StudentGroupService groupService;
  private final UserService userService;
  private final CouncilService councilService;

  public StudentRegistrationPeriodController(
      StudentGroupService groupService,
      UserService userService,
      CouncilService councilService) {
    this.groupService = groupService;
    this.userService = userService;
    this.councilService = councilService;
  }

  @GetMapping("/nhom-cua-toi")
  public String list(@AuthenticationPrincipal UserDetails principal, Model model) {
    User student = userService.findByEmail(principal.getUsername()).orElseThrow();
    List<GroupSummary> groups = groupService.groupSummariesForStudent(student);
    List<Long> periodIds = groups.stream()
        .map(summary -> summary.group().getRegistrationPeriod().getId())
        .distinct()
        .toList();
    Set<Long> publishedPeriodIds = councilService.publishedPeriodIds(periodIds);
    LocalDateTime now = LocalDateTime.now();
    List<GroupSummary> inProgress = new ArrayList<>();
    List<GroupSummary> waiting = new ArrayList<>();
    List<GroupSummary> published = new ArrayList<>();

    groups.forEach(summary -> {
      StudentGroupProgress progress =
          groupService.progress(summary.group(), publishedPeriodIds, now);
      switch (progress) {
        case PUBLISHED -> published.add(summary);
        case WAITING -> waiting.add(summary);
        case IN_PROGRESS -> inProgress.add(summary);
      }
    });

    model.addAttribute("inProgressGroups", inProgress);
    model.addAttribute("waitingGroups", waiting);
    model.addAttribute("publishedGroups", published);
    model.addAttribute("pageTitle", "student-groups.page.mine.title");
    model.addAttribute("pageDesc", "work.groups.description");
    model.addAttribute("pageIcon", "bi:people");
    return "page/group/mine";
  }
}
