# Campus Lost & Found

Java 21 / Spring Boot 3.4.13. The same semester product: register a lost item,
read it by UUID and let a custodian confirm that it was found.

- [Запуск и демонстрация по шагам](docs/START_RU.md)
- [Защита кода и термины](docs/DEFENSE_RU.md)
- [Критерии и результаты проверок](docs/CRITERIA.md)
- [Размещение на сервере](deploy/README.md)

## Submission variants

Lab 5 forbids users tables, Docker and real outbound calls. The final project
requires them. Review these variants separately, in the same repository.

| Branch/profile | Authentication | Deployment |
|---|---|---|
| `lab5-security` / `dev` | Two BCrypt in-memory users | One Spring process + PostgreSQL; no accounts service or Docker files in this branch |
| `final-microservices` / `final` | Registration and persisted users in accounts | Two Spring services, two databases, Docker Compose, optional Caddy HTTPS |

Do not submit the final branch as the strict Lab 5 hand-in. Jira story: **CSS-67**.

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

## Package diagram

```text
dto        client           handler          security        config
           RestClient       REST             JWT/filters     Application
                \              |                 /           @Service
                         domain (plain Java) <------------------+
                Id, Key, Status, Policy, Rule, Repository port
                                    ^
                               persistence
                        LostItemEntity / LostItemJpa
                                    |
                                PostgreSQL
```

Domain imports no Spring, JDBC or JPA. `dto` remains reserved. Client performs
real HTTP calls only in final.

## Final service boundaries

```text
Browser → [Caddy HTTPS on server] → items :8080 → items-db
                                   |
                                   | HTTP login/register
                                   v
                                accounts :8081 → accounts-db
```

Accounts owns `users` and `auth_event`: registration, BCrypt, authentication,
token issuing. Items validates tokens locally and owns `lost_item` and
`lost_item_event`. Separate builds, processes, ports, databases and migrations.
No shared database, cross-service SQL, Kafka or distributed transaction.
Compose does not publish accounts/database ports.

Public signup creates FINDER. CUSTODIAN needs the server invitation
`CSS_CUSTODIAN_INVITE`, not a client `role`. Keep the invitation private.
The frontend is served by items from `src/main/resources/static/`.

## Existing contracts and migrations

- POST `/api/v1/lost-items`: 201 + Location; validation 400, duplicate key 409.
- GET `/api/v1/lost-items/{uuid}`: 200; malformed UUID 400; absent item 404.
- Locked action violating domain policy: 409, unchanged row.
- Final-only GET `/api/v1/lost-items`: latest 100 records.
- `/swagger-ui.html`, `/v3/api-docs` public; protected execution needs Bearer.
- Actuator exposes health/info; only health is public.

Root V1 is unchanged; V2 adds status audit. Accounts has its own V1 and its own
`flyway_schema_history` in a different database. Flyway creates tables, Hibernate
uses `ddl-auto: validate`, `open-in-view: false`. Tests DELETE rows, not DROP tables.
Lab 3 counts remain **0** for rollback and **1** for an earlier committed request.

## Run

With a configured development PostgreSQL database:

```powershell
powershell -ExecutionPolicy Bypass -File scripts/maven.ps1 spring-boot:run
```

Open http://localhost:8080/. `scripts/maven.ps1` can use IntelliJ bundled Maven.
`scripts/verify-isolated.ps1` tests both services in a separate PostgreSQL cluster
without touching the existing `css` database.

Final, with Docker Engine running:

```powershell
powershell -ExecutionPolicy Bypass -File scripts/setup-env.ps1
docker compose up -d --build
docker compose ps
```

Open http://localhost:8080/, register your own account. For the senior demo read
the invitation locally from `.env`; never commit it. `docker compose down` stops
the stack and keeps data volumes. Full instructions are linked above.

## Verification status (2026-11-27)

Root `verify`: **35 tests passed**. Accounts `verify`: **2 tests passed**.
Both ran against isolated PostgreSQL 18. Two real Spring processes + browser
confirmed signup 201, login 200, create 201, junior 403 unchanged, senior 200 →
FOUND, list 200 and anonymous 401. Desktop/mobile screens were inspected.

Local Docker Engine is unavailable: images and Compose runtime are **not yet
verified**. No public server is provisioned/deployed. Local tests are not proof
of a published GitHub PR or a deployed website. See the criteria document.
