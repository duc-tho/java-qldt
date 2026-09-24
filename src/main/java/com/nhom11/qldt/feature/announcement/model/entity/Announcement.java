package com.nhom11.qldt.feature.announcement.model.entity;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import com.nhom11.qldt.core.entity.AuditableEntity;
import com.nhom11.qldt.feature.auth.model.entity.Role;
import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.announcement.model.enums.AnnouncementStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
@Entity @Table(name = "announcements") @Getter @Setter
public class Announcement extends AuditableEntity {
    @Column(nullable = false, length = 255) private String title;
    @Column(nullable = false, columnDefinition = "TEXT") private String content;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private AnnouncementStatus status;
    @ManyToOne(optional = false) @JoinColumn(name = "author_id", nullable = false) private User author;
    private LocalDateTime withdrawnAt;
    @ManyToMany @JoinTable(name = "announcement_roles", joinColumns = @JoinColumn(name = "announcement_id"), inverseJoinColumns = @JoinColumn(name = "role_id")) private Set<Role> roles = new HashSet<>();
}
