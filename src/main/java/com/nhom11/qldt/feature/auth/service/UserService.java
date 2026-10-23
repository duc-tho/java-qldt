package com.nhom11.qldt.feature.auth.service;

import com.nhom11.qldt.core.service.BaseCrudService;
import com.nhom11.qldt.feature.auth.model.dto.AccountForm;
import com.nhom11.qldt.feature.auth.model.dto.PasswordChangeForm;
import com.nhom11.qldt.feature.auth.model.entity.Role;
import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.auth.repository.RoleRepository;
import com.nhom11.qldt.feature.auth.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
/** Cung cấp nghiệp vụ quản lý tài khoản, hồ sơ, vai trò và mật khẩu người dùng. */
public class UserService extends BaseCrudService<User, Long> {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final RoleRepository roleRepository;

  public UserService(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      RoleRepository roleRepository) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.roleRepository = roleRepository;
  }

  @Override
  /** Trả về {@code UserRepository} để lớp base đọc và ghi dữ liệu tài khoản. */
  public UserRepository getRepository() {
    return userRepository;
  }

  /** Tìm tài khoản theo email; trả về rỗng nếu email chưa được đăng ký. */
  public Optional<User> findByEmail(String email) {
    return userRepository.findByEmail(email);
  }

  /** Kiểm tra email đã được tài khoản nào sử dụng hay chưa. */
  public boolean existsByEmail(String email) {
    return userRepository.existsByEmail(email);
  }

  /** Kiểm tra email đã bị tài khoản khác sử dụng, bỏ qua tài khoản đang chỉnh sửa. */
  public boolean existsByEmailAndIdNot(String email, Long id) {
    return userRepository.existsByEmailAndIdNot(email, id);
  }

  /** Kiểm tra mã sinh viên/giảng viên đã tồn tại trong hệ thống hay chưa. */
  public boolean existsByUserCode(String userCode) {
    return userRepository.existsByUserCode(userCode);
  }

  /** Kiểm tra mã người dùng đã bị tài khoản khác sử dụng, bỏ qua tài khoản đang chỉnh sửa. */
  public boolean existsByUserCodeAndIdNot(String userCode, Long id) {
    return userRepository.existsByUserCodeAndIdNot(userCode, id);
  }

  /** Lấy toàn bộ tài khoản có vai trò giảng viên để dùng cho các danh sách lựa chọn. */
  public List<User> findAllLecturers() {
    return userRepository.findAllLecturers();
  }

  /** Lấy các giảng viên có ID nằm trong danh sách được cung cấp. */
  public List<User> findAllLecturersByIds(Set<Long> ids) {
    return ids.isEmpty() ? List.of() : userRepository.findAllLecturersByIdIn(ids);
  }

  /** Tìm tối đa 10 giảng viên theo tên hoặc mã để hiển thị gợi ý chọn. */
  public List<User> searchLecturers(String search) {
    return userRepository.searchLecturers(search == null || search.isBlank() ? null : search.trim(), PageRequest.of(0, 10));
  }

  @Override
  /** Mã hóa mật khẩu trước khi lưu tài khoản mới vào database. */
  protected void beforeCreate(User user) {
    user.setPassword(passwordEncoder.encode(user.getPassword()));
  }

  @Override
  /** Giữ mật khẩu cũ nếu form để trống; nếu có mật khẩu mới thì mã hóa trước khi lưu. */
  protected void beforeUpdate(User updates) {
    // Biểu mẫu để trống mật khẩu nghĩa là giữ mật khẩu cũ, không phải xóa mật khẩu.
    userRepository
        .findById(updates.getId())
        .ifPresent(
            existingUser -> {
              if (updates.getPassword() == null || updates.getPassword().isBlank()) {
                updates.setPassword(existingUser.getPassword());
              } else {
                updates.setPassword(passwordEncoder.encode(updates.getPassword()));
              }
            });
  }

  /** Kiểm tra danh sách quyền, mã hóa mật khẩu rồi tạo tài khoản mới. */
  public User create(User user, Set<Long> roleIds) {
    assignRoles(user, roleIds);
    return super.create(user);
  }

  /** Kiểm tra danh sách quyền, xử lý mật khẩu rồi cập nhật tài khoản. */
  public User update(User user, Set<Long> roleIds) {
    assignRoles(user, roleIds);
    return super.update(user);
  }

  @Transactional
  /** Cập nhật họ tên và số điện thoại trong trang thông tin cá nhân. */
  public User updateAccount(Long id, AccountForm form) {
    User user = userRepository.findById(id).orElseThrow();
    user.setPhone(form.getPhone());
    user.setFullName(form.getFullName());

    return userRepository.save(user);
  }

  @Transactional
  /** Xác minh mật khẩu hiện tại trước khi mã hóa và lưu mật khẩu mới. */
  public boolean changePassword(Long id, PasswordChangeForm form) {
    User user = userRepository.findById(id).orElseThrow();
    if (!passwordEncoder.matches(form.getCurrentPassword(), user.getPassword())) {
      return false;
    }

    user.setPassword(passwordEncoder.encode(form.getNewPassword()));
    userRepository.save(user);
    return true;
  }

  private void assignRoles(User user, Set<Long> roleIds) {
    // Đối chiếu số lượng để phát hiện mã vai trò không tồn tại trước khi gán cho người dùng.
    Set<Role> roles = roleIds == null ? Set.of() : Set.copyOf(roleRepository.findAllById(roleIds));
    if (roles.size() != (roleIds == null ? 0 : roleIds.size()) || roles.isEmpty()) {
      throw new IllegalArgumentException("users.validation.roles.invalid");
    }

    // Chỉ ADMIN được kiêm nhiệm; các nhóm quyền còn lại phải được gán độc lập.
    Set<String> roleNames = roles.stream().map(role -> role.getName()).collect(Collectors.toSet());
    boolean validPair =
        roles.size() == 2
            && roleNames.contains("ADMIN")
            && (roleNames.contains("LECTURER") || roleNames.contains("HEAD_OF_FACULTY"));
    if (roles.size() != 1 && !validPair) {
      throw new IllegalArgumentException("users.validation.roles.invalid");
    }

    user.setRoles(roles);
  }
}
