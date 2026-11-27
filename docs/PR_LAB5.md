Title: CSS-67 Add Campus JWT security and auth page

Campus Lost & Found now authenticates FINDER/CUSTODIAN with 15-minute HS256 JWTs.
The page demonstrates create/read, junior 403 without a service call, senior
REPORTED → FOUND and anonymous 401 after logout. Existing HTTP and transaction
contracts remain covered.

- [x] Same product; roles/access table in README
- [x] BCrypt in-memory dev users; no users table
- [x] HS256, product issuer, roles, expiresIn 900
- [x] Common 401/403 error record and correlation ID
- [x] Narrow URL rule before broad rule; matching @PreAuthorize
- [x] Actor from Authentication; junior never calls service
- [x] Stateless; no form login/Basic; denyAll fallback
- [x] Health/docs/page public; sensitive Actuator paths closed
- [x] Secret outside main Java; prod requires CSS_JWT_SECRET
- [x] Own auth page; token in JS memory only; logout → 401
- [x] Web security slices and token issue/decode tests
- [x] Local PostgreSQL verify green; Lab 3 counts 0/1 unchanged
- [x] This Lab 5 branch has no Docker/accounts service/real HTTP outbound call
- [x] CSS-67 in title; no .env, target or production secrets

Validation: 35 root tests passed against isolated PostgreSQL 18. Browser
demonstration and separate final-mode work are described in the final branch.
