package com.nhom11.qldt.feature.group.service;

import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.group.model.entity.GroupActivity;
import com.nhom11.qldt.feature.group.model.entity.StudentGroup;
import com.nhom11.qldt.feature.group.model.enums.GroupActivityType;
import com.nhom11.qldt.feature.group.repository.GroupActivityRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class GroupActivityService {
  private final GroupActivityRepository repository;

  public GroupActivityService(GroupActivityRepository repository) {
    this.repository = repository;
  }

  public void record(
      StudentGroup group, User actor, User subject, GroupActivityType type, String detail) {
    GroupActivity activity = new GroupActivity();
    activity.setStudentGroup(group);
    activity.setActor(actor);
    activity.setSubject(subject);
    activity.setType(type);
    activity.setDetail(detail);
    activity.setOccurredAt(LocalDateTime.now());
    repository.save(activity);
  }

  public List<GroupActivity> history(Long groupId) {
    return repository.findAllByStudentGroupIdOrderByOccurredAtDescIdDesc(groupId);
  }
}
