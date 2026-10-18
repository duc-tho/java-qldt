CREATE TABLE group_activities (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_group_id BIGINT NOT NULL,
    actor_id BIGINT NOT NULL,
    subject_id BIGINT NULL,
    type VARCHAR(30) NOT NULL,
    detail TEXT NULL,
    occurred_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_group_activities_group FOREIGN KEY (student_group_id) REFERENCES student_groups(id) ON DELETE RESTRICT,
    CONSTRAINT fk_group_activities_actor FOREIGN KEY (actor_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_group_activities_subject FOREIGN KEY (subject_id) REFERENCES users(id) ON DELETE RESTRICT,
    INDEX idx_group_activities_group_time (student_group_id, occurred_at)
);
