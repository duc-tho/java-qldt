# Login Authentication Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Protect the application with Spring Security form login backed by existing users and BCrypt password storage.

**Architecture:** Spring Security owns form login, CSRF, HTTP sessions, and logout. `DatabaseUserDetailsService` calls `UserService` to load a non-deleted user by email for the security layer; `UserService` owns CRUD, email lookup, and password encoding before persistence.

**Tech Stack:** Java 25, Spring Boot 4.1.1, Spring Security, Spring Data JPA, Thymeleaf, Flyway, BCrypt.

**Spec:** `docs/superpowers/specs/2026-09-24-login-authentication-design.md`

## Global Constraints

- Authenticate by `email`; the bootstrap account is `admin@qldt.local` with initial password `admin123`.
- Configure server-side session authentication with Spring Security `formLogin`; do not add JWT or registration.
- Keep CSRF protection enabled and make logout a `POST /logout` form carrying the CSRF token.
- Do not add role or permission authorization; every authenticated user may access the same routes.
- Persist only BCrypt hashes for passwords created or changed through user CRUD and in the seed migration.
- Configure all feature repositories as Spring Data interfaces with `BaseRepositoryImpl` as their common base; prefer Derived Query Methods, then `@Query` when a derived name is unclear.
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
| `src/main/java/com/nhom11/qldt/QldtApplication.java` | Enable Spring Data repository scanning with the custom base class. |
| `src/main/java/com/nhom11/qldt/config/WebSecurityConfig.java` | Supply BCrypt and configure protected routes, form login, and logout. |
| `src/main/java/com/nhom11/qldt/core/repository/BaseRepositoryImpl.java` | Serve as Spring Data's common repository base class. |
| `src/main/java/com/nhom11/qldt/feature/auth/repository/UserRepository.java` | Declare `findByEmail` as a Derived Query Method. |
| `src/main/java/com/nhom11/qldt/feature/auth/repository/RoleRepository.java` | Declare the role Spring Data repository interface. |
| `src/main/java/com/nhom11/qldt/feature/auth/repository/PermissionRepository.java` | Declare the permission Spring Data repository interface. |
| `src/main/java/com/nhom11/qldt/feature/auth/service/DatabaseUserDetailsService.java` | Adapt a user provided by `UserService` to Spring Security `UserDetails`. |
| `src/main/java/com/nhom11/qldt/feature/auth/service/UserService.java` | BCrypt-encode passwords on create and password-changing update. |
| `src/main/java/com/nhom11/qldt/feature/auth/controller/AuthController.java` | Render only `GET /login`. |
| `src/main/resources/templates/fragment/form/login-form.html` | Submit the expected credentials and render error/logout notices. |
| `src/main/resources/templates/auth/login.html` | Remove the dead registration link. |
| `src/main/resources/templates/fragment/ui/nav.html` | Render a CSRF-protected logout form for authenticated users. |
| `src/main/resources/db/migration/V1_2__seed_admin_user.sql` | Insert the initial account with a generated BCrypt hash. |

### Task 1: Standardize Spring Data repositories

**Files:**
- Modify: `src/main/java/com/nhom11/qldt/QldtApplication.java`
- Modify: `src/main/java/com/nhom11/qldt/core/repository/BaseRepositoryImpl.java`
- Modify: `src/main/java/com/nhom11/qldt/feature/auth/repository/UserRepository.java`
- Modify: `src/main/java/com/nhom11/qldt/feature/auth/repository/RoleRepository.java`
- Modify: `src/main/java/com/nhom11/qldt/feature/auth/repository/PermissionRepository.java`

**Interfaces:**
- Consumes: `BaseRepository<T, ID>` and Spring Data's `JpaEntityInformation<T, ?>`.
- Produces: Spring Data proxies for all three feature repositories and `Optional<User> UserRepository.findByEmail(String email)`.

- [ ] **Step 1: Enable the common repository base implementation**

  Add `@EnableJpaRepositories(repositoryBaseClass = BaseRepositoryImpl.class)` to `QldtApplication`, alongside its existing `@EnableJpaAuditing`. Import `org.springframework.data.jpa.repository.config.EnableJpaRepositories` and `BaseRepositoryImpl`.

