package com.nhom11.qldt.feature.topic.service;

import com.nhom11.qldt.core.repository.BaseRepository;
import com.nhom11.qldt.core.service.BaseCrudService;
import com.nhom11.qldt.core.table.TableQuery;
import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.auth.repository.UserRepository;
import com.nhom11.qldt.feature.department.model.entity.Department;
import com.nhom11.qldt.feature.department.repository.DepartmentRepository;
import com.nhom11.qldt.feature.registrationperiod.model.entity.RegistrationPeriod;
import com.nhom11.qldt.feature.registrationperiod.model.enums.RegistrationPeriodStage;
import com.nhom11.qldt.feature.registrationperiod.service.RegistrationPeriodService;
import com.nhom11.qldt.feature.topic.model.entity.Topic;
import com.nhom11.qldt.feature.topic.model.enums.TopicStatus;
import com.nhom11.qldt.feature.topic.repository.TopicRepository;
import com.nhom11.qldt.feature.topic.view.TopicTableDefinition;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TopicService extends BaseCrudService<Topic, Long> {
  public static final String REGISTRATION_STAGE_CLOSED = "topics.error.lecturer-registration-closed";

  private final TopicRepository topicRepository;
  private final DepartmentRepository departmentRepository;
  private final UserRepository userRepository;
  private final RegistrationPeriodService registrationPeriodService;

  public TopicService(
      TopicRepository topicRepository,
      DepartmentRepository departmentRepository,
      UserRepository userRepository,
      RegistrationPeriodService registrationPeriodService) {
    this.topicRepository = topicRepository;
    this.departmentRepository = departmentRepository;
    this.userRepository = userRepository;
    this.registrationPeriodService = registrationPeriodService;
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

  public List<Topic> findApprovedByRegistrationPeriodId(Long registrationPeriodId) {
    return topicRepository.findAllByRegistrationPeriodIdAndStatus(
        registrationPeriodId, TopicStatus.APPROVED, null, null);
  }

  public List<Topic> findAllWithDetailsByRegistrationPeriodId(Long registrationPeriodId) {
    return topicRepository.findAllWithDetailsByRegistrationPeriodId(registrationPeriodId);
  }

  public Topic create(
      Topic topic, RegistrationPeriod registrationPeriod, Long departmentId, Set<Long> adviserIds) {
    assignRelations(topic, registrationPeriod, departmentId, adviserIds);
    return create(topic);
  }

  public Topic createProposal(
      Topic topic,
      RegistrationPeriod registrationPeriod,
      Long departmentId,
      Set<Long> adviserIds,
      User lecturer) {
    requireLecturerRegistrationOpen(registrationPeriod);
    if (adviserIds == null || !adviserIds.contains(lecturer.getId())) {
      throw new IllegalArgumentException("topics.validation.proposer-adviser");
    }
    assignRelations(topic, registrationPeriod, departmentId, adviserIds);
    topic.setStatus(TopicStatus.PROPOSED);
    topic.setProposedBy(lecturer);
    return create(topic);
  }

  @Transactional
  public void updateProposal(
      Long topicId,
      Long periodId,
      User proposer,
      Topic topic,
      Long departmentId,
      Set<Long> adviserIds) {
    Topic existing = topicRepository.findByIdAndRegistrationPeriodId(topicId, periodId).orElseThrow();
    requireLecturerRegistrationOpen(existing.getRegistrationPeriod());
    requireProposalOwner(existing, proposer);
    assignRelations(topic, existing.getRegistrationPeriod(), departmentId, adviserIds);
    topic.setId(existing.getId());
    topic.setStatus(TopicStatus.PROPOSED);
    topic.setProposedBy(existing.getProposedBy());
    topic.setRejectionReason(null);
    topicRepository.save(topic);
  }

  @Transactional
  public void deleteProposal(Long topicId, Long periodId, User proposer) {
    Topic topic = topicRepository.findByIdAndRegistrationPeriodId(topicId, periodId).orElseThrow();
    requireLecturerRegistrationOpen(topic.getRegistrationPeriod());
    requireProposalOwner(topic, proposer);
    topicRepository.softDelete(topic);
  }

  @Transactional
  public void approve(Long topicId, Long registrationPeriodId) {
    Topic topic =
        topicRepository
            .findByIdAndRegistrationPeriodId(topicId, registrationPeriodId)
            .orElseThrow(() -> new IllegalArgumentException("topics.error.not-found"));
    requireLecturerRegistrationOpen(topic.getRegistrationPeriod());
    if (topic.getStatus() != TopicStatus.PROPOSED) {
      throw new IllegalArgumentException("topics.error.approval-state");
    }
    topic.setStatus(TopicStatus.APPROVED);
    topic.setRejectionReason(null);
  }

  @Transactional
  public void reject(Long topicId, Long registrationPeriodId, String reason) {
    Topic topic =
        topicRepository
            .findByIdAndRegistrationPeriodId(topicId, registrationPeriodId)
            .orElseThrow(() -> new IllegalArgumentException("topics.error.not-found"));
    requireLecturerRegistrationOpen(topic.getRegistrationPeriod());
    if (topic.getStatus() != TopicStatus.PROPOSED) {
      throw new IllegalArgumentException("topics.error.rejection-state");
    }
    topic.setStatus(TopicStatus.REJECTED);
    topic.setRejectionReason(reason.trim());
  }

  public Topic update(
      Topic topic, RegistrationPeriod registrationPeriod, Long departmentId, Set<Long> adviserIds) {
    Topic existingTopic =
        topicRepository
            .findById(topic.getId())
            .orElseThrow(() -> new IllegalArgumentException("topics.error.not-found"));
    topic.setStatus(existingTopic.getStatus());
    topic.setRejectionReason(existingTopic.getRejectionReason());
    topic.setProposedBy(existingTopic.getProposedBy());
    topic.setReviewer(existingTopic.getReviewer());
    topic.setCouncil(existingTopic.getCouncil());
    assignRelations(topic, registrationPeriod, departmentId, adviserIds);
    return update(topic);
  }

  @Transactional
  public void deleteAllByRegistrationPeriodId(List<Long> ids, Long registrationPeriodId) {
    if (ids == null || ids.isEmpty()) {
      return;
    }

    RegistrationPeriod period = registrationPeriodService.findById(registrationPeriodId);
    if (period == null) {
      throw new IllegalArgumentException("topics.error.not-found");
    }
    requireLecturerRegistrationOpen(period);

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

  @Override
  protected void beforeDelete(Topic topic) {
    requireLecturerRegistrationOpen(topic.getRegistrationPeriod());
  }

  private void validateRegistrationPeriod(Topic topic) {
    if (topic.getRegistrationPeriod() == null) {
      throw new IllegalArgumentException("Topic registration period is required");
    }
    requireLecturerRegistrationOpen(topic.getRegistrationPeriod());
  }

  private void assignRelations(
      Topic topic, RegistrationPeriod registrationPeriod, Long departmentId, Set<Long> adviserIds) {
    if (adviserIds == null || adviserIds.isEmpty() || adviserIds.size() > 2) {
      throw new IllegalArgumentException("Topic must have one or two advisers");
    }

    Department department =
        departmentRepository
            .findById(departmentId)
            .orElseThrow(() -> new IllegalArgumentException("Department not found"));
    java.util.List<User> advisers = userRepository.findAllLecturersByIdIn(adviserIds);
    if (advisers.size() != adviserIds.size()) {
      throw new IllegalArgumentException("Adviser not found");
    }

    topic.setRegistrationPeriod(registrationPeriod);
    topic.setDepartment(department);
    topic.setAdvisers(Set.copyOf(advisers));
    if (topic.getStatus() == null) {
      topic.setStatus(TopicStatus.APPROVED);
    }
  }

  public boolean isTopicManagementAllowed(RegistrationPeriod registrationPeriod) {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    boolean isAdmin =
        authentication != null
            && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    return isAdmin
        || registrationPeriodService.stage(registrationPeriod, LocalDateTime.now())
            == RegistrationPeriodStage.TOPIC_PROPOSAL;
  }

  public boolean canManageTopicsAsStaff(RegistrationPeriod registrationPeriod) {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    return authentication != null
        && authentication.getAuthorities().stream()
            .anyMatch(
                authority ->
                    authority.getAuthority().equals("ROLE_ADMIN")
                        || authority.getAuthority().equals("ROLE_HEAD_OF_FACULTY"))
        && isTopicManagementAllowed(registrationPeriod);
  }

  private void requireLecturerRegistrationOpen(RegistrationPeriod registrationPeriod) {
    if (!isTopicManagementAllowed(registrationPeriod)) {
      throw new IllegalArgumentException(REGISTRATION_STAGE_CLOSED);
    }
  }

  private void requireProposalOwner(Topic topic, User user) {
    if ((topic.getStatus() != TopicStatus.PROPOSED && topic.getStatus() != TopicStatus.REJECTED)
        || topic.getProposedBy() == null
        || !topic.getProposedBy().getId().equals(user.getId())) {
      throw new IllegalArgumentException("topics.error.proposal-owner-required");
    }
  }
}
