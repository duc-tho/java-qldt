package com.nhom11.qldt.feature.period.controller;

import com.nhom11.qldt.core.table.TableQuery;
import com.nhom11.qldt.core.view.CrudViewSupport;
import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.auth.service.UserService;
import com.nhom11.qldt.feature.council.service.CouncilService;
import com.nhom11.qldt.feature.group.model.enums.TopicRegistrationStatus;
import com.nhom11.qldt.feature.group.service.StudentGroupService;
import com.nhom11.qldt.feature.period.model.dto.RegistrationPeriodForm;
import com.nhom11.qldt.feature.period.model.entity.RegistrationPeriod;
import com.nhom11.qldt.feature.period.service.RegistrationPeriodService;
import com.nhom11.qldt.feature.period.view.RegistrationPeriodTableDefinition;
import com.nhom11.qldt.feature.topic.service.TopicService;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PeriodPageController {
  private final RegistrationPeriodService periodService;
  private final RegistrationPeriodTableDefinition table;
  private final CrudViewSupport view;
  private final TopicService topicService;
  private final StudentGroupService groupService;
  private final UserService userService;
  private final CouncilService councilService;
  private final RegistrationPeriodController staffPages;

  public PeriodPageController(
      RegistrationPeriodService periodService,
      RegistrationPeriodTableDefinition table,
      CrudViewSupport view,
      TopicService topicService,
      StudentGroupService groupService,
      UserService userService,
      CouncilService councilService,
      RegistrationPeriodController staffPages) {
    this.periodService = periodService;
    this.table = table;
    this.view = view;
    this.topicService = topicService;
    this.groupService = groupService;
    this.userService = userService;
    this.councilService = councilService;
    this.staffPages = staffPages;
  }

  @GetMapping("/dot/{id}")
  public String detail(@PathVariable Long id, Authentication authentication, Model model) {
    if (hasRole(authentication, "ROLE_ADMIN") || hasRole(authentication, "ROLE_HEAD_OF_FACULTY")) {
      return staffPages.detail(id, model);
    }

    User viewer = userService.findByEmail(authentication.getName()).orElseThrow();
    RegistrationPeriod period =
        hasRole(authentication, "ROLE_LECTURER")
            ? periodService.findVisibleForLecturer(viewer.getId()).stream()
                .filter(item -> item.getId().equals(id))
                .findFirst()
                .orElse(null)
            : null;
    boolean isLecturer = period != null;
    if (period == null && hasRole(authentication, "ROLE_STUDENT")) {
      period =
          periodService.findVisibleForStudent(viewer.getId()).stream()
              .filter(item -> item.getId().equals(id))
              .findFirst()
              .orElse(null);
    }
    if (period == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);

    LocalDateTime now = LocalDateTime.now();
    model.addAttribute("isStaff", false);
    model.addAttribute("period", period);
    model.addAttribute("periodStage", periodService.stage(period, now));
    model.addAttribute("now", now);
    model.addAttribute("isLecturer", isLecturer);
    model.addAttribute("resultsPublished", councilService.resultsPublished(id));
    model.addAttribute("canPublishResults", false);
    model.addAttribute(
        "canPropose",
        isLecturer
            && !now.isBefore(period.getLecturerRegistrationStart())
            && !now.isAfter(period.getLecturerRegistrationEnd()));
    if (isLecturer) {
      model.addAttribute(
          "pendingCount",
          groupService.adviserRegistrations(viewer).stream()
              .filter(registration -> registration.getStatus() == TopicRegistrationStatus.PENDING)
              .filter(
                  registration ->
                      registration.getTopic().getRegistrationPeriod().getId().equals(id))
              .count());
      model.addAttribute(
          "assignedCount",
          councilService.workspaceRegistrations(viewer).stream()
              .filter(
                  registration ->
                      registration.getTopic().getRegistrationPeriod().getId().equals(id))
              .count());
    }
    model.addAttribute(
        "canCreateGroup",
        !isLecturer
            && !now.isBefore(period.getStudentRegistrationStart())
            && !now.isAfter(period.getStudentRegistrationEnd()));
    model.addAttribute(
        "groupId",
        isLecturer
            ? null
            : groupService.groupsForStudent(viewer).stream()
                .filter(group -> group.getRegistrationPeriod().getId().equals(id))
                .map(group -> group.getId())
                .findFirst()
                .orElse(null));
    model.addAttribute("pageTitle", "registration_periods.page.detail.title");
    model.addAttribute("pageHeading", period.getName());
    model.addAttribute("pageIcon", "bi:calendar-range");
    model.addAttribute("breadcrumbParentUrl", "/dot");
    model.addAttribute("breadcrumbParentLabel", "registration_periods.page.list.title");
    return "page/period/detail";
  }

  @GetMapping("/dot/new")
  public String oldCreateUrl() {
    return "redirect:/dot/tao";
  }

  @GetMapping("/dot/tao")
  public String createForm(Model model) {
    return staffPages.newForm(model);
  }

  @GetMapping("/dot/{id}/edit")
  public String oldEditUrl(@PathVariable Long id) {
    return "redirect:/dot/" + id + "/sua";
  }

  @GetMapping("/dot/{id}/sua")
  public String editForm(@PathVariable Long id, Model model) {
    return staffPages.edit(id, model);
  }

  @PostMapping("/dot")
  public String create(
      @Valid @ModelAttribute("data") RegistrationPeriodForm form,
      BindingResult errors,
      Model model) {
    return staffPages.create(form, errors, model);
  }

  @PostMapping("/dot/{id}")
  public String update(
      @PathVariable Long id,
      @Valid @ModelAttribute("data") RegistrationPeriodForm form,
      BindingResult errors,
      Model model) {
    return staffPages.update(id, form, errors, model);
  }

  @PostMapping("/dot/{id}/xoa")
  public String deleteCanonical(@PathVariable Long id, RedirectAttributes attributes) {
    staffPages.delete(id, attributes);
    return "redirect:/dot";
  }

  @GetMapping("/dot")
  public String list(
      @ModelAttribute TableQuery query,
      @RequestParam(name = "congViec", required = false) String work,
      @RequestParam(name = "coNhom", defaultValue = "false") boolean withGroup,
      Authentication authentication,
      Model model) {
    model.addAttribute("pageTitle", "registration_periods.page.list.title");
    model.addAttribute("pageDesc", "registration_periods.page.list.description");
    model.addAttribute("pageIcon", "bi:calendar-range");

    if (withGroup && hasRole(authentication, "ROLE_STUDENT")) {
      User viewer = userService.findByEmail(authentication.getName()).orElseThrow();
      return studentList(viewer, true, model);
    }

    if (hasRole(authentication, "ROLE_ADMIN") || hasRole(authentication, "ROLE_HEAD_OF_FACULTY")) {
      model.addAttribute("periodView", "staff");
      view.table(model, periodService.findAll(query, table), "/dot", query, table);
      return "page/period/list";
    }

    User viewer = userService.findByEmail(authentication.getName()).orElseThrow();
    if (hasRole(authentication, "ROLE_LECTURER")) {
      model.addAttribute("periodView", "lecturer");
      List<RegistrationPeriod> periods = periodService.findVisibleForLecturer(viewer.getId());
      if ("duyet".equals(work)) {
        Map<Long, Long> taskCounts =
            groupService.adviserRegistrations(viewer).stream()
                .filter(registration -> registration.getStatus() == TopicRegistrationStatus.PENDING)
                .collect(
                    Collectors.groupingBy(
                        registration -> registration.getTopic().getRegistrationPeriod().getId(),
                        Collectors.counting()));
        model.addAttribute("work", work);
        model.addAttribute("taskCounts", taskCounts);
        model.addAttribute(
            "workPeriods",
            periods.stream().filter(period -> taskCounts.containsKey(period.getId())).toList());
        model.addAttribute("pageTitle", "navigation.item.topic-registrations");
        return "page/period/list";
      } else if ("danh-gia".equals(work)) {
        Map<Long, Long> taskCounts =
            councilService.workspaceRegistrations(viewer).stream()
                .collect(
                    Collectors.groupingBy(
                        registration -> registration.getTopic().getRegistrationPeriod().getId(),
                        Collectors.counting()));
        model.addAttribute("work", work);
        model.addAttribute("taskCounts", taskCounts);
        model.addAttribute(
            "workPeriods",
            periods.stream().filter(period -> taskCounts.containsKey(period.getId())).toList());
        model.addAttribute("pageTitle", "navigation.item.council-workspace");
        return "page/period/list";
      }
      LocalDateTime now = LocalDateTime.now();
      model.addAttribute(
          "openPeriods",
          periods.stream()
              .filter(period -> !now.isAfter(period.getLecturerRegistrationEnd()))
              .sorted(Comparator.comparing(period -> period.getLecturerRegistrationEnd()))
              .toList());
      model.addAttribute(
          "closedPeriods",
          periods.stream()
              .filter(period -> now.isAfter(period.getLecturerRegistrationEnd()))
              .sorted(
                  Comparator.comparing(
                          (RegistrationPeriod period) -> period.getLecturerRegistrationEnd())
                      .reversed())
              .toList());
      model.addAttribute(
          "topicsByPeriodId",
          topicService
              .findVisibleProposalSummariesByPeriodIds(
                  periods.stream().map(period -> period.getId()).toList(), viewer.getId())
              .stream()
              .collect(Collectors.groupingBy(summary -> summary.periodId())));
      return "page/period/list";
    }

    return studentList(viewer, withGroup, model);
  }

  private String studentList(User viewer, boolean withGroup, Model model) {
    model.addAttribute("periodView", "student");
    List<RegistrationPeriod> periods = periodService.findVisibleForStudent(viewer.getId());
    var groupsByPeriod =
        groupService.groupsForStudent(viewer).stream()
            .collect(
                Collectors.toMap(
                    group -> group.getRegistrationPeriod().getId(), group -> group.getId()));
    if (withGroup) {
      periods =
          periods.stream().filter(period -> groupsByPeriod.containsKey(period.getId())).toList();
      Set<Long> visiblePeriodIds =
          periods.stream().map(period -> period.getId()).collect(Collectors.toSet());
      model.addAttribute(
          "groups",
          groupService.groupSummariesForStudent(viewer).stream()
              .filter(
                  summary ->
                      visiblePeriodIds.contains(summary.group().getRegistrationPeriod().getId()))
              .toList());
      model.addAttribute("pageTitle", "student-groups.page.mine.title");
    }
    model.addAttribute("showGroups", withGroup);
    model.addAttribute("periods", periods);
    model.addAttribute("groupsByPeriod", groupsByPeriod);
    model.addAttribute("now", LocalDateTime.now());
    return "page/period/list";
  }

  private boolean hasRole(Authentication authentication, String role) {
    return authentication.getAuthorities().stream()
        .anyMatch(authority -> authority.getAuthority().equals(role));
  }
}
