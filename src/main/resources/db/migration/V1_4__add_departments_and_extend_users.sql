ALTER TABLE users
    ADD COLUMN user_code VARCHAR(30) NULL UNIQUE;

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
