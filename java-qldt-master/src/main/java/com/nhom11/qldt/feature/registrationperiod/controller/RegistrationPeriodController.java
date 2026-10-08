package com.nhom11.qldt.feature.registrationperiod.controller;

import com.nhom11.qldt.core.table.TableQuery;
import com.nhom11.qldt.core.validation.ClientValidationRuleRenderer;
import com.nhom11.qldt.core.view.CrudViewSupport;
import com.nhom11.qldt.feature.auth.service.UserService;
import com.nhom11.qldt.feature.registrationperiod.model.dto.RegistrationPeriodForm;
import com.nhom11.qldt.feature.registrationperiod.model.entity.RegistrationPeriod;
import com.nhom11.qldt.feature.registrationperiod.model.enums.PeriodType;
import com.nhom11.qldt.feature.registrationperiod.service.RegistrationPeriodService;
import com.nhom11.qldt.feature.registrationperiod.view.RegistrationPeriodTableDefinition;
import com.nhom11.qldt.feature.studentgroup.service.StudentGroupService;
import com.nhom11.qldt.feature.topic.service.TopicService;
import com.nhom11.qldt.feature.topic.view.TopicTableDefinition;
import jakarta.validation.Valid;
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

  public RegistrationPeriodController(
      RegistrationPeriodService service,
      RegistrationPeriodTableDefinition table,
      UserService users,
      CrudViewSupport view,
      ClientValidationRuleRenderer validation,
      TopicService topicService,
      TopicTableDefinition topicTable,
      StudentGroupService studentGroupService) {
    this.service = service;
    this.table = table;
    this.users = users;
    this.view = view;
    this.validation = validation;
    this.topicService = topicService;
    this.topicTable = topicTable;
    this.studentGroupService = studentGroupService;
  }

  @GetMapping
  public String list(@ModelAttribute TableQuery query, Model model) {
    Page<RegistrationPeriod> data = service.findAll(query, table);

    view.table(model, data, "/registration-periods", query, table);
    model.addAttribute("pageTitle", "registration_periods.page.list.title");
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
    model.addAttribute("registrations", studentGroupService.registrations(id));
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
    addTopicsTable(model, id, topicQuery);
    addRegistrations(model, id);

    return "fragment/page/registration-period/detail";
  }

  @GetMapping("/{id}/edit")
  public String edit(@PathVariable Long id, @ModelAttribute TableQuery topicQuery, Model model) {
    RegistrationPeriod item = service.findById(id);

    if (item == null) return "redirect:/registration-periods";

    RegistrationPeriodForm form = new RegistrationPeriodForm();
    form.fromEntity(item);

    form(model, form, "/registration-periods/" + id, false, "registration_periods.page.edit.title");
    addTopicsTable(model, id, topicQuery);
    addRegistrations(model, id);

    return "fragment/page/registration-period/detail";
  }

  @PostMapping("/{id}")
  public String update(
      @PathVariable Long id,
      @Valid @ModelAttribute("data") RegistrationPeriodForm form,
      BindingResult errors,
      @ModelAttribute TableQuery topicQuery,
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
      addTopicsTable(model, id, topicQuery);

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
      addTopicsTable(model, id, topicQuery);

      return "fragment/page/registration-period/detail";
    }

    service.update(item);

    return "redirect:/registration-periods/" + id;
  }

  @PostMapping("/{id}/delete")
  public String delete(@PathVariable Long id) {
    service.delete(id);

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
        "w-full max-w-4xl mx-auto",
        readonly,
        "fragment/page/registration-period/fields",
        validation.render(RegistrationPeriodForm.class));

    model.addAttribute("periodTypes", PeriodType.values());
    model.addAttribute("pageTitle", pageTitle);
    model.addAttribute("pageIcon", "bi:calendar-range");
  }

  private void addBusinessValidationErrors(RegistrationPeriod period, BindingResult errors) {
    service.validationErrors(period).forEach(error -> errors.reject(error.messageCode()));
  }

  private void addTopicsTable(Model model, Long periodId, TableQuery topicQuery) {
    view.table(
        model,
        topicService.findAllByRegistrationPeriodId(periodId, topicQuery, topicTable),
        "/registration-periods/" + periodId + "/topics",
        topicQuery,
        topicTable);
  }

  private String periodSelection(Model model, String target) {
    model.addAttribute("periods", service.findAll());
    model.addAttribute("target", target);
    model.addAttribute("pageTitle", "registration-periods.page.select.title");
    model.addAttribute("pageIcon", "bi:calendar-range");
    return "fragment/page/registration-period/select";
  }

  private void addRegistrations(Model model, Long periodId) {
    model.addAttribute("registrations", studentGroupService.registrations(periodId));
  }
}
