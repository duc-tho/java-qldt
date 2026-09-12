package com.nhom11.qldt.feature.auth.controller;

import java.util.List;
import java.util.Map;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
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

import com.nhom11.qldt.core.table.TableQuery;
import com.nhom11.qldt.core.utils.HtmxRequestUtils;
import com.nhom11.qldt.core.validation.ClientValidationRuleRenderer;
import com.nhom11.qldt.core.view.CrudViewSupport;
import com.nhom11.qldt.feature.auth.model.dto.UserForm;
import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.auth.service.UserService;
import com.nhom11.qldt.feature.auth.view.UserTableDefinition;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/users")
public class UserController {

    private static final String BASE_URL = "/users";
    private static final String VIEW_FORM_FRAGMENT = "fragment/crud/form :: crud-form";
    private static final String TEMPLATE_USER_FIELDS = "fragment/page/user/fields";
    private static final String VIEW_USER_LIST = "fragment/page/user/list";
    private static final String VIEW_USER_DETAIL = "fragment/page/user/detail";
    private static final String VIEW_USER_CREATE = "fragment/page/user/create";
    private static final String TABLE_TARGET = "#crud-table";
    private static final String HX_REDIRECT_HEADER = "HX-Redirect";
    private static final String FORM_METHOD_POST = "post";
    private static final String FORM_METHOD_PUT = "put";
    private static final int FORCE_DELETE_PERMANENTLY = 1;

    private final UserService userService;
    private final ClientValidationRuleRenderer validationRuleRenderer;
    private final CrudViewSupport crudView;
    private final UserTableDefinition userTableDefinition;
    private final MessageSource messageSource;

    public UserController(
            UserService userService,
            ClientValidationRuleRenderer validationRuleRenderer,
            CrudViewSupport crudView,
            UserTableDefinition userTableDefinition,
            MessageSource messageSource) {
        this.userService = userService;
        this.validationRuleRenderer = validationRuleRenderer;
        this.crudView = crudView;
        this.userTableDefinition = userTableDefinition;
        this.messageSource = messageSource;
    }

