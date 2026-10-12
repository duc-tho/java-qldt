# QLDT Design System

## Purpose and scope

QLDT supports the Faculty of Information Technology in managing student topics for course projects, scientific research, specialized essays, and graduation theses. Its working flow is a registration period: lecturers propose topics, the Head of Faculty approves and publishes them, students form groups and register a topic, and advisers approve or reject registrations.

This document is the visual source of truth for every QLDT UI change. Read it before editing a Thymeleaf template, `src/main/resources/style/app.css`, or a shared UI fragment. Preserve routes, permission rules, field names, messages, and business flows unless a task explicitly requests a functional change.

## Design read

QLDT is an academic operations application for faculty administrators, lecturers, and students. It must feel trustworthy, calm, and highly legible under dense operational data. The visual language is **Forest Ledger**: structured academic records, clear schedules, and deliberate decisions.

Use these design dials:

| Dimension        | Value | Meaning                                                                    |
| ---------------- | ----- | -------------------------------------------------------------------------- |
| Visual density   | 6/10  | Tables and metadata remain compact, but key decisions have breathing room. |
| Layout variance  | 3/10  | Predictable grids and stable scan paths win over decorative asymmetry.     |
| Motion intensity | 2/10  | Feedback is limited to hover, focus, press, dialog, and loading states.    |

Do not add marketing heroes, glass effects, decorative illustrations, automatic animations, or dashboard metrics that do not come from the application data.

## Product structure

The registration period is the product's primary organizing object. Screens must expose its dates, current stage, and next permitted action before secondary information.

### Existing roles

- `ADMIN`: manages home, users, departments, and system-wide information.
- `HEAD_OF_FACULTY`: manages registration periods, topics, groups, departments, announcements, councils, and result publication.
- `LECTURER`: proposes topics and reviews pending topic registrations for topics they advise.
- `STUDENT`: chooses an open registration period, creates a group, manages members, chooses a published topic, and requests approval.

### Existing screen inventory

| Area                       | Current pages and outcomes                                                                                   |
| -------------------------- | ------------------------------------------------------------------------------------------------------------ |
| Authentication and profile | Login, account profile, password change, settings.                                                           |
| Administration             | Home, users, departments, announcements.                                                                     |
| Registration periods       | List, create, edit, detail, topic list, registrations, and groups.                                           |
| Topics                     | List, create, edit, detail, approve, reject with reason, and publish.                                        |
| Lecturer proposal          | Select a registration period, list proposed topics, create and edit a proposal.                              |
| Student registration       | Open periods, group creation, group detail, members, topic discovery, topic selection, and approval request. |
| Adviser review             | Pending topic-registration list, detail, approve, and reject with reason.                                    |
| Council                    | Create council, detail, members, topic assignment, and result publication records.                           |

### UI hierarchy for the principal flows

1. Registration period: show the period name, type, active time window, important deadlines, and the next relevant action.
2. Topic: show title, status, department, advisers, and approval or rejection context before the long description and edit form.
3. Student group: show approval state and selected topic first. Members are supporting information. A group can contain at most three members.
4. Topic registration review: show group, topic, advisers, status, and the approval or rejection action in one decision-oriented view.
5. Council: show the council identity and members first, then topic assignments and publication actions as separate operational sections.

## Forest Ledger tokens

Use daisyUI semantic names in markup. Define the values in the existing custom theme in `src/main/resources/style/app.css`. Do not use raw Tailwind color families for ordinary UI text or surfaces.

| Semantic token    | Value     | Role                                                                   |
| ----------------- | --------- | ---------------------------------------------------------------------- |
| `base-100`        | `#FBFCFA` | Main canvas and readable work surface.                                 |
| `base-200`        | `#F1F5F1` | Application shell, grouped background, table header.                   |
| `base-300`        | `#DDE6DF` | Borders, input outlines, quiet separators.                             |
| `base-content`    | `#17231D` | Primary text and icons.                                                |
| `primary`         | `#165B47` | The one brand accent: primary action, selected navigation, focus ring. |
| `primary-content` | `#F7FCF8` | Text and icons on primary controls.                                    |
| `secondary`       | `#6E8F7D` | Supporting emphasis only, never a competing CTA.                       |
| `accent`          | `#B9D6C2` | Subtle active surfaces and non-critical highlights.                    |
| `neutral`         | `#2B3B31` | Dark shell areas only when hierarchy requires them.                    |
| `info`            | `#5A89A8` | Informational status.                                                  |
| `success`         | `#388660` | Approved, published, completed, and safe outcomes.                     |
| `warning`         | `#B87922` | Pending, draft, approaching deadline, and caution.                     |
| `error`           | `#B54848` | Rejected, destructive actions, and field errors.                       |

