package com.nhom11.qldt.feature.auth.controller;

import com.nhom11.qldt.core.validation.ClientValidationRuleRenderer;
import com.nhom11.qldt.feature.auth.model.dto.AccountForm;
import com.nhom11.qldt.feature.auth.model.dto.PasswordChangeForm;
import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.auth.service.UserService;
import jakarta.validation.Valid;
import java.util.Objects;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/tai-khoan")
public class AccountController {

  private final UserService userService;
  private final ClientValidationRuleRenderer validationRuleRenderer;

  public AccountController(
      UserService userService, ClientValidationRuleRenderer validationRuleRenderer) {
    this.userService = userService;
    this.validationRuleRenderer = validationRuleRenderer;
  }

  @GetMapping
  public String view(@AuthenticationPrincipal UserDetails principal, Model model) {
    if (principal == null) {
      return "redirect:/login";
    }

    AccountForm form = new AccountForm();
    form.fromEntity(currentUser(principal));
    prepareModel(model, form, new PasswordChangeForm(), false);
    return "page/account/detail";
  }

  @PostMapping
  public String update(
      @AuthenticationPrincipal UserDetails principal,
      @Valid @ModelAttribute("data") AccountForm form,
      BindingResult bindingResult,
      Model model) {
    if (principal == null) {
      return "redirect:/login";
    }

    User user = currentUser(principal);
    form.setEmail(user.getEmail());
    form.setUserCode(user.getUserCode());

    if (bindingResult.hasErrors()) {
      prepareModel(model, form, new PasswordChangeForm(), false);
      return "page/account/detail";
    }

    userService.updateAccount(user.getId(), form);
    return "redirect:/tai-khoan?updated";
  }

  @PostMapping("/password")
  public String changePassword(
      @AuthenticationPrincipal UserDetails principal,
      @Valid @ModelAttribute("passwordData") PasswordChangeForm form,
      BindingResult bindingResult,
      Model model) {
    if (principal == null) {
      return "redirect:/login";
    }

    User user = currentUser(principal);
    if (!Objects.equals(form.getNewPassword(), form.getConfirmPassword())) {
      bindingResult.rejectValue("confirmPassword", "account.validation.confirm_password.mismatch");
    }

    if (bindingResult.hasErrors() || !userService.changePassword(user.getId(), form)) {
      if (!bindingResult.hasErrors()) {
        bindingResult.rejectValue("currentPassword", "account.validation.current_password.invalid");
      }
      AccountForm accountForm = new AccountForm();
      accountForm.fromEntity(user);
      prepareModel(model, accountForm, form, true);
      return "page/account/detail";
    }

    return "redirect:/tai-khoan?passwordUpdated";
  }

  private User currentUser(UserDetails principal) {
    return userService
        .findByEmail(principal.getUsername())
        .orElseThrow(() -> new IllegalStateException("Authenticated user was not found"));
  }

  private void prepareModel(
      Model model, AccountForm form, PasswordChangeForm passwordForm, boolean passwordDialogOpen) {
    model.addAttribute("data", form);
    model.addAttribute("passwordData", passwordForm);
    model.addAttribute("pageTitle", "account.page.title");
    model.addAttribute("pageIcon", "bi:person-circle");
    model.addAttribute("validationRules", validationRuleRenderer.render(AccountForm.class));
    model.addAttribute(
        "passwordValidationRules", validationRuleRenderer.render(PasswordChangeForm.class));
    model.addAttribute("passwordDialogOpen", passwordDialogOpen);
  }
}
