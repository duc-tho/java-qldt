UPDATE departments SET code = 'CNTT', name = 'Khoa Công nghệ thông tin'
WHERE code = 'KTPM' AND name = 'Kỹ thuật phần mềm';

UPDATE departments SET code = 'DDT', name = 'Khoa Điện - Điện tử'
WHERE code = 'HTTT' AND name = 'Hệ thống thông tin';

UPDATE departments SET code = 'CK', name = 'Khoa Cơ khí'
WHERE code = 'KHMT' AND name = 'Khoa học máy tính';

UPDATE departments SET code = 'XD', name = 'Khoa Xây dựng'
WHERE code = 'MMT' AND name = 'Mạng máy tính và truyền thông';

UPDATE departments SET code = 'KT', name = 'Khoa Kinh tế'
WHERE code = 'ATTT' AND name = 'An toàn thông tin';

UPDATE departments SET code = 'NN', name = 'Khoa Ngoại ngữ'
WHERE code = 'TTNT' AND name = 'Trí tuệ nhân tạo';

UPDATE departments SET code = 'LUAT', name = 'Khoa Luật'
WHERE code = 'KHDL' AND name = 'Khoa học dữ liệu';

UPDATE departments SET code = 'KHXHNV', name = 'Khoa Khoa học xã hội và Nhân văn'
WHERE code = 'IOT' AND name = 'Internet vạn vật';

INSERT INTO departments (code, name)
SELECT demo.code, demo.name
FROM (
    SELECT 'CNTT' AS code, 'Khoa Công nghệ thông tin' AS name
    UNION ALL SELECT 'DDT', 'Khoa Điện - Điện tử'
    UNION ALL SELECT 'CK', 'Khoa Cơ khí'
    UNION ALL SELECT 'XD', 'Khoa Xây dựng'
    UNION ALL SELECT 'KT', 'Khoa Kinh tế'
    UNION ALL SELECT 'NN', 'Khoa Ngoại ngữ'
    UNION ALL SELECT 'LUAT', 'Khoa Luật'
    UNION ALL SELECT 'KHXHNV', 'Khoa Khoa học xã hội và Nhân văn'
) demo
LEFT JOIN departments existing ON existing.code = demo.code OR existing.name = demo.name
WHERE existing.id IS NULL;

UPDATE users SET full_name = 'Trưởng khoa'
WHERE username = 'demo_truongbomon'
  AND email = 'truongbomon@gmail.com'
  AND full_name = 'Trưởng bộ môn';

UPDATE users SET user_code = 'DEMO_TK'
WHERE username = 'demo_truongbomon'
  AND email = 'truongbomon@gmail.com'
  AND user_code = 'DEMO_TBM';

UPDATE users SET username = 'demo_truongkhoa', email = 'truongkhoa@gmail.com'
WHERE username = 'demo_truongbomon'
  AND email = 'truongbomon@gmail.com';

INSERT INTO users (username, password, email, full_name, user_code)
SELECT demo.username,
       '$2a$10$JN1C.pKfQ4pelXiBkE9u.u1QMWifYKuyCyJcRB3rNhEikIpRpCu2W',
       demo.email,
       demo.full_name,
       demo.user_code
FROM (
    SELECT 'demo_admin' AS username, 'admin@gmail.com' AS email,
           'Quản trị viên' AS full_name, 'DEMO_ADMIN' AS user_code
    UNION ALL SELECT 'demo_truongkhoa', 'truongkhoa@gmail.com',
                     'Trưởng khoa', 'DEMO_TK'
    UNION ALL SELECT 'demo_giangvien', 'giangvien@gmail.com',
                     'Giảng viên', 'DEMO_GV'
    UNION ALL SELECT 'demo_sinhvien1', 'sinhvien1@gmail.com',
                     'Sinh viên 1', 'DEMO_SV1'
    UNION ALL SELECT 'demo_sinhvien2', 'sinhvien2@gmail.com',
                     'Sinh viên 2', 'DEMO_SV2'
) demo
LEFT JOIN users existing ON existing.email = demo.email
WHERE existing.id IS NULL;

INSERT INTO user_roles (user_id, role_id)
SELECT demo_user.id, role.id
FROM users demo_user
JOIN roles role ON role.name = CASE demo_user.username
    WHEN 'demo_admin' THEN 'ADMIN'
    WHEN 'demo_truongkhoa' THEN 'HEAD_OF_FACULTY'
    WHEN 'demo_giangvien' THEN 'LECTURER'
    WHEN 'demo_sinhvien1' THEN 'STUDENT'
    WHEN 'demo_sinhvien2' THEN 'STUDENT'
END
WHERE demo_user.username IN (
    'demo_admin', 'demo_truongkhoa', 'demo_giangvien',
    'demo_sinhvien1', 'demo_sinhvien2'
)
AND demo_user.email = CASE demo_user.username
    WHEN 'demo_admin' THEN 'admin@gmail.com'
    WHEN 'demo_truongkhoa' THEN 'truongkhoa@gmail.com'
    WHEN 'demo_giangvien' THEN 'giangvien@gmail.com'
    WHEN 'demo_sinhvien1' THEN 'sinhvien1@gmail.com'
    WHEN 'demo_sinhvien2' THEN 'sinhvien2@gmail.com'
END
AND NOT EXISTS (
    SELECT 1 FROM user_roles assigned
    WHERE assigned.user_id = demo_user.id AND assigned.role_id = role.id
);
