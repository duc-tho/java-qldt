package com.nhom11.qldt.feature.registrationperiod.controller;

import com.nhom11.qldt.core.table.TableQuery;
import com.nhom11.qldt.core.validation.ClientValidationRuleRenderer;
import com.nhom11.qldt.core.view.CrudViewSupport;
import com.nhom11.qldt.feature.auth.service.UserService;
import com.nhom11.qldt.feature.council.model.dto.PublicationRevocationForm;
import com.nhom11.qldt.feature.council.model.enums.ResultPublicationAction;
import com.nhom11.qldt.feature.council.service.CouncilService;
import com.nhom11.qldt.feature.registrationperiod.model.dto.RegistrationPeriodForm;
import com.nhom11.qldt.feature.registrationperiod.model.entity.RegistrationPeriod;
import com.nhom11.qldt.feature.registrationperiod.model.enums.PeriodType;
import com.nhom11.qldt.feature.registrationperiod.service.RegistrationPeriodService;
import com.nhom11.qldt.feature.registrationperiod.view.RegistrationPeriodTableDefinition;
import com.nhom11.qldt.feature.studentgroup.service.StudentGroupService;
import com.nhom11.qldt.feature.topic.service.TopicService;
import com.nhom11.qldt.feature.topic.view.TopicTableDefinition;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/registration-periods")
public class RegistrationPeriodController {
  private final RegistrationPeriodService service;
  private final RegistrationPeriodTableDefinition table;
  private final UserService users;
  private final CrudViewSupport view;
  private final ClientValidationRuleRenderer validation;
  private final TopicService topicService;
  private final TopicTableDefinition topicTable;
  private final StudentGroupService studentGroupService;
  private final CouncilService councilService;

  public RegistrationPeriodController(
      RegistrationPeriodService service,
      RegistrationPeriodTableDefinition table,
      UserService users,
      CrudViewSupport view,
      ClientValidationRuleRenderer validation,
      TopicService topicService,
      TopicTableDefinition topicTable,
      StudentGroupService studentGroupService,
      CouncilService councilService) {
    this.service = service;
    this.table = table;
    this.users = users;
    this.view = view;
    this.validation = validation;
    this.topicService = topicService;
    this.topicTable = topicTable;
    this.studentGroupService = studentGroupService;
    this.councilService = councilService;
  }

  @GetMapping
  public String list(@ModelAttribute TableQuery query, Model model) {
    Page<RegistrationPeriod> data = service.findAll(query, table);

    view.table(model, data, "/registration-periods", query, table);
    model.addAttribute("pageTitle", "registration_periods.page.list.title");
    model.addAttribute("pageDesc", "registration_periods.page.list.description");
    model.addAttribute("pageIcon", "bi:calendar-range");

    return "fragment/page/registration-period/list";
  }

  @GetMapping("/new")
  public String newForm(Model model) {
    form(
        model,
        new RegistrationPeriodForm(),
        "/registration-periods",
        false,
        "registration_periods.page.create.title");

    return "fragment/page/registration-period/create";
  }

  @GetMapping("/topics")
  public String topicPeriods(Model model) {
    return periodSelection(model, "topics");
  }

  @GetMapping("/groups")
  public String groupPeriods(Model model) {
    return periodSelection(model, "groups");
  }

  @GetMapping("/{id}/groups")
  public String groups(@PathVariable Long id, Model model) {
    RegistrationPeriod period = service.findById(id);
    if (period == null) return "redirect:/registration-periods/groups";

    model.addAttribute("period", period);
    model.addAttribute("groupSummaries", studentGroupService.groupSummaries(id));
    model.addAttribute("pageTitle", "student-groups.page.list.title");
    model.addAttribute("pageIcon", "bi:people");
    model.addAttribute("breadcrumbParentUrl", "/registration-periods/" + id);
    model.addAttribute("breadcrumbParentLabel", period.getName());
    return "fragment/page/registration-period/groups";
  }

  @PostMapping
  public String create(
      @Valid @ModelAttribute("data") RegistrationPeriodForm form,
      BindingResult errors,
      Model model) {

    if (errors.hasErrors()) {
      form(model, form, "/registration-periods", false, "registration_periods.page.create.title");

      return "fragment/page/registration-period/create";
    }

    RegistrationPeriod item = form.toEntity();
    addBusinessValidationErrors(item, errors);

    if (errors.hasErrors()) {
      form(model, form, "/registration-periods", false, "registration_periods.page.create.title");

      return "fragment/page/registration-period/create";
    }

    item.setCreatedBy(
        users
            .findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
            .orElseThrow());

    service.create(item);

    return "redirect:/registration-periods";
  }

