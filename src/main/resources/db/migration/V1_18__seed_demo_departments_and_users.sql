INSERT INTO departments (code, name)
SELECT demo.code, demo.name
FROM (
    SELECT 'KHMT' AS code, 'Khoa học máy tính' AS name
    UNION ALL SELECT 'KTPM', 'Kỹ thuật phần mềm'
    UNION ALL SELECT 'MMT', 'Mạng máy tính'
    UNION ALL SELECT 'ATTT', 'An toàn thông tin'
) demo
LEFT JOIN departments existing ON existing.code = demo.code OR existing.name = demo.name
WHERE existing.id IS NULL;

UPDATE users
SET password = '$2a$10$JN1C.pKfQ4pelXiBkE9u.u1QMWifYKuyCyJcRB3rNhEikIpRpCu2W',
    email = 'admin@mail.com',
    phone = '0901000001',
    full_name = 'Nguyễn Minh Quân',
    user_code = 'ADMIN001'
WHERE username = 'admin' AND email = 'admin@qldt.local';

INSERT INTO users (username, password, email, phone, full_name, user_code)
SELECT demo.username,
       '$2a$10$JN1C.pKfQ4pelXiBkE9u.u1QMWifYKuyCyJcRB3rNhEikIpRpCu2W',
       demo.email,
       demo.phone,
       demo.full_name,
       demo.user_code
