package com.nhom11.qldt.feature.topic.view;

import com.nhom11.qldt.core.table.ActionDef;
import com.nhom11.qldt.core.table.BaseTableDefinition;
import com.nhom11.qldt.core.table.ColumnDef;
import com.nhom11.qldt.feature.topic.model.entity.Topic;
import com.nhom11.qldt.feature.topic.model.enums.TopicStatus;
import com.nhom11.qldt.feature.topic.service.TopicService;
import java.util.List;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class TopicTableDefinition extends BaseTableDefinition<Topic> {
  private final MessageSource messageSource;
  private final TopicService topicService;

  public TopicTableDefinition(MessageSource messageSource, TopicService topicService) {
    this.messageSource = messageSource;
    this.topicService = topicService;
  }

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
                    .getAdvisers().stream()
                        .map(adviser -> adviser.getFullName())
                        .collect(java.util.stream.Collectors.joining(", ")),
            false,
            false,
            false),
        new ColumnDef(
            "status",
            "topics.column.status",
            item -> statusLabel((Topic) item),
            false,
            false,
            false));
  }

  @Override
  public List<ActionDef> actions(String baseUrl) {
    return List.of(
        new ActionDef.Post(
            "topics.action.approve",
            "bi:check-circle",
            item -> topicUrl((Topic) item) + "/duyet",
            item -> canReview((Topic) item)),
        new ActionDef.Reject(
            "topics.action.reject",
            "bi:x-circle",
            item -> topicUrl((Topic) item) + "/tu-choi",
            item -> canReview((Topic) item)),
        new ActionDef.Link(
            "crud.action.edit",
            "bi:pencil-square",
            item -> topicUrl((Topic) item) + "/sua",
            item -> canManageTopic((Topic) item) || canEditOwnProposal((Topic) item)),
        new ActionDef.Delete(
            "crud.action.delete",
            "bi:trash",
            item -> topicUrl((Topic) item) + "/xoa",
            item -> ((Topic) item).getTitle(),
            item -> canManageTopic((Topic) item) || canEditOwnProposal((Topic) item)));
  }

  private String statusLabel(Topic topic) {
    return messageSource.getMessage(
        "topics.status." + topic.getStatus().name(), null, LocaleContextHolder.getLocale());
  }

  private boolean canReview(Topic topic) {
    return topic.getStatus() == TopicStatus.PROPOSED
        && canManageTopic(topic);
  }

  private boolean canManageTopic(Topic topic) {
    return topicService.canManageTopicsAsStaff(topic.getRegistrationPeriod());
  }

  private boolean canEditOwnProposal(Topic topic) {
    return topicService.canEditOwnProposal(
        topic, SecurityContextHolder.getContext().getAuthentication().getName());
  }

  @Override
  public String createUrl(String baseUrl) {
    return baseUrl + "/tao";
  }

  @Override
  public String bulkDeleteUrl(String baseUrl) {
    return baseUrl + "/xoa";
  }

  private String topicUrl(Topic topic) {
    return "/dot/" + topic.getRegistrationPeriod().getId()
        + "/de-tai/" + topic.getId();
  }
}
