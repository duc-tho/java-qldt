# Thiết kế cơ sở dữ liệu

## 1. Mục tiêu

Thiết kế này đáp ứng các yêu cầu trong `00-overview.md`, `01-requirements.md` và các quyết định đã chốt trong `clarifications.md`.

Các nguyên tắc chính:

- Chỉ lưu dữ liệu phục vụ nghiệp vụ đã được xác định trong đặc tả.
- Giữ mô hình đủ chuẩn hóa để có khóa ngoại và tránh dữ liệu trùng lặp, nhưng không chia nhỏ bảng quá mức.
- Không tạo bảng riêng cho sinh viên và giảng viên. Tất cả đều là `users` và được phân biệt bằng role.
- Giữ nguyên các bảng xác thực và phân quyền hiện có.
- Dữ liệu cần audit được giữ bằng bản ghi lịch sử hoặc phiên bản; không xóa cứng lịch sử nghiệp vụ.
- Tệp báo cáo nằm trên filesystem; cơ sở dữ liệu chỉ lưu metadata và đường dẫn.
- Tên bảng và cột dùng tiếng Anh đầy đủ, dễ đọc, không dùng tên viết tắt khó hiểu.

Sơ đồ quan hệ được trình bày tại [erd.md](erd.md).

## 2. Quy ước chung

### 2.1. Kiểu khóa và thời gian

- Khóa chính dùng `BIGINT AUTO_INCREMENT`.
- Khóa ngoại dùng cùng kiểu `BIGINT`.
- Tiền tố tên bảng không được sử dụng.
- Các cột thời gian dùng `TIMESTAMP`.
- Ứng dụng và kết nối MariaDB phải sử dụng thống nhất múi giờ `Asia/Ho_Chi_Minh` khi nhập, so sánh và hiển thị thời gian.
- Các bảng chính có `created_at`; bảng được phép cập nhật có thêm `updated_at`.

### 2.2. Enum nghiệp vụ

Các enum được khai báo trong Java và lưu bằng chuỗi `VARCHAR`, không dùng ordinal. Cách này làm dữ liệu dễ đọc khi kiểm tra trực tiếp trong database.

Các nhóm giá trị chính:

| Cột                   | Giá trị                                                   |
| --------------------- | --------------------------------------------------------- |
| `period_type`         | `MON_HOC`, `NCKH`, `TLCN`, `KLTN`                         |
| `period_change_type`  | `UPDATED`, `EXTENDED`, `REOPENED`                         |
| `topic_status`        | `DRAFT`, `SUBMITTED`, `APPROVED`, `REJECTED`, `PUBLISHED` |
| `topic_review_action` | `APPROVED`, `REJECTED`, `RESET_FOR_REVIEW`, `PUBLISHED`   |
| `advisor_type`        | `RESPONSIBLE_LECTURER`, `SUPERVISOR`                      |
| `group_request_type`  | `INVITATION`, `JOIN_REQUEST`                              |
| `request_status`      | `PENDING`, `ACCEPTED`, `REJECTED`, `CANCELLED`            |
| `registration_status` | `PENDING`, `APPROVED`, `REJECTED`, `WITHDRAWN`            |
| `submission_status`   | `ON_TIME`, `LATE`, `REPLACED`                             |
| `council_member_role` | `CHAIR`, `SECRETARY`, `MEMBER`                            |
| `evaluation_status`   | `DRAFT`, `SUBMITTED`, `INVALIDATED`                       |
| `result_status`       | `CALCULATED`, `FINALIZED`                                 |
| `publication_action`  | `PUBLISHED`, `REVOKED`                                    |
| `announcement_status` | `PUBLISHED`, `WITHDRAWN`                                  |

Các giá trị enum chỉ mô tả trạng thái đã có trong nghiệp vụ, không tạo thêm quy trình ngoài đặc tả.

### 2.3. Xóa dữ liệu

- Các bảng auth hiện có tiếp tục dùng `deleted_at` theo `SoftDeleteEntity`.
- Không bổ sung `deleted_at` hàng loạt cho các bảng nghiệp vụ.
- Trạng thái nghiệp vụ như rút đăng ký, thay thế báo cáo, vô hiệu đánh giá hoặc thu hồi công bố được lưu bằng status tương ứng.
- Bản ghi lịch sử không có chức năng xóa từ giao diện.

## 3. Danh tính và tổ chức

### 3.1. `departments`

Danh mục bộ môn được dùng làm dữ liệu tham chiếu cho người dùng và đề tài. Đặc tả không yêu cầu màn hình CRUD bộ môn; dữ liệu có thể được seed bằng Flyway.

| Cột          | Kiểu           | Ràng buộc          | Ý nghĩa            |
| ------------ | -------------- | ------------------ | ------------------ |
| `id`         | `BIGINT`       | PK, auto increment | Khóa chính         |
| `code`       | `VARCHAR(20)`  | NOT NULL, UNIQUE   | Mã bộ môn          |
| `name`       | `VARCHAR(100)` | NOT NULL, UNIQUE   | Tên bộ môn         |
| `created_at` | `TIMESTAMP`    | NOT NULL           | Thời điểm tạo      |
| `updated_at` | `TIMESTAMP`    | NULL               | Thời điểm cập nhật |

