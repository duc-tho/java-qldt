CREATE TABLE departments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL
);

ALTER TABLE users
    ADD COLUMN user_code VARCHAR(30) NULL UNIQUE,
    ADD COLUMN department_id BIGINT NULL,
    ADD CONSTRAINT fk_users_department
        FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE RESTRICT;

INSERT INTO roles (name)
SELECT 'ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ADMIN');

INSERT INTO roles (name)
SELECT 'HEAD_OF_FACULTY'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'HEAD_OF_FACULTY');

INSERT INTO roles (name)
SELECT 'LECTURER'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'LECTURER');

INSERT INTO roles (name)
SELECT 'STUDENT'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'STUDENT');

INSERT INTO user_roles (user_id, role_id)
SELECT users.id, roles.id
FROM users
JOIN roles ON roles.name = 'ADMIN'
WHERE users.email = 'admin@qldt.local'
  AND NOT EXISTS (
      SELECT 1
      FROM user_roles
      WHERE user_roles.user_id = users.id
        AND user_roles.role_id = roles.id
  );
