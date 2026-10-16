package com.nhom11.qldt.feature.group.service;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.auth.repository.UserRepository;
import com.nhom11.qldt.feature.period.service.RegistrationPeriodService;
import com.nhom11.qldt.feature.group.model.entity.TopicRegistration;
import com.nhom11.qldt.feature.group.model.enums.TopicRegistrationStatus;
import com.nhom11.qldt.feature.group.repository.GroupMemberRepository;
import com.nhom11.qldt.feature.group.repository.StudentGroupRepository;
import com.nhom11.qldt.feature.group.repository.TopicRegistrationRepository;
import com.nhom11.qldt.feature.topic.repository.TopicRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StudentGroupReviewHistoryTest {
  @Mock private StudentGroupRepository studentGroupRepository;
  @Mock private GroupMemberRepository groupMemberRepository;
  @Mock private TopicRegistrationRepository topicRegistrationRepository;
  @Mock private TopicRepository topicRepository;
  @Mock private UserRepository userRepository;
  @Mock private RegistrationPeriodService registrationPeriodService;
  @InjectMocks private StudentGroupService service;

  @Test
  void adviserCanOpenApprovedRegistration() {
    User adviser = new User();
    adviser.setId(7L);
    TopicRegistration registration = new TopicRegistration();
    registration.setStatus(TopicRegistrationStatus.APPROVED);
    when(topicRegistrationRepository.findByIdAndAdviserId(11L, 7L))
        .thenReturn(Optional.of(registration));

    assertSame(registration, service.adviserRegistration(11L, adviser));
  }

  @Test
  void adviserCanOpenRejectedRegistration() {
    User adviser = new User();
    adviser.setId(7L);
    TopicRegistration registration = new TopicRegistration();
    registration.setStatus(TopicRegistrationStatus.REJECTED);
    when(topicRegistrationRepository.findByIdAndAdviserId(12L, 7L))
        .thenReturn(Optional.of(registration));

    assertSame(registration, service.adviserRegistration(12L, adviser));
  }
}
