package com.nhom11.qldt.feature.auth.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.nhom11.qldt.core.service.BaseCrudService;
import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.auth.repository.UserRepository;
import com.nhom11.qldt.feature.auth.repository.RoleRepository;
import com.nhom11.qldt.feature.department.repository.DepartmentRepository;
import com.nhom11.qldt.feature.department.model.entity.Department;
import com.nhom11.qldt.feature.auth.model.entity.Role;
import java.util.Set;

@Service
public class UserService extends BaseCrudService<User, Long> {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DepartmentRepository departmentRepository;
    private final RoleRepository roleRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, DepartmentRepository departmentRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.departmentRepository = departmentRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public UserRepository getRepository() {
        return userRepository;
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    protected void beforeCreate(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
    }

    @Override
    protected void beforeUpdate(User updates) {
        userRepository.findById(updates.getId()).ifPresent(existingUser -> {
            if (updates.getPassword() == null || updates.getPassword().isBlank()) {
                updates.setPassword(existingUser.getPassword());
            } else {
                updates.setPassword(passwordEncoder.encode(updates.getPassword()));
            }
        });
    }

    public User create(User user, Long departmentId, Set<Long> roleIds) {
        assignRelations(user, departmentId, roleIds);
        return super.create(user);
    }

    public User update(User user, Long departmentId, Set<Long> roleIds) {
        assignRelations(user, departmentId, roleIds);
        return super.update(user);
    }

    private void assignRelations(User user, Long departmentId, Set<Long> roleIds) {
        Department department = departmentId == null ? null : departmentRepository.findById(departmentId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found"));
        user.setDepartment(department);
        Set<Role> roles = roleIds == null ? Set.of() : Set.copyOf(roleRepository.findAllById(roleIds));
        if (roles.size() != (roleIds == null ? 0 : roleIds.size())) {
            throw new IllegalArgumentException("Role not found");
        }
        user.setRoles(roles);
    }
}
