package com.nhom11.qldt.feature.auth.controller;

import java.util.function.Function;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final String TABLE_FRAGMENT = "crud/table/table :: crud-table";
    private static final String FORM_FRAGMENT = "crud/detail/form :: crud-form";
    private static final String FIELDS_TEMPLATE = "auth/user/fields";
    private static final String MODAL_TARGET = "#crud-modal-content";

    private final UserService userService;

    public UserCrudController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String list(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "fullName") String sort,
            @RequestParam(defaultValue = "asc") String dir,
            HttpServletRequest request,
            Model model) {

        model.addAttribute("pageTitle", "Quản lý người dùng");

        if (isHx(request)) {
            return tableFragment(search, page, sort, dir, model);
        }

        tableFragment(search, page, sort, dir, model);
        return "auth/user/list";
    }

    @GetMapping("/new")
    public String createForm(HttpServletRequest request, Model model) {
        if (!isHx(request)) {
            return "redirect:" + PREFIX;
        }
        return formFragment(model, new UserForm(), "Thêm người dùng", PREFIX);
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("data") UserForm form,
            BindingResult bindingResult,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model) {

        if (bindingResult.hasErrors()) {
            return invalidForm(response, model, form, "Thêm người dùng", PREFIX);
        }

        userService.create(form.toEntity());
        if (isHx(request)) {
            return tableFragment(null, 0, "fullName", "asc", model);
        }
        return "redirect:" + PREFIX;
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        User user = userService.findById(id);
        if (user == null) {
            return "redirect:" + PREFIX;
        }

        UserForm form = new UserForm();
        form.fromEntity(user);

        model.addAttribute("data", form);
        model.addAttribute("pageTitle", "Chi tiết người dùng");
        model.addAttribute("isReadonly", true);
        model.addAttribute("actionUrl", PREFIX + "/" + id);
        return "auth/user/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, HttpServletRequest request, Model model) {
        User user = userService.findById(id);
        if (user == null) {
            return "redirect:" + PREFIX;
        }
        if (!isHx(request)) {
            return "redirect:" + PREFIX;
        }

        UserForm form = new UserForm();
        form.fromEntity(user);
        return formFragment(model, form, "Sửa người dùng", PREFIX + "/" + id);
    }

    @PostMapping("/{id}")
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
            return invalidForm(response, model, form, "Sửa người dùng", PREFIX + "/" + id);
        }

        User user = form.toEntity();
        user.setId(id);
        user.setPassword(existing.getPassword());
        user.setRoles(existing.getRoles());

        userService.update(user);
        if (isHx(request)) {
            return tableFragment(null, 0, "fullName", "asc", model);
        }
        return "redirect:" + PREFIX + "/" + id;
    }

    @DeleteMapping("/{id}")
    public String delete(
            @PathVariable Long id,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "fullName") String sort,
            @RequestParam(defaultValue = "asc") String dir,
            Model model) {
        userService.delete(id);
        return tableFragment(search, page, sort, dir, model);
    }

    private String tableFragment(String search, int page, String sort, String dir, Model model) {
        Sort sortObj = Sort.by(Sort.Direction.fromString(dir), sort);
        Page<User> data = userService.findAll(search, PageRequest.of(page, DEFAULT_PAGE_SIZE, sortObj));

        model.addAttribute("data", data);
        model.addAttribute("baseUrl", PREFIX);
        model.addAttribute("sortField", sort);
        model.addAttribute("sortDir", dir);
        model.addAttribute("columns", java.util.List.of(
            new ColumnDef("fullName", "Họ tên", u -> ((User) u).getFullName()),
            new ColumnDef("email", "Email", u -> ((User) u).getEmail()),
            new ColumnDef("username", "Tên đăng nhập", u -> ((User) u).getUsername()),
            new ColumnDef("phone", "Số điện thoại", u -> ((User) u).getPhone())
        ));
        return TABLE_FRAGMENT;
    }

    private String formFragment(Model model, UserForm form, String pageTitle, String actionUrl) {
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("data", form);
        model.addAttribute("actionUrl", actionUrl);
        model.addAttribute("isReadonly", false);
        model.addAttribute("fieldsTemplate", FIELDS_TEMPLATE);
        return FORM_FRAGMENT;
    }

    private String invalidForm(HttpServletResponse response, Model model, UserForm form, String pageTitle, String actionUrl) {
        response.setStatus(HttpStatus.UNPROCESSABLE_ENTITY.value());
        response.setHeader("HX-Retarget", MODAL_TARGET);
        response.setHeader("HX-Reswap", "innerHTML");
        return formFragment(model, form, pageTitle, actionUrl);
    }

    private boolean isHx(HttpServletRequest request) {
        return "true".equals(request.getHeader("HX-Request"));
    }

    public static class ColumnDef {
        public final String field;
        public final String label;
        private final Function<Object, String> valueExtractor;

        public ColumnDef(String field, String label, Function<Object, String> valueExtractor) {
            this.field = field;
            this.label = label;
            this.valueExtractor = valueExtractor;
        }

        public String getValue(Object item) {
            return valueExtractor.apply(item);
        }
    }
}
