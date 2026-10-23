package com.nhom11.qldt.feature.report.service;

import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.council.repository.CouncilMemberRepository;
import com.nhom11.qldt.feature.group.model.entity.TopicRegistration;
import com.nhom11.qldt.feature.group.model.enums.GroupActivityType;
import com.nhom11.qldt.feature.group.model.enums.TopicRegistrationStatus;
import com.nhom11.qldt.feature.group.repository.GroupMemberRepository;
import com.nhom11.qldt.feature.group.repository.TopicRegistrationRepository;
import com.nhom11.qldt.feature.group.service.GroupActivityService;
import com.nhom11.qldt.feature.report.config.ReportStorageProperties;
import com.nhom11.qldt.feature.report.model.entity.ReportFile;
import com.nhom11.qldt.feature.report.model.entity.ReportSubmission;
import com.nhom11.qldt.feature.report.model.enums.ReportFileType;
import com.nhom11.qldt.feature.report.model.enums.ReportSubmissionStatus;
import com.nhom11.qldt.feature.report.repository.ReportFileRepository;
import com.nhom11.qldt.feature.report.repository.ReportSubmissionRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
/** Xử lý nộp lại báo cáo, lưu file, kiểm tra quyền tải xuống và quản lý lịch sử nộp. */
public class ReportSubmissionService {
  private static final int MAX_FILES = 10;
  private static final int MAX_ORIGINAL_NAME_LENGTH = 255;
  private static final long DOCUMENT_MAX_BYTES = 20L * 1024 * 1024;
  private static final long ARCHIVE_MAX_BYTES = 50L * 1024 * 1024;

  private final ReportSubmissionRepository submissionRepository;
  private final ReportFileRepository fileRepository;
  private final TopicRegistrationRepository registrationRepository;
  private final GroupMemberRepository groupMemberRepository;
  private final CouncilMemberRepository councilMemberRepository;
  private final ReportStorageProperties storageProperties;
  private final GroupActivityService groupActivityService;

  public ReportSubmissionService(
      ReportSubmissionRepository submissionRepository,
      ReportFileRepository fileRepository,
      TopicRegistrationRepository registrationRepository,
      GroupMemberRepository groupMemberRepository,
      CouncilMemberRepository councilMemberRepository,
      ReportStorageProperties storageProperties,
      GroupActivityService groupActivityService) {
    this.submissionRepository = submissionRepository;
    this.fileRepository = fileRepository;
    this.registrationRepository = registrationRepository;
    this.groupMemberRepository = groupMemberRepository;
    this.councilMemberRepository = councilMemberRepository;
    this.storageProperties = storageProperties;
    this.groupActivityService = groupActivityService;
  }

  @Transactional
  /** Nộp bộ file báo cáo mới và chuyển lần nộp hiện tại trước đó sang trạng thái đã thay thế. */
  public ReportSubmission submit(
      Long periodId, Long groupId, Long registrationId, User actor, List<MultipartFile> files) {
    TopicRegistration registration = registrationRepository.findById(registrationId).orElseThrow();

    // Xác thực ngữ cảnh URL, quyền trưởng nhóm, thời gian nộp và toàn bộ tệp trước khi ghi dữ liệu.
    requireContext(registration, periodId, groupId);
    requireLeader(registration, actor);
    requireSubmissionWindow(registration);
    validateFiles(files);

    LocalDateTime submittedAt = LocalDateTime.now();
    ReportSubmission current = current(registrationId);

    // Mỗi đăng ký chỉ có một bản nộp hiện hành; bản cũ được giữ lại trong lịch sử.
    if (current != null) {
      current.setStatus(ReportSubmissionStatus.REPLACED);
      submissionRepository.save(current);
    }

    // Tạo bản ghi lần nộp trước để các tệp có thể tham chiếu đúng khóa ngoại.
    ReportSubmission submission = new ReportSubmission();
    submission.setTopicRegistration(registration);
    submission.setSubmittedBy(actor);
    submission.setReplacedSubmission(current);
    submission.setSubmittedAt(submittedAt);
    submission.setStatus(ReportSubmissionStatus.SUBMITTED);
    submission = submissionRepository.save(submission);

    // Ghi từng tệp xuống đĩa và lưu metadata tương ứng trong cơ sở dữ liệu.
    List<Path> storedPaths = new ArrayList<>();
    try {
      for (MultipartFile file : files) {
        ReportFileType type = fileType(file);
        Path storedPath = storagePath(type);
        Files.createDirectories(storedPath.getParent());
        storedPaths.add(storedPath);
        file.transferTo(storedPath);

        ReportFile reportFile = new ReportFile();
        reportFile.setReportSubmission(submission);
        reportFile.setOriginalName(file.getOriginalFilename());
        reportFile.setStoragePath(storedPath.getFileName().toString());
        reportFile.setFileType(type);
        reportFile.setBytes(file.getSize());
        fileRepository.save(reportFile);
      }
      groupActivityService.record(
          registration.getStudentGroup(), actor, null, GroupActivityType.REPORT_SUBMITTED, null);

      return submission;
    } catch (IOException exception) {
      // Transaction sẽ hoàn tác dữ liệu; các tệp đã ghi cần được dọn thủ công.
      storedPaths.forEach(this::deleteQuietly);
      throw new IllegalStateException("report-submissions.error.storage", exception);
    }
  }

