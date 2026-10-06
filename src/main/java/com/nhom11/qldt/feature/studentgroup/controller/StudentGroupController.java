package com.nhom11.qldt.feature.studentgroup.controller;

import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.auth.service.UserService;
import com.nhom11.qldt.feature.council.service.CouncilService;
import com.nhom11.qldt.feature.department.service.DepartmentService;
import com.nhom11.qldt.feature.registrationperiod.model.entity.RegistrationPeriod;
import com.nhom11.qldt.feature.registrationperiod.service.RegistrationPeriodService;
import com.nhom11.qldt.feature.reportsubmission.service.ReportSubmissionService;
import com.nhom11.qldt.feature.studentgroup.model.dto.AddGroupMemberForm;
import com.nhom11.qldt.feature.studentgroup.model.dto.CreateStudentGroupForm;
import com.nhom11.qldt.feature.studentgroup.model.entity.GroupMember;
import com.nhom11.qldt.feature.studentgroup.model.entity.StudentGroup;
import com.nhom11.qldt.feature.studentgroup.model.entity.TopicRegistration;
import com.nhom11.qldt.feature.studentgroup.model.enums.TopicRegistrationStatus;
import com.nhom11.qldt.feature.studentgroup.service.StudentGroupService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/registration-periods/{periodId}/student-group")
public class StudentGroupController {
  private final StudentGroupService studentGroupService;
  private final RegistrationPeriodService registrationPeriodService;
  private final UserService userService;
  private final DepartmentService departmentService;
  private final ReportSubmissionService reportSubmissionService;
  private final CouncilService councilService;

  public StudentGroupController(
      StudentGroupService studentGroupService,
      RegistrationPeriodService registrationPeriodService,
      UserService userService,
      DepartmentService departmentService,
      ReportSubmissionService reportSubmissionService,
      CouncilService councilService) {
    this.studentGroupService = studentGroupService;
    this.registrationPeriodService = registrationPeriodService;
    this.userService = userService;
    this.departmentService = departmentService;
    this.reportSubmissionService = reportSubmissionService;
    this.councilService = councilService;
  }

  @GetMapping
  public String index(
      @PathVariable Long periodId, @AuthenticationPrincipal UserDetails principal, Model model) {
    RegistrationPeriod period = findPeriod(periodId);
    if (period == null || principal == null) return "redirect:/registration-periods";
    StudentGroup group = studentGroupService.findCurrentGroup(periodId, currentUser(principal));
    if (group != null) return detail(periodId, group.getId(), principal, model);
    model.addAttribute("period", period);
    model.addAttribute("isOpen", studentGroupService.isOpen(period));
    model.addAttribute("pageTitle", "student-groups.page.index.title");
    model.addAttribute("pageIcon", "bi:people");
    return "fragment/page/student-group/index";
  }

  @GetMapping("/new")
  public String createForm(@PathVariable Long periodId, Model model) {
    if (findPeriod(periodId) == null) return "redirect:/registration-periods";
    if (!model.containsAttribute("data")) {
      model.addAttribute("data", new CreateStudentGroupForm());
    }
    model.addAttribute("periodId", periodId);
    model.addAttribute("pageTitle", "student-groups.page.create.title");
    model.addAttribute("pageIcon", "bi:people");
    return "fragment/page/student-group/create";
  }

  @PostMapping("/new")
  public String create(
      @PathVariable Long periodId,
      @AuthenticationPrincipal UserDetails principal,
      @Valid @ModelAttribute("data") CreateStudentGroupForm form,
      BindingResult errors,
      Model model) {
    if (principal == null || findPeriod(periodId) == null) return "redirect:/registration-periods";
    if (errors.hasErrors()) return createForm(periodId, model);
    try {
      StudentGroup group =
          studentGroupService.createGroup(periodId, currentUser(principal), form.getName());
      return redirectDetail(periodId, group.getId());
    } catch (IllegalArgumentException exception) {
      errors.reject(exception.getMessage());
      model.addAttribute("periodId", periodId);
      model.addAttribute("pageTitle", "student-groups.page.create.title");
      model.addAttribute("pageIcon", "bi:people");
      return "fragment/page/student-group/create";
    }
  }

