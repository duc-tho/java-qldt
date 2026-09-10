package com.nhom11.qldt.feature.auth.controller;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.nhom11.qldt.core.table.ActionDef;
import com.nhom11.qldt.core.table.ColumnDef;
import com.nhom11.qldt.core.validation.ClientValidationRuleRenderer;
import com.nhom11.qldt.feature.auth.model.dto.UserForm;
import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.auth.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/users")
public class UserCrudController {

    private static final String PREFIX = "/users";
    private static final List<Integer> PAGE_SIZE_OPTIONS = List.of(10, 20, 50);
    private static final String TABLE_FRAGMENT = "fragment/table/table :: crud-table";
    private static final String FORM_FRAGMENT = "fragment/form/crud-form :: crud-form";
    private static final String FIELDS_TEMPLATE = "fragment/page/user/fields";
    private static final String LIST_PAGE = "fragment/page/user/list";
    private static final String DETAIL_PAGE = "fragment/page/user/detail";
    private static final String CREATE_PAGE = "fragment/page/user/create";
    private static final String TABLE_TARGET = "#crud-table";
    private static final String METHOD_POST = "post";
    private static final String METHOD_PUT = "put";
    private static final String PASSWORD_REQUIRED_MESSAGE = "Mật khẩu không được để trống";
    private static final String MESSAGE_CREATE = "Thêm người dùng";
    private static final String MESSAGE_UPDATE = "Sửa người dùng";

    private final UserService userService;
    private final ClientValidationRuleRenderer validationRuleRenderer;

    public UserCrudController(UserService userService, ClientValidationRuleRenderer validationRuleRenderer) {
        this.userService = userService;
        this.validationRuleRenderer = validationRuleRenderer;
    }

    @GetMapping
    public String list(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "asc") String dir,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        model.addAttribute("pageTitle", "Quản lý người dùng");
        model.addAttribute("pageIcon", "bi:people");
        model.addAttribute("pageDesc", "Quản lý tài khoản người dùng trong hệ thống");
        tableFragment(search, page, sort, dir, size, model);

