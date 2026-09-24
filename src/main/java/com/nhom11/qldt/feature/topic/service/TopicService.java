package com.nhom11.qldt.feature.topic.service;

import org.springframework.stereotype.Service;

import com.nhom11.qldt.core.repository.BaseRepository;
import com.nhom11.qldt.core.service.BaseCrudService;
import com.nhom11.qldt.feature.department.model.entity.Department;
import com.nhom11.qldt.feature.department.repository.DepartmentRepository;
import com.nhom11.qldt.feature.topic.model.entity.Topic;
import com.nhom11.qldt.feature.topic.repository.TopicRepository;

@Service
public class TopicService extends BaseCrudService<Topic, Long> {

    private final TopicRepository topicRepository;
    private final DepartmentRepository departmentRepository;

    public TopicService(TopicRepository topicRepository, DepartmentRepository departmentRepository) {
        this.topicRepository = topicRepository;
        this.departmentRepository = departmentRepository;
    }

    @Override
    public BaseRepository<Topic, Long> getRepository() {
        return topicRepository;
    }

    public Topic create(Topic topic, Long departmentId) {
        topic.setDepartment(findDepartment(departmentId));
        return super.create(topic);
    }

    public Topic update(Topic topic, Long departmentId) {
        topic.setDepartment(findDepartment(departmentId));
        return super.update(topic);
    }

    private Department findDepartment(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Department not found"));
    }
}
