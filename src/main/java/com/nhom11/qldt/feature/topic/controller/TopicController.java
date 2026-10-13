package com.nhom11.qldt.feature.topic.controller;

import com.nhom11.qldt.core.table.TableQuery;
import com.nhom11.qldt.core.validation.ClientValidationRuleRenderer;
import com.nhom11.qldt.core.view.CrudViewSupport;
import com.nhom11.qldt.feature.auth.service.UserService;
import com.nhom11.qldt.feature.department.service.DepartmentService;
import com.nhom11.qldt.feature.registrationperiod.model.entity.RegistrationPeriod;
import com.nhom11.qldt.feature.registrationperiod.service.RegistrationPeriodService;
import com.nhom11.qldt.feature.studentgroup.service.StudentGroupService;
import com.nhom11.qldt.feature.topic.model.dto.TopicForm;
import com.nhom11.qldt.feature.topic.model.dto.RejectTopicForm;
import com.nhom11.qldt.feature.topic.model.entity.Topic;
import com.nhom11.qldt.feature.topic.service.TopicService;
import com.nhom11.qldt.feature.topic.view.TopicTableDefinition;
import jakarta.validation.Valid;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/registration-periods/{periodId}/topics")
public class TopicController {
  private final TopicService topicService;
  private final TopicTableDefinition tableDefinition;
  private final CrudViewSupport crudView;
  private final ClientValidationRuleRenderer validationRuleRenderer;
  private final RegistrationPeriodService registrationPeriodService;
  private final DepartmentService departmentService;
  private final UserService userService;
  private final StudentGroupService studentGroupService;

  public TopicController(
      TopicService topicService,
      TopicTableDefinition tableDefinition,
      CrudViewSupport crudView,
      ClientValidationRuleRenderer validationRuleRenderer,
      RegistrationPeriodService registrationPeriodService,
      DepartmentService departmentService,
      UserService userService,
      StudentGroupService studentGroupService) {
    this.topicService = topicService;
    this.tableDefinition = tableDefinition;
    this.crudView = crudView;
    this.validationRuleRenderer = validationRuleRenderer;
    this.registrationPeriodService = registrationPeriodService;
    this.departmentService = departmentService;
    this.userService = userService;
    this.studentGroupService = studentGroupService;
  }

  @GetMapping
  public String list(@PathVariable Long periodId, @ModelAttribute TableQuery query, Model model) {
    RegistrationPeriod period = findPeriod(periodId);
    if (period == null) {
      return "redirect:/registration-periods";
    }

    crudView.table(
        model,
        topicService.findAllByRegistrationPeriodId(periodId, query, tableDefinition),
        baseUrl(periodId),
        query,
        tableDefinition);
    model.addAttribute("pageTitle", "topics.page.list.title");
    model.addAttribute("pageIcon", "bi:journal-text");
    model.addAttribute("tableCanCreate", topicService.canManageTopicsAsStaff(period));
    addBreadcrumbParent(model, periodId);

    return "fragment/page/topic/list";
  }

  @GetMapping("/new")
  public String createForm(@PathVariable Long periodId, Model model) {
    if (findPeriod(periodId) == null) {
      return "redirect:/registration-periods";
    }

    prepare(model, new TopicForm(), baseUrl(periodId), false, "topics.page.create.title", periodId);
    return "fragment/page/topic/create";
  }

  @PostMapping
  public String create(
      @PathVariable Long periodId,
      @Valid @ModelAttribute("data") TopicForm form,
      BindingResult errors,
      Model model) {
    RegistrationPeriod period = findPeriod(periodId);
    if (period == null) {
      return "redirect:/registration-periods";
    }
    if (errors.hasErrors()) {
      prepare(model, form, baseUrl(periodId), false, "topics.page.create.title", periodId);
      return "fragment/page/topic/create";
    }

    try {
      topicService.create(form.toEntity(), period, form.getDepartmentId(), form.getAdviserIds());
    } catch (IllegalArgumentException exception) {
      if (TopicService.REGISTRATION_STAGE_CLOSED.equals(exception.getMessage())) {
        errors.reject(exception.getMessage());
      } else {
        errors.rejectValue("adviserIds", "topics.validation.advisers.invalid");
      }
      prepare(model, form, baseUrl(periodId), false, "topics.page.create.title", periodId);
      return "fragment/page/topic/create";
    }
    return periodDetailRedirect(periodId);
  }

