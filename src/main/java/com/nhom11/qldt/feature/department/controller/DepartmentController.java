package com.nhom11.qldt.feature.department.controller;

import com.nhom11.qldt.core.table.TableQuery;
import com.nhom11.qldt.core.validation.ClientValidationRuleRenderer;
import com.nhom11.qldt.core.view.CrudViewSupport;
import com.nhom11.qldt.feature.department.model.dto.DepartmentForm;
import com.nhom11.qldt.feature.department.model.entity.Department;
import com.nhom11.qldt.feature.department.service.DepartmentService;
import com.nhom11.qldt.feature.department.view.DepartmentTableDefinition;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
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
@RequestMapping("/bo-mon")
public class DepartmentController {

  private final DepartmentService service;
  private final DepartmentTableDefinition table;
  private final CrudViewSupport view;
  private final ClientValidationRuleRenderer validation;

  public DepartmentController(
      DepartmentService service,
      DepartmentTableDefinition table,
      CrudViewSupport view,
      ClientValidationRuleRenderer validation) {
    this.service = service;
    this.table = table;
    this.view = view;
    this.validation = validation;
  }

  @GetMapping
  public String list(@ModelAttribute TableQuery query, Model model) {
    view.table(model, service.findAll(query, table), "/bo-mon", query, table);
    model.addAttribute("pageTitle", "departments.page.list.title");
    model.addAttribute("pageIcon", "bi:diagram-3");
    return "page/department/list";
  }

  @GetMapping("/new")
  public String createForm(Model model) {
    form(model, new DepartmentForm(), "/bo-mon", false, "departments.page.create.title");
    return "page/department/create";
  }

  @PostMapping
  public String create(
      @Valid @ModelAttribute("data") DepartmentForm form, BindingResult errors, Model model) {
    if (errors.hasErrors()) {
      form(model, form, "/bo-mon", false, "departments.page.create.title");
      return "page/department/create";
    }

    if (service.existsByCode(form.getCode())) {
      errors.rejectValue("code", "departments.validation.code.duplicate");
      form(model, form, "/bo-mon", false, "departments.page.create.title");
      return "page/department/create";
    }

    service.create(form.toEntity());
    return "redirect:/bo-mon";
  }

  @GetMapping("/{id}")
  public String detail(@PathVariable Long id, Model model) {
    Department department = service.findById(id);
    if (department == null) return "redirect:/bo-mon";
    DepartmentForm form = new DepartmentForm();
    form.fromEntity(department);
    form(model, form, "/bo-mon/" + id, true, "departments.page.detail.title");
    return "page/department/detail";
  }

  @GetMapping("/{id}/edit")
  public String edit(@PathVariable Long id, Model model) {
    Department department = service.findById(id);
    if (department == null) return "redirect:/bo-mon";
    DepartmentForm form = new DepartmentForm();
    form.fromEntity(department);
    form(model, form, "/bo-mon/" + id, false, "departments.page.edit.title");
    return "page/department/detail";
  }

  @PostMapping("/{id}")
  public String update(
      @PathVariable Long id,
      @Valid @ModelAttribute("data") DepartmentForm form,
      BindingResult errors,
      Model model) {
    if (service.findById(id) == null) return "redirect:/bo-mon";
    if (errors.hasErrors()) {
      form.setId(id);
      form(model, form, "/bo-mon/" + id, false, "departments.page.edit.title");
      return "page/department/detail";
    }

    if (service.existsByCodeAndIdNot(form.getCode(), id)) {
      errors.rejectValue("code", "departments.validation.code.duplicate");
      form.setId(id);
      form(model, form, "/bo-mon/" + id, false, "departments.page.edit.title");
      return "page/department/detail";
    }

    Department department = form.toEntity();
    department.setId(id);
    service.update(department);
    return "redirect:/bo-mon/" + id;
  }

  @PostMapping("/{id}/delete")
  public String delete(@PathVariable Long id, RedirectAttributes attributes) {
    try {
      service.delete(id);
    } catch (IllegalStateException exception) {
      attributes.addFlashAttribute("deleteError", "departments.error.has-topics");
    } catch (DataIntegrityViolationException exception) {
      attributes.addFlashAttribute("deleteError", "crud.delete.error.in-use");
    }
    return "redirect:/bo-mon";
  }

  private void form(
      Model model, DepartmentForm data, String action, boolean readonly, String title) {
    view.form(
        model,
        data,
        action,
        "post",
        "/bo-mon",
        readonly,
        "fragment/feature/department/fields",
        validation.render(DepartmentForm.class));
    model.addAttribute("pageTitle", title);
    model.addAttribute("pageIcon", "bi:diagram-3");
  }
}
