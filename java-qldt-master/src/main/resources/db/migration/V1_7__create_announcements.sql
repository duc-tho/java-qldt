CREATE TABLE announcements (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    author_id BIGINT NOT NULL,
    withdrawn_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_announcements_author FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE RESTRICT
);

CREATE TABLE announcement_roles (
    announcement_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (announcement_id, role_id),
    CONSTRAINT fk_announcement_roles_announcement FOREIGN KEY (announcement_id) REFERENCES announcements(id) ON DELETE RESTRICT,
    CONSTRAINT fk_announcement_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE RESTRICT
);
