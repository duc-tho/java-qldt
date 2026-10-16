package com.nhom11.qldt.feature.topic.service;

import com.nhom11.qldt.core.repository.BaseRepository;
import com.nhom11.qldt.core.service.BaseCrudService;
import com.nhom11.qldt.core.table.TableQuery;
import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.auth.repository.UserRepository;
import com.nhom11.qldt.feature.department.model.entity.Department;
import com.nhom11.qldt.feature.department.repository.DepartmentRepository;
import com.nhom11.qldt.feature.period.model.entity.RegistrationPeriod;
import com.nhom11.qldt.feature.period.service.RegistrationPeriodService;
import com.nhom11.qldt.feature.topic.model.entity.Topic;
import com.nhom11.qldt.feature.topic.model.dto.TopicProposalSummary;
import com.nhom11.qldt.feature.topic.model.enums.TopicStatus;
import com.nhom11.qldt.feature.topic.repository.TopicRepository;
import com.nhom11.qldt.feature.topic.view.TopicTableDefinition;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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

  public long countByPeriod(Long periodId) {
    return topicRepository.countByRegistrationPeriodId(periodId);
  }

  public Topic update(Topic topic) {
    return super.update(topic);
  }

  public Page<Topic> findAllByRegistrationPeriodId(
      Long registrationPeriodId, TableQuery query, TopicTableDefinition definition) {
    return findAllByRegistrationPeriodId(registrationPeriodId, query, definition, null, null, null);
  }

  public Page<Topic> findAllByRegistrationPeriodId(
      Long registrationPeriodId, TableQuery query, TopicTableDefinition definition,
      Long departmentId, TopicStatus status, String adviserSearch) {
    return findAllByRegistrationPeriodId(
        registrationPeriodId, query, definition, departmentId, status, adviserSearch, null);
  }

  public Page<Topic> findAllByRegistrationPeriodId(
      Long registrationPeriodId, TableQuery query, TopicTableDefinition definition,
      Long departmentId, TopicStatus status, String adviserSearch, Long lecturerId) {
    return findAllByRegistrationPeriodId(
        registrationPeriodId, query, definition, departmentId, status, adviserSearch, lecturerId, null);
  }

  public Page<Topic> findAllByRegistrationPeriodId(
      Long registrationPeriodId, TableQuery query, TopicTableDefinition definition,
      Long departmentId, TopicStatus status, String adviserSearch, Long lecturerId,
      Long proposedById) {
    String search = query.getSearch();
    var filters = query.filterValues(definition);
    Page<Topic> page = topicRepository.findAllByRegistrationPeriodId(
        registrationPeriodId,
        search == null || search.isBlank() ? null : search.trim(),
        filters.get("title"),
        departmentId,
        status,
        lecturerId,
        proposedById,
        adviserSearch == null || adviserSearch.isBlank() ? null : adviserSearch.trim(),
        query.pageable(definition));
    if (page.isEmpty()) return page;
    var ids = page.getContent().stream().map(topic -> topic.getId()).toList();
    var details = topicRepository.findAllWithAdvisersByIdIn(ids).stream()
        .collect(java.util.stream.Collectors.toMap(topic -> topic.getId(), topic -> topic));
    return new PageImpl<>(ids.stream().map(details::get).toList(), page.getPageable(), page.getTotalElements());
  }

  public Topic findByIdAndRegistrationPeriodId(Long id, Long registrationPeriodId) {
    return topicRepository.findByIdAndRegistrationPeriodId(id, registrationPeriodId).orElse(null);
  }

  public List<Topic> findApprovedByRegistrationPeriodId(Long registrationPeriodId) {
    return findApprovedByRegistrationPeriodId(registrationPeriodId, null, null);
  }

  public List<Topic> findApprovedByRegistrationPeriodId(
      Long registrationPeriodId, String search, Long departmentId) {
    return topicRepository.findAllByRegistrationPeriodIdAndStatus(
        registrationPeriodId, TopicStatus.APPROVED,
        search == null || search.isBlank() ? null : search.trim(), departmentId);
  }

  public List<Topic> findAllWithDetailsByRegistrationPeriodId(Long registrationPeriodId) {
    return topicRepository.findAllWithDetailsByRegistrationPeriodId(registrationPeriodId);
  }

  public List<TopicProposalSummary> findProposalSummariesByPeriodIds(List<Long> periodIds) {
    return periodIds.isEmpty() ? List.of() : topicRepository.findProposalSummariesByPeriodIds(periodIds);
  }

  public List<TopicProposalSummary> findVisibleProposalSummariesByPeriodIds(
      List<Long> periodIds, Long lecturerId) {
    return periodIds.isEmpty()
        ? List.of()
        : topicRepository.findVisibleProposalSummariesByPeriodIds(periodIds, lecturerId);
  }

  public Topic create(
      Topic topic, RegistrationPeriod registrationPeriod, Long departmentId, Set<Long> adviserIds) {
    requireStaff(registrationPeriod);
    assignRelations(topic, registrationPeriod, departmentId, adviserIds);
    return create(topic);
  }

  public Topic createProposal(
      Topic topic,
      RegistrationPeriod registrationPeriod,
      Long departmentId,
      Set<Long> adviserIds,
      User lecturer) {
    requireProposalWindow(registrationPeriod);
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
    requireProposalWindow(existing.getRegistrationPeriod());
    requireProposalOwner(existing, proposer);
    if (adviserIds == null || !adviserIds.contains(proposer.getId())) {
      throw new IllegalArgumentException("topics.validation.proposer-adviser");
    }
    assignRelations(topic, existing.getRegistrationPeriod(), departmentId, adviserIds);
    validateAdviserChange(existing, adviserIds);
    existing.setTitle(topic.getTitle());
    existing.setDescription(topic.getDescription());
    existing.setDepartment(topic.getDepartment());
    existing.setAdvisers(topic.getAdvisers());
    existing.setStatus(TopicStatus.PROPOSED);
    existing.setRejectionReason(null);
  }

  @Transactional
  public void deleteProposal(Long topicId, Long periodId, User proposer) {
    Topic topic = topicRepository.findByIdAndRegistrationPeriodId(topicId, periodId).orElseThrow();
    requireProposalWindow(topic.getRegistrationPeriod());
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
    requireStaff(topic.getRegistrationPeriod());
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
    requireStaff(topic.getRegistrationPeriod());
    if (topic.getStatus() != TopicStatus.PROPOSED) {
      throw new IllegalArgumentException("topics.error.rejection-state");
    }
    topic.setStatus(TopicStatus.REJECTED);
    topic.setRejectionReason(reason.trim());
  }

  @Transactional
  public Topic update(
      Topic topic, RegistrationPeriod registrationPeriod, Long departmentId, Set<Long> adviserIds) {
    Topic existingTopic =
        topicRepository
            .findById(topic.getId())
            .orElseThrow(() -> new IllegalArgumentException("topics.error.not-found"));
    requireStaff(existingTopic.getRegistrationPeriod());
    validateAdviserChange(existingTopic, adviserIds);
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
    requireStaff(period);

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
    requireStaff(topic.getRegistrationPeriod());
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

  private void validateAdviserChange(Topic existing, Set<Long> adviserIds) {
    Set<Long> currentIds = existing.getAdvisers().stream().map(adviser -> adviser.getId())
        .collect(java.util.stream.Collectors.toSet());
    if (currentIds.equals(adviserIds)) return;
    if (topicRepository.hasSubmittedEvaluation(existing.getId())) {
      throw new IllegalArgumentException("topics.error.advisers-grading-locked");
    }
    if (existing.getReviewer() != null && adviserIds.contains(existing.getReviewer().getId())) {
      throw new IllegalArgumentException("topics.error.adviser-reviewer-conflict");
    }
    if (existing.getCouncil() != null
        && topicRepository.hasCouncilMemberIn(existing.getCouncil().getId(), adviserIds)) {
      throw new IllegalArgumentException("topics.error.adviser-council-conflict");
    }
  }

  public boolean isTopicManagementAllowed(RegistrationPeriod registrationPeriod) {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    boolean isAdmin =
        authentication != null
            && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    return isAdmin || isProposalWindowOpen(registrationPeriod);
  }

  public boolean isProposalWindowOpen(RegistrationPeriod registrationPeriod) {
    LocalDateTime now = LocalDateTime.now();
    return !now.isBefore(registrationPeriod.getLecturerRegistrationStart())
        && !now.isAfter(registrationPeriod.getLecturerRegistrationEnd());
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

  private void requireProposalWindow(RegistrationPeriod registrationPeriod) {
    if (!isProposalWindowOpen(registrationPeriod)) {
      throw new IllegalArgumentException(REGISTRATION_STAGE_CLOSED);
    }
  }

  private void requireStaff(RegistrationPeriod period) {
    if (!canManageTopicsAsStaff(period)) {
      throw new IllegalArgumentException("topics.error.staff-required");
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
