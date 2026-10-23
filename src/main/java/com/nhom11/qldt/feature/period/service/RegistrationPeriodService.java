package com.nhom11.qldt.feature.period.service;

import com.nhom11.qldt.core.repository.BaseRepository;
import com.nhom11.qldt.core.service.BaseCrudService;
import com.nhom11.qldt.core.table.BaseTableDefinition;
import com.nhom11.qldt.core.table.TableQuery;
import com.nhom11.qldt.feature.council.model.enums.ResultPublicationAction;
import com.nhom11.qldt.feature.council.repository.ResultPublicationRepository;
import com.nhom11.qldt.feature.period.model.entity.RegistrationPeriod;
import com.nhom11.qldt.feature.period.model.enums.PeriodType;
import com.nhom11.qldt.feature.period.model.enums.RegistrationPeriodValidationError;
import com.nhom11.qldt.feature.period.model.enums.RegistrationPeriodStage;
import com.nhom11.qldt.feature.period.repository.RegistrationPeriodRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
/** Kiểm tra mốc thời gian, xác định giai đoạn và lọc các đợt người dùng được xem. */
public class RegistrationPeriodService extends BaseCrudService<RegistrationPeriod, Long> {
  private final RegistrationPeriodRepository repository;
  private final ResultPublicationRepository publicationRepository;

  public RegistrationPeriodService(
      RegistrationPeriodRepository repository, ResultPublicationRepository publicationRepository) {
    this.repository = repository;
    this.publicationRepository = publicationRepository;
  }

  @Override
  /** Trả về repository để lớp base đọc và ghi dữ liệu đợt đăng ký. */
  public BaseRepository<RegistrationPeriod, Long> getRepository() {
    return repository;
  }

  @Override
  /** Kiểm tra thứ tự các mốc thời gian trước khi tạo đợt đăng ký. */
  protected void beforeCreate(RegistrationPeriod period) {
    validate(period);
  }

  @Override
  /** Kiểm tra lại thứ tự các mốc thời gian trước khi cập nhật đợt đăng ký. */
  protected void beforeUpdate(RegistrationPeriod period) {
    validate(period);
  }

  private void validate(RegistrationPeriod period) {
    if (!validationErrors(period).isEmpty()) {
      throw new IllegalArgumentException("Invalid registration period");
    }
  }

  /** Kiểm tra thứ tự các mốc thời gian và trả về toàn bộ lỗi của đợt đăng ký. */
  public List<RegistrationPeriodValidationError> validationErrors(RegistrationPeriod period) {
    List<RegistrationPeriodValidationError> errors = new ArrayList<>();

    // Các giai đoạn phải nối tiếp nhau theo đúng thứ tự thời gian.
    if (!period.getLecturerRegistrationStart().isBefore(period.getLecturerRegistrationEnd())
        || !period.getLecturerRegistrationEnd().isBefore(period.getStudentRegistrationStart())
        || !period.getStudentRegistrationStart().isBefore(period.getStudentRegistrationEnd())
        || !period.getStudentRegistrationEnd().isBefore(period.getReportDeadline())) {
      errors.add(RegistrationPeriodValidationError.TIMELINE);
    }

    // Chỉ TLCN và KLTN cần hạn phản biện, đồng thời hạn này không được trước hạn báo cáo.
    boolean reviewerRequired =
        period.getPeriodType() == PeriodType.TLCN || period.getPeriodType() == PeriodType.KLTN;

    if (reviewerRequired != (period.getReviewerDeadline() != null)
        || reviewerRequired
            && period.getReviewerDeadline() != null
            && period.getReportDeadline().isAfter(period.getReviewerDeadline())) {
      errors.add(RegistrationPeriodValidationError.REVIEWER_DEADLINE);
    }

    // KLTN bắt buộc có ngày hội đồng nằm sau hạn báo cáo và không sau hạn phản biện.
    boolean councilRequired = period.getPeriodType() == PeriodType.KLTN;
    if (councilRequired != (period.getCouncilDate() != null)
        || councilRequired
            && (period.getCouncilDate() == null
                || !period.getReportDeadline().isBefore(period.getCouncilDate())
                || period.getReviewerDeadline() == null
                || period.getCouncilDate().isAfter(period.getReviewerDeadline()))) {
      errors.add(RegistrationPeriodValidationError.COUNCIL_DATE);
    }

    return errors;
  }

