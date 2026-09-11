# User CRUD UI Infrastructure Refactor Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Refactor the existing User CRUD presentation layer around reusable table/form view models and Thymeleaf fragments while preserving all current User behavior and preparing the infrastructure for future CRUD features.

**Architecture:** Keep `UserController` explicit for routes, User metadata, validation extras, and business rules. Add small generic classes under `core.table` and `core.view`, then make `CrudViewSupport` assemble those objects for Thymeleaf. Replace loose table/form model attributes with `${table}` and `${form}` contracts; do not introduce a generic CRUD controller or change services/entities/repositories.

**Tech Stack:** Java 25, Spring Boot 4.1.1, Spring MVC `@ModelAttribute`, Spring Data `Page`/`Pageable`, Thymeleaf Layout Dialect, HTMX 2.0.10, DaisyUI 5, JUnit 5, Maven Wrapper, Vite/Yarn.

**Spec:** `docs/superpowers/specs/2026-09-11-user-crud-ui-refactor-design.md`

## Global Constraints

- Only migrate the existing User CRUD; do not add Product or Department.
- Preserve User URLs, HTTP methods, service API, entities, repositories, database schema, validation behavior, HTMX behavior, and visible UI behavior.
- Keep `UserController` feature-specific and do not create `BaseCrudController`, registry, strategy, resolver, or another generic CRUD framework.
- Keep business logic in `UserController`/existing service layers; generic UI classes only prepare presentation models.
- Use constructor-based dependency injection and existing project package/style conventions.
- Use `apply_patch` for source edits and stage only task-owned files in each commit.
- Preserve the user's pre-existing worktree changes, especially the uncommitted `UserController` rename and unrelated `.gitignore`, `package.json`, and `AGENTS.md` changes.
- Use existing HTMX names, targets, swaps, included form names, redirect headers, and DaisyUI/Iconify conventions.
- Do not add dependencies.

---

### Task 1: Add `TableQuery` with focused unit coverage

**Files:**
- Create: `src/main/java/com/nhom11/qldt/core/table/TableQuery.java`
- Create: `src/test/java/com/nhom11/qldt/core/table/TableQueryTest.java`

**Interfaces:**
- Consumes: Spring Data `PageRequest`, `Sort`.
- Produces: Spring-bindable `TableQuery` with JavaBean getters/setters and `Pageable pageable()`.

- [ ] **Step 1: Write failing tests for defaults and sorting**

```java
package com.nhom11.qldt.core.table;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

class TableQueryTest {

    @Test
    void usesGenericDefaults() {
        TableQuery query = new TableQuery();

        assertEquals(0, query.getPage());
        assertEquals(10, query.getSize());
        assertEquals("asc", query.getDir());
        assertEquals(null, query.getSearch());
        assertEquals(null, query.getSort());
    }

    @Test
    void buildsAscendingPageable() {
        TableQuery query = new TableQuery();
        query.setPage(2);
        query.setSize(20);
        query.setSort("email");
        query.setDir("asc");

        assertEquals(2, query.pageable().getPageNumber());
        assertEquals(20, query.pageable().getPageSize());
        assertEquals(Sort.Direction.ASC, query.pageable().getSort().getOrderFor("email").getDirection());
    }

    @Test
    void buildsDescendingPageableCaseInsensitively() {
        TableQuery query = new TableQuery();
        query.setSort("email");
        query.setDir("DESC");

        assertEquals(Sort.Direction.DESC, query.pageable().getSort().getOrderFor("email").getDirection());
    }

    @Test
    void leavesPageableUnsortedForBlankSort() {
        TableQuery query = new TableQuery();
        query.setSort(" ");
        query.setDir("desc");

        assertTrue(query.pageable().getSort().isUnsorted());
    }
}
```

- [ ] **Step 2: Run the focused test and verify it fails**

Run: `./mvnw -q -Dtest=TableQueryTest test`

Expected: FAIL because `TableQuery` does not exist.

- [ ] **Step 3: Implement the minimal Spring-bindable query model**

