package com.nhom11.qldt.feature.topic.controller;

import com.nhom11.qldt.core.table.TableQuery;
import com.nhom11.qldt.core.view.CrudViewSupport;
import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.auth.service.UserService;
import com.nhom11.qldt.feature.council.model.dto.CouncilLecturerOption;
import com.nhom11.qldt.feature.department.service.DepartmentService;
import com.nhom11.qldt.feature.group.model.entity.StudentGroup;
import com.nhom11.qldt.feature.group.model.entity.TopicRegistration;
import com.nhom11.qldt.feature.group.model.enums.TopicRegistrationStatus;
import com.nhom11.qldt.feature.group.service.StudentGroupService;
import com.nhom11.qldt.feature.period.model.entity.RegistrationPeriod;
import com.nhom11.qldt.feature.period.service.RegistrationPeriodService;
import com.nhom11.qldt.feature.topic.model.entity.Topic;
import com.nhom11.qldt.feature.topic.model.dto.RejectTopicForm;
import com.nhom11.qldt.feature.topic.model.dto.TopicForm;
import com.nhom11.qldt.feature.topic.model.enums.TopicStatus;
import com.nhom11.qldt.feature.topic.service.TopicService;
import com.nhom11.qldt.feature.topic.view.TopicTableDefinition;
import java.time.LocalDateTime;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class TopicPageController {
  private final TopicController staffPages;
  private final TopicProposalController proposalPages;
  private final TopicService topicService;
  private final TopicTableDefinition table;
  private final CrudViewSupport view;
  private final RegistrationPeriodService periodService;
  private final DepartmentService departmentService;
  private final UserService userService;
  private final StudentGroupService groupService;

  public TopicPageController(
      TopicController staffPages,
      TopicProposalController proposalPages,
      TopicService topicService,
      TopicTableDefinition table,
      CrudViewSupport view,
      RegistrationPeriodService periodService,
      DepartmentService departmentService,
      UserService userService,
      StudentGroupService groupService) {
    this.staffPages = staffPages;
    this.proposalPages = proposalPages;
    this.topicService = topicService;
    this.table = table;
    this.view = view;
    this.periodService = periodService;
    this.departmentService = departmentService;
    this.userService = userService;
    this.groupService = groupService;
  }

  @GetMapping("/dot/{periodId}/de-tai")
  public String list(
      @PathVariable Long periodId,
      @ModelAttribute TableQuery query,
      @RequestParam(required = false) Long departmentId,
      @RequestParam(required = false) TopicStatus status,
      @RequestParam(required = false) String adviserSearch,
      @RequestParam(name = "cuaToi", defaultValue = "false") boolean ownProposals,
      @RequestParam(name = "nhomId", required = false) Long groupId,
      Authentication authentication,
      Model model) {
    boolean studentSelection = groupId != null && hasRole(authentication, "ROLE_STUDENT");
    if (isStaff(authentication) && !studentSelection) {
      model.addAttribute("isStudent", false);
      return staffPages.list(periodId, query, departmentId, status, adviserSearch, model);
    }

    User viewer = userService.findByEmail(authentication.getName()).orElseThrow();
    PeriodAccess access = visiblePeriod(periodId, viewer,
        hasRole(authentication, "ROLE_LECTURER") && !studentSelection,
        hasRole(authentication, "ROLE_STUDENT"));
    boolean isLecturer = access.lecturer();
    RegistrationPeriod period = access.period();
    model.addAttribute("period", period);
    model.addAttribute("isStudent", !isLecturer);
    model.addAttribute("periodId", periodId);
    model.addAttribute("pageTitle", "topics.page.list.title");
    model.addAttribute("pageIcon", "bi:journal-text");
    model.addAttribute("breadcrumbParentUrl", "/dot/" + periodId);
    model.addAttribute("breadcrumbParentLabel", period.getName());
    model.addAttribute("departments", departmentService.findAll());
    model.addAttribute("departmentId", departmentId);

    if (!isLecturer) {
      if (groupId != null) prepareGroupSelection(period, groupId, viewer, model);
      model.addAttribute("topics", topicService.findApprovedByRegistrationPeriodId(
          periodId, query.getSearch(), departmentId));
      model.addAttribute("search", query.getSearch());
      return "page/topic/list";
    }

    view.table(
        model,
        topicService.findAllByRegistrationPeriodId(
            periodId, query, table, departmentId, status, adviserSearch, viewer.getId(),
            ownProposals ? viewer.getId() : null),
        "/dot/" + periodId + "/de-tai",
        query,
        table);
    model.addAttribute("periods", periodService.findVisibleForLecturer(viewer.getId()));
    model.addAttribute("statuses", TopicStatus.values());
    model.addAttribute("selectedStatus", status);
    model.addAttribute("adviserSearch", adviserSearch);
    model.addAttribute("ownProposals", ownProposals);
    model.addAttribute("tableCanCreate", false);
    model.addAttribute("canPropose", !LocalDateTime.now().isBefore(period.getLecturerRegistrationStart())
        && !LocalDateTime.now().isAfter(period.getLecturerRegistrationEnd()));
    return "page/topic/list";
  }

  @GetMapping("/dot/{periodId}/de-tai/{topicId}")
  public String detail(
      @PathVariable Long periodId,
      @PathVariable Long topicId,
      @RequestParam(name = "nhomId", required = false) Long groupId,
      Authentication authentication,
      Model model) {
    boolean studentSelection = groupId != null && hasRole(authentication, "ROLE_STUDENT");
    if (isStaff(authentication) && !studentSelection) {
      model.addAttribute("isStaff", true);
      return staffPages.detail(periodId, topicId, model);
    }

    User viewer = userService.findByEmail(authentication.getName()).orElseThrow();
    PeriodAccess access = visiblePeriod(periodId, viewer,
        hasRole(authentication, "ROLE_LECTURER") && !studentSelection,
        hasRole(authentication, "ROLE_STUDENT"));
    boolean isLecturer = access.lecturer();
    RegistrationPeriod period = access.period();
    Topic topic = topicService.findByIdAndRegistrationPeriodId(topicId, periodId);
    boolean isOwner = topic != null && topic.getProposedBy() != null
        && topic.getProposedBy().getId().equals(viewer.getId());
    if (topic == null || topic.getStatus() != TopicStatus.APPROVED && (!isLecturer || !isOwner)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
    if (!isLecturer && groupId != null) {
      prepareGroupSelection(period, groupId, viewer, model);
    }

    model.addAttribute("isStaff", false);
    model.addAttribute("topic", topic);
    model.addAttribute("showRejectionReason", isOwner);
    model.addAttribute("pageTitle", "topics.page.detail.title");
    model.addAttribute("pageHeading", topic.getTitle());
    model.addAttribute("pageIcon", "bi:journal-text");
    model.addAttribute("breadcrumbParentUrl", "/dot/" + periodId + "/de-tai");
    model.addAttribute("breadcrumbParentLabel", period.getName());
    return "page/topic/detail";
  }

  private void prepareGroupSelection(
      RegistrationPeriod period, Long groupId, User viewer, Model model) {
    StudentGroup group = groupService.findGroup(groupId, period.getId());
    StudentGroup currentGroup = groupService.findCurrentGroup(period.getId(), viewer);
    if (group == null || currentGroup == null || !currentGroup.getId().equals(groupId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
    boolean isLeader = groupService.members(groupId).stream()
        .anyMatch(member -> member.isLeader() && member.getStudent().getId().equals(viewer.getId()));
    TopicRegistration registration = groupService.registration(groupId);
    model.addAttribute("selectionGroupId", groupId);
    model.addAttribute("selectedTopicId", registration == null ? null : registration.getTopic().getId());
    model.addAttribute("canChooseTopic", isLeader && groupService.isOpen(period)
        && (registration == null || registration.getStatus() != TopicRegistrationStatus.APPROVED));
  }

  @GetMapping("/dot/{periodId}/de-tai/tao")
  public String createForm(
      @PathVariable Long periodId,
      Authentication authentication,
      @AuthenticationPrincipal UserDetails principal,
      Model model) {
    if (isStaff(authentication)) return staffPages.createForm(periodId, model);
    if (hasRole(authentication, "ROLE_LECTURER")) {
      return staffPages.proposeForm(periodId, principal, model);
    }
    throw new ResponseStatusException(HttpStatus.FORBIDDEN);
  }

  @PostMapping("/dot/{periodId}/de-tai")
  public String create(
      @PathVariable Long periodId,
      Authentication authentication,
      @AuthenticationPrincipal UserDetails principal,
      @Valid @ModelAttribute("data") TopicForm form,
      BindingResult errors,
      Model model) {
    if (isStaff(authentication)) return staffPages.create(periodId, form, errors, model);
    if (hasRole(authentication, "ROLE_LECTURER")) {
      return staffPages.propose(periodId, principal, form, errors, model);
    }
    throw new ResponseStatusException(HttpStatus.FORBIDDEN);
  }

  @GetMapping("/dot/{periodId}/de-tai/giang-vien")
  @ResponseBody
  public List<CouncilLecturerOption> lecturers(
      @PathVariable Long periodId, @RequestParam(required = false) String search) {
    return staffPages.lecturers(periodId, search);
  }

  @GetMapping("/dot/{periodId}/de-tai/{topicId}/sua")
  public String edit(
      @PathVariable Long periodId,
      @PathVariable Long topicId,
      Authentication authentication,
      @AuthenticationPrincipal UserDetails principal,
      Model model) {
    if (isStaff(authentication)) return staffPages.edit(periodId, topicId, model);
    if (hasRole(authentication, "ROLE_LECTURER")) {
      return proposalPages.editForm(periodId, topicId, principal, model);
    }
    throw new ResponseStatusException(HttpStatus.FORBIDDEN);
  }

  @PostMapping("/dot/{periodId}/de-tai/{topicId}")
  public String update(
      @PathVariable Long periodId,
      @PathVariable Long topicId,
      Authentication authentication,
      @AuthenticationPrincipal UserDetails principal,
      @Valid @ModelAttribute("data") TopicForm form,
      BindingResult errors,
      Model model) {
    if (isStaff(authentication)) return staffPages.update(periodId, topicId, form, errors, model);
    if (hasRole(authentication, "ROLE_LECTURER")) {
      return proposalPages.update(periodId, topicId, principal, form, errors, model);
    }
    throw new ResponseStatusException(HttpStatus.FORBIDDEN);
  }

  @PostMapping("/dot/{periodId}/de-tai/{topicId}/xoa")
  public String delete(
      @PathVariable Long periodId,
      @PathVariable Long topicId,
      Authentication authentication,
      @AuthenticationPrincipal UserDetails principal,
      RedirectAttributes attributes) {
    if (isStaff(authentication)) return staffPages.delete(periodId, topicId, attributes);
    if (hasRole(authentication, "ROLE_LECTURER")) {
      return proposalPages.delete(periodId, topicId, principal, attributes);
    }
    throw new ResponseStatusException(HttpStatus.FORBIDDEN);
  }

  @PostMapping("/dot/{periodId}/de-tai/xoa")
  public String deleteSelected(
      @PathVariable Long periodId,
      @RequestParam(name = "ids", required = false) List<Long> ids,
      RedirectAttributes attributes) {
    return staffPages.deleteSelected(periodId, ids, attributes);
  }

  @PostMapping("/dot/{periodId}/de-tai/{topicId}/duyet")
  public String approve(
      @PathVariable Long periodId,
      @PathVariable Long topicId,
      RedirectAttributes attributes) {
    return staffPages.approve(periodId, topicId, attributes);
  }

  @PostMapping("/dot/{periodId}/de-tai/{topicId}/tu-choi")
  public String reject(
      @PathVariable Long periodId,
      @PathVariable Long topicId,
      @Valid @ModelAttribute("rejectForm") RejectTopicForm form,
      BindingResult errors,
      Model model) {
    return staffPages.reject(periodId, topicId, form, errors, model);
  }

  private PeriodAccess visiblePeriod(Long periodId, User viewer,
      boolean isLecturer, boolean isStudent) {
    if (isLecturer) {
      RegistrationPeriod period = periodService.findVisibleForLecturer(viewer.getId()).stream()
          .filter(item -> item.getId().equals(periodId)).findFirst().orElse(null);
      if (period != null) return new PeriodAccess(period, true);
    }
    if (isStudent) {
      RegistrationPeriod period = periodService.findVisibleForStudent(viewer.getId()).stream()
          .filter(item -> item.getId().equals(periodId)).findFirst().orElse(null);
      if (period != null && !LocalDateTime.now().isBefore(period.getStudentRegistrationStart())) {
        return new PeriodAccess(period, false);
      }
    }
    throw new ResponseStatusException(HttpStatus.NOT_FOUND);
  }

  private record PeriodAccess(RegistrationPeriod period, boolean lecturer) {}

  private boolean isStaff(Authentication authentication) {
    return hasRole(authentication, "ROLE_ADMIN") || hasRole(authentication, "ROLE_HEAD_OF_FACULTY");
  }

  private boolean hasRole(Authentication authentication, String role) {
    return authentication.getAuthorities().stream()
        .anyMatch(authority -> authority.getAuthority().equals(role));
  }
}
