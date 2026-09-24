package com.nhom11.qldt.feature.topic.model.dto;

import com.nhom11.qldt.core.form.CrudForm;
import com.nhom11.qldt.feature.topic.model.entity.Topic;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TopicForm implements CrudForm<Topic> {

    private Long id;

    @NotBlank(message = "{topics.validation.title.required}")
    @Size(max = 255, message = "{topics.validation.title.max}")
    private String title;

    @NotBlank(message = "{topics.validation.description.required}")
    private String description;

    @NotNull(message = "{topics.validation.department.required}")
    private Long departmentId;

    @Override
    public Topic toEntity() {
        Topic topic = new Topic();
        topic.setId(id);
        topic.setTitle(title);
        topic.setDescription(description);
        return topic;
    }

    @Override
    public void fromEntity(Topic entity) {
        id = entity.getId();
        title = entity.getTitle();
        description = entity.getDescription();
        departmentId = entity.getDepartment().getId();
    }
}
