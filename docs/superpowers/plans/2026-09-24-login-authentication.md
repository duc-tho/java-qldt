# Login Authentication Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Protect the application with Spring Security form login backed by existing users and BCrypt password storage.

**Architecture:** Spring Security owns form login, CSRF, HTTP sessions, and logout. `DatabaseUserDetailsService` loads a non-deleted user by email for the security layer, while `UserService` retains CRUD responsibility and encodes passwords before persistence.

**Tech Stack:** Java 25, Spring Boot 4.1.1, Spring Security, Spring Data JPA, Thymeleaf, Flyway, BCrypt.

**Spec:** `docs/superpowers/specs/2026-09-24-login-authentication-design.md`

## Global Constraints

- Authenticate by `email`; the bootstrap account is `admin@qldt.local` with initial password `admin`.
- Configure server-side session authentication with Spring Security `formLogin`; do not add JWT or registration.
- Keep CSRF protection enabled and make logout a `POST /logout` form carrying the CSRF token.
- Do not add role or permission authorization; every authenticated user may access the same routes.
- Persist only BCrypt hashes for passwords created or changed through user CRUD and in the seed migration.
- Do not add automated tests, per the user's explicit request; verify by compile/build and the listed manual checks.

## Review Focus

- An anonymous request to `/users` must redirect to `/login`, not render the protected page.
- A soft-deleted user must not be loadable by email and therefore cannot log in.
- An edit submitted with no password must retain its existing BCrypt value exactly.
- An edit containing a new password must store a different BCrypt value that matches that new password.
- Invalid credentials must show one generic error without revealing whether the email exists.

---

## File Structure

| File | Responsibility |
| --- | --- |
| `src/main/java/com/nhom11/qldt/config/WebSecurityConfig.java` | Supply BCrypt and configure protected routes, form login, and logout. |
| `src/main/java/com/nhom11/qldt/core/repository/BaseRepositoryImpl.java` | Expose its `EntityManager` to focused concrete repository queries. |
| `src/main/java/com/nhom11/qldt/feature/auth/repository/UserRepository.java` | Load an active user by email via JPQL. |
| `src/main/java/com/nhom11/qldt/feature/auth/service/DatabaseUserDetailsService.java` | Adapt a stored user to Spring Security `UserDetails`. |
| `src/main/java/com/nhom11/qldt/feature/auth/service/UserService.java` | BCrypt-encode passwords on create and password-changing update. |
| `src/main/java/com/nhom11/qldt/feature/auth/controller/AuthController.java` | Render only `GET /login`. |
| `src/main/resources/templates/fragment/form/login-form.html` | Submit the expected credentials and render error/logout notices. |
| `src/main/resources/templates/auth/login.html` | Remove the dead registration link. |
| `src/main/resources/templates/fragment/ui/nav.html` | Render a CSRF-protected logout form for authenticated users. |
| `src/main/resources/db/migration/V1_2__seed_admin_user.sql` | Insert the initial account with a generated BCrypt hash. |

### Task 1: Load users for authentication and secure passwords

**Files:**
- Modify: `src/main/java/com/nhom11/qldt/core/repository/BaseRepositoryImpl.java`
- Modify: `src/main/java/com/nhom11/qldt/feature/auth/repository/UserRepository.java`
- Create: `src/main/java/com/nhom11/qldt/feature/auth/service/DatabaseUserDetailsService.java`
- Modify: `src/main/java/com/nhom11/qldt/feature/auth/service/UserService.java`

**Interfaces:**
- Consumes: `UserRepository.findByEmail(String email)` and Spring Security's `UserDetailsService` / `UserDetails`.
- Produces: `DatabaseUserDetailsService.loadUserByUsername(String email)` for Spring Security and BCrypt-backed `UserService.create(User)` / `UserService.update(User)`.

- [ ] **Step 1: Add a repository lookup for an active email**

  Add `Optional<User> findByEmail(String email)` to `UserRepository`. Use the inherited `EntityManager` supplied by `BaseRepositoryImpl` by changing its visibility from `private` to `protected`, then run this JPQL query and return the first result:

  ```java
  public Optional<User> findByEmail(String email) {
      return entityManager.createQuery(
                      "SELECT u FROM User u WHERE u.email = :email",
                      User.class)
              .setParameter("email", email)
              .getResultStream()
              .findFirst();
  }
  ```

  `User` already has `@SQLRestriction("deleted_at IS NULL")`, so the query excludes soft-deleted rows without duplicating that predicate.