Status always has a text label. Color cannot be the sole status signal. Use the application messages and actual enum state names, including `PROPOSED`, `APPROVED`, `PUBLISHED`, `REJECTED`, `DRAFT`, and `PENDING`.

## Typography and iconography

- Use the existing system sans-serif stack unless a font is explicitly added and self-hosted. Do not add remote font loading for this application.
- Use a four-step content scale: page title `text-2xl` on desktop, section title `text-xl`, card or record title `text-lg`, and standard text `text-sm` or `text-base` according to data density.
- Use `font-semibold` for hierarchy, not oversized headings. Page titles should remain concise and usually occupy one line.
- Use tabular figures or a monospace treatment only where timestamps, codes, or numeric values are repeatedly compared.
- Use Bootstrap icons through the existing Iconify integration. Keep the `bi:` icon family and use an icon only when it strengthens a label or preserves space in an icon-only control.
- Icon-only controls require an accessible name via `title` or `aria-label`.

## Spatial system

- Page shell: retain the drawer navigation, but use a broad content width for data screens. `max-w-(--breakpoint-2xl)` is appropriate.
- Page padding: `px-4 py-5` on small screens, `sm:px-6 sm:py-6`, and `xl:px-8 xl:py-8` on wide screens.
- Vertical rhythm: use `gap-3` within a compact control group, `gap-5` or `gap-6` within a section, and `mt-8` between independent page sections.
- Radius: fields and controls use `0.5rem`; cards, alerts, and modals use `0.75rem`. Use `rounded-full` only for avatars, compact status markers, and truly pill-shaped controls.
- Borders: default to one quiet `border-base-300` border. Avoid multiple nested cards or both top and bottom borders on every list row.
- Shadows: use at most a small, tinted elevation shadow where a card or dialog needs separation. Do not use black drop shadows or glow.

## Shared layout

### Navigation

- Preserve the role-aware sidebar groups and existing routes.
- The current route is the only strongly highlighted navigation item.
- Sidebar headings organize capability groups, not decorative sections.
- The top bar provides global controls only. Each page owns its local actions.
- On mobile, the drawer must close after navigation and all primary controls must remain reachable without horizontal scrolling.

### Page header

Every substantive page uses `layout/page.html` to provide:

1. Breadcrumb or contextual parent reference when a page belongs to a registration period.
2. Page title and a concise factual supporting line only when the context is not already apparent.
3. A right-aligned action group with one primary action. Destructive actions are separated and never compete visually with the primary action.

Do not place a generic card around the entire page header.

### Summary and detail pages

Use this order:

1. Summary band: identity, status, deadline or period state, and next action.
2. Primary task or decision content.
3. Supporting records, tables, and history.

On wide screens, use a 12-column grid only when it makes the primary task easier to scan. On mobile, collapse every multi-column layout to one column below `768px`.

## Component contracts

### Buttons and actions

- Use `btn btn-primary` for one page-level primary action only.
- Use `btn` or `btn btn-outline` for neutral secondary actions.
- Use `btn btn-error btn-outline` for destructive actions, placed after a visual separation or inside an overflow menu when not the main task.
- Use `btn-square` only for an obvious icon-only action with accessible labeling.
- Buttons use a small press response through `active:translate-y-px`; do not add animated glow or scale effects.
- Never make approval and rejection equal visual weight. Approval is primary when the decision is affirmative; rejection is destructive outline or a confirm dialog action.

### Status badges

- Keep badges compact and text-bearing: `badge badge-soft` plus a semantic status color.
- A badge is for state, not category spam. Department, adviser, and role metadata should use quiet text or restrained outline badges only when scanning benefits.
- Rejection reason is an error alert or contextual note, not a badge.

### Tables

- Tables remain the default for sortable, filterable, multi-record administration lists.
- Keep the search, filters, reload, bulk action, and create action in a single responsive toolbar. On small screens, stack it in task priority order: create, search, filters, then secondary actions.
- Table headers are quiet `base-200`; use zebra rows only when tables have enough density to need row tracking.
- Keep one row click destination and do not turn every cell into a competing link.
- Status, deadline, owner, and next action columns should appear before lower-value metadata where the table definition allows it.
- Empty tables must explain the actual absence through existing message keys and offer the permitted next action when one exists.

### Forms and validation

- Keep labels above inputs and errors directly below the invalid field.
- Group related dates from a registration period into the same visible scheduling section: lecturer registration, publication, student registration, report, review, and council date.
- Use helper text only for constraints that users cannot infer, such as a group maximum of three members.
- Preserve the current server-side validation and `ClientValidationRuleRenderer` behavior. Visual changes must not remove error bindings or input names.

