package com.nhom11.qldt.feature.announcement.service;

import com.nhom11.qldt.core.repository.BaseRepository;
import com.nhom11.qldt.core.service.BaseCrudService;
import com.nhom11.qldt.feature.announcement.model.dto.AnnouncementSummary;
import com.nhom11.qldt.feature.announcement.model.entity.Announcement;
import com.nhom11.qldt.feature.announcement.model.enums.AnnouncementStatus;
import com.nhom11.qldt.feature.announcement.repository.AnnouncementRepository;
import com.nhom11.qldt.feature.auth.model.entity.Role;
import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.auth.repository.RoleRepository;
import com.nhom11.qldt.feature.auth.service.UserService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
/** Xử lý tạo, cập nhật, thu hồi và lọc thông báo theo vai trò người nhận. */
public class AnnouncementService extends BaseCrudService<Announcement, Long> {
  private final AnnouncementRepository announcementRepository;
  private final RoleRepository roleRepository;
  private final UserService userService;

  public AnnouncementService(
      AnnouncementRepository announcementRepository,
      RoleRepository roleRepository,
      UserService userService) {
    this.announcementRepository = announcementRepository;
    this.roleRepository = roleRepository;
    this.userService = userService;
  }

  @Override
  /** Trả về repository để lớp base đọc và ghi dữ liệu thông báo. */
  public BaseRepository<Announcement, Long> getRepository() {
    return announcementRepository;
  }

  /** Tạo và công bố ngay thông báo cho các vai trò được chọn. */
  public Announcement create(Announcement announcement, Set<Long> roleIds) {
    // Thông báo do nhân viên tạo được công bố ngay và ghi nhận người đang đăng nhập là tác giả.
    announcement.setAuthor(currentUser());
    announcement.setStatus(AnnouncementStatus.PUBLISHED);
    announcement.setRoles(roles(roleIds));

    return super.create(announcement);
  }

  /** Cập nhật nội dung và đối tượng nhận của thông báo chưa bị thu hồi. */
  public Announcement update(Announcement announcement, Set<Long> roleIds) {
    Announcement existing = findById(announcement.getId());
    if (existing == null || existing.getStatus() == AnnouncementStatus.WITHDRAWN)
      throw new IllegalArgumentException("Announcement cannot be updated");

    // Tác giả và trạng thái do hệ thống quản lý nên không nhận lại từ dữ liệu biểu mẫu.
    announcement.setAuthor(existing.getAuthor());
    announcement.setStatus(existing.getStatus());
    announcement.setRoles(roles(roleIds));

    return super.update(announcement);
  }

  /** Chuyển thông báo đã công bố sang trạng thái thu hồi. */
  public void withdraw(Long id) {
    Announcement announcement = findById(id);

    // Thu hồi là thao tác idempotent: bản ghi không tồn tại hoặc đã thu hồi không cần cập nhật lại.
    if (announcement == null || announcement.getStatus() == AnnouncementStatus.WITHDRAWN) return;

    announcement.setStatus(AnnouncementStatus.WITHDRAWN);
    announcement.setWithdrawnAt(LocalDateTime.now());

    super.update(announcement);
  }

  /** Kiểm tra toàn bộ ID vai trò nhận thông báo có tồn tại trong database. */
  public boolean hasValidRoles(Set<Long> roleIds) {
    return roleIds == null || roleRepository.findAllById(roleIds).size() == roleIds.size();
  }

  /** Lấy các thông báo đã công bố dành cho ít nhất một vai trò của người dùng. */
  public List<Announcement> findPublishedForRoles(Set<String> roleNames) {
    if (roleNames.isEmpty()) return List.of();

    return announcementRepository.findPublishedForRoles(roleNames);
  }

  /** Lấy các thông báo mới nhất dành cho vai trò người dùng, giới hạn theo số lượng yêu cầu. */
  public List<AnnouncementSummary> findRecentPublishedForRoles(Set<String> roleNames, int limit) {
    if (roleNames.isEmpty()) return List.of();

    return announcementRepository.findRecentPublishedForRoles(
        roleNames, PageRequest.of(0, limit));
  }

  /** Tìm thông báo đã công bố theo ID và kiểm tra người dùng có thuộc đối tượng nhận hay không. */
  public Announcement findPublishedByIdForRoles(Long id, Set<String> roleNames) {
    if (roleNames.isEmpty()) return null;

    return announcementRepository.findPublishedByIdForRoles(id, roleNames).orElse(null);
  }

  private User currentUser() {
    return userService
        .findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
        .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
  }

  private Set<Role> roles(Set<Long> roleIds) {
    return roleIds == null ? Set.of() : Set.copyOf(roleRepository.findAllById(roleIds));
  }
}
