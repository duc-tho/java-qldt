package com.nhom11.qldt.feature.announcement.controller;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import com.nhom11.qldt.core.table.TableQuery;
import com.nhom11.qldt.core.validation.ClientValidationRuleRenderer;
import com.nhom11.qldt.core.view.CrudViewSupport;
import com.nhom11.qldt.feature.announcement.model.dto.AnnouncementForm;
import com.nhom11.qldt.feature.announcement.model.entity.Announcement;
import com.nhom11.qldt.feature.announcement.service.AnnouncementService;
import com.nhom11.qldt.feature.announcement.view.AnnouncementTableDefinition;
import com.nhom11.qldt.feature.auth.repository.RoleRepository;
import jakarta.validation.Valid;

@Controller @RequestMapping("/announcements")
public class AnnouncementController {
    private final AnnouncementService service; private final AnnouncementTableDefinition table; private final RoleRepository roles; private final CrudViewSupport view; private final ClientValidationRuleRenderer validation;
    public AnnouncementController(AnnouncementService service, AnnouncementTableDefinition table, RoleRepository roles, CrudViewSupport view, ClientValidationRuleRenderer validation) { this.service=service; this.table=table; this.roles=roles; this.view=view; this.validation=validation; }
    @GetMapping public String list(@ModelAttribute TableQuery query, Model model) { Page<Announcement> data=service.findAll(query, table); view.table(model,data,"/announcements",query,table); model.addAttribute("pageTitle","announcements.page.list.title"); model.addAttribute("pageIcon","bi:megaphone"); return "fragment/page/announcement/list"; }
    @GetMapping("/new") public String createForm(Model model) { form(model,new AnnouncementForm(),"/announcements",false); return "fragment/page/announcement/create"; }
    @PostMapping public String create(@Valid @ModelAttribute("data") AnnouncementForm form, BindingResult errors, Model model) { if(errors.hasErrors()) { form(model,form,"/announcements",false); return "fragment/page/announcement/create"; } service.create(form.toEntity(),form.getRoleIds()); return "redirect:/announcements"; }
    @GetMapping("/{id}") public String detail(@PathVariable Long id, Model model) { Announcement item=service.findById(id); if(item==null)return "redirect:/announcements"; AnnouncementForm form=new AnnouncementForm();form.fromEntity(item);form(model,form,"/announcements/"+id,true);return "fragment/page/announcement/detail"; }
    @GetMapping("/{id}/edit") public String edit(@PathVariable Long id, Model model) { Announcement item=service.findById(id); if(item==null || item.getStatus().name().equals("WITHDRAWN")) return "redirect:/announcements"; AnnouncementForm form=new AnnouncementForm();form.fromEntity(item);form(model,form,"/announcements/"+id,false);return "fragment/page/announcement/detail"; }
    @PostMapping("/{id}") public String update(@PathVariable Long id, @Valid @ModelAttribute("data") AnnouncementForm form, BindingResult errors, Model model) { if(errors.hasErrors()) { form(model,form,"/announcements/"+id,false); return "fragment/page/announcement/detail"; } Announcement item=form.toEntity();item.setId(id);service.update(item,form.getRoleIds());return "redirect:/announcements/"+id; }
    @PostMapping("/{id}/withdraw") public String withdraw(@PathVariable Long id) { service.withdraw(id); return "redirect:/announcements"; }
    private void form(Model model, AnnouncementForm data,String action,boolean readonly) { view.form(model,data,action,"post","/announcements",readonly,"fragment/page/announcement/fields",validation.render(AnnouncementForm.class));model.addAttribute("roles",roles.findAll()); }
}