### 3.2. `users`

Giữ bảng hiện có và bổ sung mã người dùng cùng bộ môn. Sinh viên, giảng viên, Trưởng khoa và Admin đều dùng chung bảng này.

| Cột             | Kiểu           | Ràng buộc                   | Ý nghĩa                                    |
| --------------- | -------------- | --------------------------- | ------------------------------------------ |
| `id`            | `BIGINT`       | PK, auto increment          | Khóa chính                                 |
| `username`      | `VARCHAR(50)`  | NOT NULL, UNIQUE            | Tên đăng nhập                              |
| `password`      | `VARCHAR(255)` | NOT NULL                    | Mật khẩu đã mã hóa                         |
| `email`         | `VARCHAR(50)`  | NOT NULL, UNIQUE            | Email liên hệ                              |
| `phone`         | `VARCHAR(20)`  | NULL, UNIQUE                | Số điện thoại                              |
| `full_name`     | `VARCHAR(50)`  | NOT NULL                    | Họ tên                                     |
| `user_code`     | `VARCHAR(30)`  | NULL, UNIQUE                | Mã SV hoặc mã GV; Admin có thể không có mã |
| `department_id` | `BIGINT`       | NULL, FK → `departments.id` | Bộ môn của người dùng                      |
| `created_at`    | `TIMESTAMP`    | NOT NULL                    | Thời điểm tạo                              |
| `updated_at`    | `TIMESTAMP`    | NULL                        | Thời điểm cập nhật                         |
| `deleted_at`    | `TIMESTAMP`    | NULL                        | Thời điểm khóa/xóa mềm                     |

Quy tắc nghiệp vụ:

- Admin cấp tài khoản và gán role.
- Người dùng được đổi mật khẩu, email và số điện thoại.
- Người dùng không tự sửa `user_code`, `department_id` hoặc role.
- Không tạo bảng `students` và `lecturers`; role trong `user_roles` xác định chức năng của tài khoản.

### 3.3. Các bảng phân quyền hiện có

Giữ nguyên cấu trúc:

- `roles(id, name, created_at, updated_at, deleted_at)`
- `user_roles(user_id, role_id)`
- `permissions(id, name, created_at, updated_at, deleted_at)`
- `role_permissions(role_id, permission_id)`

Các role nghiệp vụ cần seed:

- `ADMIN`
- `HEAD_OF_FACULTY`
- `LECTURER`
- `STUDENT`

Chức trách Nhóm trưởng, GVHD, GV phụ trách, Chủ tịch và Thư ký được xác định theo dữ liệu nghiệp vụ, không tạo role tài khoản riêng.

## 4. Đợt đăng ký

### 4.1. `registration_periods`

Lưu một đợt đăng ký cho một trong bốn loại đề tài.

| Cột                           | Kiểu           | Ràng buộc                 | Ý nghĩa                                      |
| ----------------------------- | -------------- | ------------------------- | -------------------------------------------- |
| `id`                          | `BIGINT`       | PK, auto increment        | Khóa chính                                   |
| `name`                        | `VARCHAR(150)` | NOT NULL                  | Tên đợt                                      |
| `period_type`                 | `VARCHAR(20)`  | NOT NULL                  | `MON_HOC`, `NCKH`, `TLCN` hoặc `KLTN`        |
| `lecturer_registration_start` | `TIMESTAMP`    | NOT NULL                  | Bắt đầu GV đề xuất đề tài                    |
| `lecturer_registration_end`   | `TIMESTAMP`    | NOT NULL                  | Kết thúc GV đề xuất đề tài                   |
| `topic_publication_at`        | `TIMESTAMP`    | NOT NULL                  | Thời điểm tự động công bố đề tài đã duyệt    |
| `student_registration_start`  | `TIMESTAMP`    | NOT NULL                  | Bắt đầu SV đăng ký                           |
| `student_registration_end`    | `TIMESTAMP`    | NOT NULL                  | Kết thúc SV đăng ký                          |
| `report_deadline`             | `TIMESTAMP`    | NOT NULL                  | Hạn nhóm nộp báo cáo                         |
| `reviewer_deadline`           | `TIMESTAMP`    | NULL                      | Hạn phản biện; bắt buộc với TLCN/KLTN        |
| `council_date`                | `TIMESTAMP`    | NULL                      | Ngày hội đồng; chỉ dùng và bắt buộc với KLTN |
| `created_by`                  | `BIGINT`       | NOT NULL, FK → `users.id` | Trưởng khoa tạo đợt                          |
| `created_at`                  | `TIMESTAMP`    | NOT NULL                  | Thời điểm tạo                                |
| `updated_at`                  | `TIMESTAMP`    | NULL                      | Thời điểm cập nhật                           |

Validation tại service:

- `lecturer_registration_start <= lecturer_registration_end`.
- `lecturer_registration_end < topic_publication_at < student_registration_start`.
- `student_registration_start <= student_registration_end`.
- TLCN/KLTN bắt buộc có `reviewer_deadline`.
- Môn học/NCKH không được có `reviewer_deadline`.
- KLTN bắt buộc có `council_date`, đồng thời `student_registration_end < council_date` và `reviewer_deadline < council_date`.
- Loại khác KLTN không được có `council_date`.
- Không đổi `period_type` sau khi đợt đã phát sinh đăng ký.

