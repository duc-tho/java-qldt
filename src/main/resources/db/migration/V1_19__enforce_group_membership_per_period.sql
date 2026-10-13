ALTER TABLE group_members ADD COLUMN registration_period_id BIGINT NULL;

UPDATE group_members member
JOIN student_groups student_group ON student_group.id = member.student_group_id
SET member.registration_period_id = student_group.registration_period_id;

ALTER TABLE group_members MODIFY registration_period_id BIGINT NOT NULL;
ALTER TABLE group_members
    ADD CONSTRAINT uq_group_members_period_student UNIQUE (registration_period_id, student_id),
    ADD CONSTRAINT fk_group_members_period FOREIGN KEY (registration_period_id) REFERENCES registration_periods(id) ON DELETE RESTRICT;