Implement a public no-argument class with fields initialized to `page=0`, `size=10`, and `dir="asc"`; add getters/setters for `search`, `page`, `sort`, `dir`, and `size`; and implement:

```java
public Pageable pageable() {
    Sort sortSpec = Sort.unsorted();

    if (sort != null && !sort.isBlank()) {
        Sort.Direction direction = "desc".equalsIgnoreCase(dir)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        sortSpec = Sort.by(direction, sort);
    }

    return PageRequest.of(page, size, sortSpec);
}
```

Use the existing project indentation/import ordering. Do not add User-specific sort fields or validation.

- [ ] **Step 4: Run the focused test and verify it passes**

Run: `./mvnw -q -Dtest=TableQueryTest test`

Expected: PASS.

- [ ] **Step 5: Commit the query model and tests**

```bash
git add src/main/java/com/nhom11/qldt/core/table/TableQuery.java src/test/java/com/nhom11/qldt/core/table/TableQueryTest.java
git commit -m "feat: add shared CRUD table query"
```

### Task 2: Add `TableModel` and `TableModelFactory`

**Files:**
- Create: `src/main/java/com/nhom11/qldt/core/table/TableModel.java`
- Create: `src/main/java/com/nhom11/qldt/core/table/TableModelFactory.java`
- Create: `src/test/java/com/nhom11/qldt/core/table/TableModelFactoryTest.java`

**Interfaces:**
- Consumes: `Page<T>`, base URL, `TableQuery`, `List<ColumnDef>`, and `List<ActionDef>`.
- Produces: immutable `TableModel<T>` and Spring component method `create(...)`.

- [ ] **Step 1: Write failing factory tests**

```java
package com.nhom11.qldt.core.table;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

class TableModelFactoryTest {

    @Test
    void normalizesNullSearchAndSortWithoutDuplicatingPageState() {
        Page<String> data = new PageImpl<>(List.of("one"));
        TableQuery query = new TableQuery();
        List<ColumnDef> columns = List.of();
        List<ActionDef> actions = List.of();

        TableModel<String> table = new TableModelFactory().create(data, "/users", query, columns, actions);

        assertSame(data, table.data());
        assertEquals("/users", table.baseUrl());
        assertEquals("", table.search());
        assertEquals("", table.sortField());
        assertEquals("asc", table.sortDir());
        assertEquals(List.of(10, 20, 50), table.pageSizeOptions());
        assertSame(columns, table.columns());
        assertSame(actions, table.actions());
    }
}
```

- [ ] **Step 2: Run the focused test and verify it fails**

Run: `./mvnw -q -Dtest=TableModelFactoryTest test`

Expected: FAIL because `TableModel` and `TableModelFactory` do not exist.

- [ ] **Step 3: Implement immutable table state and factory**

Define `TableModel<T>` as a record with components:

```java
Page<T> data,
String baseUrl,
String search,
String sortField,
String sortDir,
List<Integer> pageSizeOptions,
List<ColumnDef> columns,
List<ActionDef> actions
```

Define `TableModelFactory` as a `@Component` with:

```java
public <T> TableModel<T> create(
        Page<T> data,
        String baseUrl,
        TableQuery query,
        List<ColumnDef> columns,
        List<ActionDef> actions)
```

Keep `private static final List<Integer> PAGE_SIZE_OPTIONS = List.of(10, 20, 50);`. Normalize null search/sort to empty strings and preserve `query.getDir()`.

- [ ] **Step 4: Run the focused test and verify it passes**

Run: `./mvnw -q -Dtest=TableModelFactoryTest test`

Expected: PASS.

- [ ] **Step 5: Commit table state and factory**

```bash
git add src/main/java/com/nhom11/qldt/core/table/TableModel.java src/main/java/com/nhom11/qldt/core/table/TableModelFactory.java src/test/java/com/nhom11/qldt/core/table/TableModelFactoryTest.java
git commit -m "feat: add shared CRUD table model factory"
```

### Task 3: Add `CrudFormModel` and `CrudViewSupport`

