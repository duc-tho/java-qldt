package com.nhom11.qldt.feature.studentgroup.controller;

import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.auth.service.UserService;
import com.nhom11.qldt.feature.registrationperiod.service.RegistrationPeriodService;
import com.nhom11.qldt.feature.studentgroup.model.entity.StudentGroup;
import com.nhom11.qldt.feature.studentgroup.service.StudentGroupService;
import java.util.stream.Collectors;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/student-groups")
public class StudentRegistrationPeriodController {
  private final RegistrationPeriodService registrationPeriodService;
  private final StudentGroupService studentGroupService;
  private final UserService userService;

  public StudentRegistrationPeriodController(
      RegistrationPeriodService registrationPeriodService,
      StudentGroupService studentGroupService,
      UserService userService) {
    this.registrationPeriodService = registrationPeriodService;
    this.studentGroupService = studentGroupService;
    this.userService = userService;
  }

  @GetMapping
  public String list(@AuthenticationPrincipal UserDetails principal, Model model) {
    User student = userService.findByEmail(principal.getUsername()).orElseThrow();
    model.addAttribute("groups", studentGroupService.groupSummariesForStudent(student));
    model.addAttribute("pageTitle", "student-groups.page.mine.title");
    model.addAttribute("pageIcon", "bi:people");
    return "fragment/page/student-group/mine";
  }

  @GetMapping("/periods")
  public String periods(@AuthenticationPrincipal UserDetails principal, Model model) {
    User student = userService.findByEmail(principal.getUsername()).orElseThrow();
    model.addAttribute("periods", registrationPeriodService.findOpenForStudents());
    model.addAttribute(
        "groupsByPeriod",
        studentGroupService.groupsForStudent(student).stream()
            .collect(Collectors.toMap(
                group -> group.getRegistrationPeriod().getId(), StudentGroup::getId)));
    model.addAttribute("pageTitle", "student-groups.page.periods.title");
    model.addAttribute("pageIcon", "bi:calendar-range");
    return "fragment/page/student-group/periods";
  }
}
