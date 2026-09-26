ALTER TABLE topics
    ADD COLUMN department_id BIGINT NOT NULL,
    ADD CONSTRAINT fk_topics_department
        FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE RESTRICT;

CREATE TABLE topic_advisors (
    topic_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (topic_id, user_id),
    CONSTRAINT fk_topic_advisors_topic FOREIGN KEY (topic_id) REFERENCES topics(id) ON DELETE RESTRICT,
    CONSTRAINT fk_topic_advisors_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT
);
