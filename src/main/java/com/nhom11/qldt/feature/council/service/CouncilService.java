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
import com.nhom11.qldt.feature.council.event.ResultsPublishedEvent;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
/** Xử lý hội đồng, phân công phản biện, chấm điểm, chốt và công bố kết quả. */
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
  private final ApplicationEventPublisher eventPublisher;

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
      ReportSubmissionService reportSubmissionService,
      ApplicationEventPublisher eventPublisher) {
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
    this.eventPublisher = eventPublisher;
  }

  /** Lấy danh sách hội đồng của một đợt KLTN. */
  public List<Council> councils(Long periodId) {
    requireCouncilPeriod(periodRepository.findById(periodId).orElseThrow());
    return councilRepository.findAllByRegistrationPeriodId(periodId);
  }

  /** Đếm tổng số hội đồng đã tạo trong một đợt. */
  public long countCouncils(Long periodId) {
    return councilRepository.countByRegistrationPeriodId(periodId);
  }

  /** Thống kê số hội đồng theo từng đợt đăng ký được cung cấp. */
  public Map<Long, Long> councilCounts(List<Long> periodIds) {
    if (periodIds.isEmpty()) return Map.of();

    return counts(councilRepository.countByRegistrationPeriodIdIn(periodIds));
  }

  /** Lấy ID các đợt có hành động gần nhất là công bố kết quả. */
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

  /** Xác định các đợt đã đủ điều kiện để công bố kết quả. */
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

  /** Tổng hợp hội đồng cùng thành viên và số đề tài để hiển thị danh sách. */
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

  /** Lấy ID các đăng ký đã được chốt điểm cuối cùng trong một đợt. */
  public Set<Long> finalizedRegistrationIds(Long periodId) {
    return Set.copyOf(
        finalResultRepository.findRegistrationIdsByPeriodIdAndStatus(
            periodId, FinalResultStatus.FINALIZED));
  }

  /** Tìm hội đồng theo ID và bảo đảm hội đồng thuộc đúng đợt đăng ký. */
  public Council findCouncil(Long periodId, Long councilId) {
    return councilRepository.findByIdAndRegistrationPeriodId(councilId, periodId).orElse(null);
  }

  /** Lấy danh sách giảng viên cùng vai trò của họ trong hội đồng. */
  public List<CouncilMember> members(Long councilId) {
    return councilMemberRepository.findAllByCouncilId(councilId);
  }

  /** Kiểm tra đợt còn trong thời gian được phép tạo và phân công hội đồng. */
  public boolean isCouncilManagementOpen(Long periodId) {
    RegistrationPeriod period = periodRepository.findById(periodId).orElseThrow();
    requireCouncilPeriod(period);
    return isCouncilManagementOpen(period);
  }

  @Transactional
  /** Xóa hội đồng khi thời gian quản lý còn mở và chưa có đề tài được phân công. */
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
  /** Tạo hội đồng KLTN và lưu danh sách thành viên theo vai trò được chọn. */
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
  /** Cập nhật thông tin và danh sách thành viên của hội đồng chưa khóa. */
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
  /** Phân công một đề tài KLTN chưa có hội đồng vào hội đồng được chọn. */
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
  /** Gỡ đề tài khỏi hội đồng khi chưa phát sinh đánh giá hoặc kết quả cuối cùng. */
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

  /** Kiểm tra đợt còn trong thời gian được phép phân công giảng viên phản biện. */
  public boolean isReviewerAssignmentOpen(Long periodId) {
    RegistrationPeriod period = periodRepository.findById(periodId).orElseThrow();
    requireReviewerPeriod(period);
    return isReviewerAssignmentOpen(period);
  }

  /** Lấy các đề tài trong đợt cần hiển thị trên màn hình phân công phản biện. */
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
  /** Gán giảng viên phản biện cho đề tài sau khi kiểm tra xung đột vai trò. */
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
  /** Lưu bản nháp hoặc nộp phiếu đánh giá của giảng viên cho một đăng ký đề tài. */
  public void saveEvaluation(Long registrationId, User evaluator, EvaluationForm form, boolean submit) {
    TopicRegistration registration = registrationRepository.findById(registrationId).orElseThrow();

    // Chỉ người được phân công mới được chấm đăng ký đã duyệt, có báo cáo và còn hạn.
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

    // Tái sử dụng bản nháp; phiếu đã nộp bị khóa để bảo toàn kết quả đánh giá.
    Evaluation evaluation = evaluationRepository.findByTopicRegistrationIdAndEvaluatorId(registrationId, evaluator.getId()).orElseGet(Evaluation::new);
    if (evaluation.getId() != null && evaluation.getStatus() == EvaluationStatus.SUBMITTED) {
      throw new IllegalArgumentException("evaluations.error.submitted-locked");
    }

    // Điểm chỉ bắt buộc và được kiểm tra miền giá trị khi nộp chính thức.
    if (submit) {
      if (form.getScore() == null || form.getScore().compareTo(MIN_SCORE) < 0 || form.getScore().compareTo(MAX_SCORE) > 0) {
        throw new IllegalArgumentException("evaluations.validation.score.range");
      }
      evaluation.setStatus(EvaluationStatus.SUBMITTED);
      evaluation.setSubmittedAt(LocalDateTime.now());
    } else {
      evaluation.setStatus(EvaluationStatus.DRAFT);
    }

    // Các trường nội dung được lưu cho cả bản nháp lẫn bản nộp chính thức.
    evaluation.setTopicRegistration(registration);
    evaluation.setEvaluator(evaluator);
    evaluation.setScore(form.getScore());
    evaluation.setComment(form.getComment());
    evaluationRepository.save(evaluation);

    // Các loại đợt không dùng hội đồng được tự động chốt khi đủ phiếu bắt buộc.
    if (submit
        && registration.getTopic().getRegistrationPeriod().getPeriodType() != PeriodType.KLTN) {
      finalizeAutomaticResult(registration);
    }
  }

  @Transactional
  /** Chốt kết quả cuối cùng sau khi toàn bộ thành viên hội đồng đã nộp phiếu. */
  public void finalizeResult(Long registrationId, User chairperson, FinalResultForm form) {
    TopicRegistration registration = registrationRepository.findById(registrationId).orElseThrow();

    // Chỉ KLTN còn trong thời gian đánh giá mới được chốt thủ công.
    requireCouncilPeriod(registration.getTopic().getRegistrationPeriod());
    if (!isEvaluationOpen(registration)) {
      throw new IllegalArgumentException("final-results.error.deadline-passed");
    }
    // Người chốt bắt buộc là chủ tịch của đúng hội đồng được gán cho đề tài.
    Council council = registration.getTopic().getCouncil();
    if (council == null || members(council.getId()).stream().noneMatch(member -> member.getLecturer().getId().equals(chairperson.getId()) && member.getMemberRole() == CouncilMemberRole.CHAIRPERSON)) {
      throw new IllegalArgumentException("final-results.error.chairperson-required");
    }
    if (finalResultRepository.findByTopicRegistrationId(registrationId).isPresent()) {
      throw new IllegalArgumentException("final-results.error.already-finalized");
    }

    // Tập phiếu đã nộp phải khớp chính xác với tập thành viên hội đồng.
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

    // Điểm cuối cùng là trung bình cộng, làm tròn hai chữ số thập phân.
    BigDecimal score = evaluations.stream().map(evaluation -> evaluation.getScore()).reduce(BigDecimal.ZERO, (left, right) -> left.add(right))
        .divide(BigDecimal.valueOf(evaluations.size()), 2, RoundingMode.HALF_UP);

    // Ghi kết quả cùng người và thời điểm chốt để phục vụ truy vết.
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
  /** Công bố kết quả của đợt và ghi lại người thực hiện trong lịch sử công bố. */
  public void publishResults(Long periodId, User performer) {
    // Chỉ người có quyền công bố và đợt đã hoàn tất mọi kết quả mới được tiếp tục.
    periodRepository.findById(periodId).orElseThrow();
    requirePublisher(performer);
    if (isPublished(periodId)) {
      throw new IllegalArgumentException("result-publications.error.already-published");
    }
    if (!canPublishResults(periodId)) {
      throw new IllegalArgumentException("result-publications.error.results-incomplete");
    }

    // Lưu lịch sử trước rồi phát sự kiện để các listener gửi thông báo sau commit.
    publicationRepository.save(publication(periodId, performer, ResultPublicationAction.PUBLISHED, null));
    eventPublisher.publishEvent(new ResultsPublishedEvent(periodId));
  }

  @Transactional
  /** Thu hồi kết quả đã công bố và lưu lý do cùng người thực hiện vào lịch sử. */
  public void revokePublication(Long periodId, User performer, String reason) {
    // Thu hồi chỉ hợp lệ khi kết quả hiện đang được công bố và người thao tác có quyền.
    periodRepository.findById(periodId).orElseThrow();
    requirePublisher(performer);
    if (!isPublished(periodId)) {
      throw new IllegalArgumentException("result-publications.error.not-published");
    }

    if (reason == null || reason.isBlank()) throw new IllegalArgumentException("result-publications.validation.reason.required");

    // Không xóa lịch sử công bố; thêm một bản ghi thu hồi mới để giữ audit trail.
    publicationRepository.save(publication(periodId, performer, ResultPublicationAction.REVOKED, reason.trim()));
  }

  /** Lấy kết quả đã công bố của nhóm; trả về {@code null} nếu chưa có kết quả công khai. */
  public FinalResult publishedResult(Long groupId) {
    TopicRegistration registration = registrationRepository.findByStudentGroupId(groupId).orElse(null);
    if (registration == null || !publicationRepository.findFirstByRegistrationPeriodIdOrderByIdDesc(registration.getTopic().getRegistrationPeriod().getId()).map(item -> item.getAction() == ResultPublicationAction.PUBLISHED).orElse(false)) return null;
    return finalResultRepository.findByTopicRegistrationId(registration.getId()).orElse(null);
  }

  /** Lấy kết quả đã công bố của các nhóm được chọn trong cùng một đợt. */
  public List<FinalResult> publishedResults(Long periodId, List<Long> groupIds) {
    if (groupIds.isEmpty() || !isPublished(periodId)) return List.of();
    return finalResultRepository.findAllByPeriodAndGroupIds(periodId, groupIds);
  }

  /** Kiểm tra hành động công bố gần nhất cho biết kết quả của đợt đang được công khai. */
  public boolean resultsPublished(Long periodId) {
    return isPublished(periodId);
  }

  /** Lấy các đăng ký đề tài đã được phân công cho một hội đồng. */
  public List<TopicRegistration> assignedRegistrations(Long councilId) {
    Council council = councilRepository.findById(councilId).orElseThrow();
    requireCouncilPeriod(council.getRegistrationPeriod());
    return registrationRepository.findAllByRegistrationPeriodId(council.getRegistrationPeriod().getId()).stream()
        .filter(registration -> registration.getTopic().getCouncil() != null)
        .filter(registration -> registration.getTopic().getCouncil().getId().equals(councilId))
        .toList();
  }

  /** Lấy các đăng ký KLTN đủ điều kiện nhưng chưa được phân công vào hội đồng nào. */
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

  /** Lấy lịch sử công bố và thu hồi kết quả của một đợt, mới nhất xếp trước. */
  public List<ResultPublication> publicationHistory(Long periodId) {
    periodRepository.findById(periodId).orElseThrow();
    return publicationRepository.findAllByRegistrationPeriodIdOrderByIdDesc(periodId);
  }

  /** Tổng hợp trạng thái và điểm cuối cùng của từng đăng ký trong đợt. */
  public List<RegistrationResultSummary> resultSummaries(Long periodId) {
    List<TopicRegistration> registrations = registrationRepository
        .findAllByRegistrationPeriodIdAndStatus(periodId, TopicRegistrationStatus.APPROVED);
    if (registrations.isEmpty()) return List.of();

    // Nạp kết quả theo lô rồi lập chỉ mục theo đăng ký để tránh truy vấn từng nhóm.
    List<Long> groupIds = registrations.stream()
        .map(registration -> registration.getStudentGroup().getId())
        .toList();
    Map<Long, FinalResult> resultsByRegistrationId =
        finalResultRepository.findAllByPeriodAndGroupIds(periodId, groupIds).stream()
            .collect(Collectors.toMap(result -> result.getTopicRegistration().getId(), result -> result));

    // Giữ cả đăng ký chưa có kết quả để giao diện thể hiện trạng thái còn thiếu.
    return registrations.stream()
        .map(registration -> new RegistrationResultSummary(
            registration, resultsByRegistrationId.get(registration.getId())))
        .toList();
  }

  /** Kiểm tra tất cả đăng ký hợp lệ trong đợt đã có kết quả cuối cùng để được công bố. */
  public boolean canPublishResults(Long periodId) {
    List<TopicRegistration> registrations = registrationRepository
        .findAllByRegistrationPeriodIdAndStatus(periodId, TopicRegistrationStatus.APPROVED);
    return !registrations.isEmpty()
        && registrations.stream()
            .allMatch(
                registration ->
                    finalResultRepository
                        .findByTopicRegistrationId(registration.getId())
                        .map(result -> result.getStatus() == FinalResultStatus.FINALIZED)
                        .orElse(false));
  }

  /** Lấy lần công bố hoặc thu hồi kết quả gần nhất của một đợt. */
  public ResultPublication latestPublication(Long periodId) {
    return publicationRepository.findFirstByRegistrationPeriodIdOrderByIdDesc(periodId).orElse(null);
  }

  /** Tìm đăng ký trong không gian làm việc và kiểm tra người dùng có vai trò đánh giá liên quan. */
  public TopicRegistration workspaceRegistration(Long registrationId, User member) {
    TopicRegistration registration = registrationRepository.findById(registrationId).orElseThrow();
    if (!requiredEvaluatorIds(registration).contains(member.getId())) {
      throw new IllegalArgumentException("evaluations.error.not-assigned");
    }
    return registration;
  }

  /** Lấy toàn bộ phiếu đánh giá đã lưu của một đăng ký đề tài. */
  public List<Evaluation> evaluations(Long registrationId) {
    return evaluationRepository.findAllByTopicRegistrationId(registrationId);
  }

  /** Lấy kết quả cuối cùng của đăng ký; trả về {@code null} nếu chưa chốt điểm. */
  public FinalResult finalResult(Long registrationId) {
    return finalResultRepository.findByTopicRegistrationId(registrationId).orElse(null);
  }

  /** Kiểm tra giảng viên được phân công đánh giá và chưa nộp phiếu trước đó. */
  public boolean canEvaluate(Long registrationId, User evaluator) {
    TopicRegistration registration = workspaceRegistration(registrationId, evaluator);
    Evaluation existing = evaluation(registrationId, evaluator.getId());
    return reportSubmissionService.hasCurrentSubmission(registrationId)
        && (existing == null || existing.getStatus() == EvaluationStatus.DRAFT)
        && isEvaluationOpen(registration);
  }

  /** Kiểm tra tất cả thành viên bắt buộc của hội đồng đã nộp phiếu đánh giá. */
  public boolean allMemberEvaluationsSubmitted(Long registrationId) {
    TopicRegistration registration = registrationRepository.findById(registrationId).orElseThrow();
    Council council = registration.getTopic().getCouncil();
    if (council == null) return false;

    // So sánh theo tập mã để bảo đảm không thiếu hoặc thừa phiếu từ ngoài hội đồng.
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

  /** Kiểm tra người dùng là chủ tịch hội đồng và đã đủ phiếu để chốt kết quả KLTN. */
  public boolean canFinalizeResult(Long registrationId, User user) {
    TopicRegistration registration = workspaceRegistration(registrationId, user);

    // Chỉ KLTN có hội đồng, chưa chốt và còn thời gian đánh giá mới đi tiếp.
    if (registration.getTopic().getRegistrationPeriod().getPeriodType() != PeriodType.KLTN
        || registration.getTopic().getCouncil() == null
        || finalResult(registrationId) != null
        || !isEvaluationOpen(registration)) {
      return false;
    }

    // Quyền chốt thuộc chủ tịch và chỉ mở khi toàn bộ thành viên đã nộp phiếu.
    CouncilMember membership =
        membership(registration.getTopic().getCouncil().getId(), user.getId());
    return membership != null
        && membership.getMemberRole() == CouncilMemberRole.CHAIRPERSON
        && allMemberEvaluationsSubmitted(registrationId);
  }

  /** Lấy phiếu đánh giá của một giảng viên cho một đăng ký đề tài. */
  public Evaluation evaluation(Long registrationId, Long evaluatorId) {
    return evaluationRepository
        .findByTopicRegistrationIdAndEvaluatorId(registrationId, evaluatorId)
        .orElse(null);
  }

  /** Gom trạng thái phiếu đánh giá theo từng đăng ký để hiển thị danh sách công việc. */
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

  /** Tìm vai trò của một giảng viên trong hội đồng. */
  public CouncilMember membership(Long councilId, Long lecturerId) {
    return members(councilId).stream()
        .filter(member -> member.getLecturer().getId().equals(lecturerId))
        .findFirst()
        .orElse(null);
  }

  /** Lấy các đăng ký mà giảng viên tham gia với vai trò hướng dẫn, phản biện hoặc hội đồng. */
  public List<TopicRegistration> workspaceRegistrations(User member) {
    // Dùng map để hợp nhất đăng ký khi một giảng viên đồng thời giữ nhiều vai trò đánh giá.
    Map<Long, TopicRegistration> registrations = new LinkedHashMap<>();

    // Giảng viên hướng dẫn chỉ trực tiếp đánh giá các đợt môn học và nghiên cứu khoa học.
    registrationRepository
        .findAllByAdviserIdAndStatus(member.getId(), TopicRegistrationStatus.APPROVED)
        .stream()
        .filter(
            registration -> {
              PeriodType type = registration.getTopic().getRegistrationPeriod().getPeriodType();
              return type == PeriodType.MON_HOC || type == PeriodType.NCKH;
            })
        .forEach(registration -> registrations.put(registration.getId(), registration));

    // Phản biện và thành viên hội đồng được thêm từ các phân công tương ứng.
    registrationRepository
        .findAllByReviewerIdAndStatus(member.getId(), TopicRegistrationStatus.APPROVED)
        .forEach(registration -> registrations.put(registration.getId(), registration));

    registrationRepository
        .findAllByCouncilMemberId(member.getId(), TopicRegistrationStatus.APPROVED)
        .forEach(registration -> registrations.put(registration.getId(), registration));
    return List.copyOf(registrations.values());
  }

  /** Chuyển danh sách ID giảng viên thành dữ liệu gọn dùng cho ô lựa chọn. */
  public List<CouncilLecturerOption> lecturerOptions(List<Long> ids) {
    if (ids == null || ids.isEmpty()) return List.of();
    return userRepository.findAllLecturersByIdIn(Set.copyOf(ids)).stream()
        .map(user -> new CouncilLecturerOption(user.getId(), user.getFullName(), user.getUserCode()))
        .toList();
  }

  private Set<Long> requiredEvaluatorIds(TopicRegistration registration) {
    Topic topic = registration.getTopic();

    // Nhóm người bắt buộc chấm thay đổi theo loại đợt: hướng dẫn, phản biện hoặc hội đồng.
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
    // Không tính lại khi kết quả đã được tạo trước đó.
    if (finalResultRepository.findByTopicRegistrationId(registration.getId()).isPresent()) {
      return;
    }

    // Chỉ chốt khi tập người đã nộp phiếu khớp chính xác tập người bắt buộc chấm.
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

    // Kết quả tự động dùng trung bình cộng và không ghi người chốt thủ công.
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
    // Kiểm tra cấu trúc danh sách và bảo đảm chủ tịch, thư ký là hai thành viên khác nhau.
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

    // Đối chiếu cơ sở dữ liệu để loại mã không tồn tại hoặc không thuộc giảng viên.
    List<User> lecturers = userRepository.findAllLecturersByIdIn(Set.copyOf(ids));
    if (lecturers.size() != ids.size()) throw new IllegalArgumentException("councils.error.invalid-members");
    return lecturers;
  }

  private void saveMembers(Council council, CouncilForm form, List<User> lecturers) {
    // Vai trò chủ tịch và thư ký được ưu tiên; các giảng viên còn lại là thành viên.
    for (User lecturer : lecturers) {
      CouncilMember member = new CouncilMember();
      member.setCouncil(council);
      member.setLecturer(lecturer);
      member.setMemberRole(lecturer.getId().equals(form.getChairpersonId()) ? CouncilMemberRole.CHAIRPERSON : lecturer.getId().equals(form.getSecretaryId()) ? CouncilMemberRole.SECRETARY : CouncilMemberRole.MEMBER);
      councilMemberRepository.save(member);
    }
  }

  private void validateAssignedTopicAdvisers(Long councilId, List<User> lecturers) {
    // Người hướng dẫn đề tài không được tham gia hội đồng chấm chính đề tài đó.
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

  /** Kiểm tra thời điểm hiện tại nằm sau hạn đăng ký và không vượt quá ngày tổ chức hội đồng. */
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
