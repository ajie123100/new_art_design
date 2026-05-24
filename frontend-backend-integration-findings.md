# Frontend Backend Integration Findings

Check date: 2026-05-24

Scope: static integration review only. No frontend/backend services were started, and no API requests were executed.

## Summary

The current frontend can integrate with the backend for the core auth flow and these system pages:

- Login, captcha, refresh token, logout, current user info
- Dynamic menu loading
- User list/create/update/delete
- Role list/create/update/delete/menu grant/dept data scope
- Menu list/create/update/delete/status
- Dept list/create/update/delete/status

The largest remaining issue is that the backend already exposes many RuoYi-aligned modules and seeds visible menu entries for them, but the frontend only has pages and API wrappers for user, role, menu, and dept.

## P0 - Blocks Visible Menu Pages

### P0-1 Seeded Menus Point To Missing Frontend Components

Backend seed data makes these menu entries visible:

- `/system/dict` -> component `/system/dict`
- `/system/config` -> component `/system/config`
- `/system/notice` -> component `/system/notice`
- `/system/post` -> component `/system/post`
- `/system/file` -> component `/system/file`
- `/monitor/online` -> component `/monitor/online`
- `/monitor/cache` -> component `/monitor/cache`
- `/monitor/job` -> component `/monitor/job`
- `/monitor/database` -> component `/monitor/database`
- `/monitor/logininfor` -> component `/monitor/logininfor`
- `/monitor/operlog` -> component `/monitor/operlog`

Evidence:

- Backend menu seeds: `backend/art-admin/src/main/resources/sql/art_system.sql`
- Frontend dynamic component loader only loads files under `frontend/src/views/**`
- Existing frontend views do not include the component paths above.

Impact:

After login, these menus can appear. Clicking them will route to the fallback error component with `组件未找到: ...`.

Recommended fix:

Either add the missing frontend pages first, or temporarily mark these seeded menu entries hidden until their pages are ready.

### P0-2 Frontend API Layer Only Covers User/Role/Menu/Dept

`frontend/src/api/system-manage.ts` currently wraps only:

- `/api/user/**`
- `/api/role/**`
- `/api/menu/**`
- `/api/dept/**`
- `/api/v3/system/menus`

Backend already exposes:

- `/api/config/**`
- `/api/dict/**`
- `/api/post/**`
- `/api/notice/**`
- `/api/file/**`
- `/api/job/**`
- `/api/jobLog/**`
- `/api/logininfor/**`
- `/api/operlog/**`
- `/api/monitor/**`

Impact:

Even if the missing pages are created, most backend modules still lack typed frontend API calls and TypeScript response models.

Recommended fix:

Add module-specific API files or extend `system-manage.ts` before building those pages.

## P1 - Backend Features Not Fully Connected

### P1-1 Export And Download Need Blob-Aware HTTP Handling

Backend export/download endpoints write binary responses directly:

- Excel exports use `ExcelUtil.writeExcel(...)`
- File download writes `application/octet-stream`

The frontend HTTP wrapper assumes JSON responses shaped as `{ code, msg, data }` and returns `res.data.data`.

Impact:

When frontend starts calling export/download endpoints through the current wrapper, binary responses will be misread as normal API envelopes and likely fail.

Recommended fix:

Add a dedicated download helper using `responseType: 'blob'` and bypass JSON envelope handling for binary endpoints.

### P1-2 Import Endpoints Are Backend-Ready But Not Wired To Upload

Backend import endpoints expect multipart upload with `file`, for example:

- `POST /api/user/import`
- `POST /api/role/import`
- `POST /api/menu/import`
- `POST /api/config/import`
- `POST /api/dict/type/import`
- `POST /api/dict/data/import`

The existing frontend Excel import component parses files locally with `xlsx`; it does not upload the selected file to backend import endpoints.

Impact:

Import buttons/pages will need a real multipart upload path and should display `ImportResult(imported, skipped)`.

Recommended fix:

Create shared import/upload helpers and standardize success text around imported/skipped counts.

### P1-3 User Page Does Not Use All User Backend Capabilities

Frontend API wrappers exist for:

- `PUT /api/user/status`
- `PUT /api/user/reset-password`

But `frontend/src/views/system/user/index.vue` currently exposes only:

- add
- edit
- delete

Backend also has user import/export, but the frontend page does not expose them.

Impact:

User management is usable for basic CRUD but still incomplete compared with the backend API.

Recommended fix:

Add status switch, reset password action, import, and export controls.

### P1-4 Role Page Does Not Use All Role Backend Capabilities

Frontend API wrappers exist for role status updates, and backend has role import/export.

Current role page exposes:

- add
- edit
- delete
- menu permission

It displays role status as a tag, but does not expose an enable/disable action. Import/export are not wired.

Recommended fix:

Add role status switch/action and import/export controls.

### P1-5 File Management Menu Has No List Contract

Backend file controller currently supports:

- upload
- download by url
- delete by url

There is no file list/page endpoint, while seed data exposes a visible file management menu.

Impact:

A file management page cannot be implemented as a normal table without either a list endpoint or a different UX model.

Recommended fix:

Decide whether file management should be upload-only, or add a persisted file metadata table and list endpoint.

## P2 - Lower-Risk Integration Gaps

### P2-1 Role Options In User Dialog Are Limited To First 100 Roles

The user dialog loads roles with:

- `fetchGetRoleList({ pageNum: 1, pageSize: 100 })`

Impact:

If the role count exceeds 100, some roles cannot be assigned to users from the dialog.

Recommended fix:

Add a role `listAll` endpoint or a lightweight role option endpoint.

### P2-2 Dynamic Route Ordering May Still Drift From Menu Sort

Backend builds route trees from menu data ordered by `orderNum`, but final route sorting uses route ID order.

Impact:

Menu order may differ from configured `orderNum` when IDs and sort numbers diverge.

Recommended fix:

Carry `orderNum` into route metadata or sort routes using the original menu order.

### P2-3 Frontend System Types Stop At User/Role/Menu/Dept

`frontend/src/types/api/api.d.ts` does not yet define types for:

- config
- dict
- post
- notice
- file
- job/jobLog
- login log
- operation log
- monitor/server/cache/database/online user

Impact:

New pages will either use `any` or need a type pass before implementation.

Recommended fix:

Add DTO-aligned TypeScript interfaces as each module page/API wrapper is introduced.

## Suggested Fix Order

1. Hide missing seeded menu entries or create placeholder-compatible pages for visible backend menus.
2. Add frontend API wrappers and TypeScript models for config, dict, post, notice, monitor, logs, jobs, and file modules.
3. Add blob download/export helper and multipart import helper.
4. Finish user/role page actions: status, reset password, import, export.
5. Decide file management persistence/list behavior.
6. Build remaining RuoYi-aligned pages in batches: config/dict first, logs/monitor second, jobs/file/notice/post third.
