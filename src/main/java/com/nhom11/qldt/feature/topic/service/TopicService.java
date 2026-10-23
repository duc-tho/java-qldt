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
/** Xử lý tạo đề tài, đề xuất của giảng viên, phê duyệt và phân công người hướng dẫn. */
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
  /** Trả về {@code TopicRepository} để lớp base đọc và ghi dữ liệu đề tài. */
  public BaseRepository<Topic, Long> getRepository() {
    return topicRepository;
  }

  /** Tạo đề tài mới thông qua quy trình CRUD dùng chung. */
  public Topic create(Topic topic) {
    return super.create(topic);
  }

  /** Đếm tổng số đề tài thuộc một đợt đăng ký. */
  public long countByPeriod(Long periodId) {
    return topicRepository.countByRegistrationPeriodId(periodId);
  }

  /** Lưu thay đổi của đề tài thông qua quy trình CRUD dùng chung. */
  public Topic update(Topic topic) {
    return super.update(topic);
  }

  /** Lấy một trang đề tài thuộc đợt đăng ký, có tìm kiếm và sắp xếp từ bảng. */
  public Page<Topic> findAllByRegistrationPeriodId(
      Long registrationPeriodId, TableQuery query, TopicTableDefinition definition) {
    return findAllByRegistrationPeriodId(registrationPeriodId, query, definition, null, null, null);
  }

  /** Lọc danh sách đề tài theo bộ môn, trạng thái và tên giảng viên hướng dẫn. */
  public Page<Topic> findAllByRegistrationPeriodId(
      Long registrationPeriodId, TableQuery query, TopicTableDefinition definition,
      Long departmentId, TopicStatus status, String adviserSearch) {
    return findAllByRegistrationPeriodId(
        registrationPeriodId, query, definition, departmentId, status, adviserSearch, null);
  }

  /** Lọc đề tài theo bộ môn, trạng thái, giảng viên hướng dẫn và người đề xuất. */
  public Page<Topic> findAllByRegistrationPeriodId(
      Long registrationPeriodId, TableQuery query, TopicTableDefinition definition,
      Long departmentId, TopicStatus status, String adviserSearch, Long lecturerId) {
    return findAllByRegistrationPeriodId(
        registrationPeriodId, query, definition, departmentId, status, adviserSearch, lecturerId, null);
  }

  /** Thực hiện truy vấn phân trang đề tài với toàn bộ bộ lọc của màn hình quản lý. */
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

  /** Tìm đề tài theo ID và bảo đảm đề tài thuộc đúng đợt đăng ký. */
  public Topic findByIdAndRegistrationPeriodId(Long id, Long registrationPeriodId) {
    return topicRepository.findByIdAndRegistrationPeriodId(id, registrationPeriodId).orElse(null);
  }

  /** Lấy toàn bộ đề tài đã được phê duyệt trong một đợt. */
  public List<Topic> findApprovedByRegistrationPeriodId(Long registrationPeriodId) {
    return findApprovedByRegistrationPeriodId(registrationPeriodId, null, null);
  }

  /** Tìm đề tài đã duyệt theo từ khóa và bộ môn trong một đợt đăng ký. */
  public List<Topic> findApprovedByRegistrationPeriodId(
      Long registrationPeriodId, String search, Long departmentId) {
    return topicRepository.findAllByRegistrationPeriodIdAndStatus(
        registrationPeriodId, TopicStatus.APPROVED,
        search == null || search.isBlank() ? null : search.trim(), departmentId);
  }

  /** Lấy đề tài trong đợt cùng bộ môn và giảng viên hướng dẫn để hiển thị chi tiết. */
  public List<Topic> findAllWithDetailsByRegistrationPeriodId(Long registrationPeriodId) {
    return topicRepository.findAllWithDetailsByRegistrationPeriodId(registrationPeriodId);
  }

  /** Tổng hợp số lượng đề xuất đề tài theo từng đợt đăng ký. */
  public List<TopicProposalSummary> findProposalSummariesByPeriodIds(List<Long> periodIds) {
    return periodIds.isEmpty() ? List.of() : topicRepository.findProposalSummariesByPeriodIds(periodIds);
  }

  /** Đếm số đề xuất đang chờ trưởng khoa phê duyệt tại thời điểm hiện tại. */
  public long countPendingApprovals() {
    return topicRepository.countPendingApprovals(LocalDateTime.now());
  }

  /** Đếm tổng số đề tài do một giảng viên đề xuất. */
  public long countProposalsBy(Long lecturerId) {
    return topicRepository.countByProposedById(lecturerId);
  }

  /** Tổng hợp các đề xuất mà giảng viên được phép xem trong danh sách đợt. */
  public List<TopicProposalSummary> findVisibleProposalSummariesByPeriodIds(
      List<Long> periodIds, Long lecturerId) {
    return periodIds.isEmpty()
        ? List.of()
        : topicRepository.findVisibleProposalSummariesByPeriodIds(periodIds, lecturerId);
  }

  /** Tổng hợp đề xuất do chính giảng viên tạo trong các đợt được chọn. */
  public List<TopicProposalSummary> findProposalSummariesByPeriodIdsAndProposedById(
      List<Long> periodIds, Long lecturerId) {
    return periodIds.isEmpty()
        ? List.of()
        : topicRepository.findProposalSummariesByPeriodIdsAndProposedById(periodIds, lecturerId);
  }

  /** Tạo đề tài do nhân viên quản lý nhập và đặt trạng thái đã phê duyệt. */
  public Topic create(
      Topic topic, RegistrationPeriod registrationPeriod, Long departmentId, Set<Long> adviserIds) {
    // Đề tài do cán bộ tạo được duyệt mặc định sau khi quan hệ liên quan hợp lệ.
    requireStaff(registrationPeriod);
    assignRelations(topic, registrationPeriod, departmentId, adviserIds);

    return create(topic);
  }

  /** Tạo đề xuất đề tài của giảng viên ở trạng thái chờ phê duyệt. */
  public Topic createProposal(
      Topic topic,
      RegistrationPeriod registrationPeriod,
      Long departmentId,
      Set<Long> adviserIds,
      User lecturer) {
    // Giảng viên chỉ được đề xuất trong thời gian mở và phải tự tham gia hướng dẫn.
    requireProposalWindow(registrationPeriod);
    if (adviserIds == null || !adviserIds.contains(lecturer.getId())) {
      throw new IllegalArgumentException("topics.validation.proposer-adviser");
    }

    // Đề xuất luôn bắt đầu ở trạng thái chờ cán bộ phê duyệt.
    assignRelations(topic, registrationPeriod, departmentId, adviserIds);
    topic.setStatus(TopicStatus.PROPOSED);
    topic.setProposedBy(lecturer);
    return create(topic);
  }

  @Transactional
  /** Cập nhật đề xuất của chính giảng viên và đưa về trạng thái chờ duyệt lại. */
  public void updateProposal(
      Long topicId,
      Long periodId,
      User proposer,
      Topic topic,
      Long departmentId,
      Set<Long> adviserIds) {
    // Chỉ chủ đề xuất được sửa trong thời gian mở và vẫn phải là người hướng dẫn.
    Topic existing = topicRepository.findByIdAndRegistrationPeriodId(topicId, periodId).orElseThrow();
    requireProposalWindow(existing.getRegistrationPeriod());
    requireProposalOwner(existing, proposer);
    if (adviserIds == null || !adviserIds.contains(proposer.getId())) {
      throw new IllegalArgumentException("topics.validation.proposer-adviser");
    }

    // Kiểm tra xung đột đánh giá trước khi thay thế các quan hệ của đề tài.
    assignRelations(topic, existing.getRegistrationPeriod(), departmentId, adviserIds);
    validateAdviserChange(existing, adviserIds);

    // Mỗi lần sửa sẽ gửi lại đề xuất và xóa lý do từ chối cũ.
    existing.setTitle(topic.getTitle());
    existing.setDescription(topic.getDescription());
    existing.setDepartment(topic.getDepartment());
    existing.setAdvisers(topic.getAdvisers());
    existing.setStatus(TopicStatus.PROPOSED);
    existing.setRejectionReason(null);
  }

  @Transactional
  /** Xóa đề xuất khi người thao tác là người đề xuất và đề tài vẫn được phép chỉnh sửa. */
  public void deleteProposal(Long topicId, Long periodId, User proposer) {
    Topic topic = topicRepository.findByIdAndRegistrationPeriodId(topicId, periodId).orElseThrow();
    requireProposalWindow(topic.getRegistrationPeriod());
    requireProposalOwner(topic, proposer);
    topicRepository.softDelete(topic);
  }

  @Transactional
  /** Phê duyệt đề xuất đề tài đang chờ và xóa lý do từ chối cũ nếu có. */
  public void approve(Long topicId, Long registrationPeriodId) {
    Topic topic =
        topicRepository
            .findByIdAndRegistrationPeriodId(topicId, registrationPeriodId)
            .orElseThrow(() -> new IllegalArgumentException("topics.error.not-found"));
    requireLecturerRegistrationOpen(topic.getRegistrationPeriod());
    requireStaff(topic.getRegistrationPeriod());

    // Chỉ đề tài đang chờ duyệt mới được chuyển sang trạng thái phê duyệt.
    if (topic.getStatus() != TopicStatus.PROPOSED) {
      throw new IllegalArgumentException("topics.error.approval-state");
    }

    topic.setStatus(TopicStatus.APPROVED);
    topic.setRejectionReason(null);
  }

  @Transactional
  /** Từ chối đề xuất đề tài đang chờ và lưu lý do từ chối. */
  public void reject(Long topicId, Long registrationPeriodId, String reason) {
    Topic topic =
        topicRepository
            .findByIdAndRegistrationPeriodId(topicId, registrationPeriodId)
            .orElseThrow(() -> new IllegalArgumentException("topics.error.not-found"));
    requireLecturerRegistrationOpen(topic.getRegistrationPeriod());
    requireStaff(topic.getRegistrationPeriod());

    // Không cho từ chối lại đề tài đã được xử lý ở trạng thái khác.
    if (topic.getStatus() != TopicStatus.PROPOSED) {
      throw new IllegalArgumentException("topics.error.rejection-state");
    }

    topic.setStatus(TopicStatus.REJECTED);
    topic.setRejectionReason(reason.trim());
  }

  @Transactional
  /** Cập nhật đề tài từ màn hình quản lý và giữ nguyên các trường trạng thái do hệ thống quản lý. */
  public Topic update(
      Topic topic, RegistrationPeriod registrationPeriod, Long departmentId, Set<Long> adviserIds) {
    // Dùng bản đang lưu để kiểm tra quyền và bảo toàn trạng thái hệ thống quản lý.
    Topic existingTopic =
        topicRepository
            .findById(topic.getId())
            .orElseThrow(() -> new IllegalArgumentException("topics.error.not-found"));
    requireStaff(existingTopic.getRegistrationPeriod());
    validateAdviserChange(existingTopic, adviserIds);

    // Biểu mẫu chỉ được thay đổi nội dung; trạng thái, người đề xuất và phân công được giữ nguyên.
    topic.setStatus(existingTopic.getStatus());
    topic.setRejectionReason(existingTopic.getRejectionReason());
    topic.setProposedBy(existingTopic.getProposedBy());
    topic.setReviewer(existingTopic.getReviewer());
    topic.setCouncil(existingTopic.getCouncil());
    assignRelations(topic, registrationPeriod, departmentId, adviserIds);
    return update(topic);
  }

  @Transactional
  /** Xóa mềm nhiều đề tài sau khi kiểm tra tất cả đều thuộc đúng đợt đăng ký. */
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
  /** Kiểm tra đề tài đã được gắn với một đợt đăng ký hợp lệ trước khi tạo. */
  protected void beforeCreate(Topic topic) {
    validateRegistrationPeriod(topic);
  }

  @Override
  /** Kiểm tra đề tài vẫn thuộc một đợt đăng ký hợp lệ trước khi cập nhật. */
  protected void beforeUpdate(Topic topic) {
    validateRegistrationPeriod(topic);
  }

  @Override
  /** Kiểm tra các ràng buộc nghiệp vụ trước khi xóa. */
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
    // Mỗi đề tài bắt buộc có từ một đến hai giảng viên hướng dẫn.
    if (adviserIds == null || adviserIds.isEmpty() || adviserIds.size() > 2) {
      throw new IllegalArgumentException("Topic must have one or two advisers");
    }

    // Nạp lại quan hệ từ cơ sở dữ liệu để không tin trực tiếp mã gửi từ biểu mẫu.
    Department department =
        departmentRepository
            .findById(departmentId)
            .orElseThrow(() -> new IllegalArgumentException("Department not found"));
    java.util.List<User> advisers = userRepository.findAllLecturersByIdIn(adviserIds);
    if (advisers.size() != adviserIds.size()) {
      throw new IllegalArgumentException("Adviser not found");
    }

    // Đề tài do cán bộ tạo không truyền trạng thái sẽ được duyệt ngay.
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

    // Sau khi chấm điểm, danh sách hướng dẫn bị khóa để không làm sai lệch phân công.
    if (topicRepository.hasSubmittedEvaluation(existing.getId())) {
      throw new IllegalArgumentException("topics.error.advisers-grading-locked");
    }

    // Người hướng dẫn không được đồng thời là phản biện hoặc thành viên hội đồng.
    if (existing.getReviewer() != null && adviserIds.contains(existing.getReviewer().getId())) {
      throw new IllegalArgumentException("topics.error.adviser-reviewer-conflict");
    }
    if (existing.getCouncil() != null
        && topicRepository.hasCouncilMemberIn(existing.getCouncil().getId(), adviserIds)) {
      throw new IllegalArgumentException("topics.error.adviser-council-conflict");
    }
  }

  /** Kiểm tra người dùng hiện tại có quyền quản lý đề tài trong đợt hay không. */
  public boolean isTopicManagementAllowed(RegistrationPeriod registrationPeriod) {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    boolean isAdmin =
        authentication != null
            && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    return isAdmin || isProposalWindowOpen(registrationPeriod);
  }

  /** Kiểm tra thời gian giảng viên đăng ký và đề xuất đề tài đang mở. */
  public boolean isProposalWindowOpen(RegistrationPeriod registrationPeriod) {
    LocalDateTime now = LocalDateTime.now();
    return !now.isBefore(registrationPeriod.getLecturerRegistrationStart())
        && !now.isAfter(registrationPeriod.getLecturerRegistrationEnd());
  }

  /** Kiểm tra giảng viên có phải người đề xuất và đề tài vẫn được phép chỉnh sửa hay không. */
  public boolean canEditOwnProposal(Topic topic, String lecturerEmail) {
    if (topic == null || lecturerEmail == null || topic.getProposedBy() == null) {
      return false;
    }

    return topic.getProposedBy().getEmail().equals(lecturerEmail)
        && (topic.getStatus() == TopicStatus.PROPOSED || topic.getStatus() == TopicStatus.REJECTED)
        && isProposalWindowOpen(topic.getRegistrationPeriod());
  }

  /** Kiểm tra tài khoản hiện tại là nhân viên có quyền quản lý đề tài trong đợt. */
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
