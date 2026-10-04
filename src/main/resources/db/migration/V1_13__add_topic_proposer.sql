ALTER TABLE topics
    ADD COLUMN proposed_by BIGINT NULL,
    ADD CONSTRAINT fk_topics_proposed_by FOREIGN KEY (proposed_by) REFERENCES users(id) ON DELETE RESTRICT;
