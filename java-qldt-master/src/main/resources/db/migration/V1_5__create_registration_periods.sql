CREATE TABLE registration_periods (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL,
    period_type VARCHAR(20) NOT NULL,
    lecturer_registration_start TIMESTAMP NOT NULL,
    lecturer_registration_end TIMESTAMP NOT NULL,
    topic_publication_at TIMESTAMP NOT NULL,
    student_registration_start TIMESTAMP NOT NULL,
    student_registration_end TIMESTAMP NOT NULL,
    report_deadline TIMESTAMP NOT NULL,
    reviewer_deadline TIMESTAMP NULL,
    council_date TIMESTAMP NULL,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    CONSTRAINT fk_registration_periods_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE RESTRICT
);