    @GetMapping
    public String list(
            @ModelAttribute TableQuery query,
            HttpServletRequest request,
            Model model) {

        Page<User> users = userService.findAll(query, userTableDefinition);
        String tableView = crudView.table(
                model,
                users,
                BASE_URL,
                query,
                userTableDefinition);

        if (HtmxRequestUtils.isHtmxRequest(request)) {
            return tableView;
        }

        model.addAttribute("pageTitle", "users.page.list.title");
        model.addAttribute("pageIcon", "bi:people");
        model.addAttribute("pageDesc", "users.page.list.description");

        return VIEW_USER_LIST;
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("pageTitle", "users.page.create.title");
        model.addAttribute("pageIcon", "bi:person-plus");

        Map<String, String> validationRules = validationRuleRenderer.render(
                UserForm.class,
                Map.of(
                        "password",
                        ClientValidationRuleRenderer.createPasswordRule(
                                messageSource.getMessage(
                                        "users.validation.password.required",
                                        null,
                                        LocaleContextHolder.getLocale()))));
        crudView.form(
                model,
                new UserForm(),
                BASE_URL,
                FORM_METHOD_POST,
                BASE_URL,
                false,
                TEMPLATE_USER_FIELDS,
                validationRules);

        return VIEW_USER_CREATE;
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("data") UserForm form,
            BindingResult bindingResult,
            HttpServletResponse response,
            Model model) {

        if (form.getPassword() == null || form.getPassword().isBlank()) {
            bindingResult.rejectValue(
                    "password",
                    "NotBlank",
                    messageSource.getMessage(
                            "users.validation.password.required",
                            null,
                            LocaleContextHolder.getLocale()));
        }

        if (bindingResult.hasErrors()) {
            response.setStatus(HttpStatus.UNPROCESSABLE_CONTENT.value());

            Map<String, String> validationRules = validationRuleRenderer.render(
                    UserForm.class,
                    Map.of(
                            "password",
                            ClientValidationRuleRenderer.createPasswordRule(
                                    messageSource.getMessage(
                                            "users.validation.password.required",
                                            null,
                                            LocaleContextHolder.getLocale()))));
            crudView.form(
                    model,
                    form,
                    BASE_URL,
                    FORM_METHOD_POST,
                    BASE_URL,
                    false,
                    TEMPLATE_USER_FIELDS,
                    validationRules);

            return VIEW_FORM_FRAGMENT;
        }

        userService.create(form.toEntity());
        response.setHeader(HX_REDIRECT_HEADER, BASE_URL);

        return null;
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        User user = userService.findById(id);
        if (user == null) {
            return "redirect:" + BASE_URL;
        }

        UserForm form = new UserForm();
        form.fromEntity(user);

        model.addAttribute("pageTitle", "users.page.detail.title");
        model.addAttribute("pageIcon", "bi:person");

        Map<String, String> validationRules = validationRuleRenderer.render(UserForm.class);
        crudView.form(
                model,
                form,
                BASE_URL + "/" + id,
                FORM_METHOD_PUT,
                BASE_URL,
                true,
                TEMPLATE_USER_FIELDS,
                validationRules);

        return VIEW_USER_DETAIL;
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        User user = userService.findById(id);
        if (user == null) {
            return "redirect:" + BASE_URL;
        }

        UserForm form = new UserForm();
        form.fromEntity(user);

        model.addAttribute("pageTitle", "users.page.edit.title");
        model.addAttribute("pageIcon", "bi:person");

        Map<String, String> validationRules = validationRuleRenderer.render(UserForm.class);
        crudView.form(
                model,
                form,
                BASE_URL + "/" + id,
                FORM_METHOD_PUT,
                BASE_URL,
                false,
                TEMPLATE_USER_FIELDS,
                validationRules);

        return VIEW_USER_DETAIL;
    }

    @PutMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("data") UserForm form,
            BindingResult bindingResult,
            HttpServletResponse response,
            Model model) {

        User existingUser = userService.findById(id);
        if (existingUser == null) {
            return "redirect:" + BASE_URL;
        }

        if (bindingResult.hasErrors()) {
            response.setStatus(HttpStatus.UNPROCESSABLE_ENTITY.value());

            Map<String, String> validationRules = validationRuleRenderer.render(UserForm.class);
            crudView.form(
                    model,
                    form,
                    BASE_URL + "/" + id,
                    FORM_METHOD_PUT,
                    BASE_URL,
                    false,
                    TEMPLATE_USER_FIELDS,
                    validationRules);

            return VIEW_FORM_FRAGMENT;
        }

        User updates = form.toEntity();
        updates.setId(id);
        userService.update(updates);
        response.setHeader(HX_REDIRECT_HEADER, BASE_URL + "/" + id);

        return null;
    }

    @DeleteMapping("/{id}")
    public String delete(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int force,
            @ModelAttribute TableQuery query,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model) {

        if (force == FORCE_DELETE_PERMANENTLY) {
            userService.deletePermanently(id);
        } else {
            userService.delete(id);
        }

        if (TABLE_TARGET.equals(request.getHeader("HX-Target"))) {
            Page<User> users = userService.findAll(query, userTableDefinition);

            return crudView.table(
                    model,
                    users,
                    BASE_URL,
                    query,
                    userTableDefinition);
        }

        response.setHeader(HX_REDIRECT_HEADER, BASE_URL);

        return null;
    }

    @DeleteMapping
    public String batchDelete(
            @RequestParam("ids") List<Long> ids,
            @ModelAttribute TableQuery query,
            Model model) {

        userService.softDeleteAll(ids);

        Page<User> users = userService.findAll(query, userTableDefinition);

        return crudView.table(
                model,
                users,
                BASE_URL,
                query,
                userTableDefinition);
    }
}
