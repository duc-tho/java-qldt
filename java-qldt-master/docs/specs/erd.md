# Sơ đồ quan hệ cơ sở dữ liệu

Tài liệu này minh họa các quan hệ trong thiết kế tại [db.md](db.md). Sơ đồ được tách theo miền để dễ đọc và trình bày khi bảo vệ đồ án.

Quy ước:

- `PK`: khóa chính.
- `FK`: khóa ngoại.
- `UK`: khóa duy nhất.
- Các bảng lịch sử giữ bản ghi cũ, không bị xóa khi trạng thái nghiệp vụ thay đổi.
- Sinh viên và giảng viên đều là `USERS`; role xác định quyền của tài khoản.

## 1. Danh tính và phân quyền

```mermaid
erDiagram
    DEPARTMENTS ||--o{ USERS : contains
    USERS ||--o{ USER_ROLES : has
    ROLES ||--o{ USER_ROLES : assigned
    ROLES ||--o{ ROLE_PERMISSIONS : has
    PERMISSIONS ||--o{ ROLE_PERMISSIONS : grants

    DEPARTMENTS {
        bigint id PK
        varchar code UK
        varchar name UK
        timestamp created_at
        timestamp updated_at
    }

    USERS {
        bigint id PK
        varchar username UK
        varchar password
        varchar email UK
        varchar phone UK
        varchar full_name
        varchar user_code UK
        bigint department_id FK
        timestamp created_at
        timestamp updated_at
        timestamp deleted_at
    }

    ROLES {
        bigint id PK
        varchar name UK
        timestamp created_at
        timestamp updated_at
        timestamp deleted_at
    }

    USER_ROLES {
        bigint user_id PK,FK
        bigint role_id PK,FK
    }

    PERMISSIONS {
        bigint id PK
        varchar name UK
        timestamp created_at
        timestamp updated_at
        timestamp deleted_at
    }

    ROLE_PERMISSIONS {
        bigint role_id PK,FK
        bigint permission_id PK,FK
    }
```

## 2. Đợt, đề tài, nhóm và đăng ký

```mermaid
erDiagram
    USERS ||--o{ REGISTRATION_PERIODS : creates
    REGISTRATION_PERIODS ||--o{ REGISTRATION_PERIOD_CHANGES : has
    USERS ||--o{ REGISTRATION_PERIOD_CHANGES : changes

    DEPARTMENTS ||--o{ TOPICS : owns
    REGISTRATION_PERIODS ||--o{ PERIOD_TOPICS : offers
    TOPICS ||--o{ PERIOD_TOPICS : reused_in
    DEPARTMENTS ||--o{ PERIOD_TOPICS : snapshots
    USERS ||--o{ PERIOD_TOPICS : proposes
    PERIOD_TOPICS ||--o{ TOPIC_ADVISORS : assigned
    USERS ||--o{ TOPIC_ADVISORS : advises
    PERIOD_TOPICS ||--o{ TOPIC_REVIEWS : reviewed
    USERS ||--o{ TOPIC_REVIEWS : performs

    REGISTRATION_PERIODS ||--o{ STUDENT_GROUPS : contains
    USERS ||--o{ STUDENT_GROUPS : creates
    STUDENT_GROUPS ||--|{ GROUP_MEMBERS : has
    USERS ||--o{ GROUP_MEMBERS : joins
    STUDENT_GROUPS ||--o{ GROUP_REQUESTS : receives
    USERS ||--o{ GROUP_REQUESTS : concerns
    STUDENT_GROUPS ||--o{ TOPIC_REGISTRATIONS : submits
    PERIOD_TOPICS ||--o{ TOPIC_REGISTRATIONS : selected
    USERS ||--o{ TOPIC_REGISTRATIONS : reviews

    REGISTRATION_PERIODS {
        bigint id PK
        varchar name
        varchar period_type
        timestamp lecturer_registration_start
        timestamp lecturer_registration_end
        timestamp topic_publication_at
        timestamp student_registration_start
        timestamp student_registration_end
        timestamp report_deadline
        timestamp reviewer_deadline
        timestamp council_date
        bigint created_by FK
    }

    REGISTRATION_PERIOD_CHANGES {
        bigint id PK
        bigint registration_period_id FK
        varchar change_type
        varchar field_name
        varchar old_value
        varchar new_value
        bigint changed_by FK
        timestamp changed_at
    }

    TOPICS {
        bigint id PK
        varchar title
        text description
        bigint department_id FK
    }

    PERIOD_TOPICS {
        bigint id PK
        bigint registration_period_id FK
        bigint topic_id FK
        varchar title
        text description
        bigint department_id FK
        bigint proposed_by FK
        int max_groups
        varchar status
        bigint approved_by FK
        timestamp approved_at
        timestamp published_at
        bigint council_id FK
    }

    TOPIC_ADVISORS {
        bigint id PK
        bigint period_topic_id FK
        bigint lecturer_id FK
        varchar advisor_type
        bigint assigned_by FK
        timestamp assigned_at
        timestamp ended_at
    }

    TOPIC_REVIEWS {
        bigint id PK
        bigint period_topic_id FK
        varchar action
        varchar reason
        bigint performed_by FK
        timestamp performed_at
    }

    STUDENT_GROUPS {
        bigint id PK
        bigint registration_period_id FK
        bigint created_by FK
        timestamp locked_at
    }

    GROUP_MEMBERS {
        bigint group_id PK,FK
        bigint student_id PK,FK
        boolean is_leader
        timestamp joined_at
    }

    GROUP_REQUESTS {
        bigint id PK
        bigint group_id FK
        bigint student_id FK
        varchar request_type
        varchar status
        timestamp created_at
        timestamp responded_at
    }

    TOPIC_REGISTRATIONS {
        bigint id PK
        bigint group_id FK
        bigint period_topic_id FK
        varchar status
        timestamp submitted_at
        bigint reviewed_by FK
        timestamp reviewed_at
        varchar rejection_reason
        timestamp withdrawn_at
    }
```

