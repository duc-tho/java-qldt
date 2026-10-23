package com.nhom11.qldt.feature.announcement.controller;

import com.nhom11.qldt.core.table.TableQuery;
import com.nhom11.qldt.core.validation.ClientValidationRuleRenderer;
import com.nhom11.qldt.core.view.CrudViewSupport;
import com.nhom11.qldt.feature.announcement.model.dto.AnnouncementForm;
import com.nhom11.qldt.feature.announcement.model.entity.Announcement;
import com.nhom11.qldt.feature.announcement.model.enums.AnnouncementStatus;
import com.nhom11.qldt.feature.announcement.service.AnnouncementService;
import com.nhom11.qldt.feature.announcement.view.AnnouncementTableDefinition;
import com.nhom11.qldt.feature.auth.repository.RoleRepository;
import jakarta.validation.Valid;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/thong-bao")
/** Điều phối các yêu cầu HTTP của chức năng thông báo. */
public class AnnouncementController {
  private final AnnouncementService service;
  private final AnnouncementTableDefinition table;
  private final RoleRepository roles;
  private final CrudViewSupport view;
  private final ClientValidationRuleRenderer validation;

  public AnnouncementController(
      AnnouncementService service,
      AnnouncementTableDefinition table,
      RoleRepository roles,
      CrudViewSupport view,
      ClientValidationRuleRenderer validation) {
    this.service = service;
    this.table = table;
    this.roles = roles;
    this.view = view;
    this.validation = validation;
  }

  @GetMapping
  /** Hiển thị danh sách thông báo mà người dùng được phép truy cập. */
  public String list(@ModelAttribute TableQuery query, Authentication authentication, Model model) {
    boolean staff = isStaff(authentication);

    model.addAttribute("pageTitle", "announcements.page.list.title");
    model.addAttribute("pageIcon", "bi:megaphone");
    model.addAttribute("isStaff", staff);

    // Người dùng thông thường chỉ thấy thông báo đã công bố cho ít nhất một vai trò của họ.
    if (!staff) {
      model.addAttribute("announcements", service.findPublishedForRoles(roleNames(authentication)));
      return "page/announcement/list";
    }

    Page<Announcement> data = service.findAll(query, table);

    view.table(model, data, "/thong-bao", query, table);

    return "page/announcement/list";
  }

  @GetMapping("/new")
  /** Hiển thị biểu mẫu tạo mới thông báo. */
  public String createForm(Model model) {
    form(model, new AnnouncementForm(), "/thong-bao", false, "announcements.page.create.title");

    return "page/announcement/create";
  }

  @PostMapping
  /** Kiểm tra biểu mẫu, tạo thông báo và trả về kết quả điều hướng. */
  public String create(
      @Valid @ModelAttribute("data") AnnouncementForm form, BindingResult errors, Model model) {
    validateAudience(form, errors);

    if (errors.hasErrors()) {
      form(model, form, "/thong-bao", false, "announcements.page.create.title");

      return "page/announcement/create";
    }

    service.create(form.toEntity(), form.getRoleIds());

    return "redirect:/thong-bao";
  }

  @GetMapping("/{id}")
  /** Hiển thị thông tin chi tiết của thông báo được yêu cầu. */
  public String detail(@PathVariable Long id, Authentication authentication, Model model) {
    boolean staff = isStaff(authentication);

    // Nhân viên được tra cứu trực tiếp; người dùng khác phải đi qua truy vấn có kiểm tra đối tượng nhận.
    Announcement item = staff
        ? service.findById(id)
        : service.findPublishedByIdForRoles(id, roleNames(authentication));

    if (item == null) {
      if (!staff) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
      return "redirect:/thong-bao";
    }

    if (!staff) {
      model.addAttribute("announcement", item);
      model.addAttribute("pageTitle", "announcements.page.detail.title");
      model.addAttribute("pageIcon", "bi:megaphone");
      return "page/announcement/read-detail";
    }

    AnnouncementForm form = new AnnouncementForm();
    form.fromEntity(item);

    form(model, form, "/thong-bao/" + id, true, "announcements.page.detail.title");

    model.addAttribute("canEdit", item.getStatus() == AnnouncementStatus.PUBLISHED);

    return "page/announcement/detail";
  }

  @GetMapping("/{id}/edit")
  /** Hiển thị biểu mẫu chỉnh sửa thông báo. */
  public String edit(@PathVariable Long id, Model model) {
    Announcement item = service.findById(id);

    if (item == null || item.getStatus() == AnnouncementStatus.WITHDRAWN)
      return "redirect:/thong-bao";

    AnnouncementForm form = new AnnouncementForm();
    form.fromEntity(item);
    form(model, form, "/thong-bao/" + id, false, "announcements.page.edit.title");

    return "page/announcement/detail";
  }

  @PostMapping("/{id}")
  /** Kiểm tra biểu mẫu, cập nhật thông báo và trả về kết quả điều hướng. */
  public String update(
      @PathVariable Long id,
      @Valid @ModelAttribute("data") AnnouncementForm form,
      BindingResult errors,
      Model model) {
    Announcement existing = service.findById(id);
    if (existing == null || existing.getStatus() == AnnouncementStatus.WITHDRAWN) {
      return "redirect:/thong-bao";
    }

    validateAudience(form, errors);

    if (errors.hasErrors()) {
      form(model, form, "/thong-bao/" + id, false, "announcements.page.edit.title");
      return "page/announcement/detail";
    }
    Announcement item = form.toEntity();
    item.setId(id);
    service.update(item, form.getRoleIds());
    return "redirect:/thong-bao/" + id;
  }

  @PostMapping("/{id}/withdraw")
  /** Thu hồi thông báo đã chọn khỏi danh sách công bố. */
  public String withdraw(@PathVariable Long id) {
    service.withdraw(id);
    return "redirect:/thong-bao";
  }

  private void validateAudience(AnnouncementForm form, BindingResult errors) {
    if (!errors.hasFieldErrors("roleIds") && !service.hasValidRoles(form.getRoleIds())) {
      errors.rejectValue("roleIds", "announcements.validation.audience.invalid");
    }
  }

  private void form(
      Model model, AnnouncementForm data, String action, boolean readonly, String pageTitle) {
    view.form(
        model,
        data,
        action,
        "post",
        "/thong-bao",
        readonly,
        "fragment/feature/announcement/fields",
        validation.render(AnnouncementForm.class));

    model.addAttribute("roles", roles.findAll());
    model.addAttribute("pageTitle", pageTitle);
    model.addAttribute("pageIcon", "bi:megaphone");
  }

  private boolean isStaff(Authentication authentication) {
    return authentication.getAuthorities().stream().anyMatch(authority ->
        authority.getAuthority().equals("ROLE_ADMIN")
            || authority.getAuthority().equals("ROLE_HEAD_OF_FACULTY"));
  }

  private Set<String> roleNames(Authentication authentication) {
    // Bỏ tiền tố của Spring Security để khớp với tên vai trò được lưu trong cơ sở dữ liệu.
    return authentication.getAuthorities().stream()
        .map(authority -> authority.getAuthority())
        .filter(authority -> authority.startsWith("ROLE_"))
        .map(authority -> authority.substring("ROLE_".length()))
        .collect(Collectors.toSet());
  }
}
