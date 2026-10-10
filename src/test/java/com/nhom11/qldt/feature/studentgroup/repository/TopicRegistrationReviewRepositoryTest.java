package com.nhom11.qldt.feature.studentgroup.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.department.model.entity.Department;
import com.nhom11.qldt.feature.registrationperiod.model.entity.RegistrationPeriod;
import com.nhom11.qldt.feature.registrationperiod.model.enums.PeriodType;
import com.nhom11.qldt.feature.studentgroup.model.entity.StudentGroup;
import com.nhom11.qldt.feature.studentgroup.model.entity.TopicRegistration;
import com.nhom11.qldt.feature.studentgroup.model.enums.TopicRegistrationStatus;
import com.nhom11.qldt.feature.topic.model.entity.Topic;
import com.nhom11.qldt.feature.topic.model.enums.TopicStatus;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class TopicRegistrationReviewRepositoryTest {
  @Autowired private EntityManager entityManager;
  @Autowired private TopicRegistrationRepository repository;

  @Test
  void adviserHistoryIncludesReviewedRegistrationsButExcludesDrafts() {
    User adviser = new User();
    adviser.setUsername("test-review-adviser");
    adviser.setPassword("unused");
    adviser.setEmail("test-review-adviser@example.com");
    adviser.setFullName("Test adviser");
    entityManager.persist(adviser);

    Department department = new Department();
    department.setCode("TEST_REVIEW");
    department.setName("Test review department");
    entityManager.persist(department);

    RegistrationPeriod period = new RegistrationPeriod();
    period.setName("Test review period");
    period.setPeriodType(PeriodType.MON_HOC);
    period.setCreatedBy(adviser);
    LocalDateTime now = LocalDateTime.now();
    period.setLecturerRegistrationStart(now.minusDays(4));
    period.setLecturerRegistrationEnd(now.minusDays(3));
    period.setStudentRegistrationStart(now.minusDays(2));
    period.setStudentRegistrationEnd(now.plusDays(2));
    period.setReportDeadline(now.plusDays(3));
    entityManager.persist(period);

    Topic topic = new Topic();
    topic.setRegistrationPeriod(period);
    topic.setDepartment(department);
    topic.setTitle("Test review topic");
    topic.setDescription("Test description");
    topic.setStatus(TopicStatus.APPROVED);
    topic.setAdvisers(Set.of(adviser));
    entityManager.persist(topic);

    for (TopicRegistrationStatus status : TopicRegistrationStatus.values()) {
      StudentGroup group = new StudentGroup();
      group.setRegistrationPeriod(period);
      group.setName("Group " + status);
      entityManager.persist(group);

      TopicRegistration registration = new TopicRegistration();
      registration.setStudentGroup(group);
      registration.setTopic(topic);
      registration.setStatus(status);
      entityManager.persist(registration);
    }
    entityManager.flush();
    entityManager.clear();

    assertThat(repository.findAllByAdviserIdExcludingStatus(
            adviser.getId(), TopicRegistrationStatus.DRAFT))
        .extracting(TopicRegistration::getStatus)
        .containsExactlyInAnyOrder(
            TopicRegistrationStatus.PENDING,
            TopicRegistrationStatus.APPROVED,
            TopicRegistrationStatus.REJECTED);
  }
}
