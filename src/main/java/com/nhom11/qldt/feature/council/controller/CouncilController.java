package com.nhom11.qldt.feature.council.controller;

import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.auth.service.UserService;
import com.nhom11.qldt.feature.council.model.dto.CouncilForm;
import com.nhom11.qldt.feature.council.model.dto.CouncilLecturerOption;
import com.nhom11.qldt.feature.council.model.dto.PublicationRevocationForm;
import com.nhom11.qldt.feature.council.model.entity.Council;
import com.nhom11.qldt.feature.council.service.CouncilService;
import com.nhom11.qldt.feature.registrationperiod.model.entity.RegistrationPeriod;
import com.nhom11.qldt.feature.registrationperiod.model.enums.PeriodType;
import com.nhom11.qldt.feature.registrationperiod.service.RegistrationPeriodService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/registration-periods/{periodId}/councils")
public class CouncilController {
  private final CouncilService councilService;
  private final RegistrationPeriodService periodService;
  private final UserService userService;

  public CouncilController(CouncilService councilService, RegistrationPeriodService periodService, UserService userService) {
    this.councilService = councilService;
    this.periodService = periodService;
    this.userService = userService;
  }

  @GetMapping
  public String list(@PathVariable Long periodId, Model model) {
    RegistrationPeriod period = periodService.findById(periodId);
    if (!isCouncilPeriod(period)) return "redirect:/registration-periods";

    model.addAttribute("period", period);
    List<Council> councils = councilService.councils(periodId);
    model.addAttribute("councils", councils);
    model.addAttribute(
        "memberCounts",
        councils.stream()
            .collect(
                java.util.stream.Collectors.toMap(
                    Council::getId, council -> councilService.members(council.getId()).size())));
    model.addAttribute(
        "assignedCounts",
        councils.stream()
            .collect(
                java.util.stream.Collectors.toMap(
                    Council::getId,
                    council -> councilService.assignedRegistrations(council.getId()).size())));
    model.addAttribute("latestPublication", councilService.latestPublication(periodId));
    model.addAttribute("publicationHistory", councilService.publicationHistory(periodId));
    model.addAttribute("revocationForm", new PublicationRevocationForm());
    model.addAttribute("pageTitle", "councils.page.list.title");
    model.addAttribute("pageIcon", "bi:people-fill");
    return "fragment/page/council/list";
  }

  @GetMapping("/new")
  public String createForm(@PathVariable Long periodId, Model model) {
    if (!isCouncilPeriod(periodService.findById(periodId))) {
      return "redirect:/registration-periods";
    }
    prepareForm(
        periodId,
        model,
        new CouncilForm(),
        "/registration-periods/" + periodId + "/councils",
        "councils.page.create.title");
    return "fragment/page/council/create";
  }

  @GetMapping("/lecturers")
  @ResponseBody
  public List<CouncilLecturerOption> lecturers(
      @PathVariable Long periodId, @RequestParam(required = false) String search) {
    if (!isCouncilPeriod(periodService.findById(periodId))) return List.of();
    return userService.searchLecturers(search).stream()
        .map(item -> new CouncilLecturerOption(item.getId(), item.getFullName(), item.getUserCode()))
        .toList();
  }

  @PostMapping
  public String create(
      @PathVariable Long periodId,
      @AuthenticationPrincipal UserDetails principal,
      @Valid @ModelAttribute("data") CouncilForm form,
      BindingResult errors,
      Model model) {
    String action = "/registration-periods/" + periodId + "/councils";
    if (!isCouncilPeriod(periodService.findById(periodId))) {
      return "redirect:/registration-periods";
    }
    if (errors.hasErrors()) {
      prepareForm(periodId, model, form, action, "councils.page.create.title");
      return "fragment/page/council/create";
    }
    try {
      councilService.createCouncil(periodId, user(principal), form);
    } catch (IllegalArgumentException exception) {
      errors.reject(exception.getMessage());
      prepareForm(periodId, model, form, action, "councils.page.create.title");
      return "fragment/page/council/create";
    }
    return "redirect:/registration-periods/" + periodId + "/councils";
  }

