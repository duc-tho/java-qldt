package com.nhom11.qldt.feature.group.controller;

import com.nhom11.qldt.core.validation.ClientValidationRuleRenderer;
import com.nhom11.qldt.core.view.CrudViewSupport;
import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.auth.service.UserService;
import com.nhom11.qldt.feature.council.service.CouncilService;
import com.nhom11.qldt.feature.period.model.entity.RegistrationPeriod;
import com.nhom11.qldt.feature.period.service.RegistrationPeriodService;
import com.nhom11.qldt.feature.report.service.ReportSubmissionService;
import com.nhom11.qldt.feature.group.model.dto.AddGroupMemberForm;
import com.nhom11.qldt.feature.group.model.dto.CreateStudentGroupForm;
import com.nhom11.qldt.feature.group.model.dto.RejectTopicRegistrationForm;
import com.nhom11.qldt.feature.group.model.entity.GroupActivity;
import com.nhom11.qldt.feature.group.model.entity.GroupMember;
import com.nhom11.qldt.feature.group.model.entity.StudentGroup;
import com.nhom11.qldt.feature.group.model.entity.TopicRegistration;
import com.nhom11.qldt.feature.group.model.enums.GroupActivityType;
import com.nhom11.qldt.feature.group.model.enums.TopicRegistrationStatus;
import com.nhom11.qldt.feature.group.service.GroupActivityService;
import com.nhom11.qldt.feature.group.service.StudentGroupService;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/dot/{periodId}/nhom")
public class StudentGroupController {
  private final StudentGroupService studentGroupService;
  private final RegistrationPeriodService registrationPeriodService;
  private final UserService userService;
  private final ReportSubmissionService reportSubmissionService;
  private final CouncilService councilService;
  private final GroupActivityService groupActivityService;
  private final CrudViewSupport crudView;
  private final ClientValidationRuleRenderer validationRuleRenderer;

  public StudentGroupController(
      StudentGroupService studentGroupService,
      RegistrationPeriodService registrationPeriodService,
      UserService userService,
      ReportSubmissionService reportSubmissionService,
      CouncilService councilService,
      GroupActivityService groupActivityService,
      CrudViewSupport crudView,
      ClientValidationRuleRenderer validationRuleRenderer) {
    this.studentGroupService = studentGroupService;
    this.registrationPeriodService = registrationPeriodService;
    this.userService = userService;
    this.reportSubmissionService = reportSubmissionService;
    this.councilService = councilService;
    this.groupActivityService = groupActivityService;
    this.crudView = crudView;
    this.validationRuleRenderer = validationRuleRenderer;
  }

  @GetMapping("/tao")
  public String createForm(
      @PathVariable Long periodId, @AuthenticationPrincipal UserDetails principal, Model model) {
    RegistrationPeriod period = findPeriod(periodId);
    if (period == null) return "redirect:/dot";
    if (principal == null) return "redirect:/login";
    User student = currentUser(principal);
    if (!studentGroupService.isOpen(period)
        || studentGroupService.findCurrentGroup(periodId, student) != null) {
      return "redirect:/dot/" + periodId + "/nhom";
    }
    return createView(model, new CreateStudentGroupForm(), periodId);
  }

  @PostMapping
  public String create(
      @PathVariable Long periodId,
      @AuthenticationPrincipal UserDetails principal,
      @Valid @ModelAttribute("data") CreateStudentGroupForm form,
      BindingResult errors,
      Model model) {
    if (principal == null || findPeriod(periodId) == null) return "redirect:/dot";
    validateUniqueName(periodId, form, errors);
    if (errors.hasErrors()) return createView(model, form, periodId);

    try {
      StudentGroup group =
          studentGroupService.createGroup(periodId, currentUser(principal), form.getName());
      return redirectDetail(periodId, group.getId());
    } catch (IllegalArgumentException exception) {
      if (StudentGroupService.DUPLICATE_NAME_ERROR.equals(exception.getMessage())) {
        errors.rejectValue("name", StudentGroupService.DUPLICATE_NAME_ERROR);
      } else {
        errors.reject(exception.getMessage());
      }
    } catch (DataIntegrityViolationException exception) {
      validateUniqueName(periodId, form, errors);
      if (!errors.hasErrors()) errors.reject("student-groups.error.already-member");
    }

    return createView(model, form, periodId);
  }

