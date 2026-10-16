package com.nhom11.qldt.feature.group.controller;

import com.nhom11.qldt.feature.group.model.entity.TopicRegistration;
import com.nhom11.qldt.feature.group.service.StudentGroupService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/registration-periods/{periodId}/student-group")
public class LegacyGroupController {
  private final StudentGroupService groupService;

  public LegacyGroupController(StudentGroupService groupService) {
    this.groupService = groupService;
  }

  @GetMapping
  public String list(@PathVariable Long periodId) {
    return "redirect:/dot/" + periodId + "/nhom";
  }

  @GetMapping("/new")
  public String create(@PathVariable Long periodId) {
    return "redirect:/dot/" + periodId + "/nhom/tao";
  }

  @GetMapping("/{groupId}")
  public String detail(@PathVariable Long periodId, @PathVariable Long groupId) {
    return "redirect:/dot/" + periodId + "/nhom/" + groupId;
  }

  @GetMapping("/{groupId}/topics")
  public String topics(@PathVariable Long periodId, @PathVariable Long groupId) {
    return "redirect:/dot/" + periodId + "/de-tai?nhomId=" + groupId;
  }

  @GetMapping("/{groupId}/topic")
  public String topic(@PathVariable Long periodId, @PathVariable Long groupId) {
    if (groupService.findGroup(groupId, periodId) == null) {
      return "redirect:/dot/" + periodId + "/nhom";
    }
    TopicRegistration registration = groupService.registration(groupId);
    if (registration == null) return detail(periodId, groupId);
    return "redirect:/dot/" + periodId + "/de-tai/" + registration.getTopic().getId()
        + "?nhomId=" + groupId;
  }
}