### 4.2. `registration_period_changes`

Lưu lịch sử sửa thời gian, gia hạn và mở lại đợt. Một thay đổi nhiều trường tạo nhiều dòng với cùng thời điểm thao tác.

| Cột                      | Kiểu           | Ràng buộc                                | Ý nghĩa             |
| ------------------------ | -------------- | ---------------------------------------- | ------------------- |
| `id`                     | `BIGINT`       | PK, auto increment                       | Khóa chính          |
| `registration_period_id` | `BIGINT`       | NOT NULL, FK → `registration_periods.id` | Đợt được thay đổi   |
| `change_type`            | `VARCHAR(20)`  | NOT NULL                                 | Loại thay đổi       |
| `field_name`             | `VARCHAR(50)`  | NOT NULL                                 | Tên trường thay đổi |
| `old_value`              | `VARCHAR(100)` | NULL                                     | Giá trị cũ          |
| `new_value`              | `VARCHAR(100)` | NULL                                     | Giá trị mới         |
| `changed_by`             | `BIGINT`       | NOT NULL, FK → `users.id`                | Người thực hiện     |
| `changed_at`             | `TIMESTAMP`    | NOT NULL                                 | Thời điểm thực hiện |

## 5. Đề tài trong đợt

### 5.1. `topics`

Lưu mẫu nội dung đề tài có thể được tái sử dụng ở nhiều đợt.

| Cột             | Kiểu           | Ràng buộc                       | Ý nghĩa                            |
| --------------- | -------------- | ------------------------------- | ---------------------------------- |
| `id`            | `BIGINT`       | PK, auto increment              | Khóa chính                         |
| `title`         | `VARCHAR(255)` | NOT NULL                        | Tên đề tài; không yêu cầu duy nhất |
| `description`   | `TEXT`         | NOT NULL                        | Nội dung đề tài                    |
| `department_id` | `BIGINT`       | NOT NULL, FK → `departments.id` | Bộ môn quản lý đề tài              |
| `created_at`    | `TIMESTAMP`    | NOT NULL                        | Thời điểm tạo                      |
| `updated_at`    | `TIMESTAMP`    | NULL                            | Thời điểm cập nhật                 |

### 5.2. `period_topics`

Liên kết đề tài với một đợt và lưu dữ liệu chỉ có ý nghĩa trong đợt đó. Tên, nội dung và bộ môn được sao chép từ `topics` khi đưa đề tài vào đợt để dữ liệu của đợt cũ không thay đổi khi mẫu được tái sử dụng ở đợt mới.

| Cột                      | Kiểu           | Ràng buộc                                | Ý nghĩa                                    |
| ------------------------ | -------------- | ---------------------------------------- | ------------------------------------------ |
| `id`                     | `BIGINT`       | PK, auto increment                       | Khóa chính                                 |
| `registration_period_id` | `BIGINT`       | NOT NULL, FK → `registration_periods.id` | Đợt đăng ký                                |
| `topic_id`               | `BIGINT`       | NOT NULL, FK → `topics.id`               | Nội dung đề tài                            |
| `title`                  | `VARCHAR(255)` | NOT NULL                                 | Tên đề tài trong đợt                       |
| `description`            | `TEXT`         | NOT NULL                                 | Nội dung đề tài trong đợt                  |
| `department_id`          | `BIGINT`       | NOT NULL, FK → `departments.id`          | Bộ môn của đề tài trong đợt                |
| `proposed_by`            | `BIGINT`       | NOT NULL, FK → `users.id`                | Giảng viên đề xuất                         |
| `max_groups`             | `INT`          | NOT NULL, DEFAULT 1                      | Số nhóm tối đa trong đợt                   |
| `status`                 | `VARCHAR(20)`  | NOT NULL                                 | Trạng thái đề xuất/duyệt/công bố           |
| `approved_by`            | `BIGINT`       | NULL, FK → `users.id`                    | Trưởng khoa duyệt gần nhất                 |
| `approved_at`            | `TIMESTAMP`    | NULL                                     | Thời điểm duyệt gần nhất                   |
| `published_at`           | `TIMESTAMP`    | NULL                                     | Thời điểm hệ thống công bố                 |
| `council_id`             | `BIGINT`       | NULL, FK → `councils.id`                 | Hội đồng phụ trách; không dùng cho môn học |
| `created_at`             | `TIMESTAMP`    | NOT NULL                                 | Thời điểm đưa đề tài vào đợt               |
| `updated_at`             | `TIMESTAMP`    | NULL                                     | Thời điểm cập nhật                         |

Ràng buộc:

- UNIQUE (`registration_period_id`, `topic_id`).
- `max_groups >= 1`.
- Chỉ đề tài `APPROVED` mới được hệ thống chuyển sang `PUBLISHED` tại `topic_publication_at`.
- Việc sửa dữ liệu trong một đợt không tự động thay đổi các đợt khác cùng tham chiếu `topic_id`.
- Sau khi có đăng ký nhóm `APPROVED`, không sửa `title`, `description`, `department_id` hoặc `max_groups` của `period_topics`.

### 5.3. `topic_advisors`

