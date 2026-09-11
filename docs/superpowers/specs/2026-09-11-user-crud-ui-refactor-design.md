# User CRUD UI Infrastructure Refactor Design

## Goal

Reduce repeated presentation-layer assembly in the existing User CRUD while
providing small, reusable infrastructure for future CRUD features. The
refactor must preserve the current User URLs, HTTP methods, service API,
validation behavior, HTMX behavior, database model, and visible UI behavior
unless a change is required to pass the new model objects to the existing
fragments.

Only the existing User CRUD is migrated. Product and Department are not added.

## Current context

The repository currently has one CRUD feature: User. Its controller repeats
the `search`, `page`, `sort`, `dir`, and `size` request parameters on list and
delete actions, creates `PageRequest` and `Sort` locally, and adds separate
table and form attributes to the model. The existing `ColumnDef` and
`ActionDef` types already provide suitable feature-owned table metadata.

The existing table, pagination, delete-modal, and form templates are already
shared, but their contracts use positional arguments and loose model
attributes. User-specific fields remain in the User template. The layout
currently reads `totalCount` as a page-level attribute.

The worktree contains uncommitted user changes that rename/delete the old
`UserCrudController` in favor of `UserController`. Those changes are outside
this design and must be preserved.

## Responsibilities

| Layer | Responsibility |
| --- | --- |
| `UserController` | User routes, page metadata, User columns/actions, User validation extras, User business rules, and HTMX request decisions. |
| User service | Existing persistence and business behavior. No service API change. |
| `TableQuery` | Generic table query state and conversion to a Spring `Pageable`. |
| `TableModelFactory` | Normalize generic query state and create a `TableModel`. |
| `CrudViewSupport` | Put a table or form view model into `Model` and return/use the generic fragment contract. No business logic. |
| `TableModel<T>` | Complete state required by the generic table and pagination fragments. |
| `CrudFormModel<T>` | Complete state required by the generic form shell and feature fields. |
| Generic Thymeleaf fragments | Render table shell, actions, pagination, form shell, and delete behavior. |
| User fields fragment | Render only User inputs and User-specific field rules. |

No `BaseCrudController`, `BaseCrudService`, repository abstraction, registry,
strategy, resolver, or feature-specific filter is introduced by this change.
The existing `BaseCrudService` remains untouched.

## Java model design

### `TableQuery`

Create a small presentation model under the existing core table package. Use a
Spring-bindable class rather than relying on record constructor binding for
`@ModelAttribute` request parameters. Initialize these defaults:

| Property | Default |
| --- | --- |
| `search` | `null` |
| `page` | `0` |
| `sort` | `null` |
| `dir` | `asc` |
| `size` | `10` |

Expose getters/setters for Spring binding and a `pageable()` method. Blank or
null sort produces `Sort.unsorted()`. A case-insensitive `desc` direction
produces descending order; every other direction produces ascending order,
matching the current behavior. Preserve the current `PageRequest` failure
semantics for invalid numeric values unless focused validation is required by
the existing request flow; do not add feature-specific sort allowlists here.

Controllers use `@ModelAttribute TableQuery query` for table-related actions.
Feature-specific filters, if a future feature needs them, remain separate
feature models and are not added to `TableQuery`.

### `TableModel<T>`

Create an immutable model, preferably a record, containing:

- `Page<T> data`
- `String baseUrl`
- normalized search text
- normalized sort field
- normalized sort direction
- page-size options
- `List<ColumnDef> columns`
- `List<ActionDef> actions`

The page itself remains the source of truth for total elements, current page,
total pages, and page size. Do not store a duplicate `totalCount` in this
object.

### `TableModelFactory`

Create a Spring component that receives a `Page`, base URL, `TableQuery`,
columns, and actions, then returns a `TableModel`. Keep the current options
`10`, `20`, and `50` in one shared location because no page-size configuration
exists in the application yet. Preserve the current empty-string rendering
for null search/sort values.

### `CrudFormModel<T>`

Create an immutable model containing:

- form data
- action URL
- method (`post` or `put` in the current User flow)
- back URL
- readonly state
- fields template path
- rendered validation rules (`Map<String, String>`)

The model must represent readonly state directly. It must not make the
generic form shell infer whether a form is create, edit, or detail.

### `CrudViewSupport`

Create a Spring component with two narrowly scoped operations:

