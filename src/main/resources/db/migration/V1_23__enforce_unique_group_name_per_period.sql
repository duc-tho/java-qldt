ALTER TABLE student_groups
    ADD CONSTRAINT uq_student_groups_period_name UNIQUE (registration_period_id, name);
