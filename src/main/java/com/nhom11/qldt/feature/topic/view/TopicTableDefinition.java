package com.nhom11.qldt.feature.topic.view;

import com.nhom11.qldt.core.table.ActionDef;
import com.nhom11.qldt.core.table.BaseTableDefinition;
import com.nhom11.qldt.core.table.ColumnDef;
import com.nhom11.qldt.feature.topic.model.entity.Topic;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class TopicTableDefinition extends BaseTableDefinition<Topic> {

  @Override
  public List<ColumnDef> columns() {
    return List.of(
        new ColumnDef(
            "id",
            "topics.column.id",
            item -> String.valueOf(((Topic) item).getId()),
            false,
            false,
            true),
        new ColumnDef(
            "title", "topics.column.title", item -> ((Topic) item).getTitle(), true, true, true),
        new ColumnDef(
            "department",
            "topics.column.department",
            item -> ((Topic) item).getDepartment().getName(),
            false,
            false,
            false),
        new ColumnDef(
            "advisers",
            "topics.column.advisers",
            item ->
                ((Topic) item)
                    .getAdvisers()
                    .stream()
                    .map(adviser -> adviser.getFullName())
                    .collect(java.util.stream.Collectors.joining(", ")),
            false,
            false,
            false));
  }

  @Override
  public List<ActionDef> actions(String baseUrl) {
    return List.of(
        new ActionDef.Link(
            "topics.action.edit",
            "bi:pencil-square",
            item -> baseUrl + "/" + ((Topic) item).getId() + "/edit"),
        new ActionDef.Delete(
            "topics.action.delete",
            "bi:trash",
            item -> baseUrl + "/" + ((Topic) item).getId(),
            item -> ((Topic) item).getTitle()));
  }
}