**Files:**
- Create: `src/main/java/com/nhom11/qldt/core/view/CrudFormModel.java`
- Create: `src/main/java/com/nhom11/qldt/core/view/CrudViewSupport.java`
- Create: `src/test/java/com/nhom11/qldt/core/view/CrudViewSupportTest.java`

**Interfaces:**
- Consumes: `Model`, `Page<T>`, `TableQuery`, table metadata, form state, and rendered validation rules.
- Produces: `model["table"]`, `model["form"]`, and table fragment view name `fragment/crud/table :: crud-table`.

- [ ] **Step 1: Write failing support tests**

```java
package com.nhom11.qldt.core.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.ui.ExtendedModelMap;

import com.nhom11.qldt.core.table.TableQuery;

class CrudViewSupportTest {

    @Test
    void tableStoresFactoryResultUnderTable() {
        ExtendedModelMap model = new ExtendedModelMap();
        CrudViewSupport support = new CrudViewSupport(new TableModelFactory());
        TableQuery query = new TableQuery();

        String view = support.table(model, new PageImpl<>(List.of("one")), "/users", query, List.of(), List.of());

        assertEquals("fragment/crud/table :: crud-table", view);
        assertEquals("/users", ((com.nhom11.qldt.core.table.TableModel<?>) model.getAttribute("table")).baseUrl());
    }

    @Test
    void formStoresAllSuppliedStateUnderForm() {
        ExtendedModelMap model = new ExtendedModelMap();
        CrudViewSupport support = new CrudViewSupport(new TableModelFactory());
        Object data = new Object();
        Map<String, String> rules = Map.of("name", "[]");

        support.form(model, data, "/users/1", "put", "/users", true, "user/fields", rules);

        CrudFormModel<?> form = (CrudFormModel<?>) model.getAttribute("form");
        assertSame(data, form.data());
        assertEquals("/users/1", form.actionUrl());
        assertEquals("put", form.method());
        assertEquals("/users", form.backUrl());
        assertEquals(true, form.readonly());
        assertEquals("user/fields", form.fieldsTemplate());
        assertSame(rules, form.validationRules());
    }
}
```

- [ ] **Step 2: Run the focused test and verify it fails**

Run: `./mvnw -q -Dtest=CrudViewSupportTest test`

Expected: FAIL because the view model/support types do not exist.

- [ ] **Step 3: Implement the immutable form model and narrow support component**

Define `CrudFormModel<T>` as a record with `data`, `actionUrl`, `method`, `backUrl`, `boolean readonly`, `fieldsTemplate`, and `Map<String, String> validationRules`.

Define `CrudViewSupport` as a `@Component` with constructor injection of `TableModelFactory` and methods:

```java
public <T> String table(
        Model model,
        Page<T> data,
        String baseUrl,
        TableQuery query,
        List<ColumnDef> columns,
        List<ActionDef> actions)

public <T> void form(
        Model model,
        T data,
        String actionUrl,
        String method,
        String backUrl,
        boolean readonly,
        String fieldsTemplate,
        Map<String, String> validationRules)
```

The table method stores the factory result under `table` and returns exactly `fragment/crud/table :: crud-table`. The form method stores a `CrudFormModel` under `form`. Do not inject or call User services or validation renderers here.

- [ ] **Step 4: Run the focused test and verify it passes**

Run: `./mvnw -q -Dtest=CrudViewSupportTest test`

Expected: PASS.

- [ ] **Step 5: Commit form state and view support**

```bash
git add src/main/java/com/nhom11/qldt/core/view/CrudFormModel.java src/main/java/com/nhom11/qldt/core/view/CrudViewSupport.java src/test/java/com/nhom11/qldt/core/view/CrudViewSupportTest.java
git commit -m "feat: add shared CRUD view support"
```

### Task 4: Refactor generic Thymeleaf fragments to object contracts

