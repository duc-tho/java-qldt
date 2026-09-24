package com.nhom11.qldt.feature.auth.controller;

import java.util.List;
import java.util.Map;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.nhom11.qldt.core.table.TableQuery;
import com.nhom11.qldt.core.validation.ClientValidationRuleRenderer;
import com.nhom11.qldt.core.view.CrudViewSupport;
import com.nhom11.qldt.feature.auth.model.dto.UserForm;
import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.auth.service.UserService;
import com.nhom11.qldt.feature.auth.view.UserTableDefinition;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/users")
public class UserController {

    private static final String BASE_URL = "/users";
    private static final String TEMPLATE_USER_FIELDS = "fragment/page/user/fields";
    private static final String VIEW_USER_LIST = "fragment/page/user/list";
    private static final String VIEW_USER_DETAIL = "fragment/page/user/detail";
    private static final String VIEW_USER_CREATE = "fragment/page/user/create";
    private static final String FORM_METHOD_POST = "post";
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
            Model model) {

        Page<User> users = userService.findAll(query, userTableDefinition);
        crudView.table(
                model,
                users,
                BASE_URL,
                query,
                userTableDefinition);

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

            model.addAttribute("pageTitle", "users.page.create.title");
            model.addAttribute("pageIcon", "bi:person-plus");

            return VIEW_USER_CREATE;
        }

        userService.create(form.toEntity());

        return "redirect:" + BASE_URL;
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
                FORM_METHOD_POST,
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
                FORM_METHOD_POST,
                BASE_URL,
                false,
                TEMPLATE_USER_FIELDS,
                validationRules);

        return VIEW_USER_DETAIL;
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("data") UserForm form,
            BindingResult bindingResult,
            Model model) {

        User existingUser = userService.findById(id);
        if (existingUser == null) {
            return "redirect:" + BASE_URL;
        }

        if (bindingResult.hasErrors()) {
            Map<String, String> validationRules = validationRuleRenderer.render(UserForm.class);
            crudView.form(
                    model,
                    form,
                    BASE_URL + "/" + id,
                    FORM_METHOD_POST,
                    BASE_URL,
                    false,
                    TEMPLATE_USER_FIELDS,
                    validationRules);

            model.addAttribute("pageTitle", "users.page.edit.title");
            model.addAttribute("pageIcon", "bi:person");

            return VIEW_USER_DETAIL;
        }

        User updates = form.toEntity();
        updates.setId(id);
        userService.update(updates);
        return "redirect:" + BASE_URL + "/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int force) {

        if (force == FORCE_DELETE_PERMANENTLY) {
            userService.deletePermanently(id);
        } else {
            userService.delete(id);
        }

        return "redirect:" + BASE_URL;
    }

    @PostMapping("/delete")
    public String batchDelete(
            @RequestParam("ids") List<Long> ids) {

        userService.softDeleteAll(ids);

        return "redirect:" + BASE_URL;
    }
}
