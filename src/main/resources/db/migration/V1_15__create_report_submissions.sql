CREATE TABLE report_submissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    topic_registration_id BIGINT NOT NULL,
    submitted_by BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    replaced_submission_id BIGINT NULL,
    submitted_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    CONSTRAINT fk_report_submissions_registration FOREIGN KEY (topic_registration_id) REFERENCES topic_registrations(id) ON DELETE RESTRICT,
    CONSTRAINT fk_report_submissions_submitter FOREIGN KEY (submitted_by) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_report_submissions_replaced FOREIGN KEY (replaced_submission_id) REFERENCES report_submissions(id) ON DELETE RESTRICT,
    INDEX idx_report_submissions_registration_status (topic_registration_id, status),
    INDEX idx_report_submissions_registration_submitted_at (topic_registration_id, submitted_at)
);

CREATE TABLE report_files (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    report_submission_id BIGINT NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    storage_path VARCHAR(255) NOT NULL,
    file_type VARCHAR(10) NOT NULL,
    bytes BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    CONSTRAINT fk_report_files_submission FOREIGN KEY (report_submission_id) REFERENCES report_submissions(id) ON DELETE RESTRICT,
    INDEX idx_report_files_submission (report_submission_id)
);
