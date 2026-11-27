# Campus Lost & Found — Lab 5

CSS-67. Java 21 / Spring Boot 3.4.13. This is the standalone Lab 5 submission
for the Campus Lost & Found semester product.
## Lab 5 access rules

Product issuer: `campus-lost-and-found`. Junior: **FINDER**. Senior: **CUSTODIAN**.
Locked action: `POST /api/v1/lost-items/{uuid}/found` confirms a found item.

| Action | FINDER | CUSTODIAN | No token |
|---|---|---|---|
| POST `/api/v1/auth/login` | allowed | allowed | allowed |
| Create/read item | allowed | allowed | 401 |
| POST `/api/v1/lost-items/{uuid}/found` | 403, service not called | 200, REPORTED → FOUND | 401 |
| GET `/api/me` | allowed | allowed | 401 |
| GET `/actuator/health` | allowed | allowed | allowed |
| Page/CSS/JS and springdoc | allowed | allowed | allowed |
| Anything else | 403 | 403 | 401 |

Dev-only usernames: `finder`, `custodian`; classroom password: `campus-dev`
(override `CSS_DEV_PASSWORD`). The manager stores BCrypt hashes. No `{noop}`.
These users are absent in final.

Login returns `{accessToken, tokenType: "Bearer", expiresIn: 900}`. HS256 JWT
contains `iss`, `sub`, `iat`, `exp`, `roles` without `ROLE_`. Spring's converter
adds that prefix to authorities. The decoder checks signature, algorithm,
issuer and timestamps. Token stays in a JavaScript variable, never localStorage,
URL, logs or visible page text. Refresh logs out, PostgreSQL data remains.

| Error | HTTP | code | message |
|---|---|---|---|
| Wrong name or password | 401 | UNAUTHENTICATED | bad username or password |
| Missing/invalid API token | 401 | UNAUTHENTICATED | valid token required |
| Wrong role | 403 | FORBIDDEN | not allowed for your role |

All errors contain only `code`, `message`, `correlationId`. Supplied
`X-Correlation-Id` is echoed, otherwise generated. Narrow senior rule precedes
`/api/v1/**`; service has matching `@PreAuthorize`. Actor comes from
`Authentication.getName()`, never JSON. CSRF, form login and Basic are disabled;
session policy is stateless. Unmatched routes are denied.


## Run the lab

Use your local PostgreSQL with a database named css. The database user must own
the existing tables and be able to run the migrations.

    powershell -ExecutionPolicy Bypass -File scripts/start-dev.ps1

Enter the PostgreSQL user and password. Open http://localhost:8080/.
Log in as finder (password campus-dev), create a lost item with a unique business
key, and try the found action. It returns 403 and the row remains REPORTED.
Log out, sign in as custodian (campus-dev), then repeat: 200 and FOUND.
Log out and click the anonymous request check: 401.

The page, health endpoint and /swagger-ui.html are public.
V1 remains unchanged. V2 creates the item status audit, not an accounts table.
The domain (Id/Key/Status/Rules/Policy) remains plain Java.
Flyway owns migrations and Hibernate validates their result.

## Verify in an isolated PostgreSQL 18 database

    powershell -ExecutionPolicy Bypass -File scripts/verify-isolated.ps1

This script uses a separate instance, not your demo data. Tests cover real token
issue/decode, login, 401/403/senior success, old Lab 4 201/400/404/409, and the
Lab 3 rollback counts 0 and 1. The GitHub workflow runs Maven verify as well.

[PR checklist](docs/PR_LAB5.md)
