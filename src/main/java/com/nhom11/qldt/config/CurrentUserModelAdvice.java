package com.nhom11.qldt.config;

import com.nhom11.qldt.feature.auth.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(annotations = Controller.class)
public class CurrentUserModelAdvice {
  private final UserService userService;

  public CurrentUserModelAdvice(UserService userService) {
    this.userService = userService;
  }

  @ModelAttribute
  public void addCurrentUser(
      Authentication authentication, HttpServletRequest request, Model model) {
    model.addAttribute("currentPath", request.getRequestURI());

    if (authentication == null || !authentication.isAuthenticated()) return;

    userService
        .findByEmail(authentication.getName())
        .ifPresent(user -> model.addAttribute("currentUser", user));
  }
}