  @GetMapping("/{id}")
  public String detail(@PathVariable Long id, @ModelAttribute TableQuery topicQuery, Model model) {

    RegistrationPeriod item = service.findById(id);

    if (item == null) return "redirect:/registration-periods";

    RegistrationPeriodForm form = new RegistrationPeriodForm();
    form.fromEntity(item);

    form(
        model, form, "/registration-periods/" + id, true, "registration_periods.page.detail.title");
    addTopicsTable(model, item, topicQuery);
    addRegistrations(model, id);
    addResultPublication(model, id);
    addPeriodOverview(model, item);
    model.addAttribute("pageHeading", item.getName());

    return "fragment/page/registration-period/detail";
  }

  @GetMapping("/{id}/edit")
  public String edit(@PathVariable Long id, Model model) {
    RegistrationPeriod item = service.findById(id);

    if (item == null) return "redirect:/registration-periods";

    RegistrationPeriodForm form = new RegistrationPeriodForm();
    form.fromEntity(item);

    form(model, form, "/registration-periods/" + id, false, "registration_periods.page.edit.title");

    return "fragment/page/registration-period/detail";
  }

  @PostMapping("/{id}")
  public String update(
      @PathVariable Long id,
      @Valid @ModelAttribute("data") RegistrationPeriodForm form,
      BindingResult errors,
      Model model) {

    RegistrationPeriod old = service.findById(id);

    if (old == null) return "redirect:/registration-periods";

    if (errors.hasErrors()) {
      form(
          model,
          form,
          "/registration-periods/" + id,
          false,
          "registration_periods.page.edit.title");
      return "fragment/page/registration-period/detail";
    }

    RegistrationPeriod item = form.toEntity();
    item.setId(id);
    item.setCreatedBy(old.getCreatedBy());
    addBusinessValidationErrors(item, errors);

    if (errors.hasErrors()) {
      form(
          model,
          form,
          "/registration-periods/" + id,
          false,
          "registration_periods.page.edit.title");
      return "fragment/page/registration-period/detail";
    }

    service.update(item);

    return "redirect:/registration-periods/" + id;
  }

  @PostMapping("/{id}/delete")
  public String delete(@PathVariable Long id, RedirectAttributes attributes) {
    try {
      service.delete(id);
    } catch (DataIntegrityViolationException exception) {
      attributes.addFlashAttribute("deleteError", "crud.delete.error.in-use");
    }

    return "redirect:/registration-periods";
  }

  private void form(
      Model model, RegistrationPeriodForm form, String action, boolean readonly, String pageTitle) {
    view.form(
        model,
        form,
        action,
        "post",
        "/registration-periods",
        "w-full",
        readonly,
        "fragment/page/registration-period/fields",
        validation.render(RegistrationPeriodForm.class));

    model.addAttribute("periodTypes", PeriodType.values());
    model.addAttribute("pageTitle", pageTitle);
    model.addAttribute("pageDesc", pageTitle.replace(".title", ".description"));
    model.addAttribute("pageIcon", "bi:calendar-range");
    model.addAttribute("breadcrumbParentUrl", "/registration-periods");
    model.addAttribute("breadcrumbParentLabel", "registration_periods.page.list.title");
  }

  private void addBusinessValidationErrors(RegistrationPeriod period, BindingResult errors) {
    service.validationErrors(period).forEach(error -> errors.reject(error.messageCode()));
  }

  private void addTopicsTable(Model model, RegistrationPeriod period, TableQuery topicQuery) {
    Long periodId = period.getId();
    view.table(
        model,
        topicService.findAllByRegistrationPeriodId(periodId, topicQuery, topicTable),
        "/registration-periods/" + periodId + "/topics",
        topicQuery,
        topicTable);

    var authentication = SecurityContextHolder.getContext().getAuthentication();
    LocalDateTime now = LocalDateTime.now();
    boolean isLecturerRegistrationOpen =
        !now.isBefore(period.getLecturerRegistrationStart())
            && !now.isAfter(period.getLecturerRegistrationEnd());
    boolean canCreateTopic =
        authentication != null
            && authentication.getAuthorities().stream()
                .anyMatch(authority ->
                    authority.getAuthority().equals("ROLE_ADMIN")
                        || (authority.getAuthority().equals("ROLE_HEAD_OF_FACULTY")
                            && isLecturerRegistrationOpen));
    model.addAttribute("tableCanCreate", canCreateTopic);
  }

