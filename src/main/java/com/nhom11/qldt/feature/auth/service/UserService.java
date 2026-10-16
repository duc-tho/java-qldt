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
  public UserRepository getRepository() {
    return userRepository;
  }

  public Optional<User> findByEmail(String email) {
    return userRepository.findByEmail(email);
  }

  public List<User> findAllLecturers() {
    return userRepository.findAllLecturers();
  }

  public List<User> findAllLecturersByIds(Set<Long> ids) {
    return ids.isEmpty() ? List.of() : userRepository.findAllLecturersByIdIn(ids);
  }

  public List<User> searchLecturers(String search) {
    return userRepository.searchLecturers(search == null || search.isBlank() ? null : search.trim(), PageRequest.of(0, 10));
  }

  @Override
  protected void beforeCreate(User user) {
    user.setPassword(passwordEncoder.encode(user.getPassword()));
  }

  @Override
  protected void beforeUpdate(User updates) {
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

  public User create(User user, Set<Long> roleIds) {
    assignRoles(user, roleIds);
    return super.create(user);
  }

  public User update(User user, Set<Long> roleIds) {
    assignRoles(user, roleIds);
    return super.update(user);
  }

  @Transactional
  public User updateAccount(Long id, AccountForm form) {
    User user = userRepository.findById(id).orElseThrow();
    user.setEmail(form.getEmail());
    user.setPhone(form.getPhone());
    user.setFullName(form.getFullName());

    return userRepository.save(user);
  }

  @Transactional
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
    Set<Role> roles = roleIds == null ? Set.of() : Set.copyOf(roleRepository.findAllById(roleIds));
    if (roles.size() != (roleIds == null ? 0 : roleIds.size()) || roles.isEmpty()) {
      throw new IllegalArgumentException("users.validation.roles.invalid");
    }

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
