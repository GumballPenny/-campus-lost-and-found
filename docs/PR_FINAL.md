Title: CSS-67 Add account microservice and deployment

The final variant adds persisted registration through a separate accounts
service and its database. Items calls accounts over HTTP, validates JWTs locally,
and keeps item data/status audit in its own database. The browser supports the
full registration and item workflow. Compose and Caddy prepare server deployment.

- [x] Separate service builds/processes and separate database ownership
- [x] Registration creates users with BCrypt; no manual account INSERT
- [x] Server assigns FINDER; senior requires private invitation
- [x] Flyway per database; application audit events are separate from schema history
- [x] Frontend registration/login/create/read/list/status/logout
- [x] Local checks: 35 items tests + 2 accounts tests passed
- [x] Real two-process/browser flow: 201, 403 unchanged, senior 200 FOUND, 401
- [x] Dockerfiles, volumes, private internal ports, healthchecks and env generator
- [x] Deployment guide, Caddy HTTPS configuration and backup steps
- [x] CSS-67; no committed .env/build output
- [x] Docker builds and live Compose run on GitHub Actions
- [x] Registered account and item survive app container restart
- [x] [GitHub Maven and Compose checks passed](https://github.com/GumballPenny/-campus-lost-and-found/actions/runs/46015534453)
- [ ] Public server provisioned, DNS configured, HTTPS deployment verified

Review this against lab5-security to see only final additions. The final branch
intentionally contains features forbidden in the separate Lab 5 hand-in.
