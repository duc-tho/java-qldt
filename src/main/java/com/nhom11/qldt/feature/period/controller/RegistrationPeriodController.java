package com.nhom11.qldt.feature.period.controller;

import com.nhom11.qldt.core.validation.ClientValidationRuleRenderer;
import com.nhom11.qldt.core.view.CrudViewSupport;
import com.nhom11.qldt.feature.auth.service.UserService;
import com.nhom11.qldt.feature.council.model.dto.PublicationRevocationForm;
import com.nhom11.qldt.feature.council.model.enums.ResultPublicationAction;
import com.nhom11.qldt.feature.council.service.CouncilService;
import com.nhom11.qldt.feature.group.service.StudentGroupService;
import com.nhom11.qldt.feature.period.model.dto.RegistrationPeriodForm;
import com.nhom11.qldt.feature.period.model.entity.RegistrationPeriod;
import com.nhom11.qldt.feature.period.model.enums.PeriodType;
import com.nhom11.qldt.feature.period.service.RegistrationPeriodService;
import com.nhom11.qldt.feature.topic.service.TopicService;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
/** Điều phối các yêu cầu HTTP của chức năng đợt đăng ký. */
public class RegistrationPeriodController {
  private final RegistrationPeriodService service;
  private final UserService users;
  private final CrudViewSupport view;
  private final ClientValidationRuleRenderer validation;
  private final CouncilService councilService;
  private final TopicService topicService;
  private final StudentGroupService groupService;

  public RegistrationPeriodController(
      RegistrationPeriodService service,
      UserService users,
      CrudViewSupport view,
      ClientValidationRuleRenderer validation,
      CouncilService councilService,
      TopicService topicService,
      StudentGroupService groupService) {
    this.service = service;
    this.users = users;
    this.view = view;
    this.validation = validation;
    this.councilService = councilService;
    this.topicService = topicService;
    this.groupService = groupService;
  }

  /** Hiển thị biểu mẫu tạo mới đợt đăng ký. */
  public String newForm(Model model) {
    form(
        model,
        new RegistrationPeriodForm(),
        "/dot",
        false,
        "registration_periods.page.create.title");

    return "page/period/create";
  }

  /** Kiểm tra biểu mẫu, tạo đợt đăng ký và trả về kết quả điều hướng. */
  public String create(
      @Valid @ModelAttribute("data") RegistrationPeriodForm form,
      BindingResult errors,
      Model model) {

    if (errors.hasErrors()) {
      form(model, form, "/dot", false, "registration_periods.page.create.title");

      return "page/period/create";
    }

    RegistrationPeriod item = form.toEntity();
    addBusinessValidationErrors(item, errors);

    if (errors.hasErrors()) {
      form(model, form, "/dot", false, "registration_periods.page.create.title");

      return "page/period/create";
    }

    item.setCreatedBy(
        users
            .findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
            .orElseThrow());

    service.create(item);

    return "redirect:/dot";
  }

  /** Hiển thị thông tin chi tiết của đợt đăng ký được yêu cầu. */
  public String detail(@PathVariable Long id, Model model) {

    RegistrationPeriod item = service.findById(id);

    if (item == null) return "redirect:/dot";

    prepareReadonlyDetail(model, item);
    addResultStatus(model, id);
    model.addAttribute("periodTopics", topicService.findAllWithDetailsByRegistrationPeriodId(id));
    model.addAttribute("groupSummaries", groupService.groupSummaries(id));
    if (item.getPeriodType() == PeriodType.KLTN) {
      var councils = councilService.councils(id);
      model.addAttribute("councilSummaries", councilService.councilSummaries(id, councils));
    } else if (item.getPeriodType() == PeriodType.TLCN) {
      model.addAttribute(
          "reviewerAssignmentTopics", councilService.reviewerAssignmentTopics(id));
    }
    model.addAttribute("isStaff", true);
    model.addAttribute("isLecturer", false);
    model.addAttribute("isStudent", false);
    model.addAttribute("canCreateGroup", false);
    model.addAttribute("groupId", null);
    model.addAttribute("assignedCount", 0L);
    if (!model.containsAttribute("reviewerAssignmentTopics")) {
      model.addAttribute("reviewerAssignmentTopics", java.util.List.of());
    }

    return "page/period/detail";
  }

  /** Chuẩn bị model chỉ đọc để hiển thị chi tiết đợt đăng ký. */
  public void prepareReadonlyDetail(Model model, RegistrationPeriod item) {
    RegistrationPeriodForm form = new RegistrationPeriodForm();
    form.fromEntity(item);

    form(
        model, form, "/dot/" + item.getId(), true, "registration_periods.page.detail.title");
    addPeriodOverview(model, item);
    model.addAttribute("pageHeading", item.getName());
    model.addAttribute("topicsSectionTitle", "topics.section.title");
    model.addAttribute("groupsSectionTitle", "student-groups.section.period-groups");
  }

  /** Hiển thị biểu mẫu chỉnh sửa đợt đăng ký. */
  public String edit(@PathVariable Long id, Model model) {
    RegistrationPeriod item = service.findById(id);

    if (item == null) return "redirect:/dot";

    RegistrationPeriodForm form = new RegistrationPeriodForm();
    form.fromEntity(item);

    form(model, form, "/dot/" + id, false, "registration_periods.page.edit.title");

    return "page/period/detail";
  }

