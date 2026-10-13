package com.nhom11.qldt.feature.reportsubmission.service;

import com.nhom11.qldt.feature.auth.model.entity.Role;
import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.council.repository.CouncilMemberRepository;
import com.nhom11.qldt.feature.reportsubmission.config.ReportStorageProperties;
import com.nhom11.qldt.feature.reportsubmission.model.entity.ReportFile;
import com.nhom11.qldt.feature.reportsubmission.model.entity.ReportSubmission;
import com.nhom11.qldt.feature.reportsubmission.model.enums.ReportFileType;
import com.nhom11.qldt.feature.reportsubmission.model.enums.ReportSubmissionStatus;
import com.nhom11.qldt.feature.reportsubmission.repository.ReportFileRepository;
import com.nhom11.qldt.feature.reportsubmission.repository.ReportSubmissionRepository;
import com.nhom11.qldt.feature.studentgroup.model.entity.TopicRegistration;
import com.nhom11.qldt.feature.studentgroup.model.enums.GroupActivityType;
import com.nhom11.qldt.feature.studentgroup.service.GroupActivityService;
import com.nhom11.qldt.feature.studentgroup.model.enums.TopicRegistrationStatus;
import com.nhom11.qldt.feature.studentgroup.repository.GroupMemberRepository;
import com.nhom11.qldt.feature.studentgroup.repository.TopicRegistrationRepository;
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
  public ReportSubmission submit(
      Long periodId,
      Long groupId,
      Long registrationId,
      User actor,
      List<MultipartFile> files) {
    TopicRegistration registration = registrationRepository.findById(registrationId).orElseThrow();
    requireContext(registration, periodId, groupId);
    requireLeader(registration, actor);
    requireSubmissionWindow(registration);
    validateFiles(files);

    LocalDateTime submittedAt = LocalDateTime.now();
    ReportSubmission current = current(registrationId);
    if (current != null) {
      current.setStatus(ReportSubmissionStatus.REPLACED);
      submissionRepository.save(current);
    }

    ReportSubmission submission = new ReportSubmission();
    submission.setTopicRegistration(registration);
    submission.setSubmittedBy(actor);
    submission.setReplacedSubmission(current);
    submission.setSubmittedAt(submittedAt);
    submission.setStatus(ReportSubmissionStatus.SUBMITTED);
    submission = submissionRepository.save(submission);

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
      groupActivityService.record(registration.getStudentGroup(), actor, null, GroupActivityType.REPORT_SUBMITTED, null);
      return submission;
    } catch (IOException exception) {
      storedPaths.forEach(this::deleteQuietly);
      throw new IllegalStateException("report-submissions.error.storage", exception);
    }
  }

  public ReportSubmission current(Long registrationId) {
    return submissionRepository
        .findFirstByTopicRegistrationIdAndStatusNotOrderBySubmittedAtDesc(
            registrationId, ReportSubmissionStatus.REPLACED)
        .orElse(null);
  }

  public boolean hasCurrentSubmission(Long registrationId) {
    return current(registrationId) != null;
  }

  public List<ReportSubmission> history(Long registrationId) {
    return submissionRepository.findAllByTopicRegistrationIdOrderBySubmittedAtDesc(registrationId);
  }

  public List<ReportFile> files(Long submissionId) {
    return fileRepository.findAllByReportSubmissionId(submissionId);
  }

  public ReportFile findFile(Long fileId) {
    return fileRepository.findById(fileId).orElseThrow();
  }

  public Resource download(Long periodId, Long groupId, Long fileId, User actor) {
    ReportFile file = findFile(fileId);
    TopicRegistration registration = file.getReportSubmission().getTopicRegistration();
    requireContext(registration, periodId, groupId);
    if (!canAccess(registration, actor)) {
      throw new IllegalArgumentException("report-submissions.error.download-forbidden");
    }
    Path path = root().resolve(file.getStoragePath()).normalize();
    if (!path.startsWith(root()) || !Files.isRegularFile(path)) {
      throw new IllegalArgumentException("report-submissions.error.file-not-found");
    }
    return new FileSystemResource(path);
  }

  public boolean canUpload(TopicRegistration registration, User actor) {
    return registration != null
        && registration.getStatus() == TopicRegistrationStatus.APPROVED
        && isSubmissionOpen(registration)
        && groupMemberRepository
            .findByStudentGroupIdAndStudentId(registration.getStudentGroup().getId(), actor.getId())
            .map(member -> member.isLeader())
            .orElse(false);
  }

  public boolean isUploadClosed(TopicRegistration registration) {
    return registration != null && !isBeforeOrAtDeadline(registration);
  }

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
    if (hasRole(actor, "ADMIN") || hasRole(actor, "HEAD_OF_FACULTY")) {
      return true;
    }
    if (groupMemberRepository
        .findByStudentGroupIdAndStudentId(registration.getStudentGroup().getId(), actor.getId())
        .isPresent()) {
      return true;
    }
    if (registration.getTopic().getAdvisers().stream().anyMatch(adviser -> adviser.getId().equals(actor.getId()))) {
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
    if (files == null || files.isEmpty() || files.size() > MAX_FILES) {
      throw new IllegalArgumentException("report-submissions.validation.files.count");
    }
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
      return ReportFileType.valueOf(name.substring(name.lastIndexOf('.') + 1).toUpperCase(Locale.ROOT));
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
    return root().resolve(UUID.randomUUID() + "." + type.name().toLowerCase(Locale.ROOT)).normalize();
  }

  private Path root() {
    return Path.of(storageProperties.getRoot()).toAbsolutePath().normalize();
  }

  private boolean hasRole(User user, String roleName) {
    return user.getRoles().stream().map(Role::getName).anyMatch(roleName::equals);
  }

  private void deleteQuietly(Path path) {
    try {
      Files.deleteIfExists(path);
    } catch (IOException ignored) {
      // A failed cleanup is non-fatal; the storage error is the actionable cause.
    }
  }
}