**Files:**
- Create: `src/main/resources/templates/fragment/crud/table.html`
- Create: `src/main/resources/templates/fragment/crud/pagination.html`
- Create: `src/main/resources/templates/fragment/crud/actions.html`
- Create: `src/main/resources/templates/fragment/crud/form.html`
- Create: `src/main/resources/templates/fragment/crud/delete-modal.html`
- Test/verify: `src/main/resources/templates/fragment/page/user/list.html`, `src/main/resources/templates/fragment/page/user/create.html`, `src/main/resources/templates/fragment/page/user/detail.html`

**Interfaces:**
- Consumes: `${table}` as `TableModel`, `${form}` as `CrudFormModel`, existing `ActionDef`, and existing HTMX parameter names.
- Produces: unchanged table/form HTML behavior through `crud-table`, `crud-pagination`, `crud-actions`, `crud-form`, and `delete-modal` fragments.

- [ ] **Step 1: Copy the existing shared templates into the new `fragment/crud` location without changing behavior**

Use the current files as the source of truth:

- `fragment/table/table.html` → `fragment/crud/table.html`
- `fragment/table/pagination.html` → `fragment/crud/pagination.html`
- action rendering from the current table → `fragment/crud/actions.html`
- `fragment/form/crud-form.html` → `fragment/crud/form.html`
- `fragment/table/delete-modal.html` → `fragment/crud/delete-modal.html`

Keep the current IDs, DaisyUI classes, Iconify icons, `hx-target="#crud-table"`, `hx-swap="outerHTML"`, `hx-include` names, and delete-modal JavaScript.

- [ ] **Step 2: Change table and pagination to read one `table` object**

Change the table fragment signature to accept one model:

```html
<th:block th:fragment="crud-table(table)" th:with="listUrl=|${table.baseUrl}/list|">
```

Replace loose references as follows:

```text
data              -> table.data
baseUrl           -> table.baseUrl
search            -> table.search
sortField         -> table.sortField
sortDir           -> table.sortDir
pageSizeOptions   -> table.pageSizeOptions
columns           -> table.columns
actions           -> table.actions
```

Pass `table.data` and `table.baseUrl` to pagination. Keep all `hx-vals` and `hx-include` values semantically identical.

- [ ] **Step 3: Extract action rendering into `actions.html`**

Define a fragment accepting `actions` and `item`, preserve the first-three-inline-actions and remaining-dropdown behavior, and use only the existing `ActionDef` methods (`isDelete`, `getUrl`, `getName`, `icon`, `label`). The table should replace the action cell contents through this fragment.

- [ ] **Step 4: Change form to read one `form` object and preserve readonly behavior**

Use a fragment signature like:

```html
<th:block th:fragment="crud-form(form)">
```

Use `${form.data}`, `${form.actionUrl}`, `${form.method}`, `${form.readonly}`, `${form.backUrl}`, `${form.fieldsTemplate}`, and `${form.validationRules}`. Preserve the current form-level error summary, Alpine `formValidation`, `validated-submit`, HTMX post/put selection, and read-only branch.

The dynamic User fields include must pass explicit values:

```html
<th:block th:replace="~{${form.fieldsTemplate} :: form-fields(${form.data}, ${form.readonly}, ${form.validationRules})}"></th:block>
```

- [ ] **Step 5: Point User page templates and layout delete modal at new fragment contracts**

Update User list/create/detail templates to replace the new one-argument table/form fragments. Keep the feature fields file; only its fragment signature changes in the next task. Update `layout/page.html` to include `fragment/crud/delete-modal :: delete-modal`.

- [ ] **Step 6: Verify template references and compile resources**

Run:

```bash
rg -n "fragment/table|fragment/form/crud-form|\$\{data\}|\$\{columns\}|\$\{actions\}|\$\{actionUrl\}|\$\{isReadonly\}" src/main/resources/templates
./mvnw -q -DskipTests compile
```

Expected: no migrated User page points to the old shared fragment contracts; compile succeeds.

- [ ] **Step 7: Commit the generic fragment contract migration**

```bash
git add src/main/resources/templates/fragment/crud src/main/resources/templates/fragment/page/user/list.html src/main/resources/templates/fragment/page/user/create.html src/main/resources/templates/fragment/page/user/detail.html src/main/resources/templates/layout/page.html
git commit -m "refactor: consolidate CRUD Thymeleaf fragments"
```