  @GetMapping("/{groupId}")
  public String detail(
      @PathVariable Long periodId,
      @PathVariable Long groupId,
      @AuthenticationPrincipal UserDetails principal,
      Model model) {
    if (principal == null) return "redirect:/login";
    StudentGroup group = studentGroupService.findGroup(groupId, periodId);
    StudentGroup currentGroup =
        studentGroupService.findCurrentGroup(periodId, currentUser(principal));
    if (group == null || currentGroup == null || !currentGroup.getId().equals(groupId))
      return "redirect:/registration-periods/" + periodId + "/student-group";
    populateDetail(model, group, currentUser(principal));
    return "fragment/page/student-group/detail";
  }

  @PostMapping("/{groupId}/members")
  public String addMember(
      @PathVariable Long periodId,
      @PathVariable Long groupId,
      @AuthenticationPrincipal UserDetails principal,
      @Valid @ModelAttribute("addMemberForm") AddGroupMemberForm form,
      BindingResult errors,
      Model model) {
    if (principal == null) return "redirect:/login";
    try {
      if (!errors.hasErrors())
        studentGroupService.addMember(groupId, currentUser(principal), form.getStudentCode());
    } catch (IllegalArgumentException exception) {
      errors.reject(exception.getMessage());
    }
    if (!errors.hasErrors()) return redirectDetail(periodId, groupId);
    StudentGroup group = studentGroupService.findGroup(groupId, periodId);
    if (group == null) return "redirect:/registration-periods/" + periodId + "/student-group";
    populateDetail(model, group, currentUser(principal));
    return "fragment/page/student-group/detail";
  }

  @PostMapping("/{groupId}/members/{memberId}/delete")
  public String removeMember(
      @PathVariable Long periodId,
      @PathVariable Long groupId,
      @PathVariable Long memberId,
      @AuthenticationPrincipal UserDetails principal,
      RedirectAttributes redirectAttributes) {
    try {
      studentGroupService.removeMember(groupId, currentUser(principal), memberId);
    } catch (IllegalArgumentException exception) {
      redirectAttributes.addFlashAttribute("error", exception.getMessage());
    }
    return redirectDetail(periodId, groupId);
  }

  @PostMapping("/{groupId}/leader")
  public String transferLeadership(
      @PathVariable Long periodId,
      @PathVariable Long groupId,
      @RequestParam Long memberId,
      @AuthenticationPrincipal UserDetails principal,
      RedirectAttributes redirectAttributes) {
    try {
      studentGroupService.transferLeadership(groupId, currentUser(principal), memberId);
    } catch (IllegalArgumentException exception) {
      redirectAttributes.addFlashAttribute("error", exception.getMessage());
    }
    return redirectDetail(periodId, groupId);
  }

  @PostMapping("/{groupId}/request-approval")
  public String requestApproval(
      @PathVariable Long periodId,
      @PathVariable Long groupId,
      @AuthenticationPrincipal UserDetails principal,
      RedirectAttributes redirectAttributes) {
    if (principal == null) return "redirect:/login";
    try {
      studentGroupService.requestRegistrationApproval(groupId, currentUser(principal));
    } catch (IllegalArgumentException exception) {
      redirectAttributes.addFlashAttribute("error", exception.getMessage());
    }
    return redirectDetail(periodId, groupId);
  }

