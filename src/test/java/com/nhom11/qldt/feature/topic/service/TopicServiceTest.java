package com.nhom11.qldt.feature.topic.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.nhom11.qldt.feature.period.model.entity.RegistrationPeriod;
import com.nhom11.qldt.feature.period.service.RegistrationPeriodService;
import com.nhom11.qldt.feature.topic.model.entity.Topic;
import com.nhom11.qldt.feature.topic.model.enums.TopicStatus;
import com.nhom11.qldt.feature.topic.repository.TopicRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class TopicServiceTest {
  @Mock private TopicRepository topicRepository;
  @Mock private RegistrationPeriodService registrationPeriodService;

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void adminCanApproveTopicAfterProposalStage() {
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken("admin", "", "ROLE_ADMIN"));
    Topic topic = proposedTopic();
    when(topicRepository.findByIdAndRegistrationPeriodId(1L, 2L)).thenReturn(Optional.of(topic));
    TopicService service = new TopicService(topicRepository, null, null, registrationPeriodService);

    service.approve(1L, 2L);

    assertThat(topic.getStatus()).isEqualTo(TopicStatus.APPROVED);
    assertThat(service.canManageTopicsAsStaff(topic.getRegistrationPeriod())).isTrue();
  }

  @Test
  void headOfFacultyCannotApproveTopicAfterProposalStage() {
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken("head", "", "ROLE_HEAD_OF_FACULTY"));
    Topic topic = proposedTopic();
    when(topicRepository.findByIdAndRegistrationPeriodId(1L, 2L)).thenReturn(Optional.of(topic));
    TopicService service = new TopicService(topicRepository, null, null, registrationPeriodService);

    assertThatThrownBy(() -> service.approve(1L, 2L))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(TopicService.REGISTRATION_STAGE_CLOSED);
    assertThat(service.canManageTopicsAsStaff(topic.getRegistrationPeriod())).isFalse();
  }

  private Topic proposedTopic() {
    RegistrationPeriod period = new RegistrationPeriod();
    period.setId(2L);
    period.setLecturerRegistrationStart(LocalDateTime.now().minusDays(3));
    period.setLecturerRegistrationEnd(LocalDateTime.now().minusDays(1));
    Topic topic = new Topic();
    topic.setId(1L);
    topic.setRegistrationPeriod(period);
    topic.setStatus(TopicStatus.PROPOSED);
    return topic;
  }
}
