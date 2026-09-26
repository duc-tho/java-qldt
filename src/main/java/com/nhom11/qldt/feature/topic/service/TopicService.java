package com.nhom11.qldt.feature.topic.service;

import com.nhom11.qldt.core.repository.BaseRepository;
import com.nhom11.qldt.core.service.BaseCrudService;
import com.nhom11.qldt.core.table.TableQuery;
import com.nhom11.qldt.feature.topic.model.entity.Topic;
import com.nhom11.qldt.feature.topic.repository.TopicRepository;
import com.nhom11.qldt.feature.topic.view.TopicTableDefinition;
import com.nhom11.qldt.feature.registrationperiod.model.entity.RegistrationPeriod;
import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.auth.repository.UserRepository;
import com.nhom11.qldt.feature.department.model.entity.Department;
import com.nhom11.qldt.feature.department.repository.DepartmentRepository;
import java.util.Set;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TopicService extends BaseCrudService<Topic, Long> {

  private final TopicRepository topicRepository;
  private final DepartmentRepository departmentRepository;
  private final UserRepository userRepository;

  public TopicService(TopicRepository topicRepository, DepartmentRepository departmentRepository, UserRepository userRepository) {
    this.topicRepository = topicRepository;
    this.departmentRepository = departmentRepository;
    this.userRepository = userRepository;
  }

  @Override
  public BaseRepository<Topic, Long> getRepository() {
    return topicRepository;
  }

  public Topic create(Topic topic) {
    return super.create(topic);
  }

  public Topic update(Topic topic) {
    return super.update(topic);
  }

  public Page<Topic> findAllByRegistrationPeriodId(
      Long registrationPeriodId, TableQuery query, TopicTableDefinition definition) {
    String search = query.getSearch();

    return topicRepository.findAllByRegistrationPeriodId(
        registrationPeriodId,
        search == null || search.isBlank() ? null : search.trim(),
        query.filterValues(definition).get("title"),
        query.pageable(definition));
  }

  public Topic findByIdAndRegistrationPeriodId(Long id, Long registrationPeriodId) {
    return topicRepository.findByIdAndRegistrationPeriodId(id, registrationPeriodId).orElse(null);
  }

  public Topic create(Topic topic, RegistrationPeriod registrationPeriod, Long departmentId, Set<Long> adviserIds) {
    assignRelations(topic, registrationPeriod, departmentId, adviserIds);
    return create(topic);
  }

  public Topic update(Topic topic, RegistrationPeriod registrationPeriod, Long departmentId, Set<Long> adviserIds) {
    assignRelations(topic, registrationPeriod, departmentId, adviserIds);
    return update(topic);
  }

  @Transactional
  public void deleteAllByRegistrationPeriodId(List<Long> ids, Long registrationPeriodId) {
    if (ids == null || ids.isEmpty()) {
      return;
    }

    topicRepository.softDeleteAll(
        topicRepository.findAllByIdInAndRegistrationPeriodId(ids, registrationPeriodId));
  }

  @Override
  protected void beforeCreate(Topic topic) {
    validateRegistrationPeriod(topic);
  }

  @Override
  protected void beforeUpdate(Topic topic) {
    validateRegistrationPeriod(topic);
  }

  private void validateRegistrationPeriod(Topic topic) {
    if (topic.getRegistrationPeriod() == null) {
      throw new IllegalArgumentException("Topic registration period is required");
    }
  }

  private void assignRelations(Topic topic, RegistrationPeriod registrationPeriod, Long departmentId, Set<Long> adviserIds) {
    if (adviserIds == null || adviserIds.isEmpty() || adviserIds.size() > 2) {
      throw new IllegalArgumentException("Topic must have one or two advisers");
    }

    Department department = departmentRepository.findById(departmentId).orElseThrow(() -> new IllegalArgumentException("Department not found"));
    java.util.List<User> advisers = userRepository.findAllLecturersByIdIn(adviserIds);
    if (advisers.size() != adviserIds.size()) {
      throw new IllegalArgumentException("Adviser not found");
    }

    topic.setRegistrationPeriod(registrationPeriod);
    topic.setDepartment(department);
    topic.setAdvisers(Set.copyOf(advisers));
  }
}
