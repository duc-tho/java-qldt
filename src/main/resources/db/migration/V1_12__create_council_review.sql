CREATE TABLE councils (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    registration_period_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    CONSTRAINT fk_councils_period FOREIGN KEY (registration_period_id) REFERENCES registration_periods(id) ON DELETE RESTRICT,
    CONSTRAINT fk_councils_creator FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE RESTRICT
);

CREATE TABLE council_members (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    council_id BIGINT NOT NULL,
    lecturer_id BIGINT NOT NULL,
    member_role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    CONSTRAINT uq_council_members_council_lecturer UNIQUE (council_id, lecturer_id),
    CONSTRAINT fk_council_members_council FOREIGN KEY (council_id) REFERENCES councils(id) ON DELETE RESTRICT,
    CONSTRAINT fk_council_members_lecturer FOREIGN KEY (lecturer_id) REFERENCES users(id) ON DELETE RESTRICT
);

ALTER TABLE topics
    ADD COLUMN council_id BIGINT NULL,
    ADD CONSTRAINT fk_topics_council FOREIGN KEY (council_id) REFERENCES councils(id) ON DELETE RESTRICT;

CREATE TABLE evaluations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    topic_registration_id BIGINT NOT NULL,
    evaluator_id BIGINT NOT NULL,
    score DECIMAL(4,2) NULL,
    comment TEXT NULL,
    status VARCHAR(20) NOT NULL,
    submitted_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    CONSTRAINT uq_evaluations_registration_evaluator UNIQUE (topic_registration_id, evaluator_id),
    CONSTRAINT fk_evaluations_registration FOREIGN KEY (topic_registration_id) REFERENCES topic_registrations(id) ON DELETE RESTRICT,
    CONSTRAINT fk_evaluations_evaluator FOREIGN KEY (evaluator_id) REFERENCES users(id) ON DELETE RESTRICT
);

CREATE TABLE final_results (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    topic_registration_id BIGINT NOT NULL,
    final_score DECIMAL(4,2) NOT NULL,
    summary_comment TEXT NULL,
    status VARCHAR(20) NOT NULL,
    finalized_by BIGINT NULL,
    finalized_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    CONSTRAINT uq_final_results_registration UNIQUE (topic_registration_id),
    CONSTRAINT fk_final_results_registration FOREIGN KEY (topic_registration_id) REFERENCES topic_registrations(id) ON DELETE RESTRICT,
    CONSTRAINT fk_final_results_finalizer FOREIGN KEY (finalized_by) REFERENCES users(id) ON DELETE RESTRICT
);

CREATE TABLE result_publications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    registration_period_id BIGINT NOT NULL,
    action VARCHAR(20) NOT NULL,
    reason VARCHAR(500) NULL,
    performed_by BIGINT NOT NULL,
    performed_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    CONSTRAINT fk_result_publications_period FOREIGN KEY (registration_period_id) REFERENCES registration_periods(id) ON DELETE RESTRICT,
    CONSTRAINT fk_result_publications_performer FOREIGN KEY (performed_by) REFERENCES users(id) ON DELETE RESTRICT
);
