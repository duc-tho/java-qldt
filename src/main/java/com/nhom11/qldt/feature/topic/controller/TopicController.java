package com.nhom11.qldt.feature.topic.controller;

import com.nhom11.qldt.core.table.TableQuery;
import com.nhom11.qldt.core.validation.ClientValidationRuleRenderer;
import com.nhom11.qldt.core.view.CrudViewSupport;
import com.nhom11.qldt.feature.registrationperiod.model.entity.RegistrationPeriod;
import com.nhom11.qldt.feature.registrationperiod.service.RegistrationPeriodService;
import com.nhom11.qldt.feature.auth.service.UserService;
import com.nhom11.qldt.feature.department.service.DepartmentService;
import com.nhom11.qldt.feature.topic.model.dto.TopicForm;
import com.nhom11.qldt.feature.topic.model.entity.Topic;
import com.nhom11.qldt.feature.topic.service.TopicService;
import com.nhom11.qldt.feature.topic.view.TopicTableDefinition;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

  public TopicController(
      TopicService topicService,
      TopicTableDefinition tableDefinition,
      CrudViewSupport crudView,
      ClientValidationRuleRenderer validationRuleRenderer,
      RegistrationPeriodService registrationPeriodService,
      DepartmentService departmentService,
      UserService userService) {
    this.topicService = topicService;
    this.tableDefinition = tableDefinition;
    this.crudView = crudView;
    this.validationRuleRenderer = validationRuleRenderer;
    this.registrationPeriodService = registrationPeriodService;
    this.departmentService = departmentService;
    this.userService = userService;
  }

  @GetMapping
  public String list(@PathVariable Long periodId, @ModelAttribute TableQuery query, Model model) {
    if (findPeriod(periodId) == null) {
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
      errors.rejectValue("adviserIds", "topics.validation.advisers.invalid");
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

    TopicForm form = new TopicForm();
    form.fromEntity(topic);
    prepare(model, form, topicUrl(periodId, id), true, "topics.page.detail.title", periodId);
    return "fragment/page/topic/detail";
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
      return "fragment/page/topic/detail";
    }

    Topic topic = form.toEntity();
    topic.setId(id);
    try {
      topicService.update(topic, old.getRegistrationPeriod(), form.getDepartmentId(), form.getAdviserIds());
    } catch (IllegalArgumentException exception) {
      errors.rejectValue("adviserIds", "topics.validation.advisers.invalid");
      prepare(model, form, topicUrl(periodId, id), false, "topics.page.edit.title", periodId);
      return "fragment/page/topic/detail";
    }
    return periodDetailRedirect(periodId);
  }

  @PostMapping("/{id}/delete")
  public String delete(@PathVariable Long periodId, @PathVariable Long id) {
    if (findTopic(periodId, id) != null) {
      topicService.delete(id);
    }
    return periodDetailRedirect(periodId);
  }

  @PostMapping("/delete")
  public String deleteSelected(
      @PathVariable Long periodId,
      @RequestParam(name = "ids", required = false) java.util.List<Long> ids) {
    if (findPeriod(periodId) == null) {
      return "redirect:/registration-periods";
    }

    topicService.deleteAllByRegistrationPeriodId(ids, periodId);
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
  }
}
