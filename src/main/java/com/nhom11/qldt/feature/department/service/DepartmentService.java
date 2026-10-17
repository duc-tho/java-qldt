package com.nhom11.qldt.feature.department.service;

import com.nhom11.qldt.core.repository.BaseRepository;
import com.nhom11.qldt.core.service.BaseCrudService;
import com.nhom11.qldt.feature.department.model.entity.Department;
import com.nhom11.qldt.feature.department.repository.DepartmentRepository;
import com.nhom11.qldt.feature.topic.repository.TopicRepository;
import org.springframework.stereotype.Service;

@Service
public class DepartmentService extends BaseCrudService<Department, Long> {

  private final DepartmentRepository departmentRepository;
  private final TopicRepository topicRepository;

  public DepartmentService(
      DepartmentRepository departmentRepository, TopicRepository topicRepository) {
    this.departmentRepository = departmentRepository;
    this.topicRepository = topicRepository;
  }

  @Override
  public BaseRepository<Department, Long> getRepository() {
    return departmentRepository;
  }

  public boolean existsByCode(String code) {
    return departmentRepository.existsByCode(code);
  }

  public boolean existsByCodeAndIdNot(String code, Long id) {
    return departmentRepository.existsByCodeAndIdNot(code, id);
  }

  @Override
  public void delete(Long id) {
    if (topicRepository.existsByDepartmentId(id)) {
      throw new IllegalStateException("Department has topics");
    }

    super.delete(id);
  }
}
