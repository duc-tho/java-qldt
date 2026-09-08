package com.nhom11.qldt.feature.auth.controller;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.nhom11.qldt.core.validation.ClientValidationRuleRenderer;
import com.nhom11.qldt.feature.auth.model.dto.LoginForm;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@Controller
public class AuthController {
    private final ClientValidationRuleRenderer clientValidationRuleRenderer;

    public AuthController(ClientValidationRuleRenderer clientValidationRuleRenderer) {
        this.clientValidationRuleRenderer = clientValidationRuleRenderer;
    }

    @ModelAttribute("validationRules")
    public Map<String, String> validationRules() {
        return clientValidationRuleRenderer.render(LoginForm.class);
    }

    @GetMapping("/login")
    public String loginView(Model model) {
        model.addAttribute("data", new LoginForm());

        return "auth/login";
    }

    @PostMapping("/login")
    public String login(
            @Valid @ModelAttribute("data") LoginForm loginForm,
            BindingResult bindingResult,
            Model model, HttpServletResponse response) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("formError", "Thông tin đăng nhập không hợp lệ");

            return "layout/form/login-form :: login-form";
        }

        response.setHeader("HX-Redirect", "/");

        return null;
    }
}
