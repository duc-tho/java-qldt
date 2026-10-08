CREATE TABLE topics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    registration_period_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    CONSTRAINT fk_topics_registration_period FOREIGN KEY (registration_period_id) REFERENCES registration_periods(id) ON DELETE RESTRICT
);