  /** Lấy các đợt đang mở để sinh viên tạo nhóm và đăng ký đề tài. */
  public List<RegistrationPeriod> findOpenForStudents() {
    return repository.findOpenForStudents(LocalDateTime.now());
  }

  /** Đếm số đợt KLTN đang trong thời gian được phép quản lý hội đồng. */
  public long countOpenCouncilPeriods() {
    return repository.countOpenCouncilPeriods(LocalDateTime.now());
  }

  /** Lấy các đợt có công việc liên quan đến giảng viên để hiển thị trên trang nghiệp vụ. */
  public List<RegistrationPeriod> findVisibleForLecturers() {
    return repository.findVisibleForLecturers(LocalDateTime.now());
  }

  /** Lấy các đợt mà giảng viên tham gia với vai trò hướng dẫn, phản biện hoặc hội đồng. */
  public List<RegistrationPeriod> findVisibleForLecturer(Long lecturerId) {
    return repository.findVisibleForLecturer(LocalDateTime.now(), lecturerId);
  }

  /** Lấy một trang đợt liên quan đến giảng viên, có hỗ trợ tìm kiếm và lọc theo tên. */
  public Page<RegistrationPeriod> findVisibleForLecturer(
      Long lecturerId,
      TableQuery query,
      BaseTableDefinition<RegistrationPeriod> definition) {
    return repository.findVisibleForLecturer(
        LocalDateTime.now(),
        lecturerId,
        normalized(query.getSearch()),
        nameFilter(query, definition),
        viewerPageable(query, definition));
  }

  /** Lấy các đợt sinh viên đang được tham gia hoặc đã từng có nhóm. */
  public List<RegistrationPeriod> findVisibleForStudent(Long studentId) {
    return repository.findVisibleForStudent(LocalDateTime.now(), studentId);
  }

  /** Lấy một trang đợt liên quan đến sinh viên, có hỗ trợ tìm kiếm và lọc theo tên. */
  public Page<RegistrationPeriod> findVisibleForStudent(
      Long studentId,
      TableQuery query,
      BaseTableDefinition<RegistrationPeriod> definition) {
    return repository.findVisibleForStudent(
        LocalDateTime.now(),
        studentId,
        normalized(query.getSearch()),
        nameFilter(query, definition),
        viewerPageable(query, definition));
  }

  /** Lấy toàn bộ đợt đăng ký chưa bị xóa mềm. */
  public List<RegistrationPeriod> findAll() {
    return repository.findAll();
  }

  /** Xác định giai đoạn hiện tại của đợt dựa trên các mốc thời gian. */
  public RegistrationPeriodStage stage(RegistrationPeriod period, LocalDateTime now) {
    if (now.isBefore(period.getLecturerRegistrationStart())) {
      return RegistrationPeriodStage.UPCOMING;
    }
    if (!now.isAfter(period.getLecturerRegistrationEnd())) {
      return RegistrationPeriodStage.TOPIC_PROPOSAL;
    }
    if (now.isBefore(period.getStudentRegistrationStart())) {
      return RegistrationPeriodStage.AWAITING_STUDENT_REGISTRATION;
    }
    if (now.isBefore(period.getReportDeadline())) {
      return RegistrationPeriodStage.TOPIC_EXECUTION;
    }

    // Sau hạn báo cáo, hành động công bố gần nhất quyết định đợt đã đóng hay còn đánh giá.
    return publicationRepository
        .findFirstByRegistrationPeriodIdOrderByIdDesc(period.getId())
        .filter(publication -> publication.getAction() == ResultPublicationAction.PUBLISHED)
        .map(publication -> RegistrationPeriodStage.CLOSED)
        .orElse(RegistrationPeriodStage.EVALUATION_AND_PUBLICATION);
  }

  private String nameFilter(
      TableQuery query, BaseTableDefinition<RegistrationPeriod> definition) {
    return normalized(query.filterValues(definition).get("name"));
  }

  private Pageable viewerPageable(
      TableQuery query, BaseTableDefinition<RegistrationPeriod> definition) {
    Pageable pageable = query.pageable(definition);
    if (pageable.getSort().isSorted()) return pageable;

    // Khi người xem không chọn cách sắp xếp, ưu tiên các đợt kết thúc đăng ký gần nhất.
    return PageRequest.of(
        pageable.getPageNumber(),
        pageable.getPageSize(),
        Sort.by(Sort.Direction.DESC, "studentRegistrationEnd"));
  }

  private String normalized(String value) {
    return value == null || value.isBlank() ? null : value.trim();
  }
}
