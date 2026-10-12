Always response to user with vietnamese and use English when coding

## UI Guidelines

For UI, Tailwind, daisyUI, Thymeleaf template, or shared layout tasks, follow the existing project themes, shared components, and layout conventions. Read the daisyUI skill and relevant component guides before implementing UI changes.

# Master concept

Behavioral guidelines to reduce common LLM coding mistakes. Merge with project-specific instructions as needed.

**Tradeoff:** These guidelines bias toward caution over speed. For trivial tasks, use judgment.

## 1. Think Before Coding

**Don't assume. Don't hide confusion. Surface tradeoffs.**

Before implementing:

- State your assumptions explicitly. If uncertain, ask.
- If multiple interpretations exist, present them - don't pick silently.
- If a simpler approach exists, say so. Push back when warranted.
- If something is unclear, stop. Name what's confusing. Ask.

## 2. Simplicity First

**Minimum code that solves the problem. Nothing speculative.**

- No features beyond what was asked.
- No abstractions for single-use code.
- No "flexibility" or "configurability" that wasn't requested.
- No error handling for impossible scenarios.
- If you write 200 lines and it could be 50, rewrite it.

Ask yourself: "Would a senior engineer say this is overcomplicated?" If yes, simplify.

## 3. Surgical Changes

**Touch only what you must. Clean up only your own mess.**

When editing existing code:

- Don't "improve" adjacent code, comments, or formatting.
- Don't refactor things that aren't broken.
- Match existing style, even if you'd do it differently.
- If you notice unrelated dead code, mention it - don't delete it.

When your changes create orphans:

- Remove imports/variables/functions that YOUR changes made unused.
- Don't remove pre-existing dead code unless asked.

The test: Every changed line should trace directly to the user's request.

## 4. Goal-Driven Execution

**Define success criteria. Loop until verified.**

Transform tasks into verifiable goals:

- "Add validation" → "Write tests for invalid inputs, then make them pass"
- "Fix the bug" → "Write a test that reproduces it, then make it pass"
- "Refactor X" → "Ensure tests pass before and after"

For multi-step tasks, state a brief plan:

```
1. [Step] → verify: [check]
2. [Step] → verify: [check]
3. [Step] → verify: [check]
```

Strong success criteria let you loop independently. Weak criteria ("make it work") require constant clarification.

---

**These guidelines are working if:** fewer unnecessary changes in diffs, fewer rewrites due to overcomplication, and clarifying questions come before implementation rather than after mistakes.

# Repository Guidelines

## Project Structure & Module Organization

This is a Spring Boot WAR application with a Vite/Tailwind frontend build. Java code lives in `src/main/java/com/nhom11/qldt`: shared abstractions are under `core/`, configuration under `config/`, and domain features under `feature/<feature>/` (for example, `feature/auth/`). Keep new code within the appropriate feature package.

Server resources are in `src/main/resources`: Thymeleaf templates in `templates/`, source CSS/JS in `style/` and `script/`, configuration in `application.yaml`, messages in `i18n/`, and Flyway migrations in `db/migration/`. Vite emits browser assets to `static/assets/`. Tests mirror the production package tree in `src/test/java/`.

## Build, Test, and Development Commands

- `./mvnw spring-boot:run` — start the application with the active local profile.
- `./mvnw test` — run the JUnit/Spring Boot test suite.
- `./mvnw clean package` — compile, test, and create the WAR in `target/`.
- `yarn install` — install frontend build dependencies.
- `yarn dev` — rebuild frontend assets while source files change.
- `yarn build` — produce the frontend bundle in `src/main/resources/static/`.

Use Java 25, as declared in `pom.xml`. Do not commit `target/`, `node_modules/`, or personal `application-dev.yaml`; use `application-dev-example.yaml` as the configuration starting point.

## Coding Style & Naming Conventions

Follow existing Java style: four-space indentation, one public type per file, PascalCase class names, and camelCase fields and methods. Name Spring layers by responsibility (`AuthController`, `UserService`, `UserRepository`) and DTOs with a purpose suffix such as `LoginForm`. Keep controllers thin; put logic in services and persistence access in repositories. Use template paths such as `auth/login` and lower-case, hyphenated asset filenames.

No standalone formatter or linter is configured. Preserve the surrounding style, keep imports organized, and run both Maven tests and the Vite build after changes that span backend and UI code.

For Icon use iconify with bootstrap package, example:

```html
<span class="iconify inline" data-icon="bi:sort-numeric-down"></span>
```

For any UI component you need load skill daisyui to ref daisyui component before implement

## Commit & Pull Request Guidelines

Recent commits use concise Conventional Commit-style prefixes: `feat: partial implement Auth` and `init: create, add deps, dev container`. Use imperative summaries such as `fix: validate login form` or `refactor: extract role lookup`. Keep commits narrowly scoped. Pull requests should describe the behavioral change, list validation commands run, link the related issue when available, and include screenshots for template or styling changes.

