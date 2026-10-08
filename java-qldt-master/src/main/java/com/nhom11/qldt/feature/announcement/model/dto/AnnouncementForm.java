package com.nhom11.qldt.feature.announcement.model.dto;

import com.nhom11.qldt.core.form.CrudForm;
import com.nhom11.qldt.feature.announcement.model.entity.Announcement;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnnouncementForm implements CrudForm<Announcement> {
  private Long id;

  @NotBlank(message = "{announcements.validation.title.required}")
  @Size(max = 255, message = "{announcements.validation.title.max}")
  private String title;

  @NotBlank(message = "{announcements.validation.content.required}")
  private String content;

  private Set<Long> roleIds;

  public Announcement toEntity() {
    Announcement item = new Announcement();
    item.setTitle(title);
    item.setContent(content);

    return item;
  }

  public void fromEntity(Announcement item) {
    id = item.getId();
    title = item.getTitle();
    content = item.getContent();
    roleIds =
        item.getRoles().stream()
            .map(role -> role.getId())
            .collect(java.util.stream.Collectors.toSet());
  }
}
