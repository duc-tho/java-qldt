# Login Authentication Design

## Goal

Require an authenticated server-side session before accessing the application. Users log in with their email and password; no registration or role/permission authorization is included.

## Scope

- Authenticate existing users through Spring Security `formLogin`.
- Store every newly created or changed password with BCrypt.
- Seed one initial account so the protected user CRUD remains reachable.
- Provide logout and clear feedback for a failed login.

This design does not add registration, password recovery, account locking, remember-me login, role/permission checks, or API/JWT authentication.

## Authentication Architecture

`WebSecurityConfig` configures Spring Security form login with `/login` as the custom login page, `email` as the username parameter, and the existing `PasswordEncoder` bean. Spring Security owns CSRF validation, credential comparison, session creation, redirect to the originally requested page, failed-login redirect, and invalidation of the session at logout.

The existing `UserService` remains responsible for CRUD, password lifecycle, and finding an active user by email. A dedicated `UserDetailsService` bean depends on `UserService` and maps its result to Spring Security `UserDetails`; it does not access the repository directly. An absent email is reported as `UsernameNotFoundException`; soft-deleted users cannot be returned because the entity's SQL restriction already excludes them.

At this stage every authenticated principal has no authorities. Access control is therefore binary: anonymous or authenticated. Roles and permissions remain unchanged in the data model for later work.

## Repository Architecture

Enable Spring Data repository scanning with `BaseRepositoryImpl` as the shared base implementation. Convert `UserRepository`, `RoleRepository`, and `PermissionRepository` from manually constructed classes into interfaces extending `BaseRepository`. `BaseRepositoryImpl` receives Spring Data's `JpaEntityInformation<T, ?>` and `EntityManager` in its constructor, then initializes its existing filtering support from the entity type.

This retains every base CRUD and filter method while enabling Derived Query Methods for every feature repository. `UserRepository.findByEmail(String email)` is declared as an `Optional<User>` method and Spring Data generates its exact-email query. Future repositories prefer Derived Query Methods first and use `@Query` only when a method name cannot express the query clearly.

## Request Flow

1. An anonymous request to any application route is redirected to `GET /login`.
2. `AuthController` renders the existing login template and its client-side validation data.
3. The user submits `POST /login` with `email`, `password`, and Spring Security's CSRF token.
4. Spring Security calls the dedicated `UserDetailsService`, then compares the submitted password against the stored BCrypt hash.
5. A valid login creates an HTTP session and redirects to the original route, or `/` when no saved request exists. An invalid login redirects to `/login?error`.
6. `POST /logout` invalidates the session and redirects to `/login?logout`.

`AuthController` does not expose `POST /login`; it only renders `GET /login`. The login template displays a generic invalid-credentials alert for `error` and may display a logged-out confirmation for `logout`. It removes the non-existent registration link. The logout action is supplied in the authenticated application UI as a CSRF-protected POST form.

## Password Lifecycle

`PasswordEncoder` is a BCrypt encoder supplied as a Spring bean. `UserService.beforeCreate` hashes the nonblank password before persistence. During update, it loads the persisted user first:

- a blank or absent submitted password preserves the existing BCrypt hash;
- a supplied password is BCrypt-hashed before saving.

The service never stores, logs, or returns a plaintext password. The existing user form continues to leave the password blank when editing, so it does not reveal the stored hash.

## Persistence and Seed Data

Add one Flyway migration after `V1_1__init_auth_feature.sql`. It inserts a user with:

| Field | Value |
| --- | --- |
| username | `admin` |
| email | `admin@qldt.local` |
| password | BCrypt hash for `admin123` |
| full_name | `Administrator` |

The migration contains only the already-generated BCrypt hash, never the plaintext password. The account has no role assignment because authorization is out of scope.

## Files and Responsibilities

| File | Change |
| --- | --- |
| `QldtApplication.java` | Enable Spring Data scanning with `BaseRepositoryImpl` as the repository base class. |
| `core/repository/BaseRepositoryImpl.java` | Use the Spring Data repository-base constructor. |
| `config/WebSecurityConfig.java` | Declare BCrypt encoder and configure authenticated access, form login, and logout. |
| `feature/auth/repository/UserRepository.java` | Declare a Derived Query Method for active user lookup by email. |
| `feature/auth/repository/RoleRepository.java` | Become a Spring Data repository interface. |
| `feature/auth/repository/PermissionRepository.java` | Become a Spring Data repository interface. |
| `feature/auth/service/UserDetailsService.java` | Load a user through `UserService` and map it to Spring Security `UserDetails`. |
| `feature/auth/service/UserService.java` | Hash passwords on create and password-changing update while retaining an unchanged hash. |
| `feature/auth/controller/AuthController.java` | Keep only the login-page GET handler. |
| `templates/fragment/form/login-form.html` | Submit the fields expected by Spring Security and render authentication feedback. |
| `templates/auth/login.html` | Remove the registration link. |
| authenticated navigation fragment | Add a CSRF-protected logout form. |
| `db/migration/V1_2__seed_admin_user.sql` | Insert the initial BCrypt-protected admin account. |

## Error Handling

The login page never reveals whether an email exists. All credential failures use one generic message. Spring Security rejects missing or invalid CSRF tokens. A user removed through soft delete cannot authenticate. User CRUD validation remains its current responsibility; only password encoding moves into `UserService`.

## Verification

No automated tests are added by request. Implementation verification is manual: anonymous access redirects to login, `admin@qldt.local` / `admin123` succeeds, bad credentials show the generic error, logout ends the session, and new or updated passwords in the database begin with a BCrypt hash prefix.