### Task 5: Migrate User fields and page total state

**Files:**
- Modify: `src/main/resources/templates/fragment/page/user/fields.html`
- Modify: `src/main/resources/templates/layout/page.html`

**Interfaces:**
- Consumes: `form-fields(data, readonly, validationRules)` from the generic form fragment and `table` from table pages.
- Produces: User-only fields with identical validation/read-only behavior and a derived table count badge.

- [ ] **Step 1: Update the User fields fragment signature and references**

Change:

```html
<th:block th:fragment="form-fields(data, isReadonly)">
```

to:

```html
<th:block th:fragment="form-fields(data, isReadonly, validationRules)">
```

Keep every field name, ID, class, error expression, and `validate-data` attribute; only change the source of `validationRules` from a loose model attribute to the explicit fragment parameter.

- [ ] **Step 2: Change the page badge to derive table total elements**

Replace the page layout's `totalCount` condition/value with a null-safe expression that displays `table.data.totalElements` only when a table model exists. Keep the existing fallback behavior for non-table pages if one is needed by the current layout contract; do not add `totalCount` back in the User controller.

- [ ] **Step 3: Verify the template contract statically**

Run:

```bash
rg -n "form-fields\(|validationRules|totalCount|table\.data\.totalElements" src/main/resources/templates
```

Expected: User fields has the three-argument signature, generic form passes three arguments, and User table count comes from `table.data.totalElements`.

- [ ] **Step 4: Commit the User fields/layout contract change**

```bash
git add src/main/resources/templates/fragment/page/user/fields.html src/main/resources/templates/layout/page.html
git commit -m "refactor: bind user fields to CRUD form model"
```

### Task 6: Refactor `UserController` to use shared query and view support

**Files:**
- Modify: `src/main/java/com/nhom11/qldt/feature/auth/controller/UserController.java`
- Test/verify: `src/test/java/com/nhom11/qldt/feature/auth/controller/UserControllerTest.java` (create only if the existing test dependencies can support focused MVC tests without new dependencies)

**Interfaces:**
- Consumes: `TableQuery`, `CrudViewSupport`, existing `UserService`, `ClientValidationRuleRenderer`, `ColumnDef`, `ActionDef`.
- Produces: User routes using shared table/form model assembly with unchanged HTTP and HTMX contracts.

- [ ] **Step 1: Add failing unit-level coverage for the controller's table flow where practical**

If a Mockito/Spring MVC test can be added using already available test dependencies, cover the core interaction:

```java
verify(userService).findAll("alice", PageRequest.of(1, 20, Sort.by(Sort.Direction.DESC, "email")));
assertEquals("fragment/crud/table :: crud-table", controller.listFragment(query, request, model));
```

If the current test setup does not provide the necessary mocking support, do not add a dependency; use the model/unit tests from Tasks 1–3 and the full application test as the safety net.

- [ ] **Step 2: Inject `CrudViewSupport` and remove controller-owned generic constants/imports**

Add constructor injection for `CrudViewSupport`. Remove `PageRequest`, `Sort`, and `PAGE_SIZE_OPTIONS` usage from the controller. Keep User-specific constants, page metadata, paths, mutation methods, and validation message constants.

- [ ] **Step 3: Replace repeated query parameters with `@ModelAttribute TableQuery query`**

Apply `@ModelAttribute TableQuery query` to:

- `list`
- `listFragment`
- single `delete` (alongside `force`)
- `batchDelete` (alongside `ids`)

Preserve the existing `HttpServletRequest`, `HttpServletResponse`, and `Model` parameters and all route annotations.

- [ ] **Step 4: Refactor the private table helper**

Replace the old signature with:

```java
private String tableFragment(TableQuery query, Model model) {
    Page<User> data = userService.findAll(query.getSearch(), query.pageable());

    return crudView.table(model, data, PREFIX, query, columns(), actions());
}
```