  /** Kiểm tra biểu mẫu, cập nhật đợt đăng ký và trả về kết quả điều hướng. */
  public String update(
      @PathVariable Long id,
      @Valid @ModelAttribute("data") RegistrationPeriodForm form,
      BindingResult errors,
      Model model) {

    RegistrationPeriod old = service.findById(id);

    if (old == null) return "redirect:/dot";

    if (errors.hasErrors()) {
      form(
          model,
          form,
          "/dot/" + id,
          false,
          "registration_periods.page.edit.title");
      return "page/period/detail";
    }

    RegistrationPeriod item = form.toEntity();
    item.setId(id);
    item.setCreatedBy(old.getCreatedBy());
    addBusinessValidationErrors(item, errors);

    if (errors.hasErrors()) {
      form(
          model,
          form,
          "/dot/" + id,
          false,
          "registration_periods.page.edit.title");
      return "page/period/detail";
    }

    service.update(item);

    return "redirect:/dot/" + id;
  }

  /** Xóa đợt đăng ký được yêu cầu và chuyển hướng kèm thông báo kết quả. */
  public String delete(@PathVariable Long id, RedirectAttributes attributes) {
    try {
      service.delete(id);
    } catch (DataIntegrityViolationException exception) {
      attributes.addFlashAttribute("deleteError", "crud.delete.error.in-use");
    }

    return "redirect:/dot";
  }

  private void form(
      Model model, RegistrationPeriodForm form, String action, boolean readonly, String pageTitle) {
    view.form(
        model,
        form,
        action,
        "post",
        "/dot",
        "w-full",
        readonly,
        "fragment/feature/period/fields",
        validation.render(RegistrationPeriodForm.class));

    model.addAttribute("periodTypes", PeriodType.values());
    model.addAttribute("pageTitle", pageTitle);
    model.addAttribute("pageDesc", pageTitle.replace(".title", ".description"));
    model.addAttribute("pageIcon", "bi:calendar-range");
    model.addAttribute("breadcrumbParentUrl", "/dot");
    model.addAttribute("breadcrumbParentLabel", "registration_periods.page.list.title");
    model.addAttribute("isStaff", true);
  }

  private void addBusinessValidationErrors(RegistrationPeriod period, BindingResult errors) {
    service
        .validationErrors(period)
        .forEach(
            error -> {
              switch (error) {
                case TIMELINE -> errors.reject(error.messageCode());
                case REVIEWER_DEADLINE ->
                    errors.rejectValue(
                        "reviewerDeadline",
                        period.getReviewerDeadline() == null
                            ? "registration_periods.validation.reviewer_deadline.required"
                            : error.messageCode());
                case COUNCIL_DATE ->
                    errors.rejectValue(
                        "councilDate",
                        period.getCouncilDate() == null
                            ? "registration_periods.validation.council_date.required"
                            : error.messageCode());
              }
            });
  }

  private void addResultStatus(Model model, Long periodId) {
    var publicationHistory = councilService.publicationHistory(periodId);
    var latestPublication = publicationHistory.isEmpty() ? null : publicationHistory.getFirst();
    model.addAttribute("latestPublication", latestPublication);
    model.addAttribute("publicationHistory", publicationHistory);
    model.addAttribute(
        "finalizedRegistrationIds", councilService.finalizedRegistrationIds(periodId));
    model.addAttribute("revocationForm", new PublicationRevocationForm());
    model.addAttribute("resultsPublished", latestPublication != null
        && latestPublication.getAction() == ResultPublicationAction.PUBLISHED);
    model.addAttribute(
        "canPublishResults", councilService.publishablePeriodIds(java.util.List.of(periodId)).contains(periodId));
  }

  private void addPeriodOverview(Model model, RegistrationPeriod period) {
    LocalDateTime now = LocalDateTime.now();
    model.addAttribute("period", period);
    model.addAttribute("periodStage", service.stage(period, now));
    model.addAttribute("now", now);
    int currentStep;
    if (now.isBefore(period.getLecturerRegistrationStart())) {
      currentStep = 1;
    } else if (now.isBefore(period.getStudentRegistrationStart())) {
      currentStep = 2;
    } else if (now.isBefore(period.getStudentRegistrationEnd())) {
      currentStep = 3;
    } else if (now.isBefore(period.getReportDeadline())) {
      currentStep = 4;
    } else {
      currentStep = 0;
    }
    model.addAttribute("currentStep", currentStep);
    if (now.isBefore(period.getLecturerRegistrationStart())) {
      model.addAttribute("nextDeadline", period.getLecturerRegistrationStart());
      model.addAttribute("nextDeadlineLabel", "registration_periods.deadline.proposal-open");
    } else if (now.isBefore(period.getLecturerRegistrationEnd())) {
      model.addAttribute("nextDeadline", period.getLecturerRegistrationEnd());
      model.addAttribute("nextDeadlineLabel", "registration_periods.deadline.proposal");
    } else if (now.isBefore(period.getStudentRegistrationStart())) {
      model.addAttribute("nextDeadline", period.getStudentRegistrationStart());
      model.addAttribute("nextDeadlineLabel", "registration_periods.deadline.student-open");
    } else if (now.isBefore(period.getStudentRegistrationEnd())) {
      model.addAttribute("nextDeadline", period.getStudentRegistrationEnd());
      model.addAttribute("nextDeadlineLabel", "registration_periods.deadline.student");
    } else if (now.isBefore(period.getReportDeadline())) {
      model.addAttribute("nextDeadline", period.getReportDeadline());
      model.addAttribute("nextDeadlineLabel", "registration_periods.deadline.report");
    } else if (period.getCouncilDate() != null && now.isBefore(period.getCouncilDate())) {
      model.addAttribute("nextDeadline", period.getCouncilDate());
      model.addAttribute("nextDeadlineLabel", "registration_periods.deadline.council");
    } else if (period.getReviewerDeadline() != null
        && now.isBefore(period.getReviewerDeadline())) {
      model.addAttribute("nextDeadline", period.getReviewerDeadline());
      model.addAttribute("nextDeadlineLabel", "registration_periods.deadline.review");
    }
  }
}