- [ ] **Step 2: Add the security-only user-details adapter**

  Create `DatabaseUserDetailsService` in `feature.auth.service`. Inject `UserRepository` through the constructor. Implement `UserDetailsService` and map the matched user to a no-authority principal:

  ```java
  @Service
  public class DatabaseUserDetailsService implements UserDetailsService {
      private final UserRepository userRepository;

      public DatabaseUserDetailsService(UserRepository userRepository) {
          this.userRepository = userRepository;
      }

      @Override
      public UserDetails loadUserByUsername(String email) {
          User user = userRepository.findByEmail(email)
                  .orElseThrow(() -> new UsernameNotFoundException("User not found"));

          return org.springframework.security.core.userdetails.User
                  .withUsername(user.getEmail())
                  .password(user.getPassword())
                  .authorities(List.of())
                  .build();
      }
  }
  ```

  Import `java.util.List`, `UserDetails`, `UserDetailsService`, and `UsernameNotFoundException`. Do not add role or permission mapping.

- [ ] **Step 3: Encode password values in `UserService`**

  Inject `PasswordEncoder` alongside `UserRepository`. Implement `beforeCreate` to encode the incoming password. Extend `beforeUpdate` so it first loads the existing user, preserves the existing hash for blank input, otherwise encodes the submitted new password, then preserves the existing roles:

  ```java
  @Override
  protected void beforeCreate(User user) {
      user.setPassword(passwordEncoder.encode(user.getPassword()));
  }

  @Override
  protected void beforeUpdate(User updates) {
      userRepository.findById(updates.getId()).ifPresent(existingUser -> {
          if (updates.getPassword() == null || updates.getPassword().isBlank()) {
              updates.setPassword(existingUser.getPassword());
          } else {
              updates.setPassword(passwordEncoder.encode(updates.getPassword()));
          }

          updates.setRoles(existingUser.getRoles());
      });
  }
  ```

- [ ] **Step 4: Compile the backend**

  Run: `./mvnw compile`

  Expected: `BUILD SUCCESS`; no new test source is created.

- [ ] **Step 5: Commit the persistence and service layer**

  ```bash
  git add src/main/java/com/nhom11/qldt/core/repository/BaseRepositoryImpl.java src/main/java/com/nhom11/qldt/feature/auth/repository/UserRepository.java src/main/java/com/nhom11/qldt/feature/auth/service/DatabaseUserDetailsService.java src/main/java/com/nhom11/qldt/feature/auth/service/UserService.java
  git commit -m "feat: load users for authentication"
  ```

### Task 2: Configure Spring Security and seed the initial account

**Files:**
- Modify: `src/main/java/com/nhom11/qldt/config/WebSecurityConfig.java`
- Create: `src/main/resources/db/migration/V1_2__seed_admin_user.sql`

**Interfaces:**
- Consumes: the `DatabaseUserDetailsService` bean, `PasswordEncoder` bean, and form fields named `email` and `password`.
- Produces: an authenticated session after `POST /login`, a protected application, `POST /logout`, and the initial user record.

- [ ] **Step 1: Replace the permit-all filter chain**

  Add a `PasswordEncoder` bean that returns `new BCryptPasswordEncoder()`. Configure `HttpSecurity` as follows; retain default CSRF behavior:

  ```java
  http.authorizeHttpRequests(auth -> auth
                  .requestMatchers("/login", "/assets/**", "/images/**").permitAll()
                  .anyRequest().authenticated())
          .formLogin(form -> form
                  .loginPage("/login")
                  .loginProcessingUrl("/login")
                  .usernameParameter("email")
                  .passwordParameter("password")
                  .defaultSuccessUrl("/", false)
                  .failureUrl("/login?error")
                  .permitAll())
          .logout(logout -> logout
                  .logoutUrl("/logout")
                  .logoutSuccessUrl("/login?logout")
                  .invalidateHttpSession(true)
                  .clearAuthentication(true));
  ```

  Do not call `csrf().disable()`. Spring Security discovers `DatabaseUserDetailsService` and the BCrypt encoder through dependency injection.