## 3. Báo cáo, hội đồng, đánh giá và kết quả

```mermaid
erDiagram
    TOPIC_REGISTRATIONS ||--o{ REPORT_SUBMISSIONS : has
    USERS ||--o{ REPORT_SUBMISSIONS : submits
    REPORT_SUBMISSIONS ||--|{ REPORT_FILES : contains
    REPORT_SUBMISSIONS o|--o| REPORT_SUBMISSIONS : replaces

    REGISTRATION_PERIODS ||--o{ COUNCILS : has
    USERS ||--o{ COUNCILS : creates
    COUNCILS ||--o{ COUNCIL_MEMBERS : contains
    USERS ||--o{ COUNCIL_MEMBERS : participates
    COUNCILS ||--o{ PERIOD_TOPICS : reviews
    COUNCILS ||--o| COUNCIL_MINUTES : records
    USERS ||--o{ COUNCIL_MINUTES : writes

    TOPIC_REGISTRATIONS ||--o{ EVALUATIONS : receives
    USERS ||--o{ EVALUATIONS : evaluates
    EVALUATIONS o|--o| EVALUATIONS : previous_version
    TOPIC_REGISTRATIONS ||--o| FINAL_RESULTS : produces
    USERS ||--o{ FINAL_RESULTS : finalizes
    REGISTRATION_PERIODS ||--o{ RESULT_PUBLICATIONS : has
    USERS ||--o{ RESULT_PUBLICATIONS : performs

    REPORT_SUBMISSIONS {
        bigint id PK
        bigint topic_registration_id FK
        bigint submitted_by FK
        varchar status
        bigint replaces_submission_id FK
        timestamp submitted_at
    }

    REPORT_FILES {
        bigint id PK
        bigint report_submission_id FK
        varchar original_name
        varchar storage_path UK
        varchar file_type
        bigint size_bytes
        timestamp created_at
    }

    COUNCILS {
        bigint id PK
        bigint registration_period_id FK
        varchar name
        bigint created_by FK
    }

    COUNCIL_MEMBERS {
        bigint id PK
        bigint council_id FK
        bigint lecturer_id FK
        varchar member_role
        timestamp joined_at
        timestamp ended_at
    }

    COUNCIL_MINUTES {
        bigint id PK
        bigint council_id FK,UK
        text content
        bigint recorded_by FK
    }

    EVALUATIONS {
        bigint id PK
        bigint topic_registration_id FK
        bigint evaluator_id FK
        decimal score
        text comment
        varchar status
        bigint previous_evaluation_id FK
        timestamp submitted_at
        bigint invalidated_by FK
        timestamp invalidated_at
        varchar invalidation_reason
    }

    FINAL_RESULTS {
        bigint id PK
        bigint topic_registration_id FK,UK
        decimal final_score
        text summary_comment
        varchar status
        bigint finalized_by FK
        timestamp finalized_at
    }

    RESULT_PUBLICATIONS {
        bigint id PK
        bigint registration_period_id FK
        varchar action
        varchar reason
        bigint performed_by FK
        timestamp performed_at
    }
```

## 4. Thông báo

```mermaid
erDiagram
    USERS ||--o{ ANNOUNCEMENTS : authors
    ANNOUNCEMENTS ||--|{ ANNOUNCEMENT_ROLES : targets
    ROLES ||--o{ ANNOUNCEMENT_ROLES : receives

    ANNOUNCEMENTS {
        bigint id PK
        varchar title
        text content
        varchar status
        bigint author_id FK
        timestamp withdrawn_at
        timestamp created_at
        timestamp updated_at
    }

    ANNOUNCEMENT_ROLES {
        bigint announcement_id PK,FK
        bigint role_id PK,FK
    }
```

## 5. Luồng dữ liệu chính

```text
RegistrationPeriod
    └── PeriodTopic ── Topic
          ├── TopicAdvisor
          └── Council

RegistrationPeriod
    └── StudentGroup
          └── GroupMember
          └── TopicRegistration ── PeriodTopic
                ├── ReportSubmission ── ReportFile
                ├── Evaluation
                └── FinalResult

RegistrationPeriod
    └── ResultPublication
```

Điểm quan trọng của quan hệ trên là báo cáo, đánh giá và kết quả đều gắn với `topic_registrations`. Vì vậy dữ liệu luôn xác định được nhóm nào thực hiện đề tài nào trong đợt nào, kể cả khi cùng một nội dung đề tài được tái sử dụng ở nhiều đợt.
