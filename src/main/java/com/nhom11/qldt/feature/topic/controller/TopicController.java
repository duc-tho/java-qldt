package com.nhom11.qldt.feature.topic.controller;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.nhom11.qldt.core.table.TableQuery;
import com.nhom11.qldt.core.validation.ClientValidationRuleRenderer;
import com.nhom11.qldt.core.view.CrudViewSupport;
import com.nhom11.qldt.feature.department.service.DepartmentService;
import com.nhom11.qldt.feature.topic.model.dto.TopicForm;
import com.nhom11.qldt.feature.topic.model.entity.Topic;
import com.nhom11.qldt.feature.topic.service.TopicService;
import com.nhom11.qldt.feature.topic.view.TopicTableDefinition;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/topics")
public class TopicController {
    private static final String BASE_URL = "/topics";
    private final TopicService topicService;
    private final DepartmentService departmentService;
    private final TopicTableDefinition tableDefinition;
    private final CrudViewSupport crudView;
    private final ClientValidationRuleRenderer validationRuleRenderer;

    public TopicController(TopicService topicService, DepartmentService departmentService, TopicTableDefinition tableDefinition, CrudViewSupport crudView, ClientValidationRuleRenderer validationRuleRenderer) {
        this.topicService = topicService;
        this.departmentService = departmentService;
        this.tableDefinition = tableDefinition;
        this.crudView = crudView;
        this.validationRuleRenderer = validationRuleRenderer;
    }

    @GetMapping
    public String list(@ModelAttribute TableQuery query, Model model) {
        Page<Topic> topics = topicService.findAll(query, tableDefinition);
        crudView.table(model, topics, BASE_URL, query, tableDefinition);
        model.addAttribute("pageTitle", "topics.page.list.title"); model.addAttribute("pageIcon", "bi:journal-text"); model.addAttribute("pageDesc", "topics.page.list.description");
        return "fragment/page/topic/list";
    }

    @GetMapping("/new") public String createForm(Model model) { prepare(model, new TopicForm(), BASE_URL, false, "topics.page.create.title"); return "fragment/page/topic/create"; }
    @PostMapping public String create(@Valid @ModelAttribute("data") TopicForm form, BindingResult errors, Model model) { if (errors.hasErrors()) { prepare(model, form, BASE_URL, false, "topics.page.create.title"); return "fragment/page/topic/create"; } topicService.create(form.toEntity(), form.getDepartmentId()); return "redirect:" + BASE_URL; }
    @GetMapping("/{id}") public String detail(@PathVariable Long id, Model model) { Topic topic = topicService.findById(id); if (topic == null) return "redirect:" + BASE_URL; TopicForm form = new TopicForm(); form.fromEntity(topic); prepare(model, form, BASE_URL + "/" + id, true, "topics.page.detail.title"); return "fragment/page/topic/detail"; }
    @GetMapping("/{id}/edit") public String edit(@PathVariable Long id, Model model) { Topic topic = topicService.findById(id); if (topic == null) return "redirect:" + BASE_URL; TopicForm form = new TopicForm(); form.fromEntity(topic); prepare(model, form, BASE_URL + "/" + id, false, "topics.page.edit.title"); return "fragment/page/topic/detail"; }
    @PostMapping("/{id}") public String update(@PathVariable Long id, @Valid @ModelAttribute("data") TopicForm form, BindingResult errors, Model model) { if (topicService.findById(id) == null) return "redirect:" + BASE_URL; if (errors.hasErrors()) { form.setId(id); prepare(model, form, BASE_URL + "/" + id, false, "topics.page.edit.title"); return "fragment/page/topic/detail"; } Topic topic = form.toEntity(); topic.setId(id); topicService.update(topic, form.getDepartmentId()); return "redirect:" + BASE_URL + "/" + id; }
    @PostMapping("/{id}/delete") public String delete(@PathVariable Long id) { topicService.delete(id); return "redirect:" + BASE_URL; }

    private void prepare(Model model, TopicForm form, String actionUrl, boolean readonly, String title) { crudView.form(model, form, actionUrl, "post", BASE_URL, readonly, "fragment/page/topic/fields", validationRuleRenderer.render(TopicForm.class)); model.addAttribute("departments", departmentService.findAll()); model.addAttribute("pageTitle", title); model.addAttribute("pageIcon", "bi:journal-text"); }
}