- [ ] **Step 2: Generate a BCrypt value for the migration**

  Use a temporary JShell session with the project's Spring Security crypto JAR on the classpath to run:

  ```java
  import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
  System.out.println(new BCryptPasswordEncoder().encode("admin"));
  ```

  Copy the printed hash once. Do not commit the plaintext command output or a Java helper file.

- [ ] **Step 3: Add the Flyway seed migration**

  Create `V1_2__seed_admin_user.sql` with one `INSERT INTO users` row. Put the generated BCrypt string in `password`, and set the required data to `username = 'admin'`, `email = 'admin@qldt.local'`, and `full_name = 'Administrator'`. Do not add records to `roles` or `user_roles`.

- [ ] **Step 4: Compile and apply Flyway through the application startup**

  Run: `./mvnw spring-boot:run`

  Expected: the application starts without a Flyway checksum or migration error. Stop it after the migration completes.

- [ ] **Step 5: Commit security configuration and seed data**

  ```bash
  git add src/main/java/com/nhom11/qldt/config/WebSecurityConfig.java src/main/resources/db/migration/V1_2__seed_admin_user.sql
  git commit -m "feat: secure application login"
  ```

### Task 3: Connect the login and logout UI

**Files:**
- Modify: `src/main/java/com/nhom11/qldt/feature/auth/controller/AuthController.java`
- Modify: `src/main/resources/templates/fragment/form/login-form.html`
- Modify: `src/main/resources/templates/auth/login.html`
- Modify: `src/main/resources/templates/fragment/ui/nav.html`

**Interfaces:**
- Consumes: Spring Security redirects `?error` and `?logout`, `_csrf.parameterName`, `_csrf.token`, and the security config endpoints.
- Produces: a form accepted by `POST /login`, generic login feedback, and a CSRF-protected `POST /logout` control.

- [ ] **Step 1: Remove custom login submission handling**

  Delete `AuthController.login(...)` and its `@PostMapping`, `@Valid`, and `BindingResult` imports. Preserve `GET /login` and the model attribute that provides client validation rules. Spring Security must be the only handler for `POST /login`.

- [ ] **Step 2: Render Spring Security feedback in the login form**

  At the top of `login-form.html`, before the fields, add a generic error alert when `${param.error}` is present and a success-style logged-out alert when `${param.logout}` is present. Keep the fields named `email` and `password`; retain `th:action="@{/login}"` so Thymeleaf adds the CSRF token through Spring Security's request-data processor. Do not repopulate the password field after a failed login.

- [ ] **Step 3: Remove unreachable registration UI**

  Delete the “Chưa có tài khoản?” block and `/dang-ky` link from `auth/login.html`. Registration is explicitly out of scope.

- [ ] **Step 4: Add an authenticated logout form**

  In `fragment/ui/nav.html`, add a compact `method="post"` form with `th:action="@{/logout}"`, the existing `_csrf` hidden input pattern, and a DaisyUI button containing the Iconify Bootstrap icon `bi:box-arrow-right`. Position it in `navbar-end` after the existing notification button.

- [ ] **Step 5: Build frontend assets and compile the application**

  Run: `yarn build`

  Expected: Vite emits updated assets without errors.

  Run: `./mvnw compile`

  Expected: `BUILD SUCCESS`.

- [ ] **Step 6: Perform manual acceptance checks**

  Start the app and verify, without creating automated tests:

  1. Open `/users` while anonymous; the browser redirects to `/login`.
  2. Submit `admin@qldt.local` / `admin`; the browser reaches `/` and can open `/users`.
  3. Submit the correct email with a wrong password; `/login?error` shows only the generic credentials error.
  4. Use logout; a following `/users` request redirects to `/login`.
  5. Create a user, then inspect the database `users.password`: it is BCrypt, not plaintext.
  6. Edit that user without a password and confirm the stored hash is unchanged; edit with a new password and confirm the hash changes and authenticates the new password.

- [ ] **Step 7: Commit the authentication UI**

  ```bash
  git add src/main/java/com/nhom11/qldt/feature/auth/controller/AuthController.java src/main/resources/templates/fragment/form/login-form.html src/main/resources/templates/auth/login.html src/main/resources/templates/fragment/ui/nav.html
  git commit -m "feat: add login and logout ui"
  ```
