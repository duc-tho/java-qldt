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
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/registration-periods")
public class RegistrationPeriodController {
  private final RegistrationPeriodService service;
  private final RegistrationPeriodTableDefinition table;
  private final UserService users;
  private final CrudViewSupport view;
  private final ClientValidationRuleRenderer validation;

  public RegistrationPeriodController(
      RegistrationPeriodService service,
      RegistrationPeriodTableDefinition table,
      UserService users,
      CrudViewSupport view,
      ClientValidationRuleRenderer validation) {
    this.service = service;
    this.table = table;
    this.users = users;
    this.view = view;
    this.validation = validation;
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
    form(model, new RegistrationPeriodForm(), "/registration-periods", false);

    return "fragment/page/registration-period/create";
  }

  @PostMapping
  public String create(
      @Valid @ModelAttribute("data") RegistrationPeriodForm form,
      BindingResult errors,
      Model model) {

    if (errors.hasErrors()) {
      form(model, form, "/registration-periods", false);

      return "fragment/page/registration-period/create";
    }

    RegistrationPeriod item = form.toEntity();
    item.setCreatedBy(
        users
            .findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
            .orElseThrow());

    service.create(item);

    return "redirect:/registration-periods";
  }

  @GetMapping("/{id}")
  public String detail(@PathVariable Long id, Model model) {

    RegistrationPeriod item = service.findById(id);

    if (item == null) return "redirect:/registration-periods";

    RegistrationPeriodForm form = new RegistrationPeriodForm();
    form.fromEntity(item);

    form(model, form, "/registration-periods/" + id, true);

    return "fragment/page/registration-period/detail";
  }

  @GetMapping("/{id}/edit")
  public String edit(@PathVariable Long id, Model model) {
    RegistrationPeriod item = service.findById(id);

    if (item == null) return "redirect:/registration-periods";

    RegistrationPeriodForm form = new RegistrationPeriodForm();
    form.fromEntity(item);

    form(model, form, "/registration-periods/" + id, false);

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
      form(model, form, "/registration-periods/" + id, false);

      return "fragment/page/registration-period/detail";
    }

    RegistrationPeriod item = form.toEntity();
    item.setId(id);
    item.setCreatedBy(old.getCreatedBy());

    service.update(item);

    return "redirect:/registration-periods/" + id;
  }

  @PostMapping("/{id}/delete")
  public String delete(@PathVariable Long id) {
    service.delete(id);

    return "redirect:/registration-periods";
  }

  private void form(Model model, RegistrationPeriodForm form, String action, boolean readonly) {
    view.form(
        model,
        form,
        action,
        "post",
        "/registration-periods",
        readonly,
        "fragment/page/registration-period/fields",
        validation.render(RegistrationPeriodForm.class));

    model.addAttribute("periodTypes", PeriodType.values());
  }
}