### Dialogs, alerts, and feedback

- Use dialogs only for confirmation, rejection reason entry, or a focused task that cannot fit the current page.
- Destructive confirmation names the record using existing data and keeps cancel visually neutral.
- Inline validation errors remain near the field. Page-level errors use `alert alert-error`; success feedback should remain concise and contextual.
- Loading uses a skeleton matching the final content shape when async loading is introduced. Do not use decorative spinners.

### Cards and lists

- Use a card when it represents a bounded record or a decision surface. Do not wrap every section in a card.
- Prefer grouped sections, headings, dividers, and whitespace for dense detail pages.
- Member lists use an avatar initial, name, role label, and only the actions permitted to the current user.

## Screen prescriptions

### Registration period detail

The period header must show its name, type, current phase, and closest relevant date. The form fields belong to a schedule-oriented section. Topics, registrations, groups, councils, and publication history appear as separate subordinate sections with clear counts only if data is already available.

### Topic detail and proposal

The topic title, lifecycle status, department, and advisers form the record header. Approval or rejection is adjacent to status. The rejection reason is visible before the editable fields. A lecturer proposal visually distinguishes editable `PROPOSED` or `REJECTED` state from a submitted or approved record without changing the existing permission checks.

### Student group detail

Make the selected topic and registration approval state the lead summary. Place the request-approval action beside this state when the group leader may use it. The members section comes next, including the maximum-three-member constraint and leader controls. Topic selection or change is a focused next step, not a third equally weighted card.

### Topic discovery and registration review

Student topic discovery needs search and department filtering above a scan-friendly list. Each topic result shows title, department, advisers, and availability. Adviser review needs a decision header that puts the group, selected topic, and pending status before approval and rejection actions.

### Council

The council detail begins with name, registration period, and member roster. Topic assignment is an operational list with clear unassigned and assigned context. Result publication history is shown as a dated record of action, performer, and reason when the data is present.

### Home

The current home page is only a welcome message. Its eventual redesign should be role-aware and composed entirely from real, available data: open registration periods, pending adviser reviews, current student-group state, or administration tasks. Do not invent metrics or activity history.

## Accessibility and responsive rules

- Meet WCAG AA contrast for all text, inputs, badges, and buttons.
- Use visible `:focus-visible` states based on `primary`.
- Maintain at least 44px touch targets for primary mobile controls.
- Never rely on hover for required information or action access.
- Avoid horizontal page overflow. Tables may use their existing intentional `overflow-x-auto` wrapper.
- Respect reduced-motion preferences if animation is added later.
- Preserve keyboard operation for drawer navigation, dialogs, filters, tables, and form submission.

## Implementation boundaries

- DaisyUI is a primitive library, not the product visual identity. Use semantic daisyUI colors and component classes, then apply QLDT's documented spacing, hierarchy, and behavior.
- Do not introduce another component library, remote font, animation library, or icon family without an explicit decision.
- Do not alter URL structure, navigation labels, form field order, Thymeleaf bindings, Spring Security conditions, or i18n keys as part of a visual-only change.
- Use actual product labels and messages from `src/main/resources/i18n/`; do not introduce placeholder copy.
- Before editing shared layout or CRUD fragments, check every consumer because they affect most product screens.

## Explicitly banned patterns

- AI-purple, neon, gradients, glowing buttons, glassmorphism, and pure black.
- Generic marketing sections, artificial social proof, fake metrics, or invented dashboard analytics.
- Three equal feature-card layouts for operational content.
- Centered empty page headings that compete with the primary task.
- Excessive pills, decorative status dots, or status represented only by color.
- Nested cards, large drop shadows, and mixed corner-radius systems.
- Emoji used as product iconography, hand-drawn SVG icons, or a second icon family.
- Decorative motion, scroll effects, custom cursors, and animations that move layout dimensions.
- Layouts that require horizontal scrolling on mobile, except intentionally scrollable data tables.

## Design review checklist

- Does the screen expose the registration period, current state, and next permitted action at the appropriate level?
- Is there exactly one strongest primary action?
- Are approved, pending, rejected, and draft states understandable without color alone?
- Does the hierarchy work in a quick scan at desktop and a one-column mobile view?
- Are real routes, labels, data, i18n keys, and permission conditions preserved?
- Are loading, empty, validation, error, and destructive-confirmation states addressed where the feature has them?
- Does the change use Forest Ledger tokens and the shared radius, spacing, icon, and component rules?
