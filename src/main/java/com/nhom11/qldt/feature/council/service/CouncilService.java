package com.nhom11.qldt.feature.council.service;

import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.auth.repository.UserRepository;
import com.nhom11.qldt.feature.council.model.dto.CouncilForm;
import com.nhom11.qldt.feature.council.model.dto.CouncilLecturerOption;
import com.nhom11.qldt.feature.council.model.dto.CouncilSummary;
import com.nhom11.qldt.feature.council.model.dto.EvaluationForm;
import com.nhom11.qldt.feature.council.model.dto.FinalResultForm;
import com.nhom11.qldt.feature.council.model.dto.RegistrationResultSummary;
import com.nhom11.qldt.feature.council.model.entity.Council;
import com.nhom11.qldt.feature.council.model.entity.CouncilMember;
import com.nhom11.qldt.feature.council.model.entity.Evaluation;
import com.nhom11.qldt.feature.council.model.entity.FinalResult;
import com.nhom11.qldt.feature.council.model.entity.ResultPublication;
import com.nhom11.qldt.feature.council.model.enums.CouncilMemberRole;
import com.nhom11.qldt.feature.council.model.enums.EvaluationStatus;
import com.nhom11.qldt.feature.council.model.enums.FinalResultStatus;
import com.nhom11.qldt.feature.council.model.enums.ResultPublicationAction;
import com.nhom11.qldt.feature.council.repository.CouncilMemberRepository;
import com.nhom11.qldt.feature.council.repository.CouncilRepository;
import com.nhom11.qldt.feature.council.repository.EvaluationRepository;
import com.nhom11.qldt.feature.council.repository.FinalResultRepository;
import com.nhom11.qldt.feature.council.repository.ResultPublicationRepository;
import com.nhom11.qldt.feature.report.service.ReportSubmissionService;
import com.nhom11.qldt.feature.period.model.entity.RegistrationPeriod;
import com.nhom11.qldt.feature.period.model.enums.PeriodType;
import com.nhom11.qldt.feature.period.repository.RegistrationPeriodRepository;
import com.nhom11.qldt.feature.group.model.entity.TopicRegistration;
import com.nhom11.qldt.feature.group.model.enums.TopicRegistrationStatus;
import com.nhom11.qldt.feature.group.repository.TopicRegistrationRepository;
import com.nhom11.qldt.feature.topic.model.entity.Topic;
import com.nhom11.qldt.feature.topic.repository.TopicRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CouncilService {
  private static final int MIN_MEMBERS = 3;
  private static final int MAX_MEMBERS = 5;
  private static final BigDecimal MIN_SCORE = BigDecimal.ZERO;
  private static final BigDecimal MAX_SCORE = BigDecimal.TEN;

  private final CouncilRepository councilRepository;
  private final CouncilMemberRepository councilMemberRepository;
  private final EvaluationRepository evaluationRepository;
  private final FinalResultRepository finalResultRepository;
  private final ResultPublicationRepository publicationRepository;
  private final RegistrationPeriodRepository periodRepository;
  private final TopicRepository topicRepository;
  private final TopicRegistrationRepository registrationRepository;
  private final UserRepository userRepository;
  private final ReportSubmissionService reportSubmissionService;

  public CouncilService(
      CouncilRepository councilRepository,
      CouncilMemberRepository councilMemberRepository,
      EvaluationRepository evaluationRepository,
      FinalResultRepository finalResultRepository,
      ResultPublicationRepository publicationRepository,
      RegistrationPeriodRepository periodRepository,
      TopicRepository topicRepository,
      TopicRegistrationRepository registrationRepository,
      UserRepository userRepository,
      ReportSubmissionService reportSubmissionService) {
    this.councilRepository = councilRepository;
    this.councilMemberRepository = councilMemberRepository;
    this.evaluationRepository = evaluationRepository;
    this.finalResultRepository = finalResultRepository;
    this.publicationRepository = publicationRepository;
    this.periodRepository = periodRepository;
    this.topicRepository = topicRepository;
    this.registrationRepository = registrationRepository;
    this.userRepository = userRepository;
    this.reportSubmissionService = reportSubmissionService;
  }

  public List<Council> councils(Long periodId) {
    requireCouncilPeriod(periodRepository.findById(periodId).orElseThrow());
    return councilRepository.findAllByRegistrationPeriodId(periodId);
  }

  public long countCouncils(Long periodId) {
    return councilRepository.countByRegistrationPeriodId(periodId);
  }

  public Map<Long, Long> councilCounts(List<Long> periodIds) {
    if (periodIds.isEmpty()) return Map.of();

    return counts(councilRepository.countByRegistrationPeriodIdIn(periodIds));
  }

  public Set<Long> publishedPeriodIds(List<Long> periodIds) {
    if (periodIds.isEmpty()) return Set.of();

    Map<Long, ResultPublicationAction> latestActions = new LinkedHashMap<>();
    publicationRepository.findAllByRegistrationPeriodIdInOrderByIdDesc(periodIds).forEach(
        publication -> latestActions.putIfAbsent(
            publication.getRegistrationPeriod().getId(), publication.getAction()));
    return latestActions.entrySet().stream()
        .filter(entry -> entry.getValue() == ResultPublicationAction.PUBLISHED)
        .map(Map.Entry::getKey)
        .collect(Collectors.toSet());
  }

  public Set<Long> publishablePeriodIds(List<Long> periodIds) {
    if (periodIds.isEmpty()) return Set.of();

    Map<Long, Long> approvedCounts = counts(
        registrationRepository.countByPeriodIdsAndStatus(
            periodIds, TopicRegistrationStatus.APPROVED));
    Map<Long, Long> finalizedCounts = counts(
        finalResultRepository.countByPeriodIdsAndStatus(
            periodIds, FinalResultStatus.FINALIZED));
    return periodIds.stream()
        .filter(periodId -> approvedCounts.getOrDefault(periodId, 0L) > 0)
        .filter(periodId -> approvedCounts.get(periodId).equals(
            finalizedCounts.getOrDefault(periodId, 0L)))
        .collect(Collectors.toSet());
  }

  public List<CouncilSummary> councilSummaries(Long periodId, List<Council> councils) {
    if (councils.isEmpty()) return List.of();

    var councilIds = councils.stream().map(council -> council.getId()).toList();
    Map<Long, List<CouncilMember>> membersByCouncil = councilMemberRepository.findAllByCouncilIdIn(councilIds)
        .stream().collect(Collectors.groupingBy(member -> member.getCouncil().getId()));
    Map<Long, Long> assignedCounts = topicRepository.countByCouncilInPeriod(periodId).stream()
        .collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));
    Map<Long, Long> finalizedCounts = counts(
        finalResultRepository.countByCouncilIdsAndPeriodIdAndStatus(
            councilIds, periodId, FinalResultStatus.FINALIZED));

    return councils.stream().map(council -> {
      var members = membersByCouncil.getOrDefault(council.getId(), List.of());
      String chairpersonName = members.stream()
          .filter(member -> member.getMemberRole() == CouncilMemberRole.CHAIRPERSON)
          .map(member -> member.getLecturer().getFullName()).findFirst().orElse("");
      return new CouncilSummary(council, chairpersonName, members.size(),
          assignedCounts.getOrDefault(council.getId(), 0L),
          finalizedCounts.getOrDefault(council.getId(), 0L));
    }).toList();
  }

  public Set<Long> finalizedRegistrationIds(Long periodId) {
    return Set.copyOf(
        finalResultRepository.findRegistrationIdsByPeriodIdAndStatus(
            periodId, FinalResultStatus.FINALIZED));
  }

  public Council findCouncil(Long periodId, Long councilId) {
    return councilRepository.findByIdAndRegistrationPeriodId(councilId, periodId).orElse(null);
  }

  public List<CouncilMember> members(Long councilId) {
    return councilMemberRepository.findAllByCouncilId(councilId);
  }

  public boolean isCouncilManagementOpen(Long periodId) {
    RegistrationPeriod period = periodRepository.findById(periodId).orElseThrow();
    requireCouncilPeriod(period);
    return isCouncilManagementOpen(period);
  }

  @Transactional
  public void deleteCouncil(Long periodId, Long councilId) {
    Council council = requireCouncil(periodId, councilId);
    requireCouncilPeriod(council.getRegistrationPeriod());
    requireCouncilManagementWindow(council.getRegistrationPeriod());
    if (topicRepository.existsByCouncilId(councilId)) {
      throw new IllegalArgumentException("councils.error.delete-assigned");
    }
    councilMemberRepository.deleteAll(members(councilId));
    councilRepository.softDelete(council);
  }

  @Transactional
  public Council createCouncil(Long periodId, User creator, CouncilForm form) {
    RegistrationPeriod period = periodRepository.findById(periodId).orElseThrow();
    requireCouncilPeriod(period);
    requireCouncilManagementWindow(period);
    List<User> lecturers = validatedLecturers(form);
    Council council = new Council();
    council.setRegistrationPeriod(period);
    council.setCreatedBy(creator);
    council.setName(form.getName());
    council = councilRepository.save(council);
    saveMembers(council, form, lecturers);
    return council;
  }

  @Transactional
  public void updateCouncil(Long periodId, Long councilId, CouncilForm form) {
    Council council = requireCouncil(periodId, councilId);
    requireCouncilPeriod(council.getRegistrationPeriod());
    requireCouncilManagementWindow(council.getRegistrationPeriod());
    if (evaluationRepository.existsByTopicRegistrationTopicCouncilId(councilId)) {
      throw new IllegalArgumentException("councils.error.members-locked");
    }
    List<User> lecturers = validatedLecturers(form);
    validateAssignedTopicAdvisers(councilId, lecturers);
    council.setName(form.getName());
    councilRepository.save(council);
    councilMemberRepository.deleteAll(members(councilId));
    councilMemberRepository.flush();
    saveMembers(council, form, lecturers);
  }

  @Transactional
  public void assignTopic(Long periodId, Long councilId, Long topicId) {
    Council council = requireCouncil(periodId, councilId);
    Topic topic = topicRepository.findById(topicId).orElseThrow();
    requireCouncilPeriod(council.getRegistrationPeriod());
    requireCouncilManagementWindow(council.getRegistrationPeriod());
    if (!topic.getRegistrationPeriod().getId().equals(council.getRegistrationPeriod().getId())) {
      throw new IllegalArgumentException("councils.error.topic-period");
    }
    TopicRegistration registration =
        registrationRepository.findAllByRegistrationPeriodId(council.getRegistrationPeriod().getId()).stream()
            .filter(item -> item.getTopic().getId().equals(topicId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("councils.error.topic-registration-required"));
    if (registration.getStatus() != TopicRegistrationStatus.APPROVED) {
      throw new IllegalArgumentException("councils.error.topic-registration-required");
    }
    if (topic.getCouncil() != null && !topic.getCouncil().getId().equals(councilId)) {
      throw new IllegalArgumentException("councils.error.topic-already-assigned");
    }
    Set<Long> adviserIds = topic.getAdvisers().stream().map(adviser -> adviser.getId()).collect(java.util.stream.Collectors.toSet());
    if (members(councilId).stream().anyMatch(member -> adviserIds.contains(member.getLecturer().getId()))) {
      throw new IllegalArgumentException("councils.error.adviser-member-conflict");
    }
    topic.setCouncil(council);
    topicRepository.save(topic);
  }

  @Transactional
  public void unassignTopic(Long periodId, Long councilId, Long topicId) {
    Council council = requireCouncil(periodId, councilId);
    requireCouncilPeriod(council.getRegistrationPeriod());
    requireCouncilManagementWindow(council.getRegistrationPeriod());

    Topic topic =
        topicRepository
            .findByIdAndRegistrationPeriodId(topicId, periodId)
            .orElseThrow(() -> new IllegalArgumentException("councils.error.topic-period"));
    if (topic.getCouncil() == null || !topic.getCouncil().getId().equals(councilId)) {
      throw new IllegalArgumentException("councils.error.topic-not-assigned");
    }

    TopicRegistration registration =
        registrationRepository.findAllByRegistrationPeriodId(periodId).stream()
            .filter(item -> item.getTopic().getId().equals(topicId))
            .findFirst()
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "councils.error.topic-registration-required"));
    if (evaluationRepository.existsByTopicRegistrationId(registration.getId())) {
      throw new IllegalArgumentException("councils.error.assignment-locked");
    }

    topic.setCouncil(null);
    topicRepository.save(topic);
  }

  public boolean isReviewerAssignmentOpen(Long periodId) {
    RegistrationPeriod period = periodRepository.findById(periodId).orElseThrow();
    requireReviewerPeriod(period);
    return isReviewerAssignmentOpen(period);
  }

  public List<Topic> reviewerAssignmentTopics(Long periodId) {
    RegistrationPeriod period = periodRepository.findById(periodId).orElseThrow();
    requireReviewerPeriod(period);
    Set<Long> approvedTopicIds =
        registrationRepository.findAllByRegistrationPeriodId(periodId).stream()
            .filter(registration -> registration.getStatus() == TopicRegistrationStatus.APPROVED)
            .map(registration -> registration.getTopic().getId())
            .collect(java.util.stream.Collectors.toSet());
    return topicRepository.findAllWithDetailsByRegistrationPeriodId(periodId).stream()
        .filter(topic -> approvedTopicIds.contains(topic.getId()))
        .toList();
  }

  @Transactional
  public void assignReviewer(Long periodId, Long topicId, Long reviewerId) {
    RegistrationPeriod period = periodRepository.findById(periodId).orElseThrow();
    requireReviewerPeriod(period);
    requireReviewerAssignmentWindow(period);

    Topic topic =
        topicRepository
            .findByIdAndRegistrationPeriodId(topicId, periodId)
            .orElseThrow(() -> new IllegalArgumentException("topics.error.not-found"));
    boolean hasApprovedRegistration =
        registrationRepository.findAllByRegistrationPeriodId(periodId).stream()
            .anyMatch(
                registration ->
                    registration.getTopic().getId().equals(topicId)
                        && registration.getStatus() == TopicRegistrationStatus.APPROVED);
    if (!hasApprovedRegistration) {
      throw new IllegalArgumentException("reviewer-assignments.error.registration-required");
    }

    User reviewer =
        userRepository.findAllLecturersByIdIn(Set.of(reviewerId)).stream()
            .findFirst()
            .orElseThrow(
                () -> new IllegalArgumentException("reviewer-assignments.error.invalid-reviewer"));
    if (topic.getAdvisers().stream()
        .anyMatch(adviser -> adviser.getId().equals(reviewer.getId()))) {
      throw new IllegalArgumentException("reviewer-assignments.error.adviser-conflict");
    }

    boolean gradingStarted =
        registrationRepository.findAllByRegistrationPeriodId(periodId).stream()
            .filter(registration -> registration.getTopic().getId().equals(topicId))
            .anyMatch(
                registration ->
                    evaluationRepository.existsByTopicRegistrationId(registration.getId()));
    if (gradingStarted) {
      throw new IllegalArgumentException("reviewer-assignments.error.locked");
    }

    topic.setReviewer(reviewer);
    topicRepository.save(topic);
  }

  @Transactional
  public void saveEvaluation(Long registrationId, User evaluator, EvaluationForm form, boolean submit) {
    TopicRegistration registration = registrationRepository.findById(registrationId).orElseThrow();
    if (registration.getStatus() != TopicRegistrationStatus.APPROVED) {
      throw new IllegalArgumentException("evaluations.error.registration-required");
    }
    if (!requiredEvaluatorIds(registration).contains(evaluator.getId())) {
      throw new IllegalArgumentException("evaluations.error.not-assigned");
    }
    if (!reportSubmissionService.hasCurrentSubmission(registrationId)) {
      throw new IllegalArgumentException("evaluations.error.report-required");
    }
    if (!isEvaluationOpen(registration)) {
      throw new IllegalArgumentException("evaluations.error.deadline-passed");
    }
    Evaluation evaluation = evaluationRepository.findByTopicRegistrationIdAndEvaluatorId(registrationId, evaluator.getId()).orElseGet(Evaluation::new);
    if (evaluation.getId() != null && evaluation.getStatus() == EvaluationStatus.SUBMITTED) {
      throw new IllegalArgumentException("evaluations.error.submitted-locked");
    }
    if (submit) {
      if (form.getScore() == null || form.getScore().compareTo(MIN_SCORE) < 0 || form.getScore().compareTo(MAX_SCORE) > 0) {
        throw new IllegalArgumentException("evaluations.validation.score.range");
      }
      evaluation.setStatus(EvaluationStatus.SUBMITTED);
      evaluation.setSubmittedAt(LocalDateTime.now());
    } else {
      evaluation.setStatus(EvaluationStatus.DRAFT);
    }
    evaluation.setTopicRegistration(registration);
    evaluation.setEvaluator(evaluator);
    evaluation.setScore(form.getScore());
    evaluation.setComment(form.getComment());
    evaluationRepository.save(evaluation);

    if (submit
        && registration.getTopic().getRegistrationPeriod().getPeriodType() != PeriodType.KLTN) {
      finalizeAutomaticResult(registration);
    }
  }

  @Transactional
  public void finalizeResult(Long registrationId, User chairperson, FinalResultForm form) {
    TopicRegistration registration = registrationRepository.findById(registrationId).orElseThrow();
    requireCouncilPeriod(registration.getTopic().getRegistrationPeriod());
    if (!isEvaluationOpen(registration)) {
      throw new IllegalArgumentException("final-results.error.deadline-passed");
    }
    Council council = registration.getTopic().getCouncil();
    if (council == null || members(council.getId()).stream().noneMatch(member -> member.getLecturer().getId().equals(chairperson.getId()) && member.getMemberRole() == CouncilMemberRole.CHAIRPERSON)) {
      throw new IllegalArgumentException("final-results.error.chairperson-required");
    }
    if (finalResultRepository.findByTopicRegistrationId(registrationId).isPresent()) {
      throw new IllegalArgumentException("final-results.error.already-finalized");
    }
    List<Evaluation> evaluations = evaluationRepository.findAllByTopicRegistrationIdAndStatus(registrationId, EvaluationStatus.SUBMITTED);
    Set<Long> memberIds =
        members(council.getId()).stream()
            .map(member -> member.getLecturer().getId())
            .collect(java.util.stream.Collectors.toSet());
    if (evaluations.size() != memberIds.size()
        || evaluations.stream()
            .map(evaluation -> evaluation.getEvaluator().getId())
            .anyMatch(evaluatorId -> !memberIds.contains(evaluatorId))) {
      throw new IllegalArgumentException("final-results.error.evaluations-incomplete");
    }
    BigDecimal score = evaluations.stream().map(evaluation -> evaluation.getScore()).reduce(BigDecimal.ZERO, (left, right) -> left.add(right))
        .divide(BigDecimal.valueOf(evaluations.size()), 2, RoundingMode.HALF_UP);
    FinalResult result = finalResultRepository.findByTopicRegistrationId(registrationId).orElseGet(FinalResult::new);
    result.setTopicRegistration(registration);
    result.setFinalScore(score);
    result.setSummaryComment(form.getSummaryComment());
    result.setStatus(FinalResultStatus.FINALIZED);
    result.setFinalizedBy(chairperson);
    result.setFinalizedAt(LocalDateTime.now());
    finalResultRepository.save(result);
  }

  @Transactional
  public void publishResults(Long periodId, User performer) {
    periodRepository.findById(periodId).orElseThrow();
    requirePublisher(performer);
    if (isPublished(periodId)) {
      throw new IllegalArgumentException("result-publications.error.already-published");
    }
    if (!canPublishResults(periodId)) {
      throw new IllegalArgumentException("result-publications.error.results-incomplete");
    }
    publicationRepository.save(publication(periodId, performer, ResultPublicationAction.PUBLISHED, null));
  }

  @Transactional
  public void revokePublication(Long periodId, User performer, String reason) {
    periodRepository.findById(periodId).orElseThrow();
    requirePublisher(performer);
    if (!isPublished(periodId)) {
      throw new IllegalArgumentException("result-publications.error.not-published");
    }
    if (reason == null || reason.isBlank()) throw new IllegalArgumentException("result-publications.validation.reason.required");
    publicationRepository.save(publication(periodId, performer, ResultPublicationAction.REVOKED, reason.trim()));
  }

  public FinalResult publishedResult(Long groupId) {
    TopicRegistration registration = registrationRepository.findByStudentGroupId(groupId).orElse(null);
    if (registration == null || !publicationRepository.findFirstByRegistrationPeriodIdOrderByIdDesc(registration.getTopic().getRegistrationPeriod().getId()).map(item -> item.getAction() == ResultPublicationAction.PUBLISHED).orElse(false)) return null;
    return finalResultRepository.findByTopicRegistrationId(registration.getId()).orElse(null);
  }

  public List<FinalResult> publishedResults(Long periodId, List<Long> groupIds) {
    if (groupIds.isEmpty() || !isPublished(periodId)) return List.of();
    return finalResultRepository.findAllByPeriodAndGroupIds(periodId, groupIds);
  }

  public boolean resultsPublished(Long periodId) {
    return isPublished(periodId);
  }

  public List<TopicRegistration> assignedRegistrations(Long councilId) {
    Council council = councilRepository.findById(councilId).orElseThrow();
    requireCouncilPeriod(council.getRegistrationPeriod());
    return registrationRepository.findAllByRegistrationPeriodId(council.getRegistrationPeriod().getId()).stream()
        .filter(registration -> registration.getTopic().getCouncil() != null)
        .filter(registration -> registration.getTopic().getCouncil().getId().equals(councilId))
        .toList();
  }

  public List<TopicRegistration> eligibleUnassignedRegistrations(Long councilId) {
    Council council = councilRepository.findById(councilId).orElseThrow();
    requireCouncilPeriod(council.getRegistrationPeriod());
    if (!isCouncilManagementOpen(council.getRegistrationPeriod())) return List.of();
    Set<Long> adviserIds = members(councilId).stream().map(member -> member.getLecturer().getId()).collect(java.util.stream.Collectors.toSet());
    return registrationRepository.findAllByRegistrationPeriodId(council.getRegistrationPeriod().getId()).stream()
        .filter(registration -> registration.getStatus() == TopicRegistrationStatus.APPROVED)
        .filter(registration -> registration.getTopic().getCouncil() == null)
        .filter(registration -> registration.getTopic().getAdvisers().stream().noneMatch(adviser -> adviserIds.contains(adviser.getId())))
        .toList();
  }

  public List<ResultPublication> publicationHistory(Long periodId) {
    periodRepository.findById(periodId).orElseThrow();
    return publicationRepository.findAllByRegistrationPeriodIdOrderByIdDesc(periodId);
  }

  public List<RegistrationResultSummary> resultSummaries(Long periodId) {
    List<TopicRegistration> registrations =
        registrationRepository.findAllByRegistrationPeriodId(periodId).stream()
            .filter(registration -> registration.getStatus() == TopicRegistrationStatus.APPROVED)
            .toList();
    if (registrations.isEmpty()) return List.of();

    List<Long> groupIds = registrations.stream()
        .map(registration -> registration.getStudentGroup().getId())
        .toList();
    Map<Long, FinalResult> resultsByRegistrationId =
        finalResultRepository.findAllByPeriodAndGroupIds(periodId, groupIds).stream()
            .collect(Collectors.toMap(result -> result.getTopicRegistration().getId(), result -> result));

    return registrations.stream()
        .map(registration -> new RegistrationResultSummary(
            registration, resultsByRegistrationId.get(registration.getId())))
        .toList();
  }

  public boolean canPublishResults(Long periodId) {
    List<TopicRegistration> registrations =
        registrationRepository.findAllByRegistrationPeriodId(periodId).stream()
            .filter(registration -> registration.getStatus() == TopicRegistrationStatus.APPROVED)
            .toList();
    return !registrations.isEmpty()
        && registrations.stream()
            .allMatch(
                registration ->
                    finalResultRepository
                        .findByTopicRegistrationId(registration.getId())
                        .map(result -> result.getStatus() == FinalResultStatus.FINALIZED)
                        .orElse(false));
  }

  public ResultPublication latestPublication(Long periodId) {
    return publicationRepository.findFirstByRegistrationPeriodIdOrderByIdDesc(periodId).orElse(null);
  }

  public TopicRegistration workspaceRegistration(Long registrationId, User member) {
    TopicRegistration registration = registrationRepository.findById(registrationId).orElseThrow();
    if (!requiredEvaluatorIds(registration).contains(member.getId())) {
      throw new IllegalArgumentException("evaluations.error.not-assigned");
    }
    return registration;
  }

  public List<Evaluation> evaluations(Long registrationId) {
    return evaluationRepository.findAllByTopicRegistrationId(registrationId);
  }

  public FinalResult finalResult(Long registrationId) {
    return finalResultRepository.findByTopicRegistrationId(registrationId).orElse(null);
  }

  public boolean canEvaluate(Long registrationId, User evaluator) {
    TopicRegistration registration = workspaceRegistration(registrationId, evaluator);
    Evaluation existing = evaluation(registrationId, evaluator.getId());
    return reportSubmissionService.hasCurrentSubmission(registrationId)
        && (existing == null || existing.getStatus() == EvaluationStatus.DRAFT)
        && isEvaluationOpen(registration);
  }

  public boolean allMemberEvaluationsSubmitted(Long registrationId) {
    TopicRegistration registration = registrationRepository.findById(registrationId).orElseThrow();
    Council council = registration.getTopic().getCouncil();
    if (council == null) return false;
    Set<Long> memberIds =
        members(council.getId()).stream()
            .map(member -> member.getLecturer().getId())
            .collect(java.util.stream.Collectors.toSet());
    Set<Long> submittedEvaluatorIds =
        evaluationRepository
            .findAllByTopicRegistrationIdAndStatus(registrationId, EvaluationStatus.SUBMITTED)
            .stream()
            .map(evaluation -> evaluation.getEvaluator().getId())
            .collect(java.util.stream.Collectors.toSet());
    return !memberIds.isEmpty() && submittedEvaluatorIds.equals(memberIds);
  }

  public boolean canFinalizeResult(Long registrationId, User user) {
    TopicRegistration registration = workspaceRegistration(registrationId, user);
    if (registration.getTopic().getRegistrationPeriod().getPeriodType() != PeriodType.KLTN
        || registration.getTopic().getCouncil() == null
        || finalResult(registrationId) != null
        || !isEvaluationOpen(registration)) {
      return false;
    }
    CouncilMember membership =
        membership(registration.getTopic().getCouncil().getId(), user.getId());
    return membership != null
        && membership.getMemberRole() == CouncilMemberRole.CHAIRPERSON
        && allMemberEvaluationsSubmitted(registrationId);
  }

  public Evaluation evaluation(Long registrationId, Long evaluatorId) {
    return evaluationRepository
        .findByTopicRegistrationIdAndEvaluatorId(registrationId, evaluatorId)
        .orElse(null);
  }

  public Map<Long, EvaluationStatus> evaluationStatuses(
      List<Long> registrationIds, Long evaluatorId) {
    if (registrationIds.isEmpty()) return Map.of();

    return evaluationRepository
        .findAllByTopicRegistrationIdInAndEvaluatorId(registrationIds, evaluatorId)
        .stream()
        .collect(Collectors.toMap(
            evaluation -> evaluation.getTopicRegistration().getId(),
            Evaluation::getStatus));
  }

  public CouncilMember membership(Long councilId, Long lecturerId) {
    return members(councilId).stream()
        .filter(member -> member.getLecturer().getId().equals(lecturerId))
        .findFirst()
        .orElse(null);
  }

  public List<TopicRegistration> workspaceRegistrations(User member) {
    Map<Long, TopicRegistration> registrations = new LinkedHashMap<>();
    registrationRepository
        .findAllByAdviserIdAndStatus(member.getId(), TopicRegistrationStatus.APPROVED)
        .stream()
        .filter(
            registration -> {
              PeriodType type = registration.getTopic().getRegistrationPeriod().getPeriodType();
              return type == PeriodType.MON_HOC || type == PeriodType.NCKH;
            })
        .forEach(registration -> registrations.put(registration.getId(), registration));
    registrationRepository
        .findAllByReviewerIdAndStatus(member.getId(), TopicRegistrationStatus.APPROVED)
        .forEach(registration -> registrations.put(registration.getId(), registration));
    registrationRepository
        .findAllByCouncilMemberId(member.getId(), TopicRegistrationStatus.APPROVED)
        .forEach(registration -> registrations.put(registration.getId(), registration));
    return List.copyOf(registrations.values());
  }

  public List<CouncilLecturerOption> lecturerOptions(List<Long> ids) {
    if (ids == null || ids.isEmpty()) return List.of();
    return userRepository.findAllLecturersByIdIn(Set.copyOf(ids)).stream()
        .map(user -> new CouncilLecturerOption(user.getId(), user.getFullName(), user.getUserCode()))
        .toList();
  }

  private Set<Long> requiredEvaluatorIds(TopicRegistration registration) {
    Topic topic = registration.getTopic();
    return switch (topic.getRegistrationPeriod().getPeriodType()) {
      case MON_HOC, NCKH ->
          topic.getAdvisers().stream()
              .map(member -> member.getId())
              .collect(java.util.stream.Collectors.toSet());
      case TLCN -> topic.getReviewer() == null ? Set.of() : Set.of(topic.getReviewer().getId());
      case KLTN -> {
        if (topic.getCouncil() == null) {
          yield Set.of();
        }
        yield members(topic.getCouncil().getId()).stream()
            .map(member -> member.getLecturer().getId())
            .collect(java.util.stream.Collectors.toSet());
      }
    };
  }

  private boolean isEvaluationOpen(TopicRegistration registration) {
    PeriodType type = registration.getTopic().getRegistrationPeriod().getPeriodType();
    if (type == PeriodType.MON_HOC || type == PeriodType.NCKH) {
      return true;
    }
    LocalDateTime deadline = registration.getTopic().getRegistrationPeriod().getReviewerDeadline();
    return deadline != null && LocalDateTime.now().isBefore(deadline);
  }

  private void finalizeAutomaticResult(TopicRegistration registration) {
    if (finalResultRepository.findByTopicRegistrationId(registration.getId()).isPresent()) {
      return;
    }

    Set<Long> requiredEvaluatorIds = requiredEvaluatorIds(registration);
    List<Evaluation> evaluations =
        evaluationRepository.findAllByTopicRegistrationIdAndStatus(
            registration.getId(), EvaluationStatus.SUBMITTED);
    Set<Long> submittedEvaluatorIds =
        evaluations.stream()
            .map(evaluation -> evaluation.getEvaluator().getId())
            .collect(java.util.stream.Collectors.toSet());
    if (requiredEvaluatorIds.isEmpty() || !submittedEvaluatorIds.equals(requiredEvaluatorIds)) {
      return;
    }

    BigDecimal score =
        evaluations.stream()
            .map(evaluation -> evaluation.getScore())
            .reduce(BigDecimal.ZERO, (left, right) -> left.add(right))
            .divide(BigDecimal.valueOf(evaluations.size()), 2, RoundingMode.HALF_UP);
    FinalResult result = new FinalResult();
    result.setTopicRegistration(registration);
    result.setFinalScore(score);
    result.setStatus(FinalResultStatus.FINALIZED);
    result.setFinalizedAt(LocalDateTime.now());
    finalResultRepository.save(result);
  }

  private void requireReviewerAssignmentWindow(RegistrationPeriod period) {
    if (!isReviewerAssignmentOpen(period)) {
      throw new IllegalArgumentException("reviewer-assignments.error.management-window");
    }
  }

  private boolean isReviewerAssignmentOpen(RegistrationPeriod period) {
    LocalDateTime registrationEnd = period.getStudentRegistrationEnd();
    LocalDateTime reviewerDeadline = period.getReviewerDeadline();
    LocalDateTime now = LocalDateTime.now();
    return registrationEnd != null
        && reviewerDeadline != null
        && now.isAfter(registrationEnd)
        && now.isBefore(reviewerDeadline);
  }

  private List<User> validatedLecturers(CouncilForm form) {
    List<Long> ids = form.getMemberIds();
    if (ids == null
        || form.getChairpersonId() == null
        || form.getSecretaryId() == null
        || ids.size() < MIN_MEMBERS
        || ids.size() > MAX_MEMBERS
        || ids.stream().distinct().count() != ids.size()
        || !ids.contains(form.getChairpersonId())
        || !ids.contains(form.getSecretaryId())
        || form.getChairpersonId().equals(form.getSecretaryId())) {
      throw new IllegalArgumentException("councils.error.invalid-members");
    }
    List<User> lecturers = userRepository.findAllLecturersByIdIn(Set.copyOf(ids));
    if (lecturers.size() != ids.size()) throw new IllegalArgumentException("councils.error.invalid-members");
    return lecturers;
  }

  private void saveMembers(Council council, CouncilForm form, List<User> lecturers) {
    for (User lecturer : lecturers) {
      CouncilMember member = new CouncilMember();
      member.setCouncil(council);
      member.setLecturer(lecturer);
      member.setMemberRole(lecturer.getId().equals(form.getChairpersonId()) ? CouncilMemberRole.CHAIRPERSON : lecturer.getId().equals(form.getSecretaryId()) ? CouncilMemberRole.SECRETARY : CouncilMemberRole.MEMBER);
      councilMemberRepository.save(member);
    }
  }

  private void validateAssignedTopicAdvisers(Long councilId, List<User> lecturers) {
    Set<Long> lecturerIds = lecturers.stream().map(lecturer -> lecturer.getId()).collect(java.util.stream.Collectors.toSet());
    boolean hasConflict =
        assignedRegistrations(councilId).stream()
            .flatMap(registration -> registration.getTopic().getAdvisers().stream())
            .anyMatch(adviser -> lecturerIds.contains(adviser.getId()));
    if (hasConflict) {
      throw new IllegalArgumentException("councils.error.adviser-member-conflict");
    }
  }

  private Council requireCouncil(Long periodId, Long councilId) {
    return councilRepository
        .findByIdAndRegistrationPeriodId(councilId, periodId)
        .orElseThrow(() -> new IllegalArgumentException("councils.error.not-found"));
  }

  private void requireCouncilManagementWindow(RegistrationPeriod period) {
    if (!isCouncilManagementOpen(period)) {
      throw new IllegalArgumentException("councils.error.management-window");
    }
  }

  public boolean isCouncilManagementOpen(RegistrationPeriod period) {
    LocalDateTime registrationEnd = period.getStudentRegistrationEnd();
    LocalDateTime councilDate = period.getCouncilDate();
    LocalDateTime now = LocalDateTime.now();
    return registrationEnd != null
        && councilDate != null
        && now.isAfter(registrationEnd)
        && now.isBefore(councilDate);
  }

  private boolean isPublished(Long periodId) {
    ResultPublication latest = latestPublication(periodId);
    return latest != null && latest.getAction() == ResultPublicationAction.PUBLISHED;
  }

  private Map<Long, Long> counts(List<Object[]> rows) {
    return rows.stream().collect(Collectors.toMap(
        row -> (Long) row[0], row -> (Long) row[1]));
  }

  private ResultPublication publication(Long periodId, User performer, ResultPublicationAction action, String reason) {
    ResultPublication publication = new ResultPublication();
    publication.setRegistrationPeriod(periodRepository.findById(periodId).orElseThrow());
    publication.setAction(action);
    publication.setReason(reason);
    publication.setPerformedBy(performer);
    publication.setPerformedAt(LocalDateTime.now());
    return publication;
  }

  private void requireCouncilPeriod(RegistrationPeriod period) {
    if (period.getPeriodType() != PeriodType.KLTN) throw new IllegalArgumentException("councils.error.period-type");
  }

  private void requireReviewerPeriod(RegistrationPeriod period) {
    if (period.getPeriodType() != PeriodType.TLCN) {
      throw new IllegalArgumentException("reviewer-assignments.error.period-type");
    }
  }

  private void requirePublisher(User user) {
    boolean publisher = user.getRoles().stream().map(role -> role.getName()).anyMatch(role -> role.equals("ADMIN") || role.equals("HEAD_OF_FACULTY"));
    if (!publisher) throw new IllegalArgumentException("result-publications.error.publisher-required");
  }
}