  @GetMapping("/{councilId}/edit")
  public String edit(@PathVariable Long periodId, @PathVariable Long councilId, Model model) {
    Council council = councilService.findCouncil(periodId, councilId);
    if (council == null) {
      return "redirect:/registration-periods/" + periodId + "/councils";
    }
    CouncilForm form = new CouncilForm();
    form.setName(council.getName());
    var members = councilService.members(councilId);
    form.setMemberIds(members.stream().map(member -> member.getLecturer().getId()).toList());
    members.forEach(member -> {
      if (member.getMemberRole().name().equals("CHAIRPERSON")) form.setChairpersonId(member.getLecturer().getId());
      if (member.getMemberRole().name().equals("SECRETARY")) form.setSecretaryId(member.getLecturer().getId());
    });
    prepareForm(
        periodId,
        model,
        form,
        "/registration-periods/" + periodId + "/councils/" + councilId,
        "councils.page.edit.title");
    return "fragment/page/council/create";
  }

  @PostMapping("/{councilId}")
  public String update(
      @PathVariable Long periodId,
      @PathVariable Long councilId,
      @Valid @ModelAttribute("data") CouncilForm form,
      BindingResult errors,
      Model model) {
    Council council = councilService.findCouncil(periodId, councilId);
    if (council == null)
      return "redirect:/registration-periods/" + periodId + "/councils";
    String action = "/registration-periods/" + periodId + "/councils/" + councilId;
    if (errors.hasErrors()) {
      prepareForm(periodId, model, form, action, "councils.page.edit.title");
      return "fragment/page/council/create";
    }
    try {
      councilService.updateCouncil(periodId, councilId, form);
    } catch (IllegalArgumentException exception) {
      errors.reject(exception.getMessage());
      prepareForm(periodId, model, form, action, "councils.page.edit.title");
      return "fragment/page/council/create";
    }
    return "redirect:/registration-periods/" + periodId + "/councils/" + councilId;
  }

  @GetMapping("/{councilId}")
  public String detail(@PathVariable Long periodId, @PathVariable Long councilId, Model model) {
    Council council = councilService.findCouncil(periodId, councilId);
    if (council == null)
      return "redirect:/registration-periods/" + periodId + "/councils";
    model.addAttribute("council", council);
    model.addAttribute("members", councilService.members(councilId));
    model.addAttribute("eligibleRegistrations", councilService.eligibleUnassignedRegistrations(councilId));
    model.addAttribute("assignedRegistrations", councilService.assignedRegistrations(councilId));
    model.addAttribute("pageTitle", "councils.page.detail.title");
    model.addAttribute("pageIcon", "bi:people-fill");
    return "fragment/page/council/detail";
  }

  @PostMapping("/{councilId}/topics/{topicId}")
  public String assignTopic(
      @PathVariable Long periodId,
      @PathVariable Long councilId,
      @PathVariable Long topicId,
      org.springframework.web.servlet.mvc.support.RedirectAttributes attributes) {
    try {
      councilService.assignTopic(periodId, councilId, topicId);
    } catch (IllegalArgumentException exception) {
      attributes.addFlashAttribute("error", exception.getMessage());
    }
    return "redirect:/registration-periods/" + periodId + "/councils/" + councilId;
  }

  @PostMapping("/{councilId}/delete")
  public String delete(@PathVariable Long periodId, @PathVariable Long councilId, org.springframework.web.servlet.mvc.support.RedirectAttributes attributes) {
    try {
      councilService.deleteCouncil(periodId, councilId);
    } catch (IllegalArgumentException exception) {
      attributes.addFlashAttribute("error", exception.getMessage());
    }
    return "redirect:/registration-periods/" + periodId + "/councils";
  }

  private void prepareForm(
      Long periodId, Model model, CouncilForm form, String action, String pageTitle) {
    model.addAttribute("period", periodService.findById(periodId));
    model.addAttribute("data", form);
    model.addAttribute("formAction", action);
    model.addAttribute("initialMembers", councilService.lecturerOptions(form.getMemberIds()));
    model.addAttribute("pageTitle", pageTitle);
    model.addAttribute("pageIcon", "bi:people-fill");
  }

  private boolean isCouncilPeriod(RegistrationPeriod period) {
    return period != null && period.getPeriodType() == PeriodType.KLTN;
  }

  private User user(UserDetails principal) {
    return userService.findByEmail(principal.getUsername()).orElseThrow();
  }
}