  @GetMapping("/{groupId}/topics")
  public String topics(
      @PathVariable Long periodId,
      @PathVariable Long groupId,
      @RequestParam(required = false) String search,
      @RequestParam(required = false) Long departmentId,
      @AuthenticationPrincipal UserDetails principal,
      Model model) {
    StudentGroup group = studentGroupService.findGroup(groupId, periodId);
    if (group == null || principal == null)
      return "redirect:/registration-periods/" + periodId + "/student-group";
    StudentGroup currentGroup =
        studentGroupService.findCurrentGroup(periodId, currentUser(principal));
    if (currentGroup == null || !currentGroup.getId().equals(groupId))
      return "redirect:/registration-periods/" + periodId + "/student-group";
    model.addAttribute("group", group);
    TopicRegistration registration = studentGroupService.registration(groupId);
    model.addAttribute(
        "topics", studentGroupService.approvedTopics(periodId, search, departmentId));
    model.addAttribute(
        "selectedTopicId", registration == null ? null : registration.getTopic().getId());
    model.addAttribute("departments", departmentService.findAll());
    model.addAttribute("search", search);
    model.addAttribute("departmentId", departmentId);
    model.addAttribute("isLeader", isLeader(groupId, currentUser(principal)));
    model.addAttribute("isOpen", studentGroupService.isOpen(group.getRegistrationPeriod()));
    model.addAttribute("pageTitle", "student-groups.page.topics.title");
    model.addAttribute("pageIcon", "bi:journal-check");
    return "fragment/page/student-group/topics";
  }

  @PostMapping("/{groupId}/topics/{topicId}")
  public String selectTopic(
      @PathVariable Long periodId,
      @PathVariable Long groupId,
      @PathVariable Long topicId,
      @AuthenticationPrincipal UserDetails principal,
      RedirectAttributes redirectAttributes) {
    try {
      studentGroupService.selectTopic(groupId, currentUser(principal), topicId);
    } catch (IllegalArgumentException exception) {
      redirectAttributes.addFlashAttribute("error", exception.getMessage());
    }
    return redirectDetail(periodId, groupId);
  }

  private void populateDetail(Model model, StudentGroup group, User viewer) {
    List<GroupMember> members = studentGroupService.members(group.getId());
    model.addAttribute("group", group);
    model.addAttribute("members", members);
    TopicRegistration registration = studentGroupService.registration(group.getId());
    model.addAttribute("registration", registration);
    model.addAttribute("currentReport", registration == null ? null : reportSubmissionService.current(registration.getId()));
    model.addAttribute("reportHistory", registration == null ? List.of() : reportSubmissionService.history(registration.getId()));
    model.addAttribute("canUploadReport", registration != null && reportSubmissionService.canUpload(registration, viewer));
    model.addAttribute("isReportUploadClosed", reportSubmissionService.isUploadClosed(registration));
    model.addAttribute("isReportUploadNotStarted", reportSubmissionService.isUploadNotStarted(registration));
    model.addAttribute("publishedResult", councilService.publishedResult(group.getId()));
    model.addAttribute(
        "isDraftRegistration",
        registration != null && registration.getStatus() == TopicRegistrationStatus.DRAFT);
    model.addAttribute("isLeader", isLeader(group.getId(), viewer));
    model.addAttribute("isOpen", studentGroupService.isOpen(group.getRegistrationPeriod()));
    if (!model.containsAttribute("addMemberForm")) {
      model.addAttribute("addMemberForm", new AddGroupMemberForm());
    }
    model.addAttribute("pageTitle", "student-groups.page.detail.title");
    model.addAttribute("pageIcon", "bi:people");
  }

  private boolean isLeader(Long groupId, User user) {
    return studentGroupService.members(groupId).stream()
        .anyMatch(member -> member.isLeader() && member.getStudent().getId().equals(user.getId()));
  }

  private RegistrationPeriod findPeriod(Long periodId) {
    return registrationPeriodService.findById(periodId);
  }

  private User currentUser(UserDetails principal) {
    return userService.findByEmail(principal.getUsername()).orElseThrow();
  }

  private String redirectDetail(Long periodId, Long groupId) {
    return "redirect:/registration-periods/" + periodId + "/student-group/" + groupId;
  }
}
