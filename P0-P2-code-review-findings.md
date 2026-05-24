# P0-P2 Code Review Findings

Static review date: 2026-05-23

Scope: frontend and backend source review only. No build, tests, or runtime checks were executed.

## Overall Assessment

The project is a solid admin-dashboard foundation, but it is still closer to a template integration project than a production-ready art/design business system.

- Backend foundation: authentication, captcha, users, roles, menus, departments, dictionaries, logs, and permission checks exist.
- Frontend foundation: Vue 3, Vite, Pinia, Element Plus, dynamic routes, layout, theme, table abstractions, and system management pages exist.
- Business completion: domain-specific art/design modules are not yet visible.
- Production readiness: blocked by security hardening, database pagination, mock/TODO cleanup, and deployment configuration.

Estimated completion:

- Admin framework: 75%-85%
- Production engineering maturity: 55%-60%
- Real business product completion: 35%-45%

## P0 - Must Fix Before Real Deployment

### P0-1 Hard-coded Local Database Credentials

Location:

- `backend/art-admin/src/main/resources/application.yml`

Issue:

The datasource uses local credentials directly:

- Database: `art_design`
- Username: `root`
- Password: `root`

Impact:

This is acceptable for local setup, but unsafe for shared environments or deployment. It also makes environment switching brittle.

Recommended fix:

Move datasource settings to environment variables or profile-specific configuration, for example `application-dev.yml`, `application-prod.yml`, and `${DB_PASSWORD}` placeholders.

### P0-2 Default Admin Passwords Are Seeded As Plain Text

Location:

- `backend/art-admin/src/main/resources/sql/art_system.sql`
- `backend/art-system/src/main/java/com/artdesign/system/service/SysAuthService.java`

Issue:

Seed users are inserted with `123456` as plain text. `SysAuthService` can upgrade plain passwords after login, but the database initially contains weak plain-text credentials.

Impact:

This is a serious deployment risk if seed data reaches any non-local environment.

Recommended fix:

Seed BCrypt hashes instead of plain text, force password change on first login, and avoid shipping known default credentials in production data.

### P0-3 CORS Allows All Origins

Location:

- `backend/art-framework/src/main/java/com/artdesign/framework/config/WebConfig.java`

Issue:

`allowedOriginPatterns("*")` allows requests from any origin.

Impact:

This is convenient during development but too broad for production.

Recommended fix:

Use environment-specific allowed origins. Keep wildcard only in local development.

### P0-4 Generic Exception Handler Returns Raw Exception Messages

Location:

- `backend/art-framework/src/main/java/com/artdesign/framework/web/GlobalExceptionHandler.java`

Issue:

The fallback exception handler returns `ex.getMessage()` to clients.

Impact:

Unexpected exceptions may expose internal details such as SQL errors, stack-sensitive messages, or configuration clues.

Recommended fix:

Log full exception details server-side, return a generic client-facing message, and add stable error codes.

## P1 - Should Fix Before Feature Expansion

### P1-1 Backend Pagination Is Done In Memory

Locations:

- `backend/art-system/src/main/java/com/artdesign/system/service/SysUserService.java`
- `backend/art-system/src/main/java/com/artdesign/system/service/SysRoleService.java`
- `backend/art-system/src/main/java/com/artdesign/system/service/SysDictService.java`
- `backend/art-system/src/main/java/com/artdesign/system/service/SysLoginLogService.java`
- `backend/art-system/src/main/java/com/artdesign/system/service/SysOperLogService.java`
- `backend/art-system/src/main/java/com/artdesign/system/service/SysPostService.java`

Issue:

Several list methods query all matching records and then call `subList` for pagination.

Impact:

This works with small seed data, but performance and memory usage will degrade quickly as data grows.

Recommended fix:

Use MyBatis-Plus database pagination (`Page`, `selectPage`) and push filtering, sorting, and limits into SQL.

### P1-2 Frontend Still Uses Mock Data In Real System Pages

Locations:

- `frontend/src/views/system/user/index.vue`
- `frontend/src/mock/temp/formData.ts`
- Other `frontend/src/mock/**` usages

Issue:

The user management page replaces returned avatars with mock avatar data. Other pages also import mock data directly.

Impact:

This blurs the line between demo data and real data, making QA and production behavior harder to trust.

Recommended fix:

Remove mock transformations from real system pages. Keep demos isolated under example routes only.