  private String periodSelection(Model model, String target) {
    model.addAttribute("periods", service.findAll());
    model.addAttribute("target", target);
    model.addAttribute("pageTitle", "registration-periods.page.select.title");
    model.addAttribute("pageIcon", "bi:calendar-range");
    return "fragment/page/registration-period/select";
  }

  private void addRegistrations(Model model, Long periodId) {
    model.addAttribute("groupSummaries", studentGroupService.groupSummaries(periodId));
  }

  private void addResultPublication(Model model, Long periodId) {
    var latestPublication = councilService.latestPublication(periodId);
    model.addAttribute("latestPublication", latestPublication);
    model.addAttribute(
        "resultsPublished",
        latestPublication != null && latestPublication.getAction() == ResultPublicationAction.PUBLISHED);
    model.addAttribute("publicationHistory", councilService.publicationHistory(periodId));
    model.addAttribute("canPublishResults", councilService.canPublishResults(periodId));
    model.addAttribute("revocationForm", new PublicationRevocationForm());
  }

  private void addPeriodOverview(Model model, RegistrationPeriod period) {
    LocalDateTime now = LocalDateTime.now();
    model.addAttribute("period", period);
    model.addAttribute("periodStage", service.stage(period, now));
    model.addAttribute("now", now);
    int currentStep;
    if (now.isBefore(period.getLecturerRegistrationStart())) {
      currentStep = 1;
    } else if (now.isBefore(period.getStudentRegistrationStart())) {
      currentStep = 2;
    } else if (now.isBefore(period.getStudentRegistrationEnd())) {
      currentStep = 3;
    } else if (now.isBefore(period.getReportDeadline())) {
      currentStep = 4;
    } else {
      currentStep = 0;
    }
    model.addAttribute("currentStep", currentStep);
    if (now.isBefore(period.getLecturerRegistrationStart())) {
      model.addAttribute("nextDeadline", period.getLecturerRegistrationStart());
      model.addAttribute("nextDeadlineLabel", "registration_periods.deadline.proposal-open");
    } else if (now.isBefore(period.getLecturerRegistrationEnd())) {
      model.addAttribute("nextDeadline", period.getLecturerRegistrationEnd());
      model.addAttribute("nextDeadlineLabel", "registration_periods.deadline.proposal");
    } else if (now.isBefore(period.getStudentRegistrationStart())) {
      model.addAttribute("nextDeadline", period.getStudentRegistrationStart());
      model.addAttribute("nextDeadlineLabel", "registration_periods.deadline.student-open");
    } else if (now.isBefore(period.getStudentRegistrationEnd())) {
      model.addAttribute("nextDeadline", period.getStudentRegistrationEnd());
      model.addAttribute("nextDeadlineLabel", "registration_periods.deadline.student");
    } else if (now.isBefore(period.getReportDeadline())) {
      model.addAttribute("nextDeadline", period.getReportDeadline());
      model.addAttribute("nextDeadlineLabel", "registration_periods.deadline.report");
    } else if (period.getCouncilDate() != null && now.isBefore(period.getCouncilDate())) {
      model.addAttribute("nextDeadline", period.getCouncilDate());
      model.addAttribute("nextDeadlineLabel", "registration_periods.deadline.council");
    } else if (period.getReviewerDeadline() != null
        && now.isBefore(period.getReviewerDeadline())) {
      model.addAttribute("nextDeadline", period.getReviewerDeadline());
      model.addAttribute("nextDeadlineLabel", "registration_periods.deadline.review");
    }
    if (period.getPeriodType() == PeriodType.KLTN) {
      var councils = councilService.councils(period.getId());
      model.addAttribute("councils", councils);
      model.addAttribute("councilSummaries", councilService.councilSummaries(period.getId(), councils));
      model.addAttribute("councilCount", councils.size());
      model.addAttribute("evaluationAssignmentOpen", councilService.isCouncilManagementOpen(period.getId()));
    } else if (period.getPeriodType() == PeriodType.TLCN) {
      model.addAttribute("evaluationAssignmentOpen", councilService.isReviewerAssignmentOpen(period.getId()));
    }
  }
}