Lưu GV phụ trách đối với môn học hoặc GVHD đối với NCKH/TLCN/KLTN. Khi Trưởng khoa điều chỉnh phân công, bản ghi cũ được kết thúc thay vì xóa.

| Cột               | Kiểu          | Ràng buộc                         | Ý nghĩa                                   |
| ----------------- | ------------- | --------------------------------- | ----------------------------------------- |
| `id`              | `BIGINT`      | PK, auto increment                | Khóa chính                                |
| `period_topic_id` | `BIGINT`      | NOT NULL, FK → `period_topics.id` | Đề tài trong đợt                          |
| `lecturer_id`     | `BIGINT`      | NOT NULL, FK → `users.id`         | Giảng viên được phân công                 |
| `advisor_type`    | `VARCHAR(30)` | NOT NULL                          | GV phụ trách hoặc GVHD                    |
| `assigned_by`     | `BIGINT`      | NOT NULL, FK → `users.id`         | Người phân công                           |
| `assigned_at`     | `TIMESTAMP`   | NOT NULL                          | Bắt đầu phân công                         |
| `ended_at`        | `TIMESTAMP`   | NULL                              | Kết thúc phân công; NULL là đang hiệu lực |

Service phải bảo đảm đề tài môn học có đúng một GV phụ trách đang hiệu lực; đề tài NCKH/TLCN/KLTN có 1–2 GVHD đang hiệu lực.

### 5.4. `topic_reviews`

Lưu lịch sử duyệt, từ chối, đưa về chờ duyệt và công bố đề tài.

| Cột               | Kiểu           | Ràng buộc                         | Ý nghĩa                                       |
| ----------------- | -------------- | --------------------------------- | --------------------------------------------- |
| `id`              | `BIGINT`       | PK, auto increment                | Khóa chính                                    |
| `period_topic_id` | `BIGINT`       | NOT NULL, FK → `period_topics.id` | Đề tài được xử lý                             |
| `action`          | `VARCHAR(30)`  | NOT NULL                          | Hành động                                     |
| `reason`          | `VARCHAR(500)` | NULL                              | Lý do từ chối hoặc đưa về duyệt lại           |
| `performed_by`    | `BIGINT`       | NULL, FK → `users.id`             | Người thực hiện; NULL nếu hệ thống tự công bố |
| `performed_at`    | `TIMESTAMP`    | NOT NULL                          | Thời điểm thực hiện                           |

## 6. Nhóm sinh viên và đăng ký đề tài

### 6.1. `student_groups`

Mỗi nhóm thuộc đúng một đợt. Nhóm không cần tên riêng theo đặc tả.

| Cột                      | Kiểu        | Ràng buộc                                | Ý nghĩa                                                    |
| ------------------------ | ----------- | ---------------------------------------- | ---------------------------------------------------------- |
| `id`                     | `BIGINT`    | PK, auto increment                       | Khóa chính                                                 |
| `registration_period_id` | `BIGINT`    | NOT NULL, FK → `registration_periods.id` | Đợt của nhóm                                               |
| `created_by`             | `BIGINT`    | NOT NULL, FK → `users.id`                | Sinh viên tạo nhóm và là trưởng nhóm ban đầu               |
| `locked_at`              | `TIMESTAMP` | NULL                                     | Thời điểm khóa cơ cấu nhóm sau khi đăng ký được chấp thuận |
| `created_at`             | `TIMESTAMP` | NOT NULL                                 | Thời điểm tạo                                              |
| `updated_at`             | `TIMESTAMP` | NULL                                     | Thời điểm cập nhật                                         |

### 6.2. `group_members`

| Cột          | Kiểu        | Ràng buộc                    | Ý nghĩa                       |
| ------------ | ----------- | ---------------------------- | ----------------------------- |
| `group_id`   | `BIGINT`    | PK, FK → `student_groups.id` | Nhóm                          |
| `student_id` | `BIGINT`    | PK, FK → `users.id`          | Sinh viên                     |
| `is_leader`  | `BOOLEAN`   | NOT NULL, DEFAULT FALSE      | Có phải nhóm trưởng hay không |
| `joined_at`  | `TIMESTAMP` | NOT NULL                     | Thời điểm tham gia            |

Service phải bảo đảm:

- Nhóm có 1–3 sinh viên.
- Nhóm có đúng một trưởng nhóm.
- Mỗi sinh viên thuộc tối đa một nhóm trong cùng đợt.
- Không thay đổi thành viên hoặc trưởng nhóm sau `student_groups.locked_at`.

### 6.3. `group_requests`

Dùng chung cho lời mời của trưởng nhóm và yêu cầu tham gia của sinh viên.

| Cột            | Kiểu          | Ràng buộc                          | Ý nghĩa                              |
| -------------- | ------------- | ---------------------------------- | ------------------------------------ |
| `id`           | `BIGINT`      | PK, auto increment                 | Khóa chính                           |
| `group_id`     | `BIGINT`      | NOT NULL, FK → `student_groups.id` | Nhóm liên quan                       |
| `student_id`   | `BIGINT`      | NOT NULL, FK → `users.id`          | Sinh viên được mời hoặc xin tham gia |
| `request_type` | `VARCHAR(20)` | NOT NULL                           | Lời mời hoặc yêu cầu tham gia        |
| `status`       | `VARCHAR(20)` | NOT NULL                           | Trạng thái xử lý                     |
| `created_at`   | `TIMESTAMP`   | NOT NULL                           | Thời điểm tạo                        |
| `responded_at` | `TIMESTAMP`   | NULL                               | Thời điểm xử lý                      |