  /** Lấy lần nộp báo cáo hiện tại, không tính các lần đã bị thay thế. */
  public ReportSubmission current(Long registrationId) {
    return submissionRepository
        .findFirstByTopicRegistrationIdAndStatusNotOrderBySubmittedAtDesc(
            registrationId, ReportSubmissionStatus.REPLACED)
        .orElse(null);
  }

  /** Kiểm tra đăng ký đề tài đã có ít nhất một lần nộp báo cáo còn hiệu lực. */
  public boolean hasCurrentSubmission(Long registrationId) {
    return current(registrationId) != null;
  }

  /** Lấy toàn bộ lịch sử nộp báo cáo, sắp xếp lần nộp mới nhất lên trước. */
  public List<ReportSubmission> history(Long registrationId) {
    return submissionRepository.findAllByTopicRegistrationIdOrderBySubmittedAtDesc(registrationId);
  }

  /** Lấy danh sách file thuộc một lần nộp báo cáo. */
  public List<ReportFile> files(Long submissionId) {
    return fileRepository.findAllByReportSubmissionId(submissionId);
  }

  /** Tìm thông tin file báo cáo theo ID; báo lỗi nếu record không tồn tại. */
  public ReportFile findFile(Long fileId) {
    return fileRepository.findById(fileId).orElseThrow();
  }

  /** Kiểm tra quyền của người dùng rồi nạp tệp báo cáo từ vùng lưu trữ để tải xuống. */
  public Resource download(Long periodId, Long groupId, Long fileId, User actor) {
    ReportFile file = findFile(fileId);
    TopicRegistration registration = file.getReportSubmission().getTopicRegistration();

    requireContext(registration, periodId, groupId);

    // Quyền tải được mở cho quản trị, thành viên nhóm và giảng viên tham gia đánh giá.
    if (!canAccess(registration, actor)) {
      throw new IllegalArgumentException("report-submissions.error.download-forbidden");
    }

    // Chuẩn hóa đường dẫn và bắt buộc tệp nằm trong thư mục lưu trữ cấu hình.
    Path path = root().resolve(file.getStoragePath()).normalize();
    if (!path.startsWith(root()) || !Files.isRegularFile(path)) {
      throw new IllegalArgumentException("report-submissions.error.file-not-found");
    }
    return new FileSystemResource(path);
  }

  /** Kiểm tra người dùng là trưởng nhóm và thời gian nộp báo cáo đang mở. */
  public boolean canUpload(TopicRegistration registration, User actor) {
    return registration != null
        && registration.getStatus() == TopicRegistrationStatus.APPROVED
        && isSubmissionOpen(registration)
        && groupMemberRepository
            .findByStudentGroupIdAndStudentId(registration.getStudentGroup().getId(), actor.getId())
            .map(member -> member.isLeader())
            .orElse(false);
  }

  /** Kiểm tra thời hạn nộp báo cáo đã kết thúc hay chưa. */
  public boolean isUploadClosed(TopicRegistration registration) {
    return registration != null && !isBeforeOrAtDeadline(registration);
  }

  /** Kiểm tra thời gian nộp báo cáo chưa bắt đầu vì đợt đăng ký sinh viên vẫn đang mở. */
  public boolean isUploadNotStarted(TopicRegistration registration) {
    if (registration == null) {
      return false;
    }
    LocalDateTime registrationEnd =
        registration.getTopic().getRegistrationPeriod().getStudentRegistrationEnd();
    return registrationEnd == null || !LocalDateTime.now().isAfter(registrationEnd);
  }

  private void requireLeader(TopicRegistration registration, User actor) {
    boolean isLeader =
        registration.getStatus() == TopicRegistrationStatus.APPROVED
            && groupMemberRepository
                .findByStudentGroupIdAndStudentId(
                    registration.getStudentGroup().getId(), actor.getId())
                .map(member -> member.isLeader())
                .orElse(false);
    if (!isLeader) {
      throw new IllegalArgumentException("report-submissions.error.leader-required");
    }
  }

