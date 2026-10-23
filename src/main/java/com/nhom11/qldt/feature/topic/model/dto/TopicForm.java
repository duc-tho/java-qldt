package com.nhom11.qldt.feature.topic.model.dto;

import com.nhom11.qldt.core.form.CrudForm;
import com.nhom11.qldt.feature.topic.model.entity.Topic;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/** Nhận thông tin đề tài, bộ môn và danh sách giảng viên hướng dẫn từ form. */
public class TopicForm implements CrudForm<Topic> {

  private Long id;

  @NotBlank(message = "{topics.validation.title.required}")
  @Size(max = 255, message = "{topics.validation.title.max}")
  private String title;

  @NotBlank(message = "{topics.validation.description.required}")
  private String description;

  @NotNull(message = "{topics.validation.department.required}")
  private Long departmentId;

  @NotEmpty(message = "{topics.validation.advisers.required}")
  @Size(max = 2, message = "{topics.validation.advisers.max}")
  private Set<Long> adviserIds = new LinkedHashSet<>();

  @Override
  /** Chuyển dữ liệu biểu mẫu thành thực thể để tầng dịch vụ xử lý. */
  public Topic toEntity() {
    Topic topic = new Topic();
    topic.setTitle(title);
    topic.setDescription(description);
    return topic;
  }

  @Override
  /** Điền dữ liệu thực thể vào biểu mẫu để hiển thị hoặc chỉnh sửa. */
  public void fromEntity(Topic entity) {
    id = entity.getId();
    title = entity.getTitle();
    description = entity.getDescription();
    departmentId = entity.getDepartment().getId();
    adviserIds =
        entity.getAdvisers().stream()
            .map(adviser -> adviser.getId())
            .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
  }
}