Java Spring Boot Coding Convention

# Coding Convention

- YAGNI
- KISS
- SOLID
- DRY
- Early Return
- NO MAGIC NUMBER
- NO HARDCODE
- Direct strings are acceptable for routes, mappings, endpoints, annotation values, and framework-specific values when appropriate.
- Do not create constants for strings used only once in annotations such as `@GetMapping`, `@PostMapping`, `@RequestMapping`, `@Profile`, `@Qualifier`, etc.
- Reuse existing constants before creating new ones.
- When creating a constant, do not hardcode reusable text, numbers, values, or literals.
- Prefer enums for fixed sets of values.
- Prefer constants for values used in multiple places.
- Do not create a constant if the value is used only once.
- Do not duplicate literals across classes or methods.
- Do not hardcode configuration; use `application.yml` / `application.properties` and `@ConfigurationProperties`.
- Do not hardcode URLs, timeouts, limits, sizes, statuses, messages, formats, keys, header names, or business values.
- Prefer existing constants from the JDK, Spring Framework, Spring Boot, and existing libraries before defining new ones.
- Constants must have meaningful names, be `static final`, and have the appropriate scope.
- Do not create utilities/helpers for simple logic that can be written directly.
- Keep methods short and follow Single Responsibility.
- Prefer composition over inheritance when appropriate.
- Do not create abstractions without an actual need.
- Validate input at the boundary.
- Fail fast.
- Prefer Early Return over deeply nested `if` statements.
- Avoid `null` when `Optional`, empty collections, or appropriate objects can be used.
- Do not return `null` for collections; prefer empty collections.
- Do not catch exceptions that cannot be handled.
- Do not swallow exceptions.
- Do not log and rethrow the same exception without adding value.
- Log at the appropriate level and avoid unnecessary logging.
- Never log sensitive information.
- Do not use exceptions for normal flow control.
- Do not duplicate business logic.
- Reuse existing services/components before creating new ones.
- Do not create unused classes, methods, or fields.
- Use constructor-based Dependency Injection.
- Do not use field injection.
- Controllers handle HTTP request/response concerns only; business logic belongs in the service/domain layer.
- Repositories handle persistence only.
- Services handle business logic.
- Do not expose entities directly through APIs when DTOs are appropriate.
- DTOs should contain only required data.
- Do not put business logic in DTOs.
- Define transaction boundaries at the service layer.
- Do not use `@Transactional` unnecessarily.
- Queries should retrieve only required data.
- Avoid N+1 queries.
- Do not call databases/APIs inside loops when batching is possible.
- Naming must clearly express intent.
- Do not write comments for self-explanatory code.
- Comments should explain **why**, not **what**.
- Avoid uncommon abbreviations.
- Use clear boolean naming with `is`, `has`, `can`, or `should`.
- Do not create methods with side effects that are not clear from their names.
- Avoid mutating objects/collections when unnecessary.
- Prefer immutable objects.
- Do not use `String` for domain values when an enum/value object is more appropriate.
- Do not suppress warnings without a valid reason.
- Do not use raw types.
- Do not use wildcard imports.
- Remove unused imports, fields, methods, and parameters.
- Remove dead code.
- Do not duplicate annotations/configuration.
- Prefer centralized configuration.
- Do not hardcode environment-specific values.
- Never hardcode credentials, secrets, tokens, or passwords.
- Do not hardcode date/time; use `Clock` or configuration when testability is required.
- Do not hardcode formats; reuse existing constants/formatters.
- Do not hardcode HTTP statuses, headers, or media types when the framework already provides constants/APIs.
- Do not hardcode validation messages/error codes when existing constants or a message source are available.
- Before creating a new constant: **search for and reuse an existing constant**.
- Before creating a new utility/helper: **search for and reuse an existing utility/helper**.
- Before creating a new abstraction: **check for an existing abstraction**.
- Do not create constants merely to replace a literal that appears exactly once.
- Do not create generic constants such as `VALUE`, `DATA`, `TEMP`, or `CONSTANT`.

## Exception Handling

- Do not use `try-catch` in application/business logic unless the exception can actually be handled at that level.
- Use `throw` to propagate exceptions.
- Let the **Global Exception Handler** handle application exceptions and convert them into appropriate HTTP responses.
- Do not catch, log, and rethrow the same exception without adding meaningful context or handling.
- Do not swallow exceptions.

## Code Formatting

- Format code for readability.
- Add a blank line between logical blocks of code.
- Add a blank line after declaration blocks when appropriate.
- Group related declarations and statements together.
- Avoid unnecessarily dense code.
- Keep formatting consistent across the project.