### 6.4. `topic_registrations`

Mỗi lần nhóm gửi, đổi hoặc đăng ký lại tạo một bản ghi mới để giữ lịch sử. Đăng ký cũ chuyển sang `WITHDRAWN` hoặc giữ `REJECTED`.

| Cột                | Kiểu           | Ràng buộc                          | Ý nghĩa                                |
| ------------------ | -------------- | ---------------------------------- | -------------------------------------- |
| `id`               | `BIGINT`       | PK, auto increment                 | Khóa chính                             |
| `group_id`         | `BIGINT`       | NOT NULL, FK → `student_groups.id` | Nhóm đăng ký                           |
| `period_topic_id`  | `BIGINT`       | NOT NULL, FK → `period_topics.id`  | Đề tài được chọn                       |
| `status`           | `VARCHAR(20)`  | NOT NULL                           | Trạng thái đăng ký                     |
| `submitted_at`     | `TIMESTAMP`    | NOT NULL                           | Thời điểm gửi                          |
| `reviewed_by`      | `BIGINT`       | NULL, FK → `users.id`              | GVHD chấp thuận hoặc từ chối           |
| `reviewed_at`      | `TIMESTAMP`    | NULL                               | Thời điểm xử lý                        |
| `rejection_reason` | `VARCHAR(500)` | NULL                               | Lý do từ chối, bắt buộc khi `REJECTED` |
| `withdrawn_at`     | `TIMESTAMP`    | NULL                               | Thời điểm nhóm rút đăng ký             |

Service phải kiểm tra trong cùng transaction:

- Đề tài đã `PUBLISHED`.
- Nhóm và đề tài cùng một đợt.
- Nhóm chưa có đăng ký `PENDING` hoặc `APPROVED` khác.
- Tổng số đăng ký `PENDING` và `APPROVED` của đề tài chưa đạt `max_groups` tại thời điểm gửi.
- Chỉ đăng ký `PENDING` mới được rút hoặc đổi đề tài; đăng ký `APPROVED` không được thu hồi.
- Khi chuyển sang `APPROVED`, khóa cơ cấu nhóm bằng `student_groups.locked_at`.

## 7. Nộp báo cáo

### 7.1. `report_submissions`

Mỗi lần nộp là một bản ghi. Khi thay thế trước hạn, lần cũ chuyển sang `REPLACED` và lần mới tham chiếu bản ghi cũ.

| Cột                      | Kiểu          | Ràng buộc                               | Ý nghĩa                               |
| ------------------------ | ------------- | --------------------------------------- | ------------------------------------- |
| `id`                     | `BIGINT`      | PK, auto increment                      | Khóa chính                            |
| `topic_registration_id`  | `BIGINT`      | NOT NULL, FK → `topic_registrations.id` | Lần thực hiện đề tài của nhóm         |
| `submitted_by`           | `BIGINT`      | NOT NULL, FK → `users.id`               | Nhóm trưởng nộp                       |
| `status`                 | `VARCHAR(20)` | NOT NULL                                | Đúng hạn, trễ hạn hoặc đã bị thay thế |
| `replaces_submission_id` | `BIGINT`      | NULL, FK → `report_submissions.id`      | Lần nộp bị thay thế                   |
| `submitted_at`           | `TIMESTAMP`   | NOT NULL                                | Thời điểm nộp                         |

### 7.2. `report_files`

| Cột                    | Kiểu           | Ràng buộc                              | Ý nghĩa                                 |
| ---------------------- | -------------- | -------------------------------------- | --------------------------------------- |
| `id`                   | `BIGINT`       | PK, auto increment                     | Khóa chính                              |
| `report_submission_id` | `BIGINT`       | NOT NULL, FK → `report_submissions.id` | Lần nộp                                 |
| `original_name`        | `VARCHAR(255)` | NOT NULL                               | Tên tệp người dùng tải lên              |
| `storage_path`         | `VARCHAR(500)` | NOT NULL, UNIQUE                       | Định danh/đường dẫn tệp trên filesystem |
| `file_type`            | `VARCHAR(10)`  | NOT NULL                               | `PDF`, `DOCX`, `ZIP` hoặc `RAR`         |
| `size_bytes`           | `BIGINT`       | NOT NULL                               | Kích thước tệp theo byte                |
| `created_at`           | `TIMESTAMP`    | NOT NULL                               | Thời điểm lưu metadata                  |

Validation tại service trước khi lưu:

- Chỉ nhóm trưởng được nộp.
- Mỗi lần nộp tối đa 10 tệp.
- PDF/DOCX tối đa 20 MB mỗi tệp.
- ZIP/RAR tối đa 50 MB mỗi tệp.
- Sau `report_deadline`, lần nộp mang trạng thái `LATE`.
- Chỉ thay thế lần nộp trước hạn.

## 8. Hội đồng

### 8.1. `councils`