Keep `columns()` and `actions()` as private User-specific methods. Move their current `List.of(...)` definitions out of the helper only if required to make the helper readable; do not change labels, extractors, URLs, or action ordering.

- [ ] **Step 5: Update all table callers and preserve HTMX branches**

Call `tableFragment(query, model)` from list, HTMX list, table-target single delete, and batch delete. Keep the non-HTMX redirect, `HX-Target` comparison, force-delete behavior, and response redirect headers unchanged.

- [ ] **Step 6: Refactor form assembly through `CrudViewSupport.form(...)`**

Replace the old loose `formFragment` implementation with a helper that computes `validationRuleRenderer.render(UserForm.class, ...)` and calls:

```java
crudView.form(
        model,
        form,
        actionUrl,
        method,
        backUrl,
        readonly,
        FIELDS_TEMPLATE,
        validationRules);
```

Pass `readonly=true` for detail and `readonly=false` for create/edit/error postbacks according to the current behavior. Keep the password-required extra rule only on create and keep password preservation/roles preservation in update.

- [ ] **Step 7: Run focused compilation/tests and inspect the diff**

Run:

```bash
./mvnw -q -Dtest=TableQueryTest,TableModelFactoryTest,CrudViewSupportTest test
./mvnw -q -DskipTests compile
git diff --check
```

Expected: all focused tests pass, compilation succeeds, and no whitespace errors appear.

- [ ] **Step 8: Commit the User controller migration**

```bash
git add src/main/java/com/nhom11/qldt/feature/auth/controller/UserController.java
git commit -m "refactor: use shared CRUD view models in UserController"
```

If a controller test was created, include only that test file in the same commit.

### Task 7: Remove obsolete fragments and verify application behavior

**Files:**
- Delete only after reference search: `src/main/resources/templates/fragment/table/table.html`
- Delete only after reference search: `src/main/resources/templates/fragment/table/pagination.html`
- Delete only after reference search: `src/main/resources/templates/fragment/table/delete-modal.html`
- Delete only after reference search: `src/main/resources/templates/fragment/form/crud-form.html`
- Modify if needed: `src/main/resources/templates/fragment/page/user/list.html`
- Modify if needed: `src/main/resources/templates/fragment/page/user/create.html`
- Modify if needed: `src/main/resources/templates/fragment/page/user/detail.html`

**Interfaces:**
- Consumes: all migrated references from Tasks 4–6.
- Produces: one generic CRUD fragment set with no obsolete loose-attribute contract.

- [ ] **Step 1: Prove old fragments have no remaining references**

Run:

```bash
rg -n "fragment/table|fragment/form/crud-form|crud-table\(\$\{data\}|crud-form\(\$\{data\}" src/main src/test || true
```

Expected: no active reference to the old files/contracts.

- [ ] **Step 2: Delete only the now-unreferenced old fragments**

Use `apply_patch` to delete the four old shared files. Do not delete feature fields or the existing JavaScript assets.

- [ ] **Step 3: Search for obsolete controller assembly patterns**

Run:

```bash
rg -n "PageRequest|Sort\.by|@RequestParam.*(search|page|sort|dir|size)|addAttribute\(\"(data|columns|actions|baseUrl|search|sortField|sortDir|pageSizeOptions|actionUrl|method|isReadonly|backUrl|fieldsTemplate|validationRules|totalCount)" src/main/java/com/nhom11/qldt/feature/auth/controller src/main/resources/templates
```

Expected: User table/form state is accessed through `TableQuery`, `table`, and `form`; unrelated login validation/data attributes remain valid.

- [ ] **Step 4: Run the full verification suite**

Run:

```bash
./mvnw test
yarn build
git diff --check
git status --short
```

Expected: Maven tests pass, Vite produces the frontend bundle successfully, diff check passes, and the final status shows only the intended refactor commits plus the user's pre-existing worktree changes.

- [ ] **Step 5: Commit cleanup only after verification**

```bash
git add -u src/main/resources/templates/fragment/table src/main/resources/templates/fragment/form
git commit -m "chore: remove obsolete CRUD fragments"
```

