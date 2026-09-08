# Shared Form Validation Design

## Goal

Provide one client-side validation mechanism for Thymeleaf forms. DTO Bean Validation constraints are the source for rules that have a JustValidate equivalent. Alpine coordinates form events, HTMX performs requests, and server-side failures appear as one form-level alert.

## Scope

- Replace the login form's inline Alpine validation expressions.
- Render a `validate-data` JSON attribute for each validated input from its DTO field.
- Add a reusable JavaScript factory that configures JustValidate for one supplied form.
- Keep server-side Bean Validation active; it remains the security and correctness boundary.
- Render all postback failures from the backend in a single alert at the top of the form, never next to an input.

This design does not add browser equivalents for Bean Validation constraints that cannot be checked independently in the browser, such as cross-field, nested-object, database, or custom constraints. Those constraints remain server-only.

## Responsibilities

| Layer | Responsibility |
| --- | --- |
| DTO | Defines validation annotations and Vietnamese messages. |
| Backend rule renderer | Reads a named DTO property and serializes its browser-compatible constraints to JSON. |
| Thymeleaf form | Places the serialized JSON in `validate-data`, declares an error container, and initializes the Alpine controller. |
| `form-validation.js` | Exports a factory that creates a JustValidate instance for exactly one form. It does not query the whole document or attach global submit handlers. |
| Alpine | Owns validator lifecycle and decides when to revalidate and dispatch the successful-submit event. |
| HTMX | Listens for that custom event, performs the request, and swaps the returned fragment. |
| Controller/service | Performs server validation and returns a safe, form-level failure message. |

## DTO-to-client mapping

The renderer preserves each annotation's declared `message` as `errorMessage`. It emits the following only when present:

| Bean Validation constraint | JustValidate rule |
| --- | --- |
| `@NotNull`, `@NotEmpty`, `@NotBlank` | `required` |
| `@Email` | `email` |
| `@Size(min, max)` on text-like fields | `minLength` and/or `maxLength` |
| `@Min`, `@DecimalMin(inclusive = true)` | `minNumber` |
| `@Max`, `@DecimalMax(inclusive = true)` | `maxNumber` |
| `@Pattern` | `customRegexp` |

Rules supported by JustValidate but not inferable from standard DTO annotations (for example `integer`, `password`, `strongPassword`, and file rules) remain supported by the JavaScript factory whenever a form supplies a valid `validate-data` JSON rule object. They are not fabricated by the DTO renderer. Constraints whose semantics cannot be represented exactly by a built-in browser rule, including exclusive decimal boundaries and `@Positive`/`@Negative`, remain server-only. A later, explicit DTO annotation can extend the renderer if such a form needs it.

## HTML contract

Each participating input has a unique `id`, a `name`, a JSON `validate-data` attribute, and a stable sibling error container. The JSON is an array of JustValidate rule objects; it has no custom DSL. Example:

```html
<input id="login-email" name="email"
       validate-data='[{"rule":"required","errorMessage":"Email không được để trống"},{"rule":"email","errorMessage":"Email không đúng định dạng"}]'>
<div id="login-email-error"></div>
```

The renderer must escape JSON for an HTML attribute through Thymeleaf rather than concatenate JavaScript or JSON strings in the template.

## Browser lifecycle

The login form has an Alpine controller. During `init`, it calls `createFormValidator(this.$el)`. On native submit, Alpine prevents the browser submission and awaits `validator.revalidate()`. If valid, Alpine dispatches a bubbling `validated-submit` event from the form. `hx-trigger="validated-submit"` starts the existing `hx-post` request. The controller's `destroy` lifecycle hook calls `validator.destroy()`.

The factory parses only `[validate-data]` descendants of its supplied form and registers each one with `addField`. It configures the field's declared error container and common DaisyUI/Tailwind error classes. It contains no Alpine, HTMX, request, swap, or application-wide DOM logic.

Since the response replaces the form with `outerHTML`, Alpine destroys the old instance and initializes the returned form, including a fresh validator.

## Server errors

The form exposes one optional alert region before the fields:

```html
<div role="alert" class="alert alert-error alert-soft">
  <span>...</span>
</div>
```

The controller converts any `BindingResult` failure into a single user-safe message in the model and returns the same form fragment. Business errors (such as a duplicate email) use the same model field and alert. Field-level `th:errors`, `#fields.hasErrors`, and Alpine error state are removed. The alert is absent on an initial GET and a successful response.

## Login refactor

The login form becomes plain semantic form markup plus declarative validation data. Its existing `x-data` error state, `@input` validation expressions, and duplicate error spans are deleted. Alpine remains only for the small form controller described above; it does not contain validation rules.

## Verification

No automated or manual test work is included for this project by request. During implementation, run only the applicable compile/bundle checks (`./mvnw compile` and `yarn build`) to catch integration and packaging errors.
