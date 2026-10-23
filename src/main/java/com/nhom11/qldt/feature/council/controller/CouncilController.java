package com.nhom11.qldt.feature.council.controller;

import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.auth.service.UserService;
import com.nhom11.qldt.feature.council.model.dto.CouncilForm;
import com.nhom11.qldt.feature.council.model.dto.CouncilLecturerOption;
import com.nhom11.qldt.feature.council.model.entity.Council;
import com.nhom11.qldt.feature.council.service.CouncilService;
import com.nhom11.qldt.feature.period.model.entity.RegistrationPeriod;
import com.nhom11.qldt.feature.period.model.enums.PeriodType;
import com.nhom11.qldt.feature.period.service.RegistrationPeriodService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
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
@RequestMapping("/dot/{periodId}/hoi-dong")
/** Điều phối các yêu cầu HTTP của chức năng hội đồng, đánh giá và công bố kết quả. */
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
  /** Hiển thị danh sách hội đồng và kết quả đánh giá mà người dùng được phép truy cập. */
  public String list(@PathVariable Long periodId, Model model) {
    RegistrationPeriod period = periodService.findById(periodId);
    if (!isCouncilPeriod(period)) return "redirect:/dot";

    model.addAttribute("period", period);
    List<Council> councils = councilService.councils(periodId);
    model.addAttribute("councils", councils);
    model.addAttribute(
        "memberCounts",
        councils.stream()
            .collect(
                java.util.stream.Collectors.toMap(
                    council -> council.getId(),
                    council -> councilService.members(council.getId()).size())));
    model.addAttribute(
        "assignedCounts",
        councils.stream()
            .collect(
                java.util.stream.Collectors.toMap(
                    council -> council.getId(),
                    council -> councilService.assignedRegistrations(council.getId()).size())));
    model.addAttribute("managementOpen", councilService.isCouncilManagementOpen(periodId));
    model.addAttribute("pageTitle", "councils.page.list.title");
    model.addAttribute("pageIcon", "bi:people-fill");
    return "page/council/list";
  }

  @GetMapping("/tao")
  /** Hiển thị biểu mẫu tạo mới hội đồng và kết quả đánh giá. */
  public String createForm(@PathVariable Long periodId, Model model) {
    if (!isCouncilPeriod(periodService.findById(periodId))) {
      return "redirect:/dot";
    }
    if (!councilService.isCouncilManagementOpen(periodId)) {
      return "redirect:/dot/" + periodId + "/hoi-dong";
    }
    prepareForm(
        periodId,
        model,
        new CouncilForm(),
        "/dot/" + periodId + "/hoi-dong",
        "councils.page.create.title");
    return "page/council/create";
  }

  @GetMapping("/giang-vien")
  @ResponseBody
  /** Tìm danh sách giảng viên phù hợp để sử dụng trong ô lựa chọn. */
  public List<CouncilLecturerOption> lecturers(
      @PathVariable Long periodId, @RequestParam(required = false) String search) {
    if (!isCouncilPeriod(periodService.findById(periodId))) return List.of();
    return userService.searchLecturers(search).stream()
        .map(item -> new CouncilLecturerOption(item.getId(), item.getFullName(), item.getUserCode()))
        .toList();
  }

  @PostMapping
  /** Kiểm tra biểu mẫu, tạo hội đồng và kết quả đánh giá và trả về kết quả điều hướng. */
  public String create(
      @PathVariable Long periodId,
      @AuthenticationPrincipal UserDetails principal,
      @Valid @ModelAttribute("data") CouncilForm form,
      BindingResult errors,
      Model model) {
    String action = "/dot/" + periodId + "/hoi-dong";
    if (!isCouncilPeriod(periodService.findById(periodId))) {
      return "redirect:/dot";
    }
    if (errors.hasErrors()) {
      prepareForm(periodId, model, form, action, "councils.page.create.title");
      return "page/council/create";
    }
    try {
      councilService.createCouncil(periodId, user(principal), form);
    } catch (IllegalArgumentException exception) {
      errors.reject(exception.getMessage());
      prepareForm(periodId, model, form, action, "councils.page.create.title");
      return "page/council/create";
    }
    return "redirect:/dot/" + periodId + "/hoi-dong";
  }

  @GetMapping("/{councilId}/sua")
  /** Hiển thị biểu mẫu chỉnh sửa hội đồng và kết quả đánh giá. */
  public String edit(@PathVariable Long periodId, @PathVariable Long councilId, Model model) {
    Council council = councilService.findCouncil(periodId, councilId);
    if (council == null) {
      return "redirect:/dot/" + periodId + "/hoi-dong";
    }
    if (!councilService.isCouncilManagementOpen(periodId)) {
      return "redirect:/dot/" + periodId + "/hoi-dong/" + councilId;
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
        "/dot/" + periodId + "/hoi-dong/" + councilId,
        "councils.page.edit.title");
    return "page/council/create";
  }

  @PostMapping("/{councilId}")
  /** Kiểm tra biểu mẫu, cập nhật hội đồng và kết quả đánh giá và trả về kết quả điều hướng. */
  public String update(
      @PathVariable Long periodId,
      @PathVariable Long councilId,
      @Valid @ModelAttribute("data") CouncilForm form,
      BindingResult errors,
      Model model) {
    Council council = councilService.findCouncil(periodId, councilId);
    if (council == null)
      return "redirect:/dot/" + periodId + "/hoi-dong";
    String action = "/dot/" + periodId + "/hoi-dong/" + councilId;
    if (errors.hasErrors()) {
      prepareForm(periodId, model, form, action, "councils.page.edit.title");
      return "page/council/create";
    }
    try {
      councilService.updateCouncil(periodId, councilId, form);
    } catch (IllegalArgumentException exception) {
      errors.reject(exception.getMessage());
      prepareForm(periodId, model, form, action, "councils.page.edit.title");
      return "page/council/create";
    }
    return "redirect:/dot/" + periodId + "/hoi-dong/" + councilId;
  }

  @GetMapping("/{councilId}")
  /** Hiển thị thông tin chi tiết của hội đồng và kết quả đánh giá được yêu cầu. */
  public String detail(@PathVariable Long periodId, @PathVariable Long councilId, Model model) {
    Council council = councilService.findCouncil(periodId, councilId);
    if (council == null)
      return "redirect:/dot/" + periodId + "/hoi-dong";
    model.addAttribute("council", council);
    model.addAttribute("members", councilService.members(councilId));
    model.addAttribute("eligibleRegistrations", councilService.eligibleUnassignedRegistrations(councilId));
    model.addAttribute("assignedRegistrations", councilService.assignedRegistrations(councilId));
    model.addAttribute("managementOpen", councilService.isCouncilManagementOpen(periodId));
    model.addAttribute("pageTitle", "councils.page.detail.title");
    model.addAttribute("pageIcon", "bi:people-fill");
    return "page/council/detail";
  }

  @PostMapping("/{councilId}/de-tai/{topicId}")
  /** Gán đề tài được chọn cho hội đồng. */
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
    return "redirect:/dot/" + periodId + "/hoi-dong/" + councilId;
  }

  @PostMapping("/{councilId}/de-tai/{topicId}/bo-phan-cong")
  /** Gỡ đề tài khỏi hội đồng hiện tại. */
  public String unassignTopic(
      @PathVariable Long periodId,
      @PathVariable Long councilId,
      @PathVariable Long topicId,
      org.springframework.web.servlet.mvc.support.RedirectAttributes attributes) {
    try {
      councilService.unassignTopic(periodId, councilId, topicId);
    } catch (IllegalArgumentException exception) {
      attributes.addFlashAttribute("error", exception.getMessage());
    }
    return "redirect:/dot/" + periodId + "/hoi-dong/" + councilId;
  }

  @PostMapping("/{councilId}/xoa")
  /** Xóa hội đồng và kết quả đánh giá được yêu cầu và chuyển hướng kèm thông báo kết quả. */
  public String delete(@PathVariable Long periodId, @PathVariable Long councilId, org.springframework.web.servlet.mvc.support.RedirectAttributes attributes) {
    try {
      councilService.deleteCouncil(periodId, councilId);
    } catch (IllegalArgumentException exception) {
      attributes.addFlashAttribute("error", exception.getMessage());
    } catch (DataIntegrityViolationException exception) {
      attributes.addFlashAttribute("deleteError", "crud.delete.error.in-use");
    }
    return "redirect:/dot/" + periodId + "/hoi-dong";
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
