package com.nhom11.qldt.feature.auth.controller;

import com.nhom11.qldt.core.validation.ClientValidationRuleRenderer;
import com.nhom11.qldt.feature.auth.model.dto.LoginForm;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

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
  public String loginView(
      @RequestParam(required = false) String error, HttpSession session, Model model) {
    LoginForm loginForm = new LoginForm();

    if (error != null) {
      Object email = session.getAttribute(LoginForm.SESSION_EMAIL_ATTRIBUTE);
      session.removeAttribute(LoginForm.SESSION_EMAIL_ATTRIBUTE);

      if (email instanceof String loginEmail) {
        loginForm.setEmail(loginEmail);
      }
    }

    model.addAttribute("data", loginForm);

    return "auth/login";
  }
}