  @GetMapping("/{id}")
  public String detail(@PathVariable Long periodId, @PathVariable Long id, Model model) {
    Topic topic = findTopic(periodId, id);
    if (topic == null) {
      return periodDetailRedirect(periodId);
    }

    return detailView(model, topic, periodId);
  }

  @GetMapping("/{id}/edit")
  public String edit(@PathVariable Long periodId, @PathVariable Long id, Model model) {
    Topic topic = findTopic(periodId, id);
    if (topic == null) {
      return periodDetailRedirect(periodId);
    }

    TopicForm form = new TopicForm();
    form.fromEntity(topic);
    prepare(model, form, topicUrl(periodId, id), false, "topics.page.edit.title", periodId);
    model.addAttribute("topic", topic);
    model.addAttribute("rejectForm", new RejectTopicForm());
    return "fragment/page/topic/detail";
  }

  @PostMapping("/{id}")
  public String update(
      @PathVariable Long periodId,
      @PathVariable Long id,
      @Valid @ModelAttribute("data") TopicForm form,
      BindingResult errors,
      Model model) {
    Topic old = findTopic(periodId, id);
    if (old == null) {
      return periodDetailRedirect(periodId);
    }
    if (errors.hasErrors()) {
      form.setId(id);
      prepare(model, form, topicUrl(periodId, id), false, "topics.page.edit.title", periodId);
      model.addAttribute("topic", old);
      model.addAttribute("rejectForm", new RejectTopicForm());
      return "fragment/page/topic/detail";
    }

    Topic topic = form.toEntity();
    topic.setId(id);
    try {
      topicService.update(
          topic, old.getRegistrationPeriod(), form.getDepartmentId(), form.getAdviserIds());
    } catch (IllegalArgumentException exception) {
      if (TopicService.REGISTRATION_STAGE_CLOSED.equals(exception.getMessage())) {
        errors.reject(exception.getMessage());
      } else {
        errors.rejectValue("adviserIds", "topics.validation.advisers.invalid");
      }
      prepare(model, form, topicUrl(periodId, id), false, "topics.page.edit.title", periodId);
      model.addAttribute("topic", old);
      model.addAttribute("rejectForm", new RejectTopicForm());
      return "fragment/page/topic/detail";
    }
    return periodDetailRedirect(periodId);
  }

  @PostMapping("/{id}/delete")
  public String delete(
      @PathVariable Long periodId, @PathVariable Long id, RedirectAttributes redirectAttributes) {
    if (findTopic(periodId, id) != null) {
      try {
        topicService.delete(id);
      } catch (IllegalArgumentException exception) {
        redirectAttributes.addFlashAttribute("deleteError", exception.getMessage());
      } catch (DataIntegrityViolationException exception) {
        redirectAttributes.addFlashAttribute("deleteError", "crud.delete.error.in-use");
      }
    }
    return periodDetailRedirect(periodId);
  }

  @PostMapping("/delete")
  public String deleteSelected(
      @PathVariable Long periodId,
      @RequestParam(name = "ids", required = false) java.util.List<Long> ids,
      RedirectAttributes redirectAttributes) {
    if (findPeriod(periodId) == null) {
      return "redirect:/registration-periods";
    }

    try {
      topicService.deleteAllByRegistrationPeriodId(ids, periodId);
    } catch (IllegalArgumentException exception) {
      redirectAttributes.addFlashAttribute("deleteError", exception.getMessage());
    } catch (DataIntegrityViolationException exception) {
      redirectAttributes.addFlashAttribute("deleteError", "crud.delete.error.in-use");
    }
    return periodDetailRedirect(periodId);
  }

  private RegistrationPeriod findPeriod(Long periodId) {
    return registrationPeriodService.findById(periodId);
  }

  private Topic findTopic(Long periodId, Long id) {
    if (findPeriod(periodId) == null) {
      return null;
    }

    return topicService.findByIdAndRegistrationPeriodId(id, periodId);
  }

  private String baseUrl(Long periodId) {
    return "/registration-periods/" + periodId + "/topics";
  }

  private String topicUrl(Long periodId, Long id) {
    return baseUrl(periodId) + "/" + id;
  }

  private String periodDetailRedirect(Long periodId) {
    return "redirect:/registration-periods/" + periodId;
  }

