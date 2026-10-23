package com.nhom11.qldt.feature.council.service;

import com.nhom11.qldt.core.service.EmailService;
import com.nhom11.qldt.feature.council.event.ResultsPublishedEvent;
import com.nhom11.qldt.feature.group.model.entity.GroupMember;
import com.nhom11.qldt.feature.group.model.entity.StudentGroup;
import com.nhom11.qldt.feature.group.model.entity.TopicRegistration;
import com.nhom11.qldt.feature.group.model.enums.TopicRegistrationStatus;
import com.nhom11.qldt.feature.group.repository.GroupMemberRepository;
import com.nhom11.qldt.feature.group.repository.TopicRegistrationRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
/** Lắng nghe sự kiện nghiệp vụ và gửi email liên quan đến hội đồng, đánh giá và công bố kết quả. */
public class ResultPublicationEmailListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(ResultPublicationEmailListener.class);

  private final TopicRegistrationRepository registrationRepository;
  private final GroupMemberRepository groupMemberRepository;
  private final EmailService emailService;

  public ResultPublicationEmailListener(
      TopicRegistrationRepository registrationRepository,
      GroupMemberRepository groupMemberRepository,
      EmailService emailService) {
    this.registrationRepository = registrationRepository;
    this.groupMemberRepository = groupMemberRepository;
    this.emailService = emailService;
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  /** Gửi thông báo kết quả đến sinh viên sau khi sự kiện công bố được phát ra. */
  public void notifyStudents(ResultsPublishedEvent event) {
    List<TopicRegistration> registrations = registrationRepository
        .findAllByRegistrationPeriodIdAndStatus(
            event.periodId(), TopicRegistrationStatus.APPROVED);
    if (registrations.isEmpty()) return;

    Map<Long, TopicRegistration> registrationsByGroup = new LinkedHashMap<>();
    registrations.forEach(registration ->
        registrationsByGroup.put(registration.getStudentGroup().getId(), registration));

    Map<Long, List<GroupMember>> membersByGroup = groupMemberRepository
        .findAllByStudentGroupIdIn(List.copyOf(registrationsByGroup.keySet()))
        .stream()
        .collect(Collectors.groupingBy(
            member -> member.getStudentGroup().getId(), LinkedHashMap::new, Collectors.toList()));

    registrationsByGroup.forEach((groupId, registration) ->
        sendGroupNotification(registration, membersByGroup.getOrDefault(groupId, List.of())));
  }

  private void sendGroupNotification(
      TopicRegistration registration, List<GroupMember> members) {
    StudentGroup group = registration.getStudentGroup();
    List<String> recipients = members.stream()
        .map(member -> member.getStudent().getEmail())
        .distinct()
        .toList();
    if (recipients.isEmpty()) return;

    String subject = "Kết quả đề tài của nhóm " + group.getName() + " đã được công bố";
    String content = """
        Xin chào,

        Kết quả đề tài "%s" của nhóm "%s" trong đợt "%s" đã được công bố.
        Vui lòng đăng nhập hệ thống để xem chi tiết điểm.
        """.formatted(
            registration.getTopic().getTitle(),
            group.getName(),
            group.getRegistrationPeriod().getName());

    try {
      emailService.send(recipients, subject, content);
    } catch (MailException exception) {
      LOGGER.error("Failed to send published result email for group {}", group.getId(), exception);
    }
  }
}