Chỉ dùng cho NCKH, TLCN và KLTN.

| Cột                      | Kiểu           | Ràng buộc                                | Ý nghĩa               |
| ------------------------ | -------------- | ---------------------------------------- | --------------------- |
| `id`                     | `BIGINT`       | PK, auto increment                       | Khóa chính            |
| `registration_period_id` | `BIGINT`       | NOT NULL, FK → `registration_periods.id` | Đợt sử dụng hội đồng  |
| `name`                   | `VARCHAR(150)` | NOT NULL                                 | Tên hội đồng          |
| `created_by`             | `BIGINT`       | NOT NULL, FK → `users.id`                | Trưởng khoa thành lập |
| `created_at`             | `TIMESTAMP`    | NOT NULL                                 | Thời điểm tạo         |
| `updated_at`             | `TIMESTAMP`    | NULL                                     | Thời điểm cập nhật    |

### 8.2. `council_members`

Thành viên cũ được kết thúc bằng `ended_at` để giữ lịch sử phân công.

| Cột           | Kiểu          | Ràng buộc                    | Ý nghĩa                                   |
| ------------- | ------------- | ---------------------------- | ----------------------------------------- |
| `id`          | `BIGINT`      | PK, auto increment           | Khóa chính                                |
| `council_id`  | `BIGINT`      | NOT NULL, FK → `councils.id` | Hội đồng                                  |
| `lecturer_id` | `BIGINT`      | NOT NULL, FK → `users.id`    | Giảng viên tham gia                       |
| `member_role` | `VARCHAR(20)` | NOT NULL                     | Chủ tịch, Thư ký hoặc Thành viên          |
| `joined_at`   | `TIMESTAMP`   | NOT NULL                     | Thời điểm bắt đầu                         |
| `ended_at`    | `TIMESTAMP`   | NULL                         | Thời điểm kết thúc; NULL là đang hiệu lực |

Service phải bảo đảm trước khi đưa hội đồng vào sử dụng:

- Có 3–5 thành viên đang hiệu lực.
- Có đúng một Chủ tịch và một Thư ký khác nhau.
- Mọi thành viên đều có role `LECTURER`.
- Không có thành viên là GVHD của bất kỳ đề tài nào được giao cho hội đồng.

### 8.3. `council_minutes`

Mỗi hội đồng có một biên bản đang được quản lý bởi Thư ký.

| Cột           | Kiểu        | Ràng buộc                            | Ý nghĩa              |
| ------------- | ----------- | ------------------------------------ | -------------------- |
| `id`          | `BIGINT`    | PK, auto increment                   | Khóa chính           |
| `council_id`  | `BIGINT`    | NOT NULL, UNIQUE, FK → `councils.id` | Hội đồng             |
| `content`     | `TEXT`      | NOT NULL                             | Nội dung biên bản    |
| `recorded_by` | `BIGINT`    | NOT NULL, FK → `users.id`            | Thư ký nhập biên bản |
| `created_at`  | `TIMESTAMP` | NOT NULL                             | Thời điểm tạo        |
| `updated_at`  | `TIMESTAMP` | NULL                                 | Thời điểm cập nhật   |

## 9. Đánh giá và kết quả

### 9.1. `evaluations`

Điểm gắn với `topic_registrations`, tức lần một nhóm thực hiện một đề tài trong một đợt. Không gắn trực tiếp với nội dung `topics` dùng chung.

Khi Trưởng khoa mở lại đánh giá, bản đã gửi được chuyển sang `INVALIDATED`; người chấm tạo phiên bản mới và `previous_evaluation_id` trỏ về phiên bản cũ.

| Cột                      | Kiểu           | Ràng buộc                               | Ý nghĩa                          |
| ------------------------ | -------------- | --------------------------------------- | -------------------------------- |
| `id`                     | `BIGINT`       | PK, auto increment                      | Khóa chính                       |
| `topic_registration_id`  | `BIGINT`       | NOT NULL, FK → `topic_registrations.id` | Nhóm/đề tài được chấm            |
| `evaluator_id`           | `BIGINT`       | NOT NULL, FK → `users.id`               | Người chấm                       |
| `score`                  | `DECIMAL(4,2)` | NULL                                    | Điểm từ 0 đến 10                 |
| `comment`                | `TEXT`         | NULL                                    | Nhận xét nội bộ của người chấm   |
| `status`                 | `VARCHAR(20)`  | NOT NULL                                | Nháp, đã gửi hoặc vô hiệu        |
| `previous_evaluation_id` | `BIGINT`       | NULL, FK → `evaluations.id`             | Phiên bản trước                  |
| `submitted_at`           | `TIMESTAMP`    | NULL                                    | Thời điểm gửi                    |
| `invalidated_by`         | `BIGINT`       | NULL, FK → `users.id`                   | Trưởng khoa vô hiệu/mở lại       |
| `invalidated_at`         | `TIMESTAMP`    | NULL                                    | Thời điểm vô hiệu                |
| `invalidation_reason`    | `VARCHAR(500)` | NULL                                    | Lý do mở lại hoặc đổi người chấm |
| `created_at`             | `TIMESTAMP`    | NOT NULL                                | Thời điểm tạo phiên bản          |
| `updated_at`             | `TIMESTAMP`    | NULL                                    | Thời điểm sửa bản nháp           |