FROM (
    SELECT 'admin' AS username, 'admin@mail.com' AS email, '0901000001' AS phone,
           'Nguyễn Minh Quân' AS full_name, 'ADMIN001' AS user_code
    UNION ALL SELECT 'tk', 'tk@mail.com', '0901000002', 'Trần Văn Hùng', 'TK001'
    UNION ALL SELECT 'gv1', 'gv1@mail.com', '0901000003', 'Nguyễn Văn An', 'GV001'
    UNION ALL SELECT 'gv2', 'gv2@mail.com', '0901000004', 'Trần Thị Lan', 'GV002'
    UNION ALL SELECT 'gv3', 'gv3@mail.com', '0901000005', 'Lê Minh Tuấn', 'GV003'
    UNION ALL SELECT 'gv4', 'gv4@mail.com', '0901000006', 'Phạm Thu Hà', 'GV004'
    UNION ALL SELECT 'gv5', 'gv5@mail.com', '0901000007', 'Hoàng Quốc Bảo', 'GV005'
    UNION ALL SELECT 'gv6', 'gv6@mail.com', '0901000008', 'Võ Ngọc Mai', 'GV006'
    UNION ALL SELECT 'gv7', 'gv7@mail.com', '0901000009', 'Đặng Thành Nam', 'GV007'
    UNION ALL SELECT 'gv8', 'gv8@mail.com', '0901000010', 'Bùi Thanh Thảo', 'GV008'
    UNION ALL SELECT 'gv9', 'gv9@mail.com', '0901000011', 'Đỗ Anh Khoa', 'GV009'
    UNION ALL SELECT 'gv10', 'gv10@mail.com', '0901000012', 'Phan Mỹ Linh', 'GV010'
    UNION ALL SELECT 'sv1', 'sv1@mail.com', '0901000013', 'Nguyễn Hoàng Anh', 'SV001'
    UNION ALL SELECT 'sv2', 'sv2@mail.com', '0901000014', 'Trần Minh Đức', 'SV002'
    UNION ALL SELECT 'sv3', 'sv3@mail.com', '0901000015', 'Lê Ngọc Anh', 'SV003'
    UNION ALL SELECT 'sv4', 'sv4@mail.com', '0901000016', 'Phạm Gia Huy', 'SV004'
    UNION ALL SELECT 'sv5', 'sv5@mail.com', '0901000017', 'Hoàng Thảo Vy', 'SV005'
    UNION ALL SELECT 'sv6', 'sv6@mail.com', '0901000018', 'Võ Thành Đạt', 'SV006'
    UNION ALL SELECT 'sv7', 'sv7@mail.com', '0901000019', 'Đặng Minh Châu', 'SV007'
    UNION ALL SELECT 'sv8', 'sv8@mail.com', '0901000020', 'Bùi Quốc Khánh', 'SV008'
    UNION ALL SELECT 'sv9', 'sv9@mail.com', '0901000021', 'Đỗ Phương Nhi', 'SV009'
    UNION ALL SELECT 'sv10', 'sv10@mail.com', '0901000022', 'Phan Đức Anh', 'SV010'
    UNION ALL SELECT 'sv11', 'sv11@mail.com', '0901000023', 'Nguyễn Tuấn Kiệt', 'SV011'
    UNION ALL SELECT 'sv12', 'sv12@mail.com', '0901000024', 'Trần Khánh Linh', 'SV012'
    UNION ALL SELECT 'sv13', 'sv13@mail.com', '0901000025', 'Lê Quốc Việt', 'SV013'
    UNION ALL SELECT 'sv14', 'sv14@mail.com', '0901000026', 'Phạm Ngọc Hân', 'SV014'
    UNION ALL SELECT 'sv15', 'sv15@mail.com', '0901000027', 'Hoàng Minh Nhật', 'SV015'
    UNION ALL SELECT 'sv16', 'sv16@mail.com', '0901000028', 'Võ Thanh Tâm', 'SV016'
    UNION ALL SELECT 'sv17', 'sv17@mail.com', '0901000029', 'Đặng Gia Bảo', 'SV017'
    UNION ALL SELECT 'sv18', 'sv18@mail.com', '0901000030', 'Bùi Hải Yến', 'SV018'
    UNION ALL SELECT 'sv19', 'sv19@mail.com', '0901000031', 'Đỗ Minh Khôi', 'SV019'
    UNION ALL SELECT 'sv20', 'sv20@mail.com', '0901000032', 'Phan Thùy Dương', 'SV020'
    UNION ALL SELECT 'sv21', 'sv21@mail.com', '0901000033', 'Nguyễn Đức Thịnh', 'SV021'
    UNION ALL SELECT 'sv22', 'sv22@mail.com', '0901000034', 'Trần Bảo Ngọc', 'SV022'
    UNION ALL SELECT 'sv23', 'sv23@mail.com', '0901000035', 'Lê Anh Thư', 'SV023'
    UNION ALL SELECT 'sv24', 'sv24@mail.com', '0901000036', 'Phạm Hoàng Long', 'SV024'
    UNION ALL SELECT 'sv25', 'sv25@mail.com', '0901000037', 'Hoàng Kim Ngân', 'SV025'
    UNION ALL SELECT 'sv26', 'sv26@mail.com', '0901000038', 'Võ Minh Trí', 'SV026'
    UNION ALL SELECT 'sv27', 'sv27@mail.com', '0901000039', 'Đặng Ngọc Diệp', 'SV027'
    UNION ALL SELECT 'sv28', 'sv28@mail.com', '0901000040', 'Bùi Nhật Minh', 'SV028'
    UNION ALL SELECT 'sv29', 'sv29@mail.com', '0901000041', 'Đỗ Thanh Huyền', 'SV029'
    UNION ALL SELECT 'sv30', 'sv30@mail.com', '0901000042', 'Phan Quốc Thịnh', 'SV030'
) demo
LEFT JOIN users existing ON existing.username = demo.username
    OR existing.email = demo.email
    OR existing.phone = demo.phone
    OR existing.user_code = demo.user_code
WHERE existing.id IS NULL;

INSERT INTO user_roles (user_id, role_id)
SELECT demo_user.id, role.id
FROM users demo_user
JOIN roles role ON role.name = CASE demo_user.username
    WHEN 'admin' THEN 'ADMIN'
    WHEN 'tk' THEN 'HEAD_OF_FACULTY'
    ELSE CASE
        WHEN demo_user.username LIKE 'gv%' THEN 'LECTURER'
        WHEN demo_user.username LIKE 'sv%' THEN 'STUDENT'
    END
END
WHERE (
    (demo_user.username = 'admin' AND demo_user.email = 'admin@mail.com')
    OR (demo_user.username = 'tk' AND demo_user.email = 'tk@mail.com')
    OR (demo_user.username REGEXP '^gv([1-9]|10)$'
        AND demo_user.email = CONCAT(demo_user.username, '@mail.com'))
    OR (demo_user.username REGEXP '^sv([1-9]|[12][0-9]|30)$'
        AND demo_user.email = CONCAT(demo_user.username, '@mail.com'))
)
AND NOT EXISTS (
    SELECT 1 FROM user_roles assigned
    WHERE assigned.user_id = demo_user.id AND assigned.role_id = role.id
);
