# Foundation CRUD Design

## Goal

Deliver the five foundation-management features needed before the workflow
features: departments, users, registration periods, reusable topics, and
faculty announcements. Each feature follows the existing server-rendered CRUD
pattern and lives entirely in its own feature package.

The implementation reuses `BaseCrudService`, `CrudViewSupport`, the shared
table/form fragments, client validation renderer, and the existing User CRUD
as the concrete template. It does not introduce a metadata-driven CRUD layer,
REST API, or workflow features that are outside the selected scope.

## Scope and Ordering

The implementation order follows data dependencies:

1. Create the Department persistence model and its CRUD.
2. Extend User with department, user code, and role assignment.
3. Create RegistrationPeriod CRUD and conditional time validation.
4. Create reusable Topic CRUD.
5. Create Announcement CRUD with audience-role selection and withdrawal.

`period_topics`, topic approval/publication, assignment of lecturers,
registration-period change history, and automatic publication are deliberately
excluded. They belong to later workflow features.

## Package Layout

Every new feature uses the existing boundaries below. Files are copied from
the User CRUD only when their responsibility remains the same, then renamed
and reduced to the fields of the target domain.

```text
feature/
  department/
    controller/DepartmentController.java
    model/dto/DepartmentForm.java
    model/entity/Department.java
    repository/DepartmentRepository.java
    service/DepartmentService.java
    view/DepartmentTableDefinition.java
  registrationperiod/
    controller/RegistrationPeriodController.java
    model/dto/RegistrationPeriodForm.java
    model/entity/RegistrationPeriod.java
    model/enum/PeriodType.java
    repository/RegistrationPeriodRepository.java
    service/RegistrationPeriodService.java
    view/RegistrationPeriodTableDefinition.java
  topic/
    controller/TopicController.java
    model/dto/TopicForm.java
    model/entity/Topic.java
    repository/TopicRepository.java
    service/TopicService.java
    view/TopicTableDefinition.java
  announcement/
    controller/AnnouncementController.java
    model/dto/AnnouncementForm.java
    model/entity/Announcement.java
    model/enum/AnnouncementStatus.java
    repository/AnnouncementRepository.java
    service/AnnouncementService.java
    view/AnnouncementTableDefinition.java
```

The existing `feature/auth` package remains the home of User. Its model, DTO,
repository, service, table definition, controller and page fragments are
extended in place rather than moved. Page fragments follow the existing
`templates/fragment/page/<feature>/` layout, with `list`, `create`, `detail`,
and `fields` files per CRUD.

## Persistence and Deletion

Flyway migrations are append-only and create tables before their foreign-key
consumers. The first migration creates `departments` and adds `user_code` plus
`department_id` to `users`; a separate migration seeds the four required
roles. Later migrations create `registration_periods`, `topics`, then
`announcements` and `announcement_roles`.

`Department`, `User`, `RegistrationPeriod`, and `Topic` extend
`SoftDeleteEntity`; their tables contain `deleted_at`. The UI exposes the
existing soft-delete action only. A permanent-delete route is not added to new
features.

Announcements are the exception: their domain deletion is withdrawal. An
announcement remains stored with status `WITHDRAWN` and `withdrawn_at`, so it
does not have `deleted_at` and it has no delete action. This preserves the
publication audit without two conflicting deletion concepts.

At this foundation stage, deleting a department, registration period, or topic
that has a foreign-key-dependent record is rejected by the database and shown
as a normal validation error. No cascade delete is introduced. User soft
deletion continues to make the account unavailable for authentication.

## Feature Behavior

### Department

Department manages unique `code` and `name`. Its form has one responsive row
of two fields: code on the left and name on the right. Both fields are required
and are validated in the form; uniqueness conflicts are translated into a
field-level error by the service boundary.

### User

User is extended with nullable, unique `userCode`, nullable `Department`, and
a role collection selected by the administrator. The form is responsive: name
and user code; email and phone; username and password are paired rows.
Department and role checkboxes use the full width. Password is required at
creation, omitted for read-only detail, and optional on edit; a blank edit
password keeps the existing BCrypt hash.

The User service loads selected roles and department through their repositories
before saving. It never accepts detached relationship objects from a form. A
future self-service profile page is out of scope; this CRUD remains the Admin
management interface.

### Registration Period

RegistrationPeriod stores the selected `PeriodType`, name, the six common
timepoints, optional reviewer deadline, optional council date, and the creator
derived from the authenticated user. The form uses a two-column grid: name and
type first; then lecturer start/end, publication/student start,
student end/report deadline, and reviewer deadline/council date. The last two
fields are shown only for relevant period types and remain server-validated.

The service enforces all timing rules in `db.md`: ordering of registration and
publication milestones; reviewer deadline only for TLCN/KLTN; council date
only and required for KLTN, after student registration and reviewer deadline.
The base CRUD does not yet record `registration_period_changes` because
extension/reopening is a future workflow action, not an edit form concern.

### Topic

Topic is a reusable template with title, description, and department. Title
and department form the first two-column row; description is a full-width
textarea. It has no approval status, maximum group count, period, or lecturer
assignment because those are properties of `period_topics` and later
workflows.

### Announcement

Announcement has a full-width title field, a full-width content textarea, and
a full-width role-checkbox audience. The authenticated Head of Faculty is
always written as the author; it is never submitted by the browser. New and
edited announcements are `PUBLISHED`; withdrawal is a dedicated POST action
that changes the status to `WITHDRAWN` and records `withdrawn_at`.

The active-announcement list shows only `PUBLISHED` records to its selected
roles. Management screens retain withdrawn records for audit. Email, personal
recipients, scheduled publishing, and physical deletion are not implemented.

## Request Handling and Security

Controllers keep the User CRUD's list, create form, create, detail, edit form,
update and soft-delete endpoint shape. They bind a validated form, render the
same form with errors, and delegate all persistence and business checks to the
service. Table definitions supply search, filters, columns, and actions.

Access rules are explicit in `WebSecurityConfig`: only `ADMIN` manages users;
only `HEAD_OF_FACULTY` manages departments, registration periods, topics, and
announcements. All authenticated users may later receive feature-specific
read-only pages, but those pages are not part of this CRUD delivery. Existing
login behavior remains unchanged.

## Error Handling

Forms reject required, length, format, date-order, and conditional-field
violations before service execution when possible. Services perform the
cross-field and persistence checks, fail fast, and propagate a domain-specific
validation exception to the global exception handler. They do not catch and
silently ignore persistence exceptions. Database unique and foreign-key
violations are converted once to user-facing validation feedback.

## Verification

Tests cover service validation for period timings, preservation of an unchanged
user password, soft deletion for all soft-deletable features, role/audience
relationship persistence, and announcement withdrawal. MVC tests cover that
each management endpoint requires its intended role. Maven tests and the Vite
asset build run after implementation.