### P1-3 User Status Semantics Are Inconsistent

Locations:

- `backend/art-admin/src/main/resources/sql/art_system.sql`
- `backend/art-system/src/main/java/com/artdesign/system/service/SysUserService.java`
- `frontend/src/views/system/user/index.vue`

Issue:

Backend status values mean `1 = normal`, `2 = disabled`. Frontend labels show values as online, offline, abnormal, and canceled.

Impact:

Users may misunderstand account status. Admin actions can appear to manage online presence when they actually manage account enablement.

Recommended fix:

Align labels and types across frontend and backend, for example `Enabled` and `Disabled`.

### P1-4 Business Domain Is Mostly Missing

Locations:

- `backend/art-system/src/main/java/com/artdesign/system/**`
- `frontend/src/views/**`

Issue:

The implemented backend modules are mostly generic admin modules. Art/design-specific entities and workflows are not yet visible.

Impact:

The project may look feature-rich because of the admin template, but it is not yet a complete art/design business application.

Recommended fix:

Define core business modules before expanding UI further. Possible modules include design works, materials, projects, clients, orders, review workflow, publishing, and asset management.

### P1-5 Permission/Menu Ordering May Not Match Business Sort

Location:

- `backend/art-system/src/main/java/com/artdesign/system/service/SysMenuService.java`

Issue:

Menu records are initially sorted by `orderNum`, but route sorting later uses route ID ordering.

Impact:

Displayed route/menu order may not consistently follow configured business order.

Recommended fix:

Keep `orderNum` available in route metadata or sort route trees by the original menu ordering.

## P2 - Cleanup And Maintainability

### P2-1 Development Logs Remain In System Pages

Locations:

- `frontend/src/views/system/user/index.vue`
- `frontend/src/views/system/user/modules/user-search.vue`
- `frontend/src/views/auth/login/index.vue`

Issue:

Several `console.log`, `console.warn`, and `console.error` calls remain in user-facing flows.

Impact:

This adds noise during debugging and can leak behavior details in production.

Recommended fix:

Remove debug logs or gate them behind a development-only logger.

### P2-2 TODO And Demo Pages Need Triage

Locations:

- `frontend/src/views/auth/register/index.vue`
- `frontend/src/views/article/**`
- `frontend/src/views/dashboard/**`
- `frontend/src/components/business/comment-widget/**`

Issue:

Several views still contain TODO comments or mock imports.

Impact:

The project appears broader than it really is. This can confuse handoff, QA, and planning.

Recommended fix:

Classify each route as production, demo, or planned. Hide unfinished routes from production menus.

### P2-3 Refresh Token Is Generated But Not Backed By A Refresh Flow

Location:

- `backend/art-admin/src/main/java/com/artdesign/web/controller/auth/AuthController.java`
- `frontend/src/store/modules/user.ts`

Issue:

Login returns a random refresh token, and the frontend stores it, but no visible refresh-token API or lifecycle exists.

Impact:

This gives a false sense of session refresh support.

Recommended fix:

Either implement refresh-token persistence and rotation, or remove the refresh token field until needed.

### P2-4 Captcha Store Is In Memory

Location:

- `backend/art-system/src/main/java/com/artdesign/system/service/SysCaptchaService.java`

Issue:

Captcha values are stored in a local `ConcurrentHashMap`.

Impact:

This is fine for a single local backend instance, but it will not work reliably across multiple backend instances or restarts.

Recommended fix:

For production or clustered deployment, store captcha entries in Redis or another shared expiring store.

### P2-5 Backend CRUD Services Repeat Similar Paging And Mapping Logic

Locations:

- `backend/art-system/src/main/java/com/artdesign/system/service/**`

Issue:

Services repeat parsing, pagination, formatting, and DTO mapping patterns.

Impact:

Duplication increases maintenance cost and makes behavior drift more likely.

Recommended fix:

After database pagination is fixed, introduce small shared helpers for common query parsing and page response mapping.

## Suggested Repair Order

1. Fix P0 security/deployment blockers.
2. Replace in-memory pagination with database pagination.
3. Align frontend/backend status semantics and menu ordering.
4. Remove mock data from real system pages.
5. Decide which routes are production features versus demos.
6. Define and implement the real art/design business modules.
7. Improve refresh-token and captcha storage only if production requirements need them.

