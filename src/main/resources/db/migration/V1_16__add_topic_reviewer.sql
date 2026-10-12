ALTER TABLE topics
    ADD COLUMN reviewer_id BIGINT NULL,
    ADD CONSTRAINT fk_topics_reviewer FOREIGN KEY (reviewer_id) REFERENCES users(id) ON DELETE RESTRICT;