Validation:

- `score` nằm trong khoảng 0–10 khi gửi.
- Môn học chỉ nhận đánh giá của GV phụ trách đang hiệu lực.
- NCKH/TLCN/KLTN yêu cầu đánh giá của mọi thành viên hội đồng đang hiệu lực.
- Không cho người chấm tự sửa bản `SUBMITTED`.
- Chỉ các bản `SUBMITTED` và chưa bị vô hiệu mới tham gia tính điểm.
- Với đợt có `reviewer_deadline`, sau hạn chỉ được gửi nếu Trưởng khoa đã gia hạn mốc này.

### 9.2. `final_results`

| Cột                     | Kiểu           | Ràng buộc                                       | Ý nghĩa                                 |
| ----------------------- | -------------- | ----------------------------------------------- | --------------------------------------- |
| `id`                    | `BIGINT`       | PK, auto increment                              | Khóa chính                              |
| `topic_registration_id` | `BIGINT`       | NOT NULL, UNIQUE, FK → `topic_registrations.id` | Nhóm/đề tài có kết quả                  |
| `final_score`           | `DECIMAL(4,2)` | NOT NULL                                        | Trung bình cộng, làm tròn hai chữ số    |
| `summary_comment`       | `TEXT`         | NULL                                            | Nhận xét tổng hợp công bố cho sinh viên |
| `status`                | `VARCHAR(20)`  | NOT NULL                                        | Đã tính hoặc đã chốt                    |
| `finalized_by`          | `BIGINT`       | NULL, FK → `users.id`                           | Người chốt kết quả                      |
| `finalized_at`          | `TIMESTAMP`    | NULL                                            | Thời điểm chốt                          |
| `created_at`            | `TIMESTAMP`    | NOT NULL                                        | Thời điểm tính lần đầu                  |
| `updated_at`            | `TIMESTAMP`    | NULL                                            | Thời điểm tính/chốt lại                 |

Không có cột Đạt/Không đạt. Người chốt chỉ xác nhận kết quả hệ thống tính, không sửa trực tiếp `final_score`.

- Với môn học, người chốt là GV phụ trách.
- Với NCKH/TLCN/KLTN, người chốt là Chủ tịch hội đồng phụ trách đề tài.

### 9.3. `result_publications`

Mỗi lần công bố hoặc thu hồi toàn bộ kết quả của một đợt tạo một dòng lịch sử.

| Cột                      | Kiểu           | Ràng buộc                                | Ý nghĩa                    |
| ------------------------ | -------------- | ---------------------------------------- | -------------------------- |
| `id`                     | `BIGINT`       | PK, auto increment                       | Khóa chính                 |
| `registration_period_id` | `BIGINT`       | NOT NULL, FK → `registration_periods.id` | Đợt được công bố/thu hồi   |
| `action`                 | `VARCHAR(20)`  | NOT NULL                                 | `PUBLISHED` hoặc `REVOKED` |
| `reason`                 | `VARCHAR(500)` | NULL                                     | Bắt buộc khi thu hồi       |
| `performed_by`           | `BIGINT`       | NOT NULL, FK → `users.id`                | Trưởng khoa thực hiện      |
| `performed_at`           | `TIMESTAMP`    | NOT NULL                                 | Thời điểm thực hiện        |

Chỉ được tạo bản ghi `PUBLISHED` khi mọi kết quả bắt buộc của đợt đã `FINALIZED`. Trạng thái công bố hiện tại được xác định bằng bản ghi mới nhất của đợt.

## 10. Thông báo

### 10.1. `announcements`

| Cột            | Kiểu           | Ràng buộc                 | Ý nghĩa                 |
| -------------- | -------------- | ------------------------- | ----------------------- |
| `id`           | `BIGINT`       | PK, auto increment        | Khóa chính              |
| `title`        | `VARCHAR(255)` | NOT NULL                  | Tiêu đề                 |
| `content`      | `TEXT`         | NOT NULL                  | Nội dung                |
| `status`       | `VARCHAR(20)`  | NOT NULL                  | Đã đăng hoặc đã thu hồi |
| `author_id`    | `BIGINT`       | NOT NULL, FK → `users.id` | Trưởng khoa đăng        |
| `withdrawn_at` | `TIMESTAMP`    | NULL                      | Thời điểm thu hồi       |
| `created_at`   | `TIMESTAMP`    | NOT NULL                  | Thời điểm đăng          |
| `updated_at`   | `TIMESTAMP`    | NULL                      | Thời điểm sửa           |

### 10.2. `announcement_roles`

| Cột               | Kiểu     | Ràng buộc                   | Ý nghĩa       |
| ----------------- | -------- | --------------------------- | ------------- |
| `announcement_id` | `BIGINT` | PK, FK → `announcements.id` | Thông báo     |
| `role_id`         | `BIGINT` | PK, FK → `roles.id`         | Role được xem |

Thông báo chỉ hiển thị nội bộ. Thiết kế không có bảng email, push notification, người nhận cá nhân hoặc lịch hẹn đăng.

## 11. Chỉ mục và ràng buộc quan trọng

Ngoài PK, UNIQUE và index tự sinh cho các khóa duy nhất, cần tạo index cho các truy vấn chính:

