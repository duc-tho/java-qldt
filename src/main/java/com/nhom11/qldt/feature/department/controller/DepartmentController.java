package com.nhom11.qldt.feature.department.controller;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.nhom11.qldt.core.table.TableQuery;
import com.nhom11.qldt.core.validation.ClientValidationRuleRenderer;
import com.nhom11.qldt.core.view.CrudViewSupport;
import com.nhom11.qldt.feature.department.model.dto.DepartmentForm;
import com.nhom11.qldt.feature.department.model.entity.Department;
import com.nhom11.qldt.feature.department.service.DepartmentService;
import com.nhom11.qldt.feature.department.view.DepartmentTableDefinition;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/departments")
public class DepartmentController {

    private static final String BASE_URL = "/departments";
    private static final String FIELDS_TEMPLATE = "fragment/page/department/fields";
    private static final String LIST_VIEW = "fragment/page/department/list";
    private static final String CREATE_VIEW = "fragment/page/department/create";
    private static final String DETAIL_VIEW = "fragment/page/department/detail";

    private final DepartmentService departmentService;
    private final DepartmentTableDefinition tableDefinition;
    private final CrudViewSupport crudView;
    private final ClientValidationRuleRenderer validationRuleRenderer;

    public DepartmentController(
            DepartmentService departmentService,
            DepartmentTableDefinition tableDefinition,
            CrudViewSupport crudView,
            ClientValidationRuleRenderer validationRuleRenderer) {
        this.departmentService = departmentService;
        this.tableDefinition = tableDefinition;
        this.crudView = crudView;
        this.validationRuleRenderer = validationRuleRenderer;
    }

    @GetMapping
    public String list(@ModelAttribute TableQuery query, Model model) {
        Page<Department> departments = departmentService.findAll(query, tableDefinition);
        crudView.table(model, departments, BASE_URL, query, tableDefinition);
        model.addAttribute("pageTitle", "departments.page.list.title");
        model.addAttribute("pageIcon", "bi:building");
        model.addAttribute("pageDesc", "departments.page.list.description");
        return LIST_VIEW;
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        prepareForm(model, new DepartmentForm(), BASE_URL, false, "departments.page.create.title");
        return CREATE_VIEW;
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("data") DepartmentForm form, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            prepareForm(model, form, BASE_URL, false, "departments.page.create.title");
            return CREATE_VIEW;
        }

        departmentService.create(form.toEntity());
        return "redirect:" + BASE_URL;
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Department department = departmentService.findById(id);
        if (department == null) {
            return "redirect:" + BASE_URL;
        }

        DepartmentForm form = new DepartmentForm();
        form.fromEntity(department);
        prepareForm(model, form, BASE_URL + "/" + id, true, "departments.page.detail.title");
        return DETAIL_VIEW;
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Department department = departmentService.findById(id);
        if (department == null) {
            return "redirect:" + BASE_URL;
        }

        DepartmentForm form = new DepartmentForm();
        form.fromEntity(department);
        prepareForm(model, form, BASE_URL + "/" + id, false, "departments.page.edit.title");
        return DETAIL_VIEW;
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("data") DepartmentForm form,
            BindingResult bindingResult,
            Model model) {
        if (departmentService.findById(id) == null) {
            return "redirect:" + BASE_URL;
        }
        if (bindingResult.hasErrors()) {
            form.setId(id);
            prepareForm(model, form, BASE_URL + "/" + id, false, "departments.page.edit.title");
            return DETAIL_VIEW;
        }

        Department department = form.toEntity();
        department.setId(id);
        departmentService.update(department);
        return "redirect:" + BASE_URL + "/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        departmentService.delete(id);
        return "redirect:" + BASE_URL;
    }

    private void prepareForm(Model model, DepartmentForm form, String actionUrl, boolean readonly, String pageTitle) {
        crudView.form(model, form, actionUrl, "post", BASE_URL, readonly, FIELDS_TEMPLATE,
                validationRuleRenderer.render(DepartmentForm.class));
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("pageIcon", "bi:building");
    }
}