- Build a `TableModel` through `TableModelFactory`, put it under `table`, and
  return the table fragment view name.
- Build a `CrudFormModel` and put it under `form`.

The controller remains responsible for calling
`ClientValidationRuleRenderer`, including the User-only password-required
extra rule, and passing the resulting rules to this support component.

## User controller flow

Refactor `UserController` only after the infrastructure is in place.

The list, HTMX list, single-delete table refresh, and batch-delete actions use
`@ModelAttribute TableQuery query`. Their data flow becomes:

```text
TableQuery -> UserService.findAll(query.search(), query.pageable())
           -> CrudViewSupport.table(..., columns(), actions())
           -> model[table] -> generic table fragment
```

`columns()` and `actions()` stay private methods in `UserController` because
their labels, extractors, and URLs are User-specific. Page title, icon, and
description remain ordinary page metadata.

Create, edit, and detail actions continue to build `UserForm` and apply User
validation rules, but use `CrudViewSupport.form(...)` to populate one `form`
model. The readonly detail flow passes `readonly=true`; no later corrective
`Model.addAttribute` is needed.

The controller continues to handle:

- non-HTMX list fallback redirect;
- table-target detection for single delete;
- `HX-Redirect` mutation responses;
- User password preservation on update;
- User service calls and missing-entity redirects.

No business rule moves into the generic support classes.

## Thymeleaf contracts

Create generic fragments under `templates/fragment/crud/`:

```text
table.html
pagination.html
actions.html
form.html
delete-modal.html
```

The exact existing template paths may be retained only where doing so avoids
an unnecessary compatibility risk; the public contract is the new object
model, not the old positional attribute list. Remove old duplicate fragments
only after all references have been migrated.

### Table

The table fragment accepts one `TableModel` and reads all table state from it.
It owns the current table shell, search input, sort controls, page-size
selector, empty state, row rendering, action rendering, and pagination
include. It passes `TableModel.data` and `TableModel.baseUrl` to pagination.

Actions are split into `actions.html`, but continue to use the existing
`ActionDef` contract and the current three-inline-actions/dropdown behavior.
No new action type is introduced.

All current HTMX attributes, targets, swaps, included names, and request
parameter names remain equivalent after replacing the attribute paths with
`table.*` expressions.

### Form

The generic form fragment accepts one `CrudFormModel` and uses:

- `form.data` as the Thymeleaf object;
- `form.actionUrl`, `form.method`, and `form.backUrl` for form behavior;
- `form.readonly` for the read-only branch;
- `form.fieldsTemplate` to include feature fields;
- `form.validationRules` for field validation attributes.

The User fields fragment remains at its existing feature-specific location and
renders only User fields. Its fragment contract is updated as needed to
receive the form data, readonly state, and validation rules explicitly rather
than relying on unrelated global model attributes.

The generic shell continues to render the form-level validation summary and
generic Save/Cancel actions. It does not know User fields or password rules.

### Page total

Update the page layout's badge condition so table pages derive the count from
`table.data.totalElements` when `table` exists. Non-table pages retain their
current behavior. This removes the duplicate `totalCount` attribute without
changing the displayed User count.

## Compatibility and edge cases

- Keep `/users`, `/users/list`, `/users/new`, `/users/{id}`,
  `/users/{id}/edit`, and existing mutation routes unchanged.
- Keep GET/POST/PUT/DELETE mappings unchanged.
- Keep the current list query parameter names unchanged.
- Keep fragment return values for full-page and HTMX requests equivalent.
- Preserve `HX-Redirect` response headers and table-target refresh behavior.
- Preserve User server validation and client validation rule rendering.
- Do not introduce a User filter into `TableQuery`.
- Do not make generic fragments depend on User entity properties beyond the
  existing generic `item.id` checkbox contract; feature metadata supplies the
  displayed values and action URLs.
- Keep the current delete modal JavaScript behavior. Move it only as a
  template organization change if all references remain valid.

## Verification

Add focused unit tests for:

- `TableQuery` defaults;
- ascending, descending, and unsorted `Pageable` creation;
- `TableModelFactory` null normalization and preservation of metadata.

Add or update focused MVC/template coverage only if the existing test setup
supports it without introducing a new test framework. At minimum run:

```text
./mvnw test
yarn build
```

Also search for old table/form attribute names and old fragment references to
confirm no migrated User path still assembles the previous loose model.