| Bảng                   | Index đề xuất                                 | Mục đích                            |
| ---------------------- | --------------------------------------------- | ----------------------------------- |
| `users`                | (`department_id`)                             | Lọc người dùng theo bộ môn          |
| `registration_periods` | (`period_type`, `student_registration_start`) | Danh sách đợt theo loại/thời gian   |
| `period_topics`        | (`registration_period_id`, `status`)          | Danh sách đề tài của đợt            |
| `topic_advisors`       | (`period_topic_id`, `ended_at`)               | Tìm GVHD đang hiệu lực              |
| `student_groups`       | (`registration_period_id`)                    | Danh sách nhóm của đợt              |
| `group_members`        | (`student_id`)                                | Kiểm tra sinh viên đã có nhóm       |
| `group_requests`       | (`student_id`, `status`)                      | Danh sách lời mời/yêu cầu chờ xử lý |
| `topic_registrations`  | (`group_id`, `status`)                        | Đăng ký hiện tại của nhóm           |
| `topic_registrations`  | (`period_topic_id`, `status`)                 | Đếm số nhóm được chấp thuận         |
| `report_submissions`   | (`topic_registration_id`, `submitted_at`)     | Tìm lần nộp mới nhất                |
| `councils`             | (`registration_period_id`)                    | Hội đồng của đợt                    |
| `council_members`      | (`council_id`, `ended_at`)                    | Thành viên đang hiệu lực            |
| `evaluations`          | (`topic_registration_id`, `status`)           | Tập đánh giá dùng tính điểm         |
| `evaluations`          | (`evaluator_id`, `status`)                    | Công việc chấm của giảng viên       |
| `final_results`        | (`status`)                                    | Kiểm tra kết quả đã chốt            |
| `result_publications`  | (`registration_period_id`, `performed_at`)    | Trạng thái công bố mới nhất         |
| `announcements`        | (`status`, `created_at`)                      | Danh sách thông báo đang hiển thị   |

Các khóa ngoại mặc định dùng `ON DELETE RESTRICT`. Chỉ hai bảng nối phân quyền hiện có (`user_roles`, `role_permissions`) có thể tiếp tục dùng `ON DELETE CASCADE` như migration hiện tại.

## 12. Ràng buộc đặt tại service

MariaDB không phù hợp để biểu diễn gọn toàn bộ quy tắc có điều kiện. Các quy tắc sau được kiểm tra tại service trong transaction:

- Điều kiện mốc thời gian theo từng loại đợt.
- Đề tài môn học có đúng một GV phụ trách; đề tài NCKH/TLCN/KLTN có 1–2 GVHD đang hiệu lực.
- Nhóm có 1–3 sinh viên và đúng một trưởng nhóm.
- Một sinh viên chỉ thuộc một nhóm trong một đợt.
- Kiểm tra `max_groups` đồng thời khi gửi/chấp thuận đăng ký.
- Khóa cơ cấu nhóm khi đăng ký được chấp thuận.
- Hội đồng có 3–5 thành viên, đúng một Chủ tịch và một Thư ký.
- GVHD không thuộc hội đồng phụ trách đề tài mình hướng dẫn.
- Người đánh giá đúng với loại đợt và phân công hiện tại.
- Chỉ tính điểm khi đủ mọi đánh giá bắt buộc còn hiệu lực.
- Chỉ công bố khi toàn bộ kết quả trong đợt đã chốt.
- Quyền xem tệp, điểm và thông báo theo người liên quan/role.

Các thao tác kiểm tra số lượng hoặc sức chứa phải khóa/đọc nhất quán dữ liệu cần thiết trong cùng transaction để tránh hai yêu cầu đồng thời cùng vượt giới hạn.

## 13. Thứ tự migration đề xuất

1. Giữ migration auth hiện có.
2. Tạo `departments`, thêm `user_code` và `department_id` vào `users`.
3. Tạo `registration_periods` và `registration_period_changes`.
4. Tạo `topics`, `period_topics`, `topic_advisors`, `topic_reviews`.
5. Tạo `student_groups`, `group_members`, `group_requests`, `topic_registrations`.
6. Tạo `report_submissions`, `report_files`.
7. Tạo `councils`, `council_members`, `council_minutes`, sau đó thêm FK `period_topics.council_id` nếu cần tránh vòng thứ tự tạo bảng.
8. Tạo `evaluations`, `final_results`, `result_publications`.
9. Tạo `announcements`, `announcement_roles`.
10. Seed bộ môn, role và permission cần thiết bằng migration riêng.

## 14. Ngoài phạm vi

Thiết kế không tạo bảng cho:

- Lớp học phần, môn học, học kỳ hoặc tín chỉ.
- Tiến độ công việc hằng tuần.
- Thông báo cấp Trường, email, push hoặc thông báo cá nhân.
- Rubric, xếp loại hoặc trạng thái Đạt/Không đạt.
- Lịch chi tiết của từng hội đồng ngoài các mốc đã được cấu hình.
- Cloud/object storage.
- Hồ sơ sinh viên và giảng viên tách khỏi `users`.
- CRUD role, permission hoặc bộ môn nếu chưa có yêu cầu riêng.