  private void prepare(
      Model model,
      TopicForm form,
      String actionUrl,
      boolean readonly,
      String title,
      Long periodId) {
    crudView.form(
        model,
        form,
        actionUrl,
        "post",
        "/registration-periods/" + periodId,
        readonly,
        "fragment/page/topic/fields",
        validationRuleRenderer.render(TopicForm.class));
    model.addAttribute("pageTitle", title);
    model.addAttribute("pageIcon", "bi:journal-text");
    model.addAttribute("departments", departmentService.findAll());
    model.addAttribute("lecturers", userService.findAllLecturers());
    RegistrationPeriod period = findPeriod(periodId);
    model.addAttribute(
        "canManageTopic",
        period != null && topicService.canManageTopicsAsStaff(period));
    addBreadcrumbParent(model, periodId);
  }

  private void addBreadcrumbParent(Model model, Long periodId) {
    RegistrationPeriod period = findPeriod(periodId);
    if (period == null) return;

    model.addAttribute("breadcrumbParentUrl", "/registration-periods/" + periodId);
    model.addAttribute("breadcrumbParentLabel", period.getName());
  }

  @GetMapping("/propose")
  public String proposeForm(@PathVariable Long periodId, Model model) {
    if (findPeriod(periodId) == null) return "redirect:/registration-periods";
    prepare(
        model,
        new TopicForm(),
        baseUrl(periodId) + "/propose",
        false,
        "topics.page.propose.title",
        periodId);
    return "fragment/page/topic/create";
  }

  @PostMapping("/propose")
  public String propose(
      @PathVariable Long periodId,
      @AuthenticationPrincipal UserDetails principal,
      @Valid @ModelAttribute("data") TopicForm form,
      BindingResult errors,
      Model model) {
    RegistrationPeriod period = findPeriod(periodId);
    if (period == null || principal == null) return "redirect:/registration-periods";
    if (errors.hasErrors()) {
      prepare(
          model,
          form,
          baseUrl(periodId) + "/propose",
          false,
          "topics.page.propose.title",
          periodId);
      return "fragment/page/topic/create";
    }
    try {
      topicService.createProposal(
          form.toEntity(),
          period,
          form.getDepartmentId(),
          form.getAdviserIds(),
          userService.findByEmail(principal.getUsername()).orElseThrow());
    } catch (IllegalArgumentException exception) {
      errors.reject(exception.getMessage());
      prepare(
          model,
          form,
          baseUrl(periodId) + "/propose",
          false,
          "topics.page.propose.title",
          periodId);
      return "fragment/page/topic/create";
    }
    return "redirect:/topic-proposals";
  }

  @PostMapping("/{id}/approve")
  public String approve(
      @PathVariable Long periodId, @PathVariable Long id, RedirectAttributes redirectAttributes) {
    try {
      topicService.approve(id, periodId);
    } catch (IllegalArgumentException exception) {
      redirectAttributes.addFlashAttribute("error", exception.getMessage());
    }
    return "redirect:" + topicUrl(periodId, id);
  }

  @PostMapping("/{id}/reject")
  public String reject(
      @PathVariable Long periodId,
      @PathVariable Long id,
      @Valid @ModelAttribute("rejectForm") RejectTopicForm form,
      BindingResult errors,
      Model model) {
    Topic topic = findTopic(periodId, id);
    if (topic == null) {
      return periodDetailRedirect(periodId);
    }
    if (!errors.hasErrors()) {
      try {
        topicService.reject(id, periodId, form.getReason());
      } catch (IllegalArgumentException exception) {
        errors.reject(exception.getMessage());
      }
    }
    if (!errors.hasErrors()) {
      return "redirect:" + topicUrl(periodId, id);
    }
    return detailView(model, topic, periodId);
  }

  private String detailView(Model model, Topic topic, Long periodId) {
    TopicForm form = new TopicForm();
    form.fromEntity(topic);
    prepare(model, form, topicUrl(periodId, topic.getId()), true, "topics.page.detail.title", periodId);
    model.addAttribute("topic", topic);
    model.addAttribute("groupRegistrations", studentGroupService.registrationsForTopic(topic.getId()));
    if (!model.containsAttribute("rejectForm")) {
      model.addAttribute("rejectForm", new RejectTopicForm());
    }
    return "fragment/page/topic/detail";
  }
}
