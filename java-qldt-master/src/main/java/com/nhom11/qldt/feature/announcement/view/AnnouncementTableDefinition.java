package com.nhom11.qldt.feature.announcement.view;

import com.nhom11.qldt.core.table.ActionDef;
import com.nhom11.qldt.core.table.BaseTableDefinition;
import com.nhom11.qldt.core.table.ColumnDef;
import com.nhom11.qldt.feature.announcement.model.entity.Announcement;
import com.nhom11.qldt.feature.announcement.model.enums.AnnouncementStatus;
import java.util.List;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AnnouncementTableDefinition extends BaseTableDefinition<Announcement> {
  private final MessageSource messageSource;

  public AnnouncementTableDefinition(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  public List<ColumnDef> columns() {
    return List.of(
        new ColumnDef(
            "id",
            "announcements.column.id",
            item -> String.valueOf(((Announcement) item).getId()),
            false,
            false,
            true),
        new ColumnDef(
            "title",
            "announcements.column.title",
            item -> ((Announcement) item).getTitle(),
            true,
            true,
            true),
        new ColumnDef(
            "status",
            "announcements.column.status",
            item -> statusLabel((Announcement) item),
            false,
            false,
            true));
  }

  public List<ActionDef> actions(String baseUrl) {
    return List.of(
        new ActionDef.Link(
            "announcements.action.edit",
            "bi:pencil-square",
            item -> baseUrl + "/" + ((Announcement) item).getId() + "/edit",
            item -> ((Announcement) item).getStatus() == AnnouncementStatus.PUBLISHED),
        new ActionDef.Link(
            "announcements.action.view",
            "bi:eye",
            item -> baseUrl + "/" + ((Announcement) item).getId(),
            item -> ((Announcement) item).getStatus() == AnnouncementStatus.WITHDRAWN));
  }

  private String statusLabel(Announcement announcement) {
    return messageSource.getMessage(
        "announcements.status." + announcement.getStatus().name(),
        null,
        LocaleContextHolder.getLocale());
  }
}