  private void requireSubmissionWindow(TopicRegistration registration) {
    if (!isBeforeOrAtDeadline(registration)) {
      throw new IllegalArgumentException("report-submissions.error.deadline-passed");
    }
    if (isUploadNotStarted(registration)) {
      throw new IllegalArgumentException("report-submissions.error.not-open");
    }
  }

  private void requireContext(TopicRegistration registration, Long periodId, Long groupId) {
    if (!registration.getTopic().getRegistrationPeriod().getId().equals(periodId)
        || !registration.getStudentGroup().getId().equals(groupId)) {
      throw new IllegalArgumentException("report-submissions.error.context-mismatch");
    }
  }

  private boolean canAccess(TopicRegistration registration, User actor) {
    // Quản trị viên và trưởng khoa được phép truy cập mọi báo cáo.
    if (hasRole(actor, "ADMIN") || hasRole(actor, "HEAD_OF_FACULTY")) {
      return true;
    }

    // Sinh viên chỉ được truy cập báo cáo của chính nhóm mình.
    if (groupMemberRepository
        .findByStudentGroupIdAndStudentId(registration.getStudentGroup().getId(), actor.getId())
        .isPresent()) {
      return true;
    }

    // Giảng viên hướng dẫn, phản biện hoặc thành viên hội đồng đều tham gia đánh giá báo cáo.
    if (registration.getTopic().getAdvisers().stream()
        .anyMatch(adviser -> adviser.getId().equals(actor.getId()))) {
      return true;
    }
    if (registration.getTopic().getReviewer() != null
        && registration.getTopic().getReviewer().getId().equals(actor.getId())) {
      return true;
    }
    return registration.getTopic().getCouncil() != null
        && councilMemberRepository.existsByCouncilIdAndLecturerId(
            registration.getTopic().getCouncil().getId(), actor.getId());
  }

  private void validateFiles(List<MultipartFile> files) {
    // Kiểm tra số lượng trước khi duyệt chi tiết để báo lỗi thống nhất cho cả danh sách.
    if (files == null || files.isEmpty() || files.size() > MAX_FILES) {
      throw new IllegalArgumentException("report-submissions.validation.files.count");
    }

    // Mỗi tệp phải có tên hợp lệ, đúng định dạng hỗ trợ và không vượt giới hạn theo loại.
    for (MultipartFile file : files) {
      ReportFileType type = fileType(file);
      String originalName = file.getOriginalFilename();
      if (file.isEmpty()
          || originalName == null
          || originalName.length() > MAX_ORIGINAL_NAME_LENGTH
          || file.getSize() > maxBytes(type)) {
        throw new IllegalArgumentException("report-submissions.validation.files.invalid");
      }
    }
  }

  private ReportFileType fileType(MultipartFile file) {
    String name = file.getOriginalFilename();
    if (name == null || !name.contains(".")) {
      throw new IllegalArgumentException("report-submissions.validation.files.invalid");
    }
    try {
      return ReportFileType.valueOf(
          name.substring(name.lastIndexOf('.') + 1).toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException exception) {
      throw new IllegalArgumentException("report-submissions.validation.files.invalid");
    }
  }

  private long maxBytes(ReportFileType type) {
    return type == ReportFileType.PDF || type == ReportFileType.DOCX
        ? DOCUMENT_MAX_BYTES
        : ARCHIVE_MAX_BYTES;
  }

  private boolean isBeforeOrAtDeadline(TopicRegistration registration) {
    LocalDateTime deadline = registration.getTopic().getRegistrationPeriod().getReportDeadline();
    return deadline != null && !LocalDateTime.now().isAfter(deadline);
  }

  private boolean isSubmissionOpen(TopicRegistration registration) {
    return !isUploadNotStarted(registration) && isBeforeOrAtDeadline(registration);
  }

  private Path storagePath(ReportFileType type) {
    return root()
        .resolve(UUID.randomUUID() + "." + type.name().toLowerCase(Locale.ROOT))
        .normalize();
  }

  private Path root() {
    return Path.of(storageProperties.getRoot()).toAbsolutePath().normalize();
  }

  private boolean hasRole(User user, String roleName) {
    return user.getRoles().stream().map(role -> role.getName()).anyMatch(roleName::equals);
  }

  private void deleteQuietly(Path path) {
    try {
      Files.deleteIfExists(path);
    } catch (IOException ignored) {
      // A failed cleanup is non-fatal; the storage error is the actionable cause.
    }
  }
}
