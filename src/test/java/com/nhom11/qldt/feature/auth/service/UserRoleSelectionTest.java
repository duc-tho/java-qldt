package com.nhom11.qldt.feature.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.nhom11.qldt.feature.auth.model.entity.Role;
import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.auth.repository.RoleRepository;
import com.nhom11.qldt.feature.auth.repository.UserRepository;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserRoleSelectionTest {
  @Mock private UserRepository userRepository;
  @Mock private RoleRepository roleRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @InjectMocks private UserService service;

  @Test
  void acceptsSingleRoleAndAllowedAdminPairs() {
    when(userRepository.save(any(User.class))).thenAnswer(call -> call.getArgument(0));
    assertThat(createWithRoles(role(1L, "STUDENT")).getRoles())
        .extracting(Role::getName).containsExactly("STUDENT");
    assertThat(createWithRoles(role(2L, "ADMIN"), role(3L, "LECTURER"))
        .getRoles()).extracting(Role::getName).containsExactlyInAnyOrder("ADMIN", "LECTURER");
    assertThat(createWithRoles(role(2L, "ADMIN"), role(4L, "HEAD_OF_FACULTY"))
        .getRoles()).extracting(Role::getName).containsExactlyInAnyOrder("ADMIN", "HEAD_OF_FACULTY");
  }

  @Test
  void rejectsMissingRoleAndUnsupportedPairs() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> createWithRoles())
        .withMessage("users.validation.roles.invalid");
    assertThatIllegalArgumentException()
        .isThrownBy(() -> createWithRoles(role(1L, "STUDENT"), role(2L, "ADMIN")))
        .withMessage("users.validation.roles.invalid");
    assertThatIllegalArgumentException()
        .isThrownBy(() -> createWithRoles(role(3L, "LECTURER"), role(4L, "HEAD_OF_FACULTY")))
        .withMessage("users.validation.roles.invalid");
    assertThatIllegalArgumentException()
        .isThrownBy(() -> createWithRoles(
            role(1L, "STUDENT"), role(2L, "ADMIN"), role(3L, "LECTURER")))
        .withMessage("users.validation.roles.invalid");
  }

  private User createWithRoles(Role... roles) {
    Set<Long> ids = java.util.Arrays.stream(roles).map(Role::getId).collect(java.util.stream.Collectors.toSet());
    when(roleRepository.findAllById(ids)).thenReturn(List.of(roles));
    User user = new User();
    user.setPassword("password");
    return service.create(user, ids);
  }

  private Role role(Long id, String name) {
    Role role = new Role();
    role.setId(id);
    role.setName(name);
    return role;
  }
}