  @GetMapping("/{groupId}")
  public String detail(
      @PathVariable Long periodId,
      @PathVariable Long groupId,
      @AuthenticationPrincipal UserDetails principal,
      Model model) {
    if (principal == null) return "redirect:/login";
    StudentGroup group = studentGroupService.findGroup(groupId, periodId);
    if (group == null || !studentGroupService.canViewGroup(group, currentUser(principal)))
      return "redirect:/dot/" + periodId + "/nhom";
    populateDetail(model, group, currentUser(principal));
    return "page/group/detail";
  }

  @PostMapping("/{groupId}/thanh-vien")
  public String addMember(
      @PathVariable Long periodId,
      @PathVariable Long groupId,
      @AuthenticationPrincipal UserDetails principal,
      @Valid @ModelAttribute("addMemberForm") AddGroupMemberForm form,
      BindingResult errors,
      Model model) {
    if (principal == null) return "redirect:/login";
    requireGroupInPeriod(periodId, groupId);
    try {
      if (!errors.hasErrors())
        studentGroupService.addMember(groupId, currentUser(principal), form.getStudentCode());
    } catch (IllegalArgumentException exception) {
      errors.reject(exception.getMessage());
    } catch (DataIntegrityViolationException exception) {
      errors.reject("student-groups.error.already-member");
    }
    if (!errors.hasErrors()) return redirectDetail(periodId, groupId);
    StudentGroup group = studentGroupService.findGroup(groupId, periodId);
    if (group == null || !studentGroupService.canViewGroup(group, currentUser(principal)))
      return "redirect:/dot/" + periodId + "/nhom";
    populateDetail(model, group, currentUser(principal));
    return "page/group/detail";
  }

  @PostMapping("/{groupId}/thanh-vien/{memberId}/xoa")
  public String removeMember(
      @PathVariable Long periodId,
      @PathVariable Long groupId,
      @PathVariable Long memberId,
      @AuthenticationPrincipal UserDetails principal,
      RedirectAttributes redirectAttributes) {
    requireGroupInPeriod(periodId, groupId);
    try {
      studentGroupService.removeMember(groupId, currentUser(principal), memberId);
    } catch (IllegalArgumentException exception) {
      redirectAttributes.addFlashAttribute("error", exception.getMessage());
    } catch (DataIntegrityViolationException exception) {
      redirectAttributes.addFlashAttribute("deleteError", "crud.delete.error.in-use");
    }
    return redirectDetail(periodId, groupId);
  }

  @PostMapping("/{groupId}/truong-nhom")
  public String transferLeadership(
      @PathVariable Long periodId,
      @PathVariable Long groupId,
      @RequestParam Long memberId,
      @AuthenticationPrincipal UserDetails principal,
      RedirectAttributes redirectAttributes) {
    requireGroupInPeriod(periodId, groupId);
    try {
      studentGroupService.transferLeadership(groupId, currentUser(principal), memberId);
    } catch (IllegalArgumentException exception) {
      redirectAttributes.addFlashAttribute("error", exception.getMessage());
    }
    return redirectDetail(periodId, groupId);
  }

  @PostMapping("/{groupId}/gui-duyet")
  public String requestApproval(
      @PathVariable Long periodId,
      @PathVariable Long groupId,
      @AuthenticationPrincipal UserDetails principal,
      RedirectAttributes redirectAttributes) {
    if (principal == null) return "redirect:/login";
    requireGroupInPeriod(periodId, groupId);
    try {
      studentGroupService.requestRegistrationApproval(groupId, currentUser(principal));
    } catch (IllegalArgumentException exception) {
      redirectAttributes.addFlashAttribute("error", exception.getMessage());
    }
    return redirectDetail(periodId, groupId);
  }

  @PostMapping("/{groupId}/de-tai")
  public String selectTopic(
      @PathVariable Long periodId,
      @PathVariable Long groupId,
      @RequestParam Long topicId,
      @AuthenticationPrincipal UserDetails principal,
      RedirectAttributes redirectAttributes) {
    requireGroupInPeriod(periodId, groupId);
    try {
      studentGroupService.selectTopic(groupId, currentUser(principal), topicId);
    } catch (IllegalArgumentException exception) {
      redirectAttributes.addFlashAttribute("error", exception.getMessage());
    }
    return redirectDetail(periodId, groupId);
  }

