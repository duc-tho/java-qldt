package com.nhom11.qldt.feature.announcement.service;

import java.time.LocalDateTime;
import java.util.Set;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.nhom11.qldt.core.repository.BaseRepository;
import com.nhom11.qldt.core.service.BaseCrudService;
import com.nhom11.qldt.feature.announcement.model.entity.Announcement;
import com.nhom11.qldt.feature.announcement.model.enums.AnnouncementStatus;
import com.nhom11.qldt.feature.announcement.repository.AnnouncementRepository;
import com.nhom11.qldt.feature.auth.model.entity.Role;
import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.auth.repository.RoleRepository;
import com.nhom11.qldt.feature.auth.service.UserService;

@Service
public class AnnouncementService extends BaseCrudService<Announcement, Long> {
    private final AnnouncementRepository announcementRepository;
    private final RoleRepository roleRepository;
    private final UserService userService;

    public AnnouncementService(AnnouncementRepository announcementRepository, RoleRepository roleRepository, UserService userService) { this.announcementRepository = announcementRepository; this.roleRepository = roleRepository; this.userService = userService; }
    @Override public BaseRepository<Announcement, Long> getRepository() { return announcementRepository; }
    public Announcement create(Announcement announcement, Set<Long> roleIds) { announcement.setAuthor(currentUser()); announcement.setStatus(AnnouncementStatus.PUBLISHED); announcement.setRoles(roles(roleIds)); return super.create(announcement); }
    public Announcement update(Announcement announcement, Set<Long> roleIds) { Announcement existing = findById(announcement.getId()); if (existing == null || existing.getStatus() == AnnouncementStatus.WITHDRAWN) throw new IllegalArgumentException("Announcement cannot be updated"); announcement.setAuthor(existing.getAuthor()); announcement.setStatus(existing.getStatus()); announcement.setRoles(roles(roleIds)); return super.update(announcement); }
    public void withdraw(Long id) { Announcement announcement = findById(id); if (announcement == null || announcement.getStatus() == AnnouncementStatus.WITHDRAWN) return; announcement.setStatus(AnnouncementStatus.WITHDRAWN); announcement.setWithdrawnAt(LocalDateTime.now()); super.update(announcement); }
    private User currentUser() { return userService.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(() -> new IllegalStateException("Authenticated user not found")); }
    private Set<Role> roles(Set<Long> roleIds) { return roleIds == null ? Set.of() : Set.copyOf(roleRepository.findAllById(roleIds)); }
}