        return LIST_PAGE;
    }

    @PostMapping("/list")
    public String listFragment(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "asc") String dir,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request,
            Model model) {

        if (!isHx(request)) {
            return "redirect:" + PREFIX;
        }

        return tableFragment(search, page, sort, dir, size, model);
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("pageTitle", MESSAGE_CREATE);
        model.addAttribute("pageIcon", "bi:person-plus");
        formFragment(model, new UserForm(), METHOD_POST, PREFIX, PREFIX, true);

        return CREATE_PAGE;
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("data") UserForm form,
            BindingResult bindingResult,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model) {

        if (form.getPassword() == null || form.getPassword().isBlank()) {
            bindingResult.rejectValue("password", "NotBlank", PASSWORD_REQUIRED_MESSAGE);
        }

        if (bindingResult.hasErrors()) {
            response.setStatus(HttpStatus.UNPROCESSABLE_ENTITY.value());
            formFragment(model, form, METHOD_POST, PREFIX, PREFIX, true);
            return FORM_FRAGMENT;
        }

        userService.create(form.toEntity());
        response.setHeader("HX-Redirect", PREFIX);
        return null;
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        User user = userService.findById(id);
        if (user == null) {
            return "redirect:" + PREFIX;
        }

        UserForm form = new UserForm();
        form.fromEntity(user);

        model.addAttribute("pageTitle", "Chi tiết người dùng");
        model.addAttribute("pageIcon", "bi:person");
        model.addAttribute("baseUrl", PREFIX);
        formFragment(model, form, METHOD_PUT, PREFIX + "/" + id, PREFIX, false);
        model.addAttribute("isReadonly", true);

        return DETAIL_PAGE;
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        User user = userService.findById(id);
        if (user == null) {
            return "redirect:" + PREFIX;
        }

        UserForm form = new UserForm();
        form.fromEntity(user);

        model.addAttribute("pageTitle", MESSAGE_UPDATE);
        model.addAttribute("pageIcon", "bi:person");
        model.addAttribute("baseUrl", PREFIX);
        formFragment(model, form, METHOD_PUT, PREFIX + "/" + id, PREFIX, false);

        return DETAIL_PAGE;
    }

    @PutMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("data") UserForm form,
            BindingResult bindingResult,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model) {

        User existing = userService.findById(id);
        if (existing == null) {
            return "redirect:" + PREFIX;
        }

        if (bindingResult.hasErrors()) {
            response.setStatus(HttpStatus.UNPROCESSABLE_ENTITY.value());
            formFragment(model, form, METHOD_PUT, PREFIX + "/" + id, PREFIX, false);
            return FORM_FRAGMENT;
        }

        User user = form.toEntity();
        user.setId(id);
        if (form.getPassword() == null || form.getPassword().isBlank()) {
            user.setPassword(existing.getPassword());
        }
        user.setRoles(existing.getRoles());

        userService.update(user);
        response.setHeader("HX-Redirect", PREFIX + "/" + id);
        return null;
    }

    @DeleteMapping("/{id}")
    public String delete(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int force,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "asc") String dir,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model) {

        if (force == 1) {
            userService.deletePermanently(id);
        } else {
            userService.delete(id);
        }

        if (TABLE_TARGET.equals(request.getHeader("HX-Target"))) {
            return tableFragment(search, page, sort, dir, size, model);
        }

        response.setHeader("HX-Redirect", PREFIX);
        return null;
    }

    @DeleteMapping
    public String batchDelete(
            @RequestParam("ids") List<Long> ids,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "asc") String dir,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        userService.softDeleteAll(ids);

        return tableFragment(search, page, sort, dir, size, model);
    }

    private String tableFragment(String search, int page, String sort, String dir, int size, Model model) {
        Page<User> data = userService.findAll(search, PageRequest.of(page, size, toSort(sort, dir)));

        model.addAttribute("data", data);
        model.addAttribute("baseUrl", PREFIX);
        model.addAttribute("search", search == null ? "" : search);
        model.addAttribute("sortField", sort == null ? "" : sort);
        model.addAttribute("sortDir", dir);
        model.addAttribute("pageSizeOptions", PAGE_SIZE_OPTIONS);
        model.addAttribute("columns", List.of(
                new ColumnDef("id", "ID", u -> String.valueOf(((User) u).getId())),
                new ColumnDef("fullName", "Họ tên", u -> ((User) u).getFullName()),
                new ColumnDef("email", "Email", u -> ((User) u).getEmail()),
                new ColumnDef("username", "Tên đăng nhập", u -> ((User) u).getUsername()),
                new ColumnDef("phone", "Số điện thoại", u -> ((User) u).getPhone())));
        model.addAttribute("actions", List.of(
                new ActionDef.Link("Sửa", "bi:pencil-square", u -> PREFIX + "/" + ((User) u).getId() + "/edit"),
                new ActionDef.Delete("Xóa", "bi:trash", u -> PREFIX + "/" + ((User) u).getId(),
                        u -> ((User) u).getFullName())));
        model.addAttribute("totalCount", data.getTotalElements());

        return TABLE_FRAGMENT;
    }

    private void formFragment(Model model, UserForm form, String method, String actionUrl, String backUrl,
            boolean passwordRequired) {
        model.addAttribute("data", form);
        model.addAttribute("actionUrl", actionUrl);
        model.addAttribute("method", method);
        model.addAttribute("isReadonly", false);
        model.addAttribute("backUrl", backUrl);
        model.addAttribute("fieldsTemplate", FIELDS_TEMPLATE);
        model.addAttribute("validationRules", validationRuleRenderer.render(UserForm.class,
                passwordRequired ? Map.of("password", ClientValidationRuleRenderer.createPasswordRule(PASSWORD_REQUIRED_MESSAGE))
                        : Map.of()));
    }

    private Sort toSort(String sort, String dir) {
        if (sort == null || sort.isBlank()) {
            return Sort.unsorted();
        }

        Sort.Direction direction = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;

        return Sort.by(direction, sort);
    }

    private boolean isHx(HttpServletRequest request) {
        return "true".equals(request.getHeader("HX-Request"));
    }
}