  @PostMapping("/{groupId}/duyet-de-tai")
  public String approveTopic(
      @PathVariable Long periodId,
      @PathVariable Long groupId,
      @AuthenticationPrincipal UserDetails principal,
      RedirectAttributes attributes) {
    requireGroupInPeriod(periodId, groupId);
    TopicRegistration registration = studentGroupService.registration(groupId);
    if (registration == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    try {
      studentGroupService.approveRegistration(registration.getId(), currentUser(principal));
    } catch (IllegalArgumentException exception) {
      attributes.addFlashAttribute("error", exception.getMessage());
    }
    return redirectDetail(periodId, groupId);
  }

  @PostMapping("/{groupId}/tu-choi-de-tai")
  public String rejectTopic(
      @PathVariable Long periodId,
      @PathVariable Long groupId,
      @AuthenticationPrincipal UserDetails principal,
      @Valid @ModelAttribute("rejectForm") RejectTopicRegistrationForm form,
      BindingResult errors,
      Model model) {
    StudentGroup group = requireGroupInPeriod(periodId, groupId);
    TopicRegistration registration = studentGroupService.registration(groupId);
    if (registration == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    if (!errors.hasErrors()) {
      try {
        studentGroupService.rejectRegistration(
            registration.getId(), currentUser(principal), form.getReason());
      } catch (IllegalArgumentException exception) {
        errors.reject(exception.getMessage());
      }
    }
    if (!errors.hasErrors()) return redirectDetail(periodId, groupId);
    populateDetail(model, group, currentUser(principal));
    return "page/group/detail";
  }

  private void populateDetail(Model model, StudentGroup group, User viewer) {
    List<GroupMember> members = studentGroupService.members(group.getId());
    model.addAttribute("group", group);
    model.addAttribute("members", members);
    model.addAttribute("leader", members.stream().filter(member -> member.isLeader()).findFirst().orElse(null));
    List<GroupActivity> activities = groupActivityService.history(group.getId());
    model.addAttribute("activities", activities);
    TopicRegistration registration = studentGroupService.registration(group.getId());
    model.addAttribute("registration", registration);
    if (registration != null) {
      LocalDateTime registrationUpdatedAt = registration.getUpdatedAt() == null ? null
          : LocalDateTime.ofInstant(registration.getUpdatedAt(), ZoneId.systemDefault());
      model.addAttribute("statusChangedAt", switch (registration.getStatus()) {
        case DRAFT -> registrationUpdatedAt;
        case PENDING -> activities.stream()
            .filter(activity -> activity.getType() == GroupActivityType.APPROVAL_REQUESTED)
            .map(activity -> activity.getOccurredAt())
            .findFirst()
            .orElse(registrationUpdatedAt);
        case APPROVED, REJECTED -> registration.getReviewedAt();
      });
    }
    var currentReport = registration == null ? null : reportSubmissionService.current(registration.getId());
    boolean reportUploadClosed = reportSubmissionService.isUploadClosed(registration);
    boolean reportUploadNotStarted = reportSubmissionService.isUploadNotStarted(registration);
    boolean registrationOpen = studentGroupService.isOpen(group.getRegistrationPeriod());
    model.addAttribute("currentReport", currentReport);
    model.addAttribute("reportHistory", registration == null ? List.of() : reportSubmissionService.history(registration.getId()));
    model.addAttribute("canUploadReport", registration != null && reportSubmissionService.canUpload(registration, viewer));
    model.addAttribute("isReportUploadClosed", reportUploadClosed);
    model.addAttribute("isReportUploadNotStarted", reportUploadNotStarted);
    model.addAttribute("statusGuidanceKey", statusGuidanceKey(
        registration, registrationOpen, members.size(), reportUploadClosed,
        reportUploadNotStarted, currentReport != null));
    model.addAttribute("publishedResult", councilService.publishedResult(group.getId()));
    model.addAttribute(
        "isDraftRegistration",
        registration != null && registration.getStatus() == TopicRegistrationStatus.DRAFT);
    model.addAttribute("isLeader", isLeader(group.getId(), viewer));
    model.addAttribute("canEditRegistration", registration == null || registration.getStatus() != TopicRegistrationStatus.APPROVED);
    model.addAttribute("isOpen", registrationOpen);
    model.addAttribute("isPending", registration != null
        && registration.getStatus() == TopicRegistrationStatus.PENDING);
    model.addAttribute("canReviewRegistration", registration != null
        && registration.getStatus() == TopicRegistrationStatus.PENDING
        && registrationOpen
        && registration.getTopic().getAdvisers().stream()
            .anyMatch(adviser -> adviser.getId().equals(viewer.getId())));
    if (!model.containsAttribute("rejectForm")) {
      model.addAttribute("rejectForm", new RejectTopicRegistrationForm());
    }
    if (!model.containsAttribute("addMemberForm")) {
      model.addAttribute("addMemberForm", new AddGroupMemberForm());
    }
    model.addAttribute("pageTitle", "student-groups.page.detail.title");
    model.addAttribute("pageHeading", group.getName());
    model.addAttribute("pageIcon", "bi:people");
  }

  private String statusGuidanceKey(
      TopicRegistration registration, boolean registrationOpen, int memberCount,
      boolean reportUploadClosed, boolean reportUploadNotStarted, boolean reportSubmitted) {
    if (registration == null) {
      return registrationOpen ? "student-groups.status.guide.NONE" : "student-groups.status.guide.NONE_CLOSED";
    }

    return switch (registration.getStatus()) {
      case DRAFT -> !registrationOpen ? "student-groups.status.guide.DRAFT_CLOSED"
          : memberCount < 3 ? "student-groups.status.guide.DRAFT_MEMBERS"
          : "student-groups.status.guide.DRAFT";
      case PENDING -> registrationOpen ? "student-groups.status.guide.PENDING"
          : "student-groups.status.guide.PENDING_CLOSED";
      case APPROVED -> reportUploadClosed ? "student-groups.status.guide.APPROVED_CLOSED"
          : reportSubmitted ? "student-groups.status.guide.APPROVED_SUBMITTED"
          : reportUploadNotStarted ? "student-groups.status.guide.APPROVED_NOT_STARTED"
          : "student-groups.status.guide.APPROVED";
      case REJECTED -> registrationOpen ? "student-groups.status.guide.REJECTED"
          : "student-groups.status.guide.REJECTED_CLOSED";
    };
  }

  private boolean isLeader(Long groupId, User user) {
    return studentGroupService.members(groupId).stream()
        .anyMatch(member -> member.isLeader() && member.getStudent().getId().equals(user.getId()));
  }

  private RegistrationPeriod findPeriod(Long periodId) {
    return registrationPeriodService.findById(periodId);
  }

  private String createView(Model model, CreateStudentGroupForm form, Long periodId) {
    String groupListUrl = "/dot/" + periodId + "/nhom";
    crudView.form(
        model,
        form,
        groupListUrl,
        "post",
        groupListUrl,
        false,
        "fragment/feature/group/fields",
        validationRuleRenderer.render(CreateStudentGroupForm.class));
    model.addAttribute("pageTitle", "student-groups.page.create.title");
    model.addAttribute("pageIcon", "bi:people");
    model.addAttribute("breadcrumbParentUrl", groupListUrl);
    model.addAttribute("breadcrumbParentLabel", "student-groups.page.list.title");

    return "page/group/create";
  }

  private void validateUniqueName(
      Long periodId, CreateStudentGroupForm form, BindingResult errors) {
    if (errors.hasFieldErrors("name")
        || !studentGroupService.existsByName(periodId, form.getName())) {
      return;
    }

    errors.rejectValue("name", StudentGroupService.DUPLICATE_NAME_ERROR);
  }

  private User currentUser(UserDetails principal) {
    return userService.findByEmail(principal.getUsername()).orElseThrow();
  }

  private StudentGroup requireGroupInPeriod(Long periodId, Long groupId) {
    StudentGroup group = studentGroupService.findGroup(groupId, periodId);
    if (group == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    return group;
  }

  private String redirectDetail(Long periodId, Long groupId) {
    return "redirect:/dot/" + periodId + "/nhom/" + groupId;
  }
}