- [ ] **Step 2: Supply Spring Data's repository-base constructor**

  Replace the constructor in `BaseRepositoryImpl` with the constructor Spring Data uses to instantiate a custom base class. Keep the existing fields and initialize `domainClass` and `filterQueryBuilder` from `entityInformation`:

  ```java
  public BaseRepositoryImpl(
          JpaEntityInformation<T, ?> entityInformation,
          EntityManager entityManager) {
      super(entityInformation, entityManager);

      this.domainClass = entityInformation.getJavaType();
      this.entityManager = entityManager;
      this.filterQueryBuilder = new FilterQueryBuilder<>(domainClass, entityManager);
  }
  ```

  Replace the `Class<T>` import with `org.springframework.data.jpa.repository.support.JpaEntityInformation`.

- [ ] **Step 3: Convert each feature repository to an interface**

  Replace the class bodies, `@Repository`, `EntityManager`, and constructor imports in all three repositories. Their complete declarations are:

  ```java
  public interface UserRepository extends BaseRepository<User, Long> {
      Optional<User> findByEmail(String email);
  }
  ```

  ```java
  public interface RoleRepository extends BaseRepository<Role, Long> {
  }
  ```

  ```java
  public interface PermissionRepository extends BaseRepository<Permission, Long> {
  }
  ```

  `UserRepository` imports `java.util.Optional`; all three import `BaseRepository` and their own entity class. Do not use `@Repository`, because Spring Data registers the interface proxies.

- [ ] **Step 4: Compile the repository refactor**

  Run: `./mvnw compile`

  Expected: `BUILD SUCCESS`; the existing `UserService` constructor can receive the generated `UserRepository` proxy.

### Task 2: Load users for authentication and secure passwords

**Files:**
- Create: `src/main/java/com/nhom11/qldt/feature/auth/service/DatabaseUserDetailsService.java`
- Modify: `src/main/java/com/nhom11/qldt/feature/auth/service/UserService.java`

**Interfaces:**
- Consumes: Spring Security's `UserDetailsService` / `UserDetails` and `UserService.findByEmail(String email)`.
- Produces: `UserService.findByEmail(String email)`, `DatabaseUserDetailsService.loadUserByUsername(String email)`, and BCrypt-backed `UserService.create(User)` / `UserService.update(User)`.



- [ ] **Step 1: Add the security-only user-details adapter**

  Create `DatabaseUserDetailsService` in `feature.auth.service`. Inject `UserService` through the constructor. Implement `UserDetailsService` and map the user returned by `UserService.findByEmail(email)` to a no-authority principal:

  ```java
  @Service
  public class DatabaseUserDetailsService implements UserDetailsService {
      private final UserService userService;

      public DatabaseUserDetailsService(UserService userService) {
          this.userService = userService;
      }

      @Override
      public UserDetails loadUserByUsername(String email) {
          User user = userService.findByEmail(email)
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



- [ ] **Step 2: Expose email lookup and encode password values in `UserService`**

  Inject `PasswordEncoder` alongside `UserRepository`. Add `public Optional<User> findByEmail(String email)` that returns `userRepository.findByEmail(email)`. Implement `beforeCreate` to encode the incoming password. Extend `beforeUpdate` so it first loads the existing user, preserves the existing hash for blank input, otherwise encodes the submitted new password, then preserves the existing roles:

  ```java
  public Optional<User> findByEmail(String email) {
      return userRepository.findByEmail(email);
  }

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


- [ ] **Step 3: Compile the backend**

  Run: `./mvnw compile`

  Expected: `BUILD SUCCESS`; no new test source is created.

### Task 3: Configure Spring Security and seed the initial account

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
  System.out.println(new BCryptPasswordEncoder().encode("admin123"));
  ```

  Copy the printed hash once. Do not commit the plaintext command output or a Java helper file.

- [ ] **Step 3: Add the Flyway seed migration**

  Create `V1_2__seed_admin_user.sql` with one `INSERT INTO users` row. Put the generated BCrypt string in `password`, and set the required data to `username = 'admin'`, `email = 'admin@qldt.local'`, and `full_name = 'Administrator'`. Do not add records to `roles` or `user_roles`.

- [ ] **Step 4: Compile and apply Flyway through the application startup**

  Run: `./mvnw spring-boot:run`

  Expected: the application starts without a Flyway checksum or migration error. Stop it after the migration completes.

### Task 4: Connect the login and logout UI

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
  2. Submit `admin@qldt.local` / `admin123`; the browser reaches `/` and can open `/users`.
  3. Submit the correct email with a wrong password; `/login?error` shows only the generic credentials error.
  4. Use logout; a following `/users` request redirects to `/login`.
  5. Create a user, then inspect the database `users.password`: it is BCrypt, not plaintext.
  6. Edit that user without a password and confirm the stored hash is unchanged; edit with a new password and confirm the hash changes and authenticates the new password.
