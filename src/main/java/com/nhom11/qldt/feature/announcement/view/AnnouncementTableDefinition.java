package com.nhom11.qldt.feature.announcement.view;
import java.util.List;
import org.springframework.stereotype.Component;
import com.nhom11.qldt.core.table.ActionDef;
import com.nhom11.qldt.core.table.BaseTableDefinition;
import com.nhom11.qldt.core.table.ColumnDef;
import com.nhom11.qldt.feature.announcement.model.entity.Announcement;
@Component public class AnnouncementTableDefinition extends BaseTableDefinition<Announcement> {
 public List<ColumnDef> columns() { return List.of(new ColumnDef("id", "announcements.column.id", item -> String.valueOf(((Announcement) item).getId()), false, false, true), new ColumnDef("title", "announcements.column.title", item -> ((Announcement) item).getTitle(), true, true, true), new ColumnDef("status", "announcements.column.status", item -> ((Announcement) item).getStatus().name(), false, true, true)); }
 public List<ActionDef> actions(String baseUrl) { return List.of(new ActionDef.Link("announcements.action.edit", "bi:pencil-square", item -> baseUrl + "/" + ((Announcement)item).getId() + "/edit")); }
}
