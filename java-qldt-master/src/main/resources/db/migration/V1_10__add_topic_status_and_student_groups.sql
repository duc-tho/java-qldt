ALTER TABLE topics ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'APPROVED';

CREATE TABLE student_groups (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    registration_period_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    CONSTRAINT fk_student_groups_period FOREIGN KEY (registration_period_id) REFERENCES registration_periods(id) ON DELETE RESTRICT
);

CREATE TABLE group_members (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_group_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    is_leader BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    CONSTRAINT uq_group_members_group_student UNIQUE (student_group_id, student_id),
    CONSTRAINT fk_group_members_group FOREIGN KEY (student_group_id) REFERENCES student_groups(id) ON DELETE RESTRICT,
    CONSTRAINT fk_group_members_student FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE RESTRICT
);

CREATE TABLE topic_registrations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_group_id BIGINT NOT NULL,
    topic_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    rejection_reason TEXT NULL,
    reviewed_by BIGINT NULL,
    reviewed_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    CONSTRAINT uq_topic_registrations_group UNIQUE (student_group_id),
    CONSTRAINT fk_topic_registrations_group FOREIGN KEY (student_group_id) REFERENCES student_groups(id) ON DELETE RESTRICT,
    CONSTRAINT fk_topic_registrations_topic FOREIGN KEY (topic_id) REFERENCES topics(id) ON DELETE RESTRICT,
    CONSTRAINT fk_topic_registrations_reviewer FOREIGN KEY (reviewed_by) REFERENCES users(id) ON DELETE RESTRICT
);
